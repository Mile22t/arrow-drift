(function(root){
const dimensions=[8,8,16],directions=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]];
const names=['right (+X)','left (−X)','up (+Y)','down (−Y)','front (+Z)','back (−Z)'];
const key=p=>p.join(',');
function occupiedCells(b){const a=[];for(let x=0;x<b.s[0];x++)for(let y=0;y<b.s[1];y++)for(let z=0;z<b.s[2];z++)a.push([b.p[0]+x,b.p[1]+y,b.p[2]+z]);return a}
function occupancy(blocks){const map=new Map();for(const b of blocks)for(const c of occupiedCells(b))map.set(key(c),b.id);return map}
function canExitMap(b,map){const d=directions[b.d],axis=d.findIndex(v=>v!==0),sign=d[axis];const other=[0,1,2].filter(a=>a!==axis);for(let a=0;a<b.s[other[0]];a++)for(let c=0;c<b.s[other[1]];c++){let p=[...b.p];p[other[0]]+=a;p[other[1]]+=c;for(p[axis]=sign>0?b.p[axis]+b.s[axis]:b.p[axis]-1;p[axis]>=0&&p[axis]<dimensions[axis];p[axis]+=sign){const id=map.get(key(p));if(id!==undefined&&id!==b.id)return false}}return true}
function canExit(b,blocks){return canExitMap(b,occupancy(blocks))}
function rng(seed){let state=seed>>>0;return()=>{state=(1664525*state+1013904223)>>>0;return state/4294967296}}
function shuffled(a,random){for(let i=a.length-1;i>0;i--){const j=Math.floor(random()*(i+1));[a[i],a[j]]=[a[j],a[i]]}return a}
function assignDirections(blocks,random){const result=blocks.map(b=>({...b,p:[...b.p],s:[...b.s]})),map=occupancy(result),pending=[...result];while(pending.length){let removed=0;shuffled(pending,random);for(let i=pending.length-1;i>=0;i--){const b=pending[i],ds=directions.map((_,d)=>d).filter(d=>canExitMap({...b,d},map));if(!ds.length)continue;b.d=ds[Math.floor(random()*ds.length)];for(const c of occupiedCells(b))map.delete(key(c));pending.splice(i,1);removed++}if(!removed)throw Error('This arrangement cannot be peeled')}return result}
function generate(level){const random=rng(level*7919),map=new Set(),blocks=[];for(let z=0;z<16;z++)for(let y=0;y<8;y++)for(let x=0;x<8;x++){const p=[x,y,z];if(map.has(key(p)))continue;const r=random(),sizes=r<.24?[[2,2,1],[1,2,1],[1,1,1]]:r<.55?[[1,2,1],[1,1,1]]:[[1,1,1]];let s=sizes.find(s=>p.every((v,i)=>v+s[i]<=dimensions[i])&&occupiedCells({p,s}).every(c=>!map.has(key(c))));const b={id:blocks.length,p,s,d:0};blocks.push(b);for(const c of occupiedCells(b))map.add(key(c))}return{dimensions:[...dimensions],blocks:assignDirections(blocks,random)}}
function shuffle(blocks,seed){return assignDirections(blocks,rng(seed))}
const api={dimensions,directions,names,key,occupiedCells,occupancy,canExitMap,canExit,generate,shuffle};root.Puzzle3D=api;if(typeof module!=='undefined')module.exports=api;
})(globalThis);
