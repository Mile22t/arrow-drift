(function(){
const canvas=document.createElement('canvas'),renderer=new DepthRenderer(canvas),gl=renderer.gl;
const options={camera:p=>p,width:128,height:128,panX:0,panY:0,focal:2400,distance:48};
const red=[200,50,50],green=[50,200,50];
const sloped={c:[0,0,0],u:[.8,0,.6],v:[0,1,0],normal:[-.6,0,.8],hu:2,hv:2,rgb:red,exposed:false,direction:[1,0,0]};
const flat={c:[0,0,0],u:[1,0,0],v:[0,1,0],normal:[0,0,1],hu:2,hv:2,rgb:green,exposed:false,direction:[1,0,0]};
const read=(x,y)=>{const p=new Uint8Array(4);gl.readPixels(x,y,1,1,gl.RGBA,gl.UNSIGNED_BYTE,p);return p};
const check=(actual,expected)=>expected.every((n,i)=>Math.abs(actual[i]-n)<=2)&&actual[3]===255;
for(const dpr of [1,2]){renderer.resize(128,128,dpr);for(const order of [[sloped,flat],[flat,sloped]]){renderer.render(order,options);if(!check(read(32*dpr,64*dpr),green)||!check(read(96*dpr,64*dpr),red))throw Error('Per-pixel depth failed: intersecting projected faces or draw order affected visibility')}}
renderer.resize(128,128,1);
const foreground={...flat,c:[0,0,2],rgb:red},hiddenArrow={...flat,c:[0,0,-2],rgb:green,exposed:true};
for(const order of [[foreground,hiddenArrow],[hiddenArrow,foreground]]){renderer.render(order,options);if(!check(read(64,63),red))throw Error('An occluded arrow rendered through the foreground face')}
if(gl.getParameter(gl.DEPTH_BITS)<16||gl.getError()!==gl.NO_ERROR)throw Error('Depth buffer or WebGL state unavailable');
const lose=gl.getExtension('WEBGL_lose_context');if(lose)lose.loseContext();return true;
})()
