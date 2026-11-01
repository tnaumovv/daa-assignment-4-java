"""Independent tiny oracles, witness checks, and full-domain known-answer tests."""
import itertools, json, pathlib, random, subprocess, sys, time
from functools import lru_cache
ROOT = pathlib.Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / 'tools'))
from run import build, execute
R = random.Random(20261101)
cases = []
def add(req, expected, why): cases.append((req, expected, why))
def search(t,p): return [i for i in range(len(t)+1) if t[i:i+len(p)] == p]
def subseq(s,t):
    it=iter(t)
    return all(any(c==x for x in it) for c in s)
def lcs_oracle(a,b):
    return max((len(s) for mask in range(1<<len(a)) if subseq(s:=''.join(c for i,c in enumerate(a) if mask>>i&1),b)),default=0)
@lru_cache(None)
def merge_oracle(a):
    if len(a)<2:return 0
    return min(a[i]+a[i+1]+merge_oracle(a[:i]+(a[i]+a[i+1],)+a[i+2:]) for i in range(len(a)-1))
def suffix_oracle(t):
    sa=sorted(range(len(t)),key=lambda i:t[i:]); lcp=[0]*len(t)
    for k in range(1,len(t)):
        i,j=sa[k-1],sa[k]
        while i+lcp[k]<len(t) and j+lcp[k]<len(t) and t[i+lcp[k]]==t[j+lcp[k]]: lcp[k]+=1
    return sa,lcp
for a,b in [('', ''),('', 'abc'),('abc',''),('tea_time','team'),('ab','ba'),('aaaa','aa')]+[(''.join(R.choices('abc',k=R.randrange(9))),''.join(R.choices('abc',k=R.randrange(9)))) for _ in range(100)]:
    add(dict(task='lcs',a=a,b=b),lcs_oracle(a,b),'exhaustive subsequences; ties and empty inputs')
for a in [[],[0],[4],[0,0,0],[1,100,1],[10**9]*4]+[[R.randrange(8) for _ in range(R.randrange(8))] for _ in range(80)]:
    add(dict(task='merge',sizes=a),merge_oracle(tuple(a)),'all adjacent merge orders and long totals')
for n in range(11):
    for _ in range(8):
        e=[[i,R.randrange(i)] for i in range(1,n)]; w=[R.randrange(-5,8) for _ in range(n)]
        best=max(sum(w[i] for i in range(n) if mask>>i&1) for mask in range(1<<n) if all(not(mask>>u&1 and mask>>v&1) for u,v in e))
        add(dict(task='tree',n=n,weights=w,edges=e),best,'independent subset enumeration')
pairs=[('',''),('','x'),('abc',''),('a','long'),('aaaaa','aaa'),('abdj','ab'),('a A! a A!',' A!')]
pairs += [(''.join(R.choices(' abAB!~',k=R.randrange(60))),''.join(R.choices(' abAB!~',k=R.randrange(12)))) for _ in range(200)]
for t,p in pairs:
    for task in ['naive','kmp','rk']:add(dict(task=task,text=t,pattern=p),search(t,p),'slice oracle: overlap, empty, collision, ASCII')
for t in ['', 'a','banana','aaaaaa','abcdef','abXabYcdZcd']+[''.join(R.choices(' aAb!',k=R.randrange(50))) for _ in range(100)]:
    add(dict(task='suffix',text=t),suffix_oracle(t),'direct sorted suffix and adjacent LCP oracle')
for p,ts in [([],['','a']),(['a'],[]),(['he','she','he'],['sheshe','sheshe','']),(['a','aa','aaa'],['aaaa'])]+[([''.join(R.choices('ab !',k=R.randrange(1,8))) for _ in range(R.randrange(12))],[''.join(R.choices('ab !',k=R.randrange(40))) for _ in range(R.randrange(5))]) for _ in range(100)]:
    add(dict(task='aho',patterns=p,texts=ts),[[len(search(t,x)) for x in p] for t in ts],'direct dictionary oracle with duplicates and fresh texts')

def validate(q,a,e):
    t=q['task']
    if t=='lcs':
        assert type(a['length']) is int and a['length']==e
        assert isinstance(a['sequence'],str) and len(a['sequence'])==e and subseq(a['sequence'],q['a']) and subseq(a['sequence'],q['b'])
    elif t=='merge':
        assert type(a['cost']) is int and a['cost']==e
        blocks={(i,i):v for i,v in enumerate(q['sizes'])}; cost=0
        assert len(a['plan'])==max(0,len(q['sizes'])-1)
        for l,k,r in a['plan']:
            assert all(type(v) is int for v in [l,k,r]) and l<=k<r
            v=blocks.pop((l,k))+blocks.pop((k+1,r)); blocks[l,r]=v; cost+=v
        assert cost==e and len(blocks)<=1
    elif t=='tree':
        assert type(a['weight']) is int and a['weight']==e
        s=a['selected']; assert all(type(v) is int and 0<=v<q['n'] for v in s)
        assert len(s)==len(set(s)) and sum(q['weights'][i] for i in s)==e
        s=set(s); assert all(not(u in s and v in s) for u,v in q['edges'])
    elif t in ['naive','kmp','rk']:
        assert a['positions']==e and all(type(v) is int for v in a['positions'])
    elif t=='suffix':
        assert a['sa']==e[0] and a['lcp']==e[1]
        assert all(type(v) is int for v in a['sa']+a['lcp'])
        rep=a['repeated']; assert isinstance(rep,str) and len(rep)==max(e[1],default=0)
        assert not rep or len(search(q['text'],rep))>=2
    else:
        assert a['counts']==e and all(type(v) is int for row in a['counts'] for v in row)

def main():
    command=build('Java'); failures=[]; evidence=[]; start=time.perf_counter()
    answers=execute(command,[q for q,e,w in cases]*2)
    for (q,e,w),a in zip(cases*2,answers):
        try:validate(q,a,e)
        except (AssertionError,KeyError,TypeError,ValueError) as ex:failures.append((q,a,str(ex)))
        if len(evidence)<16 or (q['task'] not in {x['input']['task'] for x in evidence}):evidence.append(dict(input=q,expected=e,actual=a,detects=w))
    if failures:
        print(f'FAIL {len(failures)}/{len(answers)}: {failures[:3]}');return 1
    print(f'PASS {len(answers)} independently checked mixed requests (two batches)')
    if '--stress' in sys.argv:
        stress=[]
        stress.append((dict(task='lcs',a='a'*1000,b='a'*1000),1000))
        # Equal leaf weights: optimal alphabetic full binary tree depths floor/ceil(log2 n).
        stress.append((dict(task='merge',sizes=[10**9]*200),1544*10**9))
        for shape in ['chain','star']:
            n=100000;e=[[i-1 if shape=='chain' else 0,i] for i in range(1,n)]
            stress.append((dict(task='tree',n=n,weights=[10**9]*n,edges=e),(n//2 if shape=='chain' else n-1)*10**9))
        for task,n,m in [('naive',3000,1500),('kmp',200000,100000),('rk',10000,2000)]:
            stress.append((dict(task=task,text='a'*n,pattern='a'*m),list(range(n-m+1))))
        stress.append((dict(task='kmp',text='x'*200000,pattern=''),list(range(200001))))
        n=50000;stress.append((dict(task='suffix',text='a'*n),(list(range(n-1,-1,-1)),list(range(n)))))
        pats=['a'*10]*10000;ts=['a'*2000]*100
        stress.append((dict(task='aho',patterns=pats,texts=ts),[[1991]*10000 for _ in ts]))
        pats=['a'*100000]; ts=['a'*200000]
        stress.append((dict(task='aho',patterns=pats,texts=ts),[[100001]]))
        for q,e in stress:
            tic=time.perf_counter();a=execute(command,[q])[0];validate(q,a,e)
            print(f"PASS scale {q['task']}: {time.perf_counter()-tic:.3f}s")
    path=ROOT/'tests/student/results/test-evidence.json'
    path.write_text(json.dumps(evidence,indent=2,sort_keys=True)+'\n')
    print(f'PASS total wall time {time.perf_counter()-start:.3f}s; Java heap 384 MiB')
    return 0
if __name__=='__main__':sys.exit(main())
