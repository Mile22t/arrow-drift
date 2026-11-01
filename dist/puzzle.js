(function(root){
const directions=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]];
const names=['right (+X)','left (−X)','up (+Y)','down (−Y)','front (+Z)','back (−Z)'];
const key=p=>p.join(',');
function canExit(block,blocks){const d=directions[block.d];return !blocks.some(b=>b.id!==block.id&&d.every((v,i)=>v===0?b.p[i]===block.p[i]:((b.p[i]-block.p[i])*v>0)))}
function generate(level){let seed=(level*7919)>>>0;const random=()=>{seed=(1664525*seed+1013904223)>>>0;return seed/4294967296};let n=Math.min(5,3+Math.floor((level-1)/3));let blocks=[];for(let x=0;x<n;x++)for(let y=0;y<n;y++)for(let z=0;z<n;z++)blocks.push({id:blocks.length,p:[x,y,z],d:0});const remaining=blocks.map(b=>({...b}));while(remaining.length){const choices=[];for(const b of remaining){let ds=directions.map((_,d)=>d).filter(d=>canExit({...b,d},remaining));if(ds.length)choices.push({b,ds})}if(!choices.length)throw Error('No peelable block');const c=choices[Math.floor(random()*choices.length)];blocks[c.b.id].d=c.ds[Math.floor(random()*c.ds.length)];remaining.splice(remaining.findIndex(b=>b.id===c.b.id),1)}return {n,blocks}}
const api={directions,names,key,canExit,generate};root.Puzzle3D=api;if(typeof module!=='undefined')module.exports=api;
})(globalThis);
