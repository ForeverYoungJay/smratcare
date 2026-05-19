import{u as cc,r as ct,w as Hi,J as uc,e1 as et,ed as mt,eZ as ge,f as Ge,E as ms,v as wh,eY as O,d_ as Bi,e2 as Xe,aL as st,eb as ki,C as ke,f5 as ld,aq as cd,ar as ud,f1 as Rh,e0 as dn,e9 as Qs,eW as Fn,e_ as di,ag as yn,dQ as _s}from"./auth-oL2NLDlK.js";import{d as hd,u as fd}from"./admin-DrbWSNhP.js";import{P as dd}from"./PageContainer-Ct1sr5pK.js";import{_ as hc}from"./_plugin-vue_export-helper-DlAUqK2U.js";import{g as pd,a as md}from"./bed-DSOnAHYT.js";import{g as _d}from"./elder-BxkAGxif.js";import{u as gd}from"./useLiveSyncRefresh-DOQWM2R7.js";import{u as vd}from"./user-B6XowGVs.js";import"./request-CFenD-uP.js";import"./pageAccess-qd2y-21W.js";const xd={class:"animated-number"},Md=cc({__name:"AnimatedMetricNumber",props:{value:{},duration:{default:900},decimals:{default:0},prefix:{default:""},suffix:{default:""}},setup(r){const e=r,t=ct(e.value);let n=0;function i(){n&&(cancelAnimationFrame(n),n=0)}function s(o){i();const l=t.value,c=o-l;if(!c)return;const u=performance.now(),f=h=>{const d=Math.min((h-u)/e.duration,1),_=1-Math.pow(1-d,3);if(t.value=l+c*_,d<1){n=requestAnimationFrame(f);return}t.value=o,n=0};n=requestAnimationFrame(f)}Hi(()=>e.value,o=>{s(o)},{immediate:!0}),uc(()=>{i()});const a=Ge(()=>{const o=t.value.toFixed(e.decimals);return`${e.prefix}${o}${e.suffix}`});return(o,l)=>(et(),mt("span",xd,ge(a.value),1))}}),Sd=hc(Md,[["__scopeId","data-v-9090121e"]]);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */const fc="183",$r={ROTATE:0,DOLLY:1,PAN:2},Xr={ROTATE:0,PAN:1,DOLLY_PAN:2,DOLLY_ROTATE:3},yd=0,Kc=1,bd=2,Da=1,Ed=2,As=3,Zi=0,mn=1,Xn=2,bi=0,Kr=1,Zc=2,jc=3,Jc=4,Td=5,dr=100,Ad=101,wd=102,Rd=103,Cd=104,Pd=200,Dd=201,Ld=202,Id=203,tl=204,nl=205,Ud=206,Nd=207,Fd=208,Od=209,Bd=210,kd=211,zd=212,Vd=213,Gd=214,il=0,rl=1,sl=2,es=3,al=4,ol=5,ll=6,cl=7,Ch=0,Hd=1,Wd=2,si=0,Ph=1,Dh=2,Lh=3,dc=4,Ih=5,Uh=6,Nh=7,Fh=300,yr=301,ts=302,co=303,uo=304,eo=306,ul=1e3,yi=1001,hl=1002,qt=1003,Xd=1004,ea=1005,en=1006,ho=1007,mr=1008,Rn=1009,Oh=1010,Bh=1011,Ns=1012,pc=1013,oi=1014,ni=1015,Ti=1016,mc=1017,_c=1018,Fs=1020,kh=35902,zh=35899,Vh=1021,Gh=1022,qn=1023,Ai=1026,_r=1027,Hh=1028,gc=1029,ns=1030,vc=1031,xc=1033,La=33776,Ia=33777,Ua=33778,Na=33779,fl=35840,dl=35841,pl=35842,ml=35843,_l=36196,gl=37492,vl=37496,xl=37488,Ml=37489,Sl=37490,yl=37491,bl=37808,El=37809,Tl=37810,Al=37811,wl=37812,Rl=37813,Cl=37814,Pl=37815,Dl=37816,Ll=37817,Il=37818,Ul=37819,Nl=37820,Fl=37821,Ol=36492,Bl=36494,kl=36495,zl=36283,Vl=36284,Gl=36285,Hl=36286,qd=3200,Wh=0,Yd=1,Gi="",Tn="srgb",is="srgb-linear",Va="linear",_t="srgb",Rr=7680,Qc=519,$d=512,Kd=513,Zd=514,Mc=515,jd=516,Jd=517,Sc=518,Qd=519,eu=35044,tu="300 es",ii=2e3,Os=2001;function ep(r){for(let e=r.length-1;e>=0;--e)if(r[e]>=65535)return!0;return!1}function Ga(r){return document.createElementNS("http://www.w3.org/1999/xhtml",r)}function tp(){const r=Ga("canvas");return r.style.display="block",r}const nu={};function iu(...r){const e="THREE."+r.shift();console.log(e,...r)}function Xh(r){const e=r[0];if(typeof e=="string"&&e.startsWith("TSL:")){const t=r[1];t&&t.isStackTrace?r[0]+=" "+t.getLocation():r[1]='Stack trace not available. Enable "THREE.Node.captureStackTrace" to capture stack traces.'}return r}function He(...r){r=Xh(r);const e="THREE."+r.shift();{const t=r[0];t&&t.isStackTrace?console.warn(t.getError(e)):console.warn(e,...r)}}function ut(...r){r=Xh(r);const e="THREE."+r.shift();{const t=r[0];t&&t.isStackTrace?console.error(t.getError(e)):console.error(e,...r)}}function Ha(...r){const e=r.join(" ");e in nu||(nu[e]=!0,He(...r))}function np(r,e,t){return new Promise(function(n,i){function s(){switch(r.clientWaitSync(e,r.SYNC_FLUSH_COMMANDS_BIT,0)){case r.WAIT_FAILED:i();break;case r.TIMEOUT_EXPIRED:setTimeout(s,t);break;default:n()}}setTimeout(s,t)})}const ip={[il]:rl,[sl]:ll,[al]:cl,[es]:ol,[rl]:il,[ll]:sl,[cl]:al,[ol]:es};class br{addEventListener(e,t){this._listeners===void 0&&(this._listeners={});const n=this._listeners;n[e]===void 0&&(n[e]=[]),n[e].indexOf(t)===-1&&n[e].push(t)}hasEventListener(e,t){const n=this._listeners;return n===void 0?!1:n[e]!==void 0&&n[e].indexOf(t)!==-1}removeEventListener(e,t){const n=this._listeners;if(n===void 0)return;const i=n[e];if(i!==void 0){const s=i.indexOf(t);s!==-1&&i.splice(s,1)}}dispatchEvent(e){const t=this._listeners;if(t===void 0)return;const n=t[e.type];if(n!==void 0){e.target=this;const i=n.slice(0);for(let s=0,a=i.length;s<a;s++)i[s].call(this,e);e.target=null}}}const jt=["00","01","02","03","04","05","06","07","08","09","0a","0b","0c","0d","0e","0f","10","11","12","13","14","15","16","17","18","19","1a","1b","1c","1d","1e","1f","20","21","22","23","24","25","26","27","28","29","2a","2b","2c","2d","2e","2f","30","31","32","33","34","35","36","37","38","39","3a","3b","3c","3d","3e","3f","40","41","42","43","44","45","46","47","48","49","4a","4b","4c","4d","4e","4f","50","51","52","53","54","55","56","57","58","59","5a","5b","5c","5d","5e","5f","60","61","62","63","64","65","66","67","68","69","6a","6b","6c","6d","6e","6f","70","71","72","73","74","75","76","77","78","79","7a","7b","7c","7d","7e","7f","80","81","82","83","84","85","86","87","88","89","8a","8b","8c","8d","8e","8f","90","91","92","93","94","95","96","97","98","99","9a","9b","9c","9d","9e","9f","a0","a1","a2","a3","a4","a5","a6","a7","a8","a9","aa","ab","ac","ad","ae","af","b0","b1","b2","b3","b4","b5","b6","b7","b8","b9","ba","bb","bc","bd","be","bf","c0","c1","c2","c3","c4","c5","c6","c7","c8","c9","ca","cb","cc","cd","ce","cf","d0","d1","d2","d3","d4","d5","d6","d7","d8","d9","da","db","dc","dd","de","df","e0","e1","e2","e3","e4","e5","e6","e7","e8","e9","ea","eb","ec","ed","ee","ef","f0","f1","f2","f3","f4","f5","f6","f7","f8","f9","fa","fb","fc","fd","fe","ff"],Ps=Math.PI/180,Wl=180/Math.PI;function qs(){const r=Math.random()*4294967295|0,e=Math.random()*4294967295|0,t=Math.random()*4294967295|0,n=Math.random()*4294967295|0;return(jt[r&255]+jt[r>>8&255]+jt[r>>16&255]+jt[r>>24&255]+"-"+jt[e&255]+jt[e>>8&255]+"-"+jt[e>>16&15|64]+jt[e>>24&255]+"-"+jt[t&63|128]+jt[t>>8&255]+"-"+jt[t>>16&255]+jt[t>>24&255]+jt[n&255]+jt[n>>8&255]+jt[n>>16&255]+jt[n>>24&255]).toLowerCase()}function nt(r,e,t){return Math.max(e,Math.min(t,r))}function rp(r,e){return(r%e+e)%e}function fo(r,e,t){return(1-t)*r+t*e}function gs(r,e){switch(e.constructor){case Float32Array:return r;case Uint32Array:return r/4294967295;case Uint16Array:return r/65535;case Uint8Array:return r/255;case Int32Array:return Math.max(r/2147483647,-1);case Int16Array:return Math.max(r/32767,-1);case Int8Array:return Math.max(r/127,-1);default:throw new Error("Invalid component type.")}}function un(r,e){switch(e.constructor){case Float32Array:return r;case Uint32Array:return Math.round(r*4294967295);case Uint16Array:return Math.round(r*65535);case Uint8Array:return Math.round(r*255);case Int32Array:return Math.round(r*2147483647);case Int16Array:return Math.round(r*32767);case Int8Array:return Math.round(r*127);default:throw new Error("Invalid component type.")}}const sp={DEG2RAD:Ps};class qe{constructor(e=0,t=0){qe.prototype.isVector2=!0,this.x=e,this.y=t}get width(){return this.x}set width(e){this.x=e}get height(){return this.y}set height(e){this.y=e}set(e,t){return this.x=e,this.y=t,this}setScalar(e){return this.x=e,this.y=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y)}copy(e){return this.x=e.x,this.y=e.y,this}add(e){return this.x+=e.x,this.y+=e.y,this}addScalar(e){return this.x+=e,this.y+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this}subScalar(e){return this.x-=e,this.y-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this}multiply(e){return this.x*=e.x,this.y*=e.y,this}multiplyScalar(e){return this.x*=e,this.y*=e,this}divide(e){return this.x/=e.x,this.y/=e.y,this}divideScalar(e){return this.multiplyScalar(1/e)}applyMatrix3(e){const t=this.x,n=this.y,i=e.elements;return this.x=i[0]*t+i[3]*n+i[6],this.y=i[1]*t+i[4]*n+i[7],this}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this}clamp(e,t){return this.x=nt(this.x,e.x,t.x),this.y=nt(this.y,e.y,t.y),this}clampScalar(e,t){return this.x=nt(this.x,e,t),this.y=nt(this.y,e,t),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(nt(n,e,t))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this}negate(){return this.x=-this.x,this.y=-this.y,this}dot(e){return this.x*e.x+this.y*e.y}cross(e){return this.x*e.y-this.y*e.x}lengthSq(){return this.x*this.x+this.y*this.y}length(){return Math.sqrt(this.x*this.x+this.y*this.y)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)}normalize(){return this.divideScalar(this.length()||1)}angle(){return Math.atan2(-this.y,-this.x)+Math.PI}angleTo(e){const t=Math.sqrt(this.lengthSq()*e.lengthSq());if(t===0)return Math.PI/2;const n=this.dot(e)/t;return Math.acos(nt(n,-1,1))}distanceTo(e){return Math.sqrt(this.distanceToSquared(e))}distanceToSquared(e){const t=this.x-e.x,n=this.y-e.y;return t*t+n*n}manhattanDistanceTo(e){return Math.abs(this.x-e.x)+Math.abs(this.y-e.y)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this}equals(e){return e.x===this.x&&e.y===this.y}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this}rotateAround(e,t){const n=Math.cos(t),i=Math.sin(t),s=this.x-e.x,a=this.y-e.y;return this.x=s*n-a*i+e.x,this.y=s*i+a*n+e.y,this}random(){return this.x=Math.random(),this.y=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y}}class ji{constructor(e=0,t=0,n=0,i=1){this.isQuaternion=!0,this._x=e,this._y=t,this._z=n,this._w=i}static slerpFlat(e,t,n,i,s,a,o){let l=n[i+0],c=n[i+1],u=n[i+2],f=n[i+3],h=s[a+0],d=s[a+1],_=s[a+2],g=s[a+3];if(f!==g||l!==h||c!==d||u!==_){let p=l*h+c*d+u*_+f*g;p<0&&(h=-h,d=-d,_=-_,g=-g,p=-p);let m=1-o;if(p<.9995){const M=Math.acos(p),S=Math.sin(M);m=Math.sin(m*M)/S,o=Math.sin(o*M)/S,l=l*m+h*o,c=c*m+d*o,u=u*m+_*o,f=f*m+g*o}else{l=l*m+h*o,c=c*m+d*o,u=u*m+_*o,f=f*m+g*o;const M=1/Math.sqrt(l*l+c*c+u*u+f*f);l*=M,c*=M,u*=M,f*=M}}e[t]=l,e[t+1]=c,e[t+2]=u,e[t+3]=f}static multiplyQuaternionsFlat(e,t,n,i,s,a){const o=n[i],l=n[i+1],c=n[i+2],u=n[i+3],f=s[a],h=s[a+1],d=s[a+2],_=s[a+3];return e[t]=o*_+u*f+l*d-c*h,e[t+1]=l*_+u*h+c*f-o*d,e[t+2]=c*_+u*d+o*h-l*f,e[t+3]=u*_-o*f-l*h-c*d,e}get x(){return this._x}set x(e){this._x=e,this._onChangeCallback()}get y(){return this._y}set y(e){this._y=e,this._onChangeCallback()}get z(){return this._z}set z(e){this._z=e,this._onChangeCallback()}get w(){return this._w}set w(e){this._w=e,this._onChangeCallback()}set(e,t,n,i){return this._x=e,this._y=t,this._z=n,this._w=i,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._w)}copy(e){return this._x=e.x,this._y=e.y,this._z=e.z,this._w=e.w,this._onChangeCallback(),this}setFromEuler(e,t=!0){const n=e._x,i=e._y,s=e._z,a=e._order,o=Math.cos,l=Math.sin,c=o(n/2),u=o(i/2),f=o(s/2),h=l(n/2),d=l(i/2),_=l(s/2);switch(a){case"XYZ":this._x=h*u*f+c*d*_,this._y=c*d*f-h*u*_,this._z=c*u*_+h*d*f,this._w=c*u*f-h*d*_;break;case"YXZ":this._x=h*u*f+c*d*_,this._y=c*d*f-h*u*_,this._z=c*u*_-h*d*f,this._w=c*u*f+h*d*_;break;case"ZXY":this._x=h*u*f-c*d*_,this._y=c*d*f+h*u*_,this._z=c*u*_+h*d*f,this._w=c*u*f-h*d*_;break;case"ZYX":this._x=h*u*f-c*d*_,this._y=c*d*f+h*u*_,this._z=c*u*_-h*d*f,this._w=c*u*f+h*d*_;break;case"YZX":this._x=h*u*f+c*d*_,this._y=c*d*f+h*u*_,this._z=c*u*_-h*d*f,this._w=c*u*f-h*d*_;break;case"XZY":this._x=h*u*f-c*d*_,this._y=c*d*f-h*u*_,this._z=c*u*_+h*d*f,this._w=c*u*f+h*d*_;break;default:He("Quaternion: .setFromEuler() encountered an unknown order: "+a)}return t===!0&&this._onChangeCallback(),this}setFromAxisAngle(e,t){const n=t/2,i=Math.sin(n);return this._x=e.x*i,this._y=e.y*i,this._z=e.z*i,this._w=Math.cos(n),this._onChangeCallback(),this}setFromRotationMatrix(e){const t=e.elements,n=t[0],i=t[4],s=t[8],a=t[1],o=t[5],l=t[9],c=t[2],u=t[6],f=t[10],h=n+o+f;if(h>0){const d=.5/Math.sqrt(h+1);this._w=.25/d,this._x=(u-l)*d,this._y=(s-c)*d,this._z=(a-i)*d}else if(n>o&&n>f){const d=2*Math.sqrt(1+n-o-f);this._w=(u-l)/d,this._x=.25*d,this._y=(i+a)/d,this._z=(s+c)/d}else if(o>f){const d=2*Math.sqrt(1+o-n-f);this._w=(s-c)/d,this._x=(i+a)/d,this._y=.25*d,this._z=(l+u)/d}else{const d=2*Math.sqrt(1+f-n-o);this._w=(a-i)/d,this._x=(s+c)/d,this._y=(l+u)/d,this._z=.25*d}return this._onChangeCallback(),this}setFromUnitVectors(e,t){let n=e.dot(t)+1;return n<1e-8?(n=0,Math.abs(e.x)>Math.abs(e.z)?(this._x=-e.y,this._y=e.x,this._z=0,this._w=n):(this._x=0,this._y=-e.z,this._z=e.y,this._w=n)):(this._x=e.y*t.z-e.z*t.y,this._y=e.z*t.x-e.x*t.z,this._z=e.x*t.y-e.y*t.x,this._w=n),this.normalize()}angleTo(e){return 2*Math.acos(Math.abs(nt(this.dot(e),-1,1)))}rotateTowards(e,t){const n=this.angleTo(e);if(n===0)return this;const i=Math.min(1,t/n);return this.slerp(e,i),this}identity(){return this.set(0,0,0,1)}invert(){return this.conjugate()}conjugate(){return this._x*=-1,this._y*=-1,this._z*=-1,this._onChangeCallback(),this}dot(e){return this._x*e._x+this._y*e._y+this._z*e._z+this._w*e._w}lengthSq(){return this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w}length(){return Math.sqrt(this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w)}normalize(){let e=this.length();return e===0?(this._x=0,this._y=0,this._z=0,this._w=1):(e=1/e,this._x=this._x*e,this._y=this._y*e,this._z=this._z*e,this._w=this._w*e),this._onChangeCallback(),this}multiply(e){return this.multiplyQuaternions(this,e)}premultiply(e){return this.multiplyQuaternions(e,this)}multiplyQuaternions(e,t){const n=e._x,i=e._y,s=e._z,a=e._w,o=t._x,l=t._y,c=t._z,u=t._w;return this._x=n*u+a*o+i*c-s*l,this._y=i*u+a*l+s*o-n*c,this._z=s*u+a*c+n*l-i*o,this._w=a*u-n*o-i*l-s*c,this._onChangeCallback(),this}slerp(e,t){let n=e._x,i=e._y,s=e._z,a=e._w,o=this.dot(e);o<0&&(n=-n,i=-i,s=-s,a=-a,o=-o);let l=1-t;if(o<.9995){const c=Math.acos(o),u=Math.sin(c);l=Math.sin(l*c)/u,t=Math.sin(t*c)/u,this._x=this._x*l+n*t,this._y=this._y*l+i*t,this._z=this._z*l+s*t,this._w=this._w*l+a*t,this._onChangeCallback()}else this._x=this._x*l+n*t,this._y=this._y*l+i*t,this._z=this._z*l+s*t,this._w=this._w*l+a*t,this.normalize();return this}slerpQuaternions(e,t,n){return this.copy(e).slerp(t,n)}random(){const e=2*Math.PI*Math.random(),t=2*Math.PI*Math.random(),n=Math.random(),i=Math.sqrt(1-n),s=Math.sqrt(n);return this.set(i*Math.sin(e),i*Math.cos(e),s*Math.sin(t),s*Math.cos(t))}equals(e){return e._x===this._x&&e._y===this._y&&e._z===this._z&&e._w===this._w}fromArray(e,t=0){return this._x=e[t],this._y=e[t+1],this._z=e[t+2],this._w=e[t+3],this._onChangeCallback(),this}toArray(e=[],t=0){return e[t]=this._x,e[t+1]=this._y,e[t+2]=this._z,e[t+3]=this._w,e}fromBufferAttribute(e,t){return this._x=e.getX(t),this._y=e.getY(t),this._z=e.getZ(t),this._w=e.getW(t),this._onChangeCallback(),this}toJSON(){return this.toArray()}_onChange(e){return this._onChangeCallback=e,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._w}}class V{constructor(e=0,t=0,n=0){V.prototype.isVector3=!0,this.x=e,this.y=t,this.z=n}set(e,t,n){return n===void 0&&(n=this.z),this.x=e,this.y=t,this.z=n,this}setScalar(e){return this.x=e,this.y=e,this.z=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setZ(e){return this.z=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;case 2:this.z=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;case 2:return this.z;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y,this.z)}copy(e){return this.x=e.x,this.y=e.y,this.z=e.z,this}add(e){return this.x+=e.x,this.y+=e.y,this.z+=e.z,this}addScalar(e){return this.x+=e,this.y+=e,this.z+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this.z=e.z+t.z,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this.z+=e.z*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this.z-=e.z,this}subScalar(e){return this.x-=e,this.y-=e,this.z-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this.z=e.z-t.z,this}multiply(e){return this.x*=e.x,this.y*=e.y,this.z*=e.z,this}multiplyScalar(e){return this.x*=e,this.y*=e,this.z*=e,this}multiplyVectors(e,t){return this.x=e.x*t.x,this.y=e.y*t.y,this.z=e.z*t.z,this}applyEuler(e){return this.applyQuaternion(ru.setFromEuler(e))}applyAxisAngle(e,t){return this.applyQuaternion(ru.setFromAxisAngle(e,t))}applyMatrix3(e){const t=this.x,n=this.y,i=this.z,s=e.elements;return this.x=s[0]*t+s[3]*n+s[6]*i,this.y=s[1]*t+s[4]*n+s[7]*i,this.z=s[2]*t+s[5]*n+s[8]*i,this}applyNormalMatrix(e){return this.applyMatrix3(e).normalize()}applyMatrix4(e){const t=this.x,n=this.y,i=this.z,s=e.elements,a=1/(s[3]*t+s[7]*n+s[11]*i+s[15]);return this.x=(s[0]*t+s[4]*n+s[8]*i+s[12])*a,this.y=(s[1]*t+s[5]*n+s[9]*i+s[13])*a,this.z=(s[2]*t+s[6]*n+s[10]*i+s[14])*a,this}applyQuaternion(e){const t=this.x,n=this.y,i=this.z,s=e.x,a=e.y,o=e.z,l=e.w,c=2*(a*i-o*n),u=2*(o*t-s*i),f=2*(s*n-a*t);return this.x=t+l*c+a*f-o*u,this.y=n+l*u+o*c-s*f,this.z=i+l*f+s*u-a*c,this}project(e){return this.applyMatrix4(e.matrixWorldInverse).applyMatrix4(e.projectionMatrix)}unproject(e){return this.applyMatrix4(e.projectionMatrixInverse).applyMatrix4(e.matrixWorld)}transformDirection(e){const t=this.x,n=this.y,i=this.z,s=e.elements;return this.x=s[0]*t+s[4]*n+s[8]*i,this.y=s[1]*t+s[5]*n+s[9]*i,this.z=s[2]*t+s[6]*n+s[10]*i,this.normalize()}divide(e){return this.x/=e.x,this.y/=e.y,this.z/=e.z,this}divideScalar(e){return this.multiplyScalar(1/e)}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this.z=Math.min(this.z,e.z),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this.z=Math.max(this.z,e.z),this}clamp(e,t){return this.x=nt(this.x,e.x,t.x),this.y=nt(this.y,e.y,t.y),this.z=nt(this.z,e.z,t.z),this}clampScalar(e,t){return this.x=nt(this.x,e,t),this.y=nt(this.y,e,t),this.z=nt(this.z,e,t),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(nt(n,e,t))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this}dot(e){return this.x*e.x+this.y*e.y+this.z*e.z}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)}normalize(){return this.divideScalar(this.length()||1)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this.z+=(e.z-this.z)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this.z=e.z+(t.z-e.z)*n,this}cross(e){return this.crossVectors(this,e)}crossVectors(e,t){const n=e.x,i=e.y,s=e.z,a=t.x,o=t.y,l=t.z;return this.x=i*l-s*o,this.y=s*a-n*l,this.z=n*o-i*a,this}projectOnVector(e){const t=e.lengthSq();if(t===0)return this.set(0,0,0);const n=e.dot(this)/t;return this.copy(e).multiplyScalar(n)}projectOnPlane(e){return po.copy(this).projectOnVector(e),this.sub(po)}reflect(e){return this.sub(po.copy(e).multiplyScalar(2*this.dot(e)))}angleTo(e){const t=Math.sqrt(this.lengthSq()*e.lengthSq());if(t===0)return Math.PI/2;const n=this.dot(e)/t;return Math.acos(nt(n,-1,1))}distanceTo(e){return Math.sqrt(this.distanceToSquared(e))}distanceToSquared(e){const t=this.x-e.x,n=this.y-e.y,i=this.z-e.z;return t*t+n*n+i*i}manhattanDistanceTo(e){return Math.abs(this.x-e.x)+Math.abs(this.y-e.y)+Math.abs(this.z-e.z)}setFromSpherical(e){return this.setFromSphericalCoords(e.radius,e.phi,e.theta)}setFromSphericalCoords(e,t,n){const i=Math.sin(t)*e;return this.x=i*Math.sin(n),this.y=Math.cos(t)*e,this.z=i*Math.cos(n),this}setFromCylindrical(e){return this.setFromCylindricalCoords(e.radius,e.theta,e.y)}setFromCylindricalCoords(e,t,n){return this.x=e*Math.sin(t),this.y=n,this.z=e*Math.cos(t),this}setFromMatrixPosition(e){const t=e.elements;return this.x=t[12],this.y=t[13],this.z=t[14],this}setFromMatrixScale(e){const t=this.setFromMatrixColumn(e,0).length(),n=this.setFromMatrixColumn(e,1).length(),i=this.setFromMatrixColumn(e,2).length();return this.x=t,this.y=n,this.z=i,this}setFromMatrixColumn(e,t){return this.fromArray(e.elements,t*4)}setFromMatrix3Column(e,t){return this.fromArray(e.elements,t*3)}setFromEuler(e){return this.x=e._x,this.y=e._y,this.z=e._z,this}setFromColor(e){return this.x=e.r,this.y=e.g,this.z=e.b,this}equals(e){return e.x===this.x&&e.y===this.y&&e.z===this.z}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this.z=e[t+2],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e[t+2]=this.z,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this.z=e.getZ(t),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this}randomDirection(){const e=Math.random()*Math.PI*2,t=Math.random()*2-1,n=Math.sqrt(1-t*t);return this.x=n*Math.cos(e),this.y=t,this.z=n*Math.sin(e),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z}}const po=new V,ru=new ji;class Ke{constructor(e,t,n,i,s,a,o,l,c){Ke.prototype.isMatrix3=!0,this.elements=[1,0,0,0,1,0,0,0,1],e!==void 0&&this.set(e,t,n,i,s,a,o,l,c)}set(e,t,n,i,s,a,o,l,c){const u=this.elements;return u[0]=e,u[1]=i,u[2]=o,u[3]=t,u[4]=s,u[5]=l,u[6]=n,u[7]=a,u[8]=c,this}identity(){return this.set(1,0,0,0,1,0,0,0,1),this}copy(e){const t=this.elements,n=e.elements;return t[0]=n[0],t[1]=n[1],t[2]=n[2],t[3]=n[3],t[4]=n[4],t[5]=n[5],t[6]=n[6],t[7]=n[7],t[8]=n[8],this}extractBasis(e,t,n){return e.setFromMatrix3Column(this,0),t.setFromMatrix3Column(this,1),n.setFromMatrix3Column(this,2),this}setFromMatrix4(e){const t=e.elements;return this.set(t[0],t[4],t[8],t[1],t[5],t[9],t[2],t[6],t[10]),this}multiply(e){return this.multiplyMatrices(this,e)}premultiply(e){return this.multiplyMatrices(e,this)}multiplyMatrices(e,t){const n=e.elements,i=t.elements,s=this.elements,a=n[0],o=n[3],l=n[6],c=n[1],u=n[4],f=n[7],h=n[2],d=n[5],_=n[8],g=i[0],p=i[3],m=i[6],M=i[1],S=i[4],y=i[7],A=i[2],w=i[5],C=i[8];return s[0]=a*g+o*M+l*A,s[3]=a*p+o*S+l*w,s[6]=a*m+o*y+l*C,s[1]=c*g+u*M+f*A,s[4]=c*p+u*S+f*w,s[7]=c*m+u*y+f*C,s[2]=h*g+d*M+_*A,s[5]=h*p+d*S+_*w,s[8]=h*m+d*y+_*C,this}multiplyScalar(e){const t=this.elements;return t[0]*=e,t[3]*=e,t[6]*=e,t[1]*=e,t[4]*=e,t[7]*=e,t[2]*=e,t[5]*=e,t[8]*=e,this}determinant(){const e=this.elements,t=e[0],n=e[1],i=e[2],s=e[3],a=e[4],o=e[5],l=e[6],c=e[7],u=e[8];return t*a*u-t*o*c-n*s*u+n*o*l+i*s*c-i*a*l}invert(){const e=this.elements,t=e[0],n=e[1],i=e[2],s=e[3],a=e[4],o=e[5],l=e[6],c=e[7],u=e[8],f=u*a-o*c,h=o*l-u*s,d=c*s-a*l,_=t*f+n*h+i*d;if(_===0)return this.set(0,0,0,0,0,0,0,0,0);const g=1/_;return e[0]=f*g,e[1]=(i*c-u*n)*g,e[2]=(o*n-i*a)*g,e[3]=h*g,e[4]=(u*t-i*l)*g,e[5]=(i*s-o*t)*g,e[6]=d*g,e[7]=(n*l-c*t)*g,e[8]=(a*t-n*s)*g,this}transpose(){let e;const t=this.elements;return e=t[1],t[1]=t[3],t[3]=e,e=t[2],t[2]=t[6],t[6]=e,e=t[5],t[5]=t[7],t[7]=e,this}getNormalMatrix(e){return this.setFromMatrix4(e).invert().transpose()}transposeIntoArray(e){const t=this.elements;return e[0]=t[0],e[1]=t[3],e[2]=t[6],e[3]=t[1],e[4]=t[4],e[5]=t[7],e[6]=t[2],e[7]=t[5],e[8]=t[8],this}setUvTransform(e,t,n,i,s,a,o){const l=Math.cos(s),c=Math.sin(s);return this.set(n*l,n*c,-n*(l*a+c*o)+a+e,-i*c,i*l,-i*(-c*a+l*o)+o+t,0,0,1),this}scale(e,t){return this.premultiply(mo.makeScale(e,t)),this}rotate(e){return this.premultiply(mo.makeRotation(-e)),this}translate(e,t){return this.premultiply(mo.makeTranslation(e,t)),this}makeTranslation(e,t){return e.isVector2?this.set(1,0,e.x,0,1,e.y,0,0,1):this.set(1,0,e,0,1,t,0,0,1),this}makeRotation(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,-n,0,n,t,0,0,0,1),this}makeScale(e,t){return this.set(e,0,0,0,t,0,0,0,1),this}equals(e){const t=this.elements,n=e.elements;for(let i=0;i<9;i++)if(t[i]!==n[i])return!1;return!0}fromArray(e,t=0){for(let n=0;n<9;n++)this.elements[n]=e[n+t];return this}toArray(e=[],t=0){const n=this.elements;return e[t]=n[0],e[t+1]=n[1],e[t+2]=n[2],e[t+3]=n[3],e[t+4]=n[4],e[t+5]=n[5],e[t+6]=n[6],e[t+7]=n[7],e[t+8]=n[8],e}clone(){return new this.constructor().fromArray(this.elements)}}const mo=new Ke,su=new Ke().set(.4123908,.3575843,.1804808,.212639,.7151687,.0721923,.0193308,.1191948,.9505322),au=new Ke().set(3.2409699,-1.5373832,-.4986108,-.9692436,1.8759675,.0415551,.0556301,-.203977,1.0569715);function ap(){const r={enabled:!0,workingColorSpace:is,spaces:{},convert:function(i,s,a){return this.enabled===!1||s===a||!s||!a||(this.spaces[s].transfer===_t&&(i.r=Ei(i.r),i.g=Ei(i.g),i.b=Ei(i.b)),this.spaces[s].primaries!==this.spaces[a].primaries&&(i.applyMatrix3(this.spaces[s].toXYZ),i.applyMatrix3(this.spaces[a].fromXYZ)),this.spaces[a].transfer===_t&&(i.r=Zr(i.r),i.g=Zr(i.g),i.b=Zr(i.b))),i},workingToColorSpace:function(i,s){return this.convert(i,this.workingColorSpace,s)},colorSpaceToWorking:function(i,s){return this.convert(i,s,this.workingColorSpace)},getPrimaries:function(i){return this.spaces[i].primaries},getTransfer:function(i){return i===Gi?Va:this.spaces[i].transfer},getToneMappingMode:function(i){return this.spaces[i].outputColorSpaceConfig.toneMappingMode||"standard"},getLuminanceCoefficients:function(i,s=this.workingColorSpace){return i.fromArray(this.spaces[s].luminanceCoefficients)},define:function(i){Object.assign(this.spaces,i)},_getMatrix:function(i,s,a){return i.copy(this.spaces[s].toXYZ).multiply(this.spaces[a].fromXYZ)},_getDrawingBufferColorSpace:function(i){return this.spaces[i].outputColorSpaceConfig.drawingBufferColorSpace},_getUnpackColorSpace:function(i=this.workingColorSpace){return this.spaces[i].workingColorSpaceConfig.unpackColorSpace},fromWorkingColorSpace:function(i,s){return Ha("ColorManagement: .fromWorkingColorSpace() has been renamed to .workingToColorSpace()."),r.workingToColorSpace(i,s)},toWorkingColorSpace:function(i,s){return Ha("ColorManagement: .toWorkingColorSpace() has been renamed to .colorSpaceToWorking()."),r.colorSpaceToWorking(i,s)}},e=[.64,.33,.3,.6,.15,.06],t=[.2126,.7152,.0722],n=[.3127,.329];return r.define({[is]:{primaries:e,whitePoint:n,transfer:Va,toXYZ:su,fromXYZ:au,luminanceCoefficients:t,workingColorSpaceConfig:{unpackColorSpace:Tn},outputColorSpaceConfig:{drawingBufferColorSpace:Tn}},[Tn]:{primaries:e,whitePoint:n,transfer:_t,toXYZ:su,fromXYZ:au,luminanceCoefficients:t,outputColorSpaceConfig:{drawingBufferColorSpace:Tn}}}),r}const ht=ap();function Ei(r){return r<.04045?r*.0773993808:Math.pow(r*.9478672986+.0521327014,2.4)}function Zr(r){return r<.0031308?r*12.92:1.055*Math.pow(r,.41666)-.055}let Cr;class op{static getDataURL(e,t="image/png"){if(/^data:/i.test(e.src)||typeof HTMLCanvasElement>"u")return e.src;let n;if(e instanceof HTMLCanvasElement)n=e;else{Cr===void 0&&(Cr=Ga("canvas")),Cr.width=e.width,Cr.height=e.height;const i=Cr.getContext("2d");e instanceof ImageData?i.putImageData(e,0,0):i.drawImage(e,0,0,e.width,e.height),n=Cr}return n.toDataURL(t)}static sRGBToLinear(e){if(typeof HTMLImageElement<"u"&&e instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&e instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&e instanceof ImageBitmap){const t=Ga("canvas");t.width=e.width,t.height=e.height;const n=t.getContext("2d");n.drawImage(e,0,0,e.width,e.height);const i=n.getImageData(0,0,e.width,e.height),s=i.data;for(let a=0;a<s.length;a++)s[a]=Ei(s[a]/255)*255;return n.putImageData(i,0,0),t}else if(e.data){const t=e.data.slice(0);for(let n=0;n<t.length;n++)t instanceof Uint8Array||t instanceof Uint8ClampedArray?t[n]=Math.floor(Ei(t[n]/255)*255):t[n]=Ei(t[n]);return{data:t,width:e.width,height:e.height}}else return He("ImageUtils.sRGBToLinear(): Unsupported image type. No color space conversion applied."),e}}let lp=0;class yc{constructor(e=null){this.isSource=!0,Object.defineProperty(this,"id",{value:lp++}),this.uuid=qs(),this.data=e,this.dataReady=!0,this.version=0}getSize(e){const t=this.data;return typeof HTMLVideoElement<"u"&&t instanceof HTMLVideoElement?e.set(t.videoWidth,t.videoHeight,0):typeof VideoFrame<"u"&&t instanceof VideoFrame?e.set(t.displayHeight,t.displayWidth,0):t!==null?e.set(t.width,t.height,t.depth||0):e.set(0,0,0),e}set needsUpdate(e){e===!0&&this.version++}toJSON(e){const t=e===void 0||typeof e=="string";if(!t&&e.images[this.uuid]!==void 0)return e.images[this.uuid];const n={uuid:this.uuid,url:""},i=this.data;if(i!==null){let s;if(Array.isArray(i)){s=[];for(let a=0,o=i.length;a<o;a++)i[a].isDataTexture?s.push(_o(i[a].image)):s.push(_o(i[a]))}else s=_o(i);n.url=s}return t||(e.images[this.uuid]=n),n}}function _o(r){return typeof HTMLImageElement<"u"&&r instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&r instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&r instanceof ImageBitmap?op.getDataURL(r):r.data?{data:Array.from(r.data),width:r.width,height:r.height,type:r.data.constructor.name}:(He("Texture: Unable to serialize Texture."),{})}let cp=0;const go=new V;class on extends br{constructor(e=on.DEFAULT_IMAGE,t=on.DEFAULT_MAPPING,n=yi,i=yi,s=en,a=mr,o=qn,l=Rn,c=on.DEFAULT_ANISOTROPY,u=Gi){super(),this.isTexture=!0,Object.defineProperty(this,"id",{value:cp++}),this.uuid=qs(),this.name="",this.source=new yc(e),this.mipmaps=[],this.mapping=t,this.channel=0,this.wrapS=n,this.wrapT=i,this.magFilter=s,this.minFilter=a,this.anisotropy=c,this.format=o,this.internalFormat=null,this.type=l,this.offset=new qe(0,0),this.repeat=new qe(1,1),this.center=new qe(0,0),this.rotation=0,this.matrixAutoUpdate=!0,this.matrix=new Ke,this.generateMipmaps=!0,this.premultiplyAlpha=!1,this.flipY=!0,this.unpackAlignment=4,this.colorSpace=u,this.userData={},this.updateRanges=[],this.version=0,this.onUpdate=null,this.renderTarget=null,this.isRenderTargetTexture=!1,this.isArrayTexture=!!(e&&e.depth&&e.depth>1),this.pmremVersion=0}get width(){return this.source.getSize(go).x}get height(){return this.source.getSize(go).y}get depth(){return this.source.getSize(go).z}get image(){return this.source.data}set image(e=null){this.source.data=e}updateMatrix(){this.matrix.setUvTransform(this.offset.x,this.offset.y,this.repeat.x,this.repeat.y,this.rotation,this.center.x,this.center.y)}addUpdateRange(e,t){this.updateRanges.push({start:e,count:t})}clearUpdateRanges(){this.updateRanges.length=0}clone(){return new this.constructor().copy(this)}copy(e){return this.name=e.name,this.source=e.source,this.mipmaps=e.mipmaps.slice(0),this.mapping=e.mapping,this.channel=e.channel,this.wrapS=e.wrapS,this.wrapT=e.wrapT,this.magFilter=e.magFilter,this.minFilter=e.minFilter,this.anisotropy=e.anisotropy,this.format=e.format,this.internalFormat=e.internalFormat,this.type=e.type,this.offset.copy(e.offset),this.repeat.copy(e.repeat),this.center.copy(e.center),this.rotation=e.rotation,this.matrixAutoUpdate=e.matrixAutoUpdate,this.matrix.copy(e.matrix),this.generateMipmaps=e.generateMipmaps,this.premultiplyAlpha=e.premultiplyAlpha,this.flipY=e.flipY,this.unpackAlignment=e.unpackAlignment,this.colorSpace=e.colorSpace,this.renderTarget=e.renderTarget,this.isRenderTargetTexture=e.isRenderTargetTexture,this.isArrayTexture=e.isArrayTexture,this.userData=JSON.parse(JSON.stringify(e.userData)),this.needsUpdate=!0,this}setValues(e){for(const t in e){const n=e[t];if(n===void 0){He(`Texture.setValues(): parameter '${t}' has value of undefined.`);continue}const i=this[t];if(i===void 0){He(`Texture.setValues(): property '${t}' does not exist.`);continue}i&&n&&i.isVector2&&n.isVector2||i&&n&&i.isVector3&&n.isVector3||i&&n&&i.isMatrix3&&n.isMatrix3?i.copy(n):this[t]=n}}toJSON(e){const t=e===void 0||typeof e=="string";if(!t&&e.textures[this.uuid]!==void 0)return e.textures[this.uuid];const n={metadata:{version:4.7,type:"Texture",generator:"Texture.toJSON"},uuid:this.uuid,name:this.name,image:this.source.toJSON(e).uuid,mapping:this.mapping,channel:this.channel,repeat:[this.repeat.x,this.repeat.y],offset:[this.offset.x,this.offset.y],center:[this.center.x,this.center.y],rotation:this.rotation,wrap:[this.wrapS,this.wrapT],format:this.format,internalFormat:this.internalFormat,type:this.type,colorSpace:this.colorSpace,minFilter:this.minFilter,magFilter:this.magFilter,anisotropy:this.anisotropy,flipY:this.flipY,generateMipmaps:this.generateMipmaps,premultiplyAlpha:this.premultiplyAlpha,unpackAlignment:this.unpackAlignment};return Object.keys(this.userData).length>0&&(n.userData=this.userData),t||(e.textures[this.uuid]=n),n}dispose(){this.dispatchEvent({type:"dispose"})}transformUv(e){if(this.mapping!==Fh)return e;if(e.applyMatrix3(this.matrix),e.x<0||e.x>1)switch(this.wrapS){case ul:e.x=e.x-Math.floor(e.x);break;case yi:e.x=e.x<0?0:1;break;case hl:Math.abs(Math.floor(e.x)%2)===1?e.x=Math.ceil(e.x)-e.x:e.x=e.x-Math.floor(e.x);break}if(e.y<0||e.y>1)switch(this.wrapT){case ul:e.y=e.y-Math.floor(e.y);break;case yi:e.y=e.y<0?0:1;break;case hl:Math.abs(Math.floor(e.y)%2)===1?e.y=Math.ceil(e.y)-e.y:e.y=e.y-Math.floor(e.y);break}return this.flipY&&(e.y=1-e.y),e}set needsUpdate(e){e===!0&&(this.version++,this.source.needsUpdate=!0)}set needsPMREMUpdate(e){e===!0&&this.pmremVersion++}}on.DEFAULT_IMAGE=null;on.DEFAULT_MAPPING=Fh;on.DEFAULT_ANISOTROPY=1;class Pt{constructor(e=0,t=0,n=0,i=1){Pt.prototype.isVector4=!0,this.x=e,this.y=t,this.z=n,this.w=i}get width(){return this.z}set width(e){this.z=e}get height(){return this.w}set height(e){this.w=e}set(e,t,n,i){return this.x=e,this.y=t,this.z=n,this.w=i,this}setScalar(e){return this.x=e,this.y=e,this.z=e,this.w=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setZ(e){return this.z=e,this}setW(e){return this.w=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;case 2:this.z=t;break;case 3:this.w=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;case 2:return this.z;case 3:return this.w;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y,this.z,this.w)}copy(e){return this.x=e.x,this.y=e.y,this.z=e.z,this.w=e.w!==void 0?e.w:1,this}add(e){return this.x+=e.x,this.y+=e.y,this.z+=e.z,this.w+=e.w,this}addScalar(e){return this.x+=e,this.y+=e,this.z+=e,this.w+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this.z=e.z+t.z,this.w=e.w+t.w,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this.z+=e.z*t,this.w+=e.w*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this.z-=e.z,this.w-=e.w,this}subScalar(e){return this.x-=e,this.y-=e,this.z-=e,this.w-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this.z=e.z-t.z,this.w=e.w-t.w,this}multiply(e){return this.x*=e.x,this.y*=e.y,this.z*=e.z,this.w*=e.w,this}multiplyScalar(e){return this.x*=e,this.y*=e,this.z*=e,this.w*=e,this}applyMatrix4(e){const t=this.x,n=this.y,i=this.z,s=this.w,a=e.elements;return this.x=a[0]*t+a[4]*n+a[8]*i+a[12]*s,this.y=a[1]*t+a[5]*n+a[9]*i+a[13]*s,this.z=a[2]*t+a[6]*n+a[10]*i+a[14]*s,this.w=a[3]*t+a[7]*n+a[11]*i+a[15]*s,this}divide(e){return this.x/=e.x,this.y/=e.y,this.z/=e.z,this.w/=e.w,this}divideScalar(e){return this.multiplyScalar(1/e)}setAxisAngleFromQuaternion(e){this.w=2*Math.acos(e.w);const t=Math.sqrt(1-e.w*e.w);return t<1e-4?(this.x=1,this.y=0,this.z=0):(this.x=e.x/t,this.y=e.y/t,this.z=e.z/t),this}setAxisAngleFromRotationMatrix(e){let t,n,i,s;const l=e.elements,c=l[0],u=l[4],f=l[8],h=l[1],d=l[5],_=l[9],g=l[2],p=l[6],m=l[10];if(Math.abs(u-h)<.01&&Math.abs(f-g)<.01&&Math.abs(_-p)<.01){if(Math.abs(u+h)<.1&&Math.abs(f+g)<.1&&Math.abs(_+p)<.1&&Math.abs(c+d+m-3)<.1)return this.set(1,0,0,0),this;t=Math.PI;const S=(c+1)/2,y=(d+1)/2,A=(m+1)/2,w=(u+h)/4,C=(f+g)/4,x=(_+p)/4;return S>y&&S>A?S<.01?(n=0,i=.707106781,s=.707106781):(n=Math.sqrt(S),i=w/n,s=C/n):y>A?y<.01?(n=.707106781,i=0,s=.707106781):(i=Math.sqrt(y),n=w/i,s=x/i):A<.01?(n=.707106781,i=.707106781,s=0):(s=Math.sqrt(A),n=C/s,i=x/s),this.set(n,i,s,t),this}let M=Math.sqrt((p-_)*(p-_)+(f-g)*(f-g)+(h-u)*(h-u));return Math.abs(M)<.001&&(M=1),this.x=(p-_)/M,this.y=(f-g)/M,this.z=(h-u)/M,this.w=Math.acos((c+d+m-1)/2),this}setFromMatrixPosition(e){const t=e.elements;return this.x=t[12],this.y=t[13],this.z=t[14],this.w=t[15],this}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this.z=Math.min(this.z,e.z),this.w=Math.min(this.w,e.w),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this.z=Math.max(this.z,e.z),this.w=Math.max(this.w,e.w),this}clamp(e,t){return this.x=nt(this.x,e.x,t.x),this.y=nt(this.y,e.y,t.y),this.z=nt(this.z,e.z,t.z),this.w=nt(this.w,e.w,t.w),this}clampScalar(e,t){return this.x=nt(this.x,e,t),this.y=nt(this.y,e,t),this.z=nt(this.z,e,t),this.w=nt(this.w,e,t),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(nt(n,e,t))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this.w=Math.floor(this.w),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this.w=Math.ceil(this.w),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this.w=Math.round(this.w),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this.w=Math.trunc(this.w),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this.w=-this.w,this}dot(e){return this.x*e.x+this.y*e.y+this.z*e.z+this.w*e.w}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)+Math.abs(this.w)}normalize(){return this.divideScalar(this.length()||1)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this.z+=(e.z-this.z)*t,this.w+=(e.w-this.w)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this.z=e.z+(t.z-e.z)*n,this.w=e.w+(t.w-e.w)*n,this}equals(e){return e.x===this.x&&e.y===this.y&&e.z===this.z&&e.w===this.w}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this.z=e[t+2],this.w=e[t+3],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e[t+2]=this.z,e[t+3]=this.w,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this.z=e.getZ(t),this.w=e.getW(t),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this.w=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z,yield this.w}}class up extends br{constructor(e=1,t=1,n={}){super(),n=Object.assign({generateMipmaps:!1,internalFormat:null,minFilter:en,depthBuffer:!0,stencilBuffer:!1,resolveDepthBuffer:!0,resolveStencilBuffer:!0,depthTexture:null,samples:0,count:1,depth:1,multiview:!1},n),this.isRenderTarget=!0,this.width=e,this.height=t,this.depth=n.depth,this.scissor=new Pt(0,0,e,t),this.scissorTest=!1,this.viewport=new Pt(0,0,e,t),this.textures=[];const i={width:e,height:t,depth:n.depth},s=new on(i),a=n.count;for(let o=0;o<a;o++)this.textures[o]=s.clone(),this.textures[o].isRenderTargetTexture=!0,this.textures[o].renderTarget=this;this._setTextureOptions(n),this.depthBuffer=n.depthBuffer,this.stencilBuffer=n.stencilBuffer,this.resolveDepthBuffer=n.resolveDepthBuffer,this.resolveStencilBuffer=n.resolveStencilBuffer,this._depthTexture=null,this.depthTexture=n.depthTexture,this.samples=n.samples,this.multiview=n.multiview}_setTextureOptions(e={}){const t={minFilter:en,generateMipmaps:!1,flipY:!1,internalFormat:null};e.mapping!==void 0&&(t.mapping=e.mapping),e.wrapS!==void 0&&(t.wrapS=e.wrapS),e.wrapT!==void 0&&(t.wrapT=e.wrapT),e.wrapR!==void 0&&(t.wrapR=e.wrapR),e.magFilter!==void 0&&(t.magFilter=e.magFilter),e.minFilter!==void 0&&(t.minFilter=e.minFilter),e.format!==void 0&&(t.format=e.format),e.type!==void 0&&(t.type=e.type),e.anisotropy!==void 0&&(t.anisotropy=e.anisotropy),e.colorSpace!==void 0&&(t.colorSpace=e.colorSpace),e.flipY!==void 0&&(t.flipY=e.flipY),e.generateMipmaps!==void 0&&(t.generateMipmaps=e.generateMipmaps),e.internalFormat!==void 0&&(t.internalFormat=e.internalFormat);for(let n=0;n<this.textures.length;n++)this.textures[n].setValues(t)}get texture(){return this.textures[0]}set texture(e){this.textures[0]=e}set depthTexture(e){this._depthTexture!==null&&(this._depthTexture.renderTarget=null),e!==null&&(e.renderTarget=this),this._depthTexture=e}get depthTexture(){return this._depthTexture}setSize(e,t,n=1){if(this.width!==e||this.height!==t||this.depth!==n){this.width=e,this.height=t,this.depth=n;for(let i=0,s=this.textures.length;i<s;i++)this.textures[i].image.width=e,this.textures[i].image.height=t,this.textures[i].image.depth=n,this.textures[i].isData3DTexture!==!0&&(this.textures[i].isArrayTexture=this.textures[i].image.depth>1);this.dispose()}this.viewport.set(0,0,e,t),this.scissor.set(0,0,e,t)}clone(){return new this.constructor().copy(this)}copy(e){this.width=e.width,this.height=e.height,this.depth=e.depth,this.scissor.copy(e.scissor),this.scissorTest=e.scissorTest,this.viewport.copy(e.viewport),this.textures.length=0;for(let t=0,n=e.textures.length;t<n;t++){this.textures[t]=e.textures[t].clone(),this.textures[t].isRenderTargetTexture=!0,this.textures[t].renderTarget=this;const i=Object.assign({},e.textures[t].image);this.textures[t].source=new yc(i)}return this.depthBuffer=e.depthBuffer,this.stencilBuffer=e.stencilBuffer,this.resolveDepthBuffer=e.resolveDepthBuffer,this.resolveStencilBuffer=e.resolveStencilBuffer,e.depthTexture!==null&&(this.depthTexture=e.depthTexture.clone()),this.samples=e.samples,this}dispose(){this.dispatchEvent({type:"dispose"})}}class ai extends up{constructor(e=1,t=1,n={}){super(e,t,n),this.isWebGLRenderTarget=!0}}class qh extends on{constructor(e=null,t=1,n=1,i=1){super(null),this.isDataArrayTexture=!0,this.image={data:e,width:t,height:n,depth:i},this.magFilter=qt,this.minFilter=qt,this.wrapR=yi,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1,this.layerUpdates=new Set}addLayerUpdate(e){this.layerUpdates.add(e)}clearLayerUpdates(){this.layerUpdates.clear()}}class hp extends on{constructor(e=null,t=1,n=1,i=1){super(null),this.isData3DTexture=!0,this.image={data:e,width:t,height:n,depth:i},this.magFilter=qt,this.minFilter=qt,this.wrapR=yi,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}class St{constructor(e,t,n,i,s,a,o,l,c,u,f,h,d,_,g,p){St.prototype.isMatrix4=!0,this.elements=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],e!==void 0&&this.set(e,t,n,i,s,a,o,l,c,u,f,h,d,_,g,p)}set(e,t,n,i,s,a,o,l,c,u,f,h,d,_,g,p){const m=this.elements;return m[0]=e,m[4]=t,m[8]=n,m[12]=i,m[1]=s,m[5]=a,m[9]=o,m[13]=l,m[2]=c,m[6]=u,m[10]=f,m[14]=h,m[3]=d,m[7]=_,m[11]=g,m[15]=p,this}identity(){return this.set(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1),this}clone(){return new St().fromArray(this.elements)}copy(e){const t=this.elements,n=e.elements;return t[0]=n[0],t[1]=n[1],t[2]=n[2],t[3]=n[3],t[4]=n[4],t[5]=n[5],t[6]=n[6],t[7]=n[7],t[8]=n[8],t[9]=n[9],t[10]=n[10],t[11]=n[11],t[12]=n[12],t[13]=n[13],t[14]=n[14],t[15]=n[15],this}copyPosition(e){const t=this.elements,n=e.elements;return t[12]=n[12],t[13]=n[13],t[14]=n[14],this}setFromMatrix3(e){const t=e.elements;return this.set(t[0],t[3],t[6],0,t[1],t[4],t[7],0,t[2],t[5],t[8],0,0,0,0,1),this}extractBasis(e,t,n){return this.determinant()===0?(e.set(1,0,0),t.set(0,1,0),n.set(0,0,1),this):(e.setFromMatrixColumn(this,0),t.setFromMatrixColumn(this,1),n.setFromMatrixColumn(this,2),this)}makeBasis(e,t,n){return this.set(e.x,t.x,n.x,0,e.y,t.y,n.y,0,e.z,t.z,n.z,0,0,0,0,1),this}extractRotation(e){if(e.determinant()===0)return this.identity();const t=this.elements,n=e.elements,i=1/Pr.setFromMatrixColumn(e,0).length(),s=1/Pr.setFromMatrixColumn(e,1).length(),a=1/Pr.setFromMatrixColumn(e,2).length();return t[0]=n[0]*i,t[1]=n[1]*i,t[2]=n[2]*i,t[3]=0,t[4]=n[4]*s,t[5]=n[5]*s,t[6]=n[6]*s,t[7]=0,t[8]=n[8]*a,t[9]=n[9]*a,t[10]=n[10]*a,t[11]=0,t[12]=0,t[13]=0,t[14]=0,t[15]=1,this}makeRotationFromEuler(e){const t=this.elements,n=e.x,i=e.y,s=e.z,a=Math.cos(n),o=Math.sin(n),l=Math.cos(i),c=Math.sin(i),u=Math.cos(s),f=Math.sin(s);if(e.order==="XYZ"){const h=a*u,d=a*f,_=o*u,g=o*f;t[0]=l*u,t[4]=-l*f,t[8]=c,t[1]=d+_*c,t[5]=h-g*c,t[9]=-o*l,t[2]=g-h*c,t[6]=_+d*c,t[10]=a*l}else if(e.order==="YXZ"){const h=l*u,d=l*f,_=c*u,g=c*f;t[0]=h+g*o,t[4]=_*o-d,t[8]=a*c,t[1]=a*f,t[5]=a*u,t[9]=-o,t[2]=d*o-_,t[6]=g+h*o,t[10]=a*l}else if(e.order==="ZXY"){const h=l*u,d=l*f,_=c*u,g=c*f;t[0]=h-g*o,t[4]=-a*f,t[8]=_+d*o,t[1]=d+_*o,t[5]=a*u,t[9]=g-h*o,t[2]=-a*c,t[6]=o,t[10]=a*l}else if(e.order==="ZYX"){const h=a*u,d=a*f,_=o*u,g=o*f;t[0]=l*u,t[4]=_*c-d,t[8]=h*c+g,t[1]=l*f,t[5]=g*c+h,t[9]=d*c-_,t[2]=-c,t[6]=o*l,t[10]=a*l}else if(e.order==="YZX"){const h=a*l,d=a*c,_=o*l,g=o*c;t[0]=l*u,t[4]=g-h*f,t[8]=_*f+d,t[1]=f,t[5]=a*u,t[9]=-o*u,t[2]=-c*u,t[6]=d*f+_,t[10]=h-g*f}else if(e.order==="XZY"){const h=a*l,d=a*c,_=o*l,g=o*c;t[0]=l*u,t[4]=-f,t[8]=c*u,t[1]=h*f+g,t[5]=a*u,t[9]=d*f-_,t[2]=_*f-d,t[6]=o*u,t[10]=g*f+h}return t[3]=0,t[7]=0,t[11]=0,t[12]=0,t[13]=0,t[14]=0,t[15]=1,this}makeRotationFromQuaternion(e){return this.compose(fp,e,dp)}lookAt(e,t,n){const i=this.elements;return bn.subVectors(e,t),bn.lengthSq()===0&&(bn.z=1),bn.normalize(),Li.crossVectors(n,bn),Li.lengthSq()===0&&(Math.abs(n.z)===1?bn.x+=1e-4:bn.z+=1e-4,bn.normalize(),Li.crossVectors(n,bn)),Li.normalize(),ta.crossVectors(bn,Li),i[0]=Li.x,i[4]=ta.x,i[8]=bn.x,i[1]=Li.y,i[5]=ta.y,i[9]=bn.y,i[2]=Li.z,i[6]=ta.z,i[10]=bn.z,this}multiply(e){return this.multiplyMatrices(this,e)}premultiply(e){return this.multiplyMatrices(e,this)}multiplyMatrices(e,t){const n=e.elements,i=t.elements,s=this.elements,a=n[0],o=n[4],l=n[8],c=n[12],u=n[1],f=n[5],h=n[9],d=n[13],_=n[2],g=n[6],p=n[10],m=n[14],M=n[3],S=n[7],y=n[11],A=n[15],w=i[0],C=i[4],x=i[8],b=i[12],W=i[1],L=i[5],G=i[9],z=i[13],q=i[2],H=i[6],N=i[10],F=i[14],ne=i[3],ie=i[7],me=i[11],Te=i[15];return s[0]=a*w+o*W+l*q+c*ne,s[4]=a*C+o*L+l*H+c*ie,s[8]=a*x+o*G+l*N+c*me,s[12]=a*b+o*z+l*F+c*Te,s[1]=u*w+f*W+h*q+d*ne,s[5]=u*C+f*L+h*H+d*ie,s[9]=u*x+f*G+h*N+d*me,s[13]=u*b+f*z+h*F+d*Te,s[2]=_*w+g*W+p*q+m*ne,s[6]=_*C+g*L+p*H+m*ie,s[10]=_*x+g*G+p*N+m*me,s[14]=_*b+g*z+p*F+m*Te,s[3]=M*w+S*W+y*q+A*ne,s[7]=M*C+S*L+y*H+A*ie,s[11]=M*x+S*G+y*N+A*me,s[15]=M*b+S*z+y*F+A*Te,this}multiplyScalar(e){const t=this.elements;return t[0]*=e,t[4]*=e,t[8]*=e,t[12]*=e,t[1]*=e,t[5]*=e,t[9]*=e,t[13]*=e,t[2]*=e,t[6]*=e,t[10]*=e,t[14]*=e,t[3]*=e,t[7]*=e,t[11]*=e,t[15]*=e,this}determinant(){const e=this.elements,t=e[0],n=e[4],i=e[8],s=e[12],a=e[1],o=e[5],l=e[9],c=e[13],u=e[2],f=e[6],h=e[10],d=e[14],_=e[3],g=e[7],p=e[11],m=e[15],M=l*d-c*h,S=o*d-c*f,y=o*h-l*f,A=a*d-c*u,w=a*h-l*u,C=a*f-o*u;return t*(g*M-p*S+m*y)-n*(_*M-p*A+m*w)+i*(_*S-g*A+m*C)-s*(_*y-g*w+p*C)}transpose(){const e=this.elements;let t;return t=e[1],e[1]=e[4],e[4]=t,t=e[2],e[2]=e[8],e[8]=t,t=e[6],e[6]=e[9],e[9]=t,t=e[3],e[3]=e[12],e[12]=t,t=e[7],e[7]=e[13],e[13]=t,t=e[11],e[11]=e[14],e[14]=t,this}setPosition(e,t,n){const i=this.elements;return e.isVector3?(i[12]=e.x,i[13]=e.y,i[14]=e.z):(i[12]=e,i[13]=t,i[14]=n),this}invert(){const e=this.elements,t=e[0],n=e[1],i=e[2],s=e[3],a=e[4],o=e[5],l=e[6],c=e[7],u=e[8],f=e[9],h=e[10],d=e[11],_=e[12],g=e[13],p=e[14],m=e[15],M=t*o-n*a,S=t*l-i*a,y=t*c-s*a,A=n*l-i*o,w=n*c-s*o,C=i*c-s*l,x=u*g-f*_,b=u*p-h*_,W=u*m-d*_,L=f*p-h*g,G=f*m-d*g,z=h*m-d*p,q=M*z-S*G+y*L+A*W-w*b+C*x;if(q===0)return this.set(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);const H=1/q;return e[0]=(o*z-l*G+c*L)*H,e[1]=(i*G-n*z-s*L)*H,e[2]=(g*C-p*w+m*A)*H,e[3]=(h*w-f*C-d*A)*H,e[4]=(l*W-a*z-c*b)*H,e[5]=(t*z-i*W+s*b)*H,e[6]=(p*y-_*C-m*S)*H,e[7]=(u*C-h*y+d*S)*H,e[8]=(a*G-o*W+c*x)*H,e[9]=(n*W-t*G-s*x)*H,e[10]=(_*w-g*y+m*M)*H,e[11]=(f*y-u*w-d*M)*H,e[12]=(o*b-a*L-l*x)*H,e[13]=(t*L-n*b+i*x)*H,e[14]=(g*S-_*A-p*M)*H,e[15]=(u*A-f*S+h*M)*H,this}scale(e){const t=this.elements,n=e.x,i=e.y,s=e.z;return t[0]*=n,t[4]*=i,t[8]*=s,t[1]*=n,t[5]*=i,t[9]*=s,t[2]*=n,t[6]*=i,t[10]*=s,t[3]*=n,t[7]*=i,t[11]*=s,this}getMaxScaleOnAxis(){const e=this.elements,t=e[0]*e[0]+e[1]*e[1]+e[2]*e[2],n=e[4]*e[4]+e[5]*e[5]+e[6]*e[6],i=e[8]*e[8]+e[9]*e[9]+e[10]*e[10];return Math.sqrt(Math.max(t,n,i))}makeTranslation(e,t,n){return e.isVector3?this.set(1,0,0,e.x,0,1,0,e.y,0,0,1,e.z,0,0,0,1):this.set(1,0,0,e,0,1,0,t,0,0,1,n,0,0,0,1),this}makeRotationX(e){const t=Math.cos(e),n=Math.sin(e);return this.set(1,0,0,0,0,t,-n,0,0,n,t,0,0,0,0,1),this}makeRotationY(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,0,n,0,0,1,0,0,-n,0,t,0,0,0,0,1),this}makeRotationZ(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,-n,0,0,n,t,0,0,0,0,1,0,0,0,0,1),this}makeRotationAxis(e,t){const n=Math.cos(t),i=Math.sin(t),s=1-n,a=e.x,o=e.y,l=e.z,c=s*a,u=s*o;return this.set(c*a+n,c*o-i*l,c*l+i*o,0,c*o+i*l,u*o+n,u*l-i*a,0,c*l-i*o,u*l+i*a,s*l*l+n,0,0,0,0,1),this}makeScale(e,t,n){return this.set(e,0,0,0,0,t,0,0,0,0,n,0,0,0,0,1),this}makeShear(e,t,n,i,s,a){return this.set(1,n,s,0,e,1,a,0,t,i,1,0,0,0,0,1),this}compose(e,t,n){const i=this.elements,s=t._x,a=t._y,o=t._z,l=t._w,c=s+s,u=a+a,f=o+o,h=s*c,d=s*u,_=s*f,g=a*u,p=a*f,m=o*f,M=l*c,S=l*u,y=l*f,A=n.x,w=n.y,C=n.z;return i[0]=(1-(g+m))*A,i[1]=(d+y)*A,i[2]=(_-S)*A,i[3]=0,i[4]=(d-y)*w,i[5]=(1-(h+m))*w,i[6]=(p+M)*w,i[7]=0,i[8]=(_+S)*C,i[9]=(p-M)*C,i[10]=(1-(h+g))*C,i[11]=0,i[12]=e.x,i[13]=e.y,i[14]=e.z,i[15]=1,this}decompose(e,t,n){const i=this.elements;e.x=i[12],e.y=i[13],e.z=i[14];const s=this.determinant();if(s===0)return n.set(1,1,1),t.identity(),this;let a=Pr.set(i[0],i[1],i[2]).length();const o=Pr.set(i[4],i[5],i[6]).length(),l=Pr.set(i[8],i[9],i[10]).length();s<0&&(a=-a),Gn.copy(this);const c=1/a,u=1/o,f=1/l;return Gn.elements[0]*=c,Gn.elements[1]*=c,Gn.elements[2]*=c,Gn.elements[4]*=u,Gn.elements[5]*=u,Gn.elements[6]*=u,Gn.elements[8]*=f,Gn.elements[9]*=f,Gn.elements[10]*=f,t.setFromRotationMatrix(Gn),n.x=a,n.y=o,n.z=l,this}makePerspective(e,t,n,i,s,a,o=ii,l=!1){const c=this.elements,u=2*s/(t-e),f=2*s/(n-i),h=(t+e)/(t-e),d=(n+i)/(n-i);let _,g;if(l)_=s/(a-s),g=a*s/(a-s);else if(o===ii)_=-(a+s)/(a-s),g=-2*a*s/(a-s);else if(o===Os)_=-a/(a-s),g=-a*s/(a-s);else throw new Error("THREE.Matrix4.makePerspective(): Invalid coordinate system: "+o);return c[0]=u,c[4]=0,c[8]=h,c[12]=0,c[1]=0,c[5]=f,c[9]=d,c[13]=0,c[2]=0,c[6]=0,c[10]=_,c[14]=g,c[3]=0,c[7]=0,c[11]=-1,c[15]=0,this}makeOrthographic(e,t,n,i,s,a,o=ii,l=!1){const c=this.elements,u=2/(t-e),f=2/(n-i),h=-(t+e)/(t-e),d=-(n+i)/(n-i);let _,g;if(l)_=1/(a-s),g=a/(a-s);else if(o===ii)_=-2/(a-s),g=-(a+s)/(a-s);else if(o===Os)_=-1/(a-s),g=-s/(a-s);else throw new Error("THREE.Matrix4.makeOrthographic(): Invalid coordinate system: "+o);return c[0]=u,c[4]=0,c[8]=0,c[12]=h,c[1]=0,c[5]=f,c[9]=0,c[13]=d,c[2]=0,c[6]=0,c[10]=_,c[14]=g,c[3]=0,c[7]=0,c[11]=0,c[15]=1,this}equals(e){const t=this.elements,n=e.elements;for(let i=0;i<16;i++)if(t[i]!==n[i])return!1;return!0}fromArray(e,t=0){for(let n=0;n<16;n++)this.elements[n]=e[n+t];return this}toArray(e=[],t=0){const n=this.elements;return e[t]=n[0],e[t+1]=n[1],e[t+2]=n[2],e[t+3]=n[3],e[t+4]=n[4],e[t+5]=n[5],e[t+6]=n[6],e[t+7]=n[7],e[t+8]=n[8],e[t+9]=n[9],e[t+10]=n[10],e[t+11]=n[11],e[t+12]=n[12],e[t+13]=n[13],e[t+14]=n[14],e[t+15]=n[15],e}}const Pr=new V,Gn=new St,fp=new V(0,0,0),dp=new V(1,1,1),Li=new V,ta=new V,bn=new V,ou=new St,lu=new ji;class li{constructor(e=0,t=0,n=0,i=li.DEFAULT_ORDER){this.isEuler=!0,this._x=e,this._y=t,this._z=n,this._order=i}get x(){return this._x}set x(e){this._x=e,this._onChangeCallback()}get y(){return this._y}set y(e){this._y=e,this._onChangeCallback()}get z(){return this._z}set z(e){this._z=e,this._onChangeCallback()}get order(){return this._order}set order(e){this._order=e,this._onChangeCallback()}set(e,t,n,i=this._order){return this._x=e,this._y=t,this._z=n,this._order=i,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._order)}copy(e){return this._x=e._x,this._y=e._y,this._z=e._z,this._order=e._order,this._onChangeCallback(),this}setFromRotationMatrix(e,t=this._order,n=!0){const i=e.elements,s=i[0],a=i[4],o=i[8],l=i[1],c=i[5],u=i[9],f=i[2],h=i[6],d=i[10];switch(t){case"XYZ":this._y=Math.asin(nt(o,-1,1)),Math.abs(o)<.9999999?(this._x=Math.atan2(-u,d),this._z=Math.atan2(-a,s)):(this._x=Math.atan2(h,c),this._z=0);break;case"YXZ":this._x=Math.asin(-nt(u,-1,1)),Math.abs(u)<.9999999?(this._y=Math.atan2(o,d),this._z=Math.atan2(l,c)):(this._y=Math.atan2(-f,s),this._z=0);break;case"ZXY":this._x=Math.asin(nt(h,-1,1)),Math.abs(h)<.9999999?(this._y=Math.atan2(-f,d),this._z=Math.atan2(-a,c)):(this._y=0,this._z=Math.atan2(l,s));break;case"ZYX":this._y=Math.asin(-nt(f,-1,1)),Math.abs(f)<.9999999?(this._x=Math.atan2(h,d),this._z=Math.atan2(l,s)):(this._x=0,this._z=Math.atan2(-a,c));break;case"YZX":this._z=Math.asin(nt(l,-1,1)),Math.abs(l)<.9999999?(this._x=Math.atan2(-u,c),this._y=Math.atan2(-f,s)):(this._x=0,this._y=Math.atan2(o,d));break;case"XZY":this._z=Math.asin(-nt(a,-1,1)),Math.abs(a)<.9999999?(this._x=Math.atan2(h,c),this._y=Math.atan2(o,s)):(this._x=Math.atan2(-u,d),this._y=0);break;default:He("Euler: .setFromRotationMatrix() encountered an unknown order: "+t)}return this._order=t,n===!0&&this._onChangeCallback(),this}setFromQuaternion(e,t,n){return ou.makeRotationFromQuaternion(e),this.setFromRotationMatrix(ou,t,n)}setFromVector3(e,t=this._order){return this.set(e.x,e.y,e.z,t)}reorder(e){return lu.setFromEuler(this),this.setFromQuaternion(lu,e)}equals(e){return e._x===this._x&&e._y===this._y&&e._z===this._z&&e._order===this._order}fromArray(e){return this._x=e[0],this._y=e[1],this._z=e[2],e[3]!==void 0&&(this._order=e[3]),this._onChangeCallback(),this}toArray(e=[],t=0){return e[t]=this._x,e[t+1]=this._y,e[t+2]=this._z,e[t+3]=this._order,e}_onChange(e){return this._onChangeCallback=e,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._order}}li.DEFAULT_ORDER="XYZ";class bc{constructor(){this.mask=1}set(e){this.mask=(1<<e|0)>>>0}enable(e){this.mask|=1<<e|0}enableAll(){this.mask=-1}toggle(e){this.mask^=1<<e|0}disable(e){this.mask&=~(1<<e|0)}disableAll(){this.mask=0}test(e){return(this.mask&e.mask)!==0}isEnabled(e){return(this.mask&(1<<e|0))!==0}}let pp=0;const cu=new V,Dr=new ji,pi=new St,na=new V,vs=new V,mp=new V,_p=new ji,uu=new V(1,0,0),hu=new V(0,1,0),fu=new V(0,0,1),du={type:"added"},gp={type:"removed"},Lr={type:"childadded",child:null},vo={type:"childremoved",child:null};class kt extends br{constructor(){super(),this.isObject3D=!0,Object.defineProperty(this,"id",{value:pp++}),this.uuid=qs(),this.name="",this.type="Object3D",this.parent=null,this.children=[],this.up=kt.DEFAULT_UP.clone();const e=new V,t=new li,n=new ji,i=new V(1,1,1);function s(){n.setFromEuler(t,!1)}function a(){t.setFromQuaternion(n,void 0,!1)}t._onChange(s),n._onChange(a),Object.defineProperties(this,{position:{configurable:!0,enumerable:!0,value:e},rotation:{configurable:!0,enumerable:!0,value:t},quaternion:{configurable:!0,enumerable:!0,value:n},scale:{configurable:!0,enumerable:!0,value:i},modelViewMatrix:{value:new St},normalMatrix:{value:new Ke}}),this.matrix=new St,this.matrixWorld=new St,this.matrixAutoUpdate=kt.DEFAULT_MATRIX_AUTO_UPDATE,this.matrixWorldAutoUpdate=kt.DEFAULT_MATRIX_WORLD_AUTO_UPDATE,this.matrixWorldNeedsUpdate=!1,this.layers=new bc,this.visible=!0,this.castShadow=!1,this.receiveShadow=!1,this.frustumCulled=!0,this.renderOrder=0,this.animations=[],this.customDepthMaterial=void 0,this.customDistanceMaterial=void 0,this.static=!1,this.userData={},this.pivot=null}onBeforeShadow(){}onAfterShadow(){}onBeforeRender(){}onAfterRender(){}applyMatrix4(e){this.matrixAutoUpdate&&this.updateMatrix(),this.matrix.premultiply(e),this.matrix.decompose(this.position,this.quaternion,this.scale)}applyQuaternion(e){return this.quaternion.premultiply(e),this}setRotationFromAxisAngle(e,t){this.quaternion.setFromAxisAngle(e,t)}setRotationFromEuler(e){this.quaternion.setFromEuler(e,!0)}setRotationFromMatrix(e){this.quaternion.setFromRotationMatrix(e)}setRotationFromQuaternion(e){this.quaternion.copy(e)}rotateOnAxis(e,t){return Dr.setFromAxisAngle(e,t),this.quaternion.multiply(Dr),this}rotateOnWorldAxis(e,t){return Dr.setFromAxisAngle(e,t),this.quaternion.premultiply(Dr),this}rotateX(e){return this.rotateOnAxis(uu,e)}rotateY(e){return this.rotateOnAxis(hu,e)}rotateZ(e){return this.rotateOnAxis(fu,e)}translateOnAxis(e,t){return cu.copy(e).applyQuaternion(this.quaternion),this.position.add(cu.multiplyScalar(t)),this}translateX(e){return this.translateOnAxis(uu,e)}translateY(e){return this.translateOnAxis(hu,e)}translateZ(e){return this.translateOnAxis(fu,e)}localToWorld(e){return this.updateWorldMatrix(!0,!1),e.applyMatrix4(this.matrixWorld)}worldToLocal(e){return this.updateWorldMatrix(!0,!1),e.applyMatrix4(pi.copy(this.matrixWorld).invert())}lookAt(e,t,n){e.isVector3?na.copy(e):na.set(e,t,n);const i=this.parent;this.updateWorldMatrix(!0,!1),vs.setFromMatrixPosition(this.matrixWorld),this.isCamera||this.isLight?pi.lookAt(vs,na,this.up):pi.lookAt(na,vs,this.up),this.quaternion.setFromRotationMatrix(pi),i&&(pi.extractRotation(i.matrixWorld),Dr.setFromRotationMatrix(pi),this.quaternion.premultiply(Dr.invert()))}add(e){if(arguments.length>1){for(let t=0;t<arguments.length;t++)this.add(arguments[t]);return this}return e===this?(ut("Object3D.add: object can't be added as a child of itself.",e),this):(e&&e.isObject3D?(e.removeFromParent(),e.parent=this,this.children.push(e),e.dispatchEvent(du),Lr.child=e,this.dispatchEvent(Lr),Lr.child=null):ut("Object3D.add: object not an instance of THREE.Object3D.",e),this)}remove(e){if(arguments.length>1){for(let n=0;n<arguments.length;n++)this.remove(arguments[n]);return this}const t=this.children.indexOf(e);return t!==-1&&(e.parent=null,this.children.splice(t,1),e.dispatchEvent(gp),vo.child=e,this.dispatchEvent(vo),vo.child=null),this}removeFromParent(){const e=this.parent;return e!==null&&e.remove(this),this}clear(){return this.remove(...this.children)}attach(e){return this.updateWorldMatrix(!0,!1),pi.copy(this.matrixWorld).invert(),e.parent!==null&&(e.parent.updateWorldMatrix(!0,!1),pi.multiply(e.parent.matrixWorld)),e.applyMatrix4(pi),e.removeFromParent(),e.parent=this,this.children.push(e),e.updateWorldMatrix(!1,!0),e.dispatchEvent(du),Lr.child=e,this.dispatchEvent(Lr),Lr.child=null,this}getObjectById(e){return this.getObjectByProperty("id",e)}getObjectByName(e){return this.getObjectByProperty("name",e)}getObjectByProperty(e,t){if(this[e]===t)return this;for(let n=0,i=this.children.length;n<i;n++){const a=this.children[n].getObjectByProperty(e,t);if(a!==void 0)return a}}getObjectsByProperty(e,t,n=[]){this[e]===t&&n.push(this);const i=this.children;for(let s=0,a=i.length;s<a;s++)i[s].getObjectsByProperty(e,t,n);return n}getWorldPosition(e){return this.updateWorldMatrix(!0,!1),e.setFromMatrixPosition(this.matrixWorld)}getWorldQuaternion(e){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(vs,e,mp),e}getWorldScale(e){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(vs,_p,e),e}getWorldDirection(e){this.updateWorldMatrix(!0,!1);const t=this.matrixWorld.elements;return e.set(t[8],t[9],t[10]).normalize()}raycast(){}traverse(e){e(this);const t=this.children;for(let n=0,i=t.length;n<i;n++)t[n].traverse(e)}traverseVisible(e){if(this.visible===!1)return;e(this);const t=this.children;for(let n=0,i=t.length;n<i;n++)t[n].traverseVisible(e)}traverseAncestors(e){const t=this.parent;t!==null&&(e(t),t.traverseAncestors(e))}updateMatrix(){this.matrix.compose(this.position,this.quaternion,this.scale);const e=this.pivot;if(e!==null){const t=e.x,n=e.y,i=e.z,s=this.matrix.elements;s[12]+=t-s[0]*t-s[4]*n-s[8]*i,s[13]+=n-s[1]*t-s[5]*n-s[9]*i,s[14]+=i-s[2]*t-s[6]*n-s[10]*i}this.matrixWorldNeedsUpdate=!0}updateMatrixWorld(e){this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||e)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,e=!0);const t=this.children;for(let n=0,i=t.length;n<i;n++)t[n].updateMatrixWorld(e)}updateWorldMatrix(e,t){const n=this.parent;if(e===!0&&n!==null&&n.updateWorldMatrix(!0,!1),this.matrixAutoUpdate&&this.updateMatrix(),this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),t===!0){const i=this.children;for(let s=0,a=i.length;s<a;s++)i[s].updateWorldMatrix(!1,!0)}}toJSON(e){const t=e===void 0||typeof e=="string",n={};t&&(e={geometries:{},materials:{},textures:{},images:{},shapes:{},skeletons:{},animations:{},nodes:{}},n.metadata={version:4.7,type:"Object",generator:"Object3D.toJSON"});const i={};i.uuid=this.uuid,i.type=this.type,this.name!==""&&(i.name=this.name),this.castShadow===!0&&(i.castShadow=!0),this.receiveShadow===!0&&(i.receiveShadow=!0),this.visible===!1&&(i.visible=!1),this.frustumCulled===!1&&(i.frustumCulled=!1),this.renderOrder!==0&&(i.renderOrder=this.renderOrder),this.static!==!1&&(i.static=this.static),Object.keys(this.userData).length>0&&(i.userData=this.userData),i.layers=this.layers.mask,i.matrix=this.matrix.toArray(),i.up=this.up.toArray(),this.pivot!==null&&(i.pivot=this.pivot.toArray()),this.matrixAutoUpdate===!1&&(i.matrixAutoUpdate=!1),this.morphTargetDictionary!==void 0&&(i.morphTargetDictionary=Object.assign({},this.morphTargetDictionary)),this.morphTargetInfluences!==void 0&&(i.morphTargetInfluences=this.morphTargetInfluences.slice()),this.isInstancedMesh&&(i.type="InstancedMesh",i.count=this.count,i.instanceMatrix=this.instanceMatrix.toJSON(),this.instanceColor!==null&&(i.instanceColor=this.instanceColor.toJSON())),this.isBatchedMesh&&(i.type="BatchedMesh",i.perObjectFrustumCulled=this.perObjectFrustumCulled,i.sortObjects=this.sortObjects,i.drawRanges=this._drawRanges,i.reservedRanges=this._reservedRanges,i.geometryInfo=this._geometryInfo.map(o=>({...o,boundingBox:o.boundingBox?o.boundingBox.toJSON():void 0,boundingSphere:o.boundingSphere?o.boundingSphere.toJSON():void 0})),i.instanceInfo=this._instanceInfo.map(o=>({...o})),i.availableInstanceIds=this._availableInstanceIds.slice(),i.availableGeometryIds=this._availableGeometryIds.slice(),i.nextIndexStart=this._nextIndexStart,i.nextVertexStart=this._nextVertexStart,i.geometryCount=this._geometryCount,i.maxInstanceCount=this._maxInstanceCount,i.maxVertexCount=this._maxVertexCount,i.maxIndexCount=this._maxIndexCount,i.geometryInitialized=this._geometryInitialized,i.matricesTexture=this._matricesTexture.toJSON(e),i.indirectTexture=this._indirectTexture.toJSON(e),this._colorsTexture!==null&&(i.colorsTexture=this._colorsTexture.toJSON(e)),this.boundingSphere!==null&&(i.boundingSphere=this.boundingSphere.toJSON()),this.boundingBox!==null&&(i.boundingBox=this.boundingBox.toJSON()));function s(o,l){return o[l.uuid]===void 0&&(o[l.uuid]=l.toJSON(e)),l.uuid}if(this.isScene)this.background&&(this.background.isColor?i.background=this.background.toJSON():this.background.isTexture&&(i.background=this.background.toJSON(e).uuid)),this.environment&&this.environment.isTexture&&this.environment.isRenderTargetTexture!==!0&&(i.environment=this.environment.toJSON(e).uuid);else if(this.isMesh||this.isLine||this.isPoints){i.geometry=s(e.geometries,this.geometry);const o=this.geometry.parameters;if(o!==void 0&&o.shapes!==void 0){const l=o.shapes;if(Array.isArray(l))for(let c=0,u=l.length;c<u;c++){const f=l[c];s(e.shapes,f)}else s(e.shapes,l)}}if(this.isSkinnedMesh&&(i.bindMode=this.bindMode,i.bindMatrix=this.bindMatrix.toArray(),this.skeleton!==void 0&&(s(e.skeletons,this.skeleton),i.skeleton=this.skeleton.uuid)),this.material!==void 0)if(Array.isArray(this.material)){const o=[];for(let l=0,c=this.material.length;l<c;l++)o.push(s(e.materials,this.material[l]));i.material=o}else i.material=s(e.materials,this.material);if(this.children.length>0){i.children=[];for(let o=0;o<this.children.length;o++)i.children.push(this.children[o].toJSON(e).object)}if(this.animations.length>0){i.animations=[];for(let o=0;o<this.animations.length;o++){const l=this.animations[o];i.animations.push(s(e.animations,l))}}if(t){const o=a(e.geometries),l=a(e.materials),c=a(e.textures),u=a(e.images),f=a(e.shapes),h=a(e.skeletons),d=a(e.animations),_=a(e.nodes);o.length>0&&(n.geometries=o),l.length>0&&(n.materials=l),c.length>0&&(n.textures=c),u.length>0&&(n.images=u),f.length>0&&(n.shapes=f),h.length>0&&(n.skeletons=h),d.length>0&&(n.animations=d),_.length>0&&(n.nodes=_)}return n.object=i,n;function a(o){const l=[];for(const c in o){const u=o[c];delete u.metadata,l.push(u)}return l}}clone(e){return new this.constructor().copy(this,e)}copy(e,t=!0){if(this.name=e.name,this.up.copy(e.up),this.position.copy(e.position),this.rotation.order=e.rotation.order,this.quaternion.copy(e.quaternion),this.scale.copy(e.scale),e.pivot!==null&&(this.pivot=e.pivot.clone()),this.matrix.copy(e.matrix),this.matrixWorld.copy(e.matrixWorld),this.matrixAutoUpdate=e.matrixAutoUpdate,this.matrixWorldAutoUpdate=e.matrixWorldAutoUpdate,this.matrixWorldNeedsUpdate=e.matrixWorldNeedsUpdate,this.layers.mask=e.layers.mask,this.visible=e.visible,this.castShadow=e.castShadow,this.receiveShadow=e.receiveShadow,this.frustumCulled=e.frustumCulled,this.renderOrder=e.renderOrder,this.static=e.static,this.animations=e.animations.slice(),this.userData=JSON.parse(JSON.stringify(e.userData)),t===!0)for(let n=0;n<e.children.length;n++){const i=e.children[n];this.add(i.clone())}return this}}kt.DEFAULT_UP=new V(0,1,0);kt.DEFAULT_MATRIX_AUTO_UPDATE=!0;kt.DEFAULT_MATRIX_WORLD_AUTO_UPDATE=!0;class Si extends kt{constructor(){super(),this.isGroup=!0,this.type="Group"}}const vp={type:"move"};class xo{constructor(){this._targetRay=null,this._grip=null,this._hand=null}getHandSpace(){return this._hand===null&&(this._hand=new Si,this._hand.matrixAutoUpdate=!1,this._hand.visible=!1,this._hand.joints={},this._hand.inputState={pinching:!1}),this._hand}getTargetRaySpace(){return this._targetRay===null&&(this._targetRay=new Si,this._targetRay.matrixAutoUpdate=!1,this._targetRay.visible=!1,this._targetRay.hasLinearVelocity=!1,this._targetRay.linearVelocity=new V,this._targetRay.hasAngularVelocity=!1,this._targetRay.angularVelocity=new V),this._targetRay}getGripSpace(){return this._grip===null&&(this._grip=new Si,this._grip.matrixAutoUpdate=!1,this._grip.visible=!1,this._grip.hasLinearVelocity=!1,this._grip.linearVelocity=new V,this._grip.hasAngularVelocity=!1,this._grip.angularVelocity=new V),this._grip}dispatchEvent(e){return this._targetRay!==null&&this._targetRay.dispatchEvent(e),this._grip!==null&&this._grip.dispatchEvent(e),this._hand!==null&&this._hand.dispatchEvent(e),this}connect(e){if(e&&e.hand){const t=this._hand;if(t)for(const n of e.hand.values())this._getHandJoint(t,n)}return this.dispatchEvent({type:"connected",data:e}),this}disconnect(e){return this.dispatchEvent({type:"disconnected",data:e}),this._targetRay!==null&&(this._targetRay.visible=!1),this._grip!==null&&(this._grip.visible=!1),this._hand!==null&&(this._hand.visible=!1),this}update(e,t,n){let i=null,s=null,a=null;const o=this._targetRay,l=this._grip,c=this._hand;if(e&&t.session.visibilityState!=="visible-blurred"){if(c&&e.hand){a=!0;for(const g of e.hand.values()){const p=t.getJointPose(g,n),m=this._getHandJoint(c,g);p!==null&&(m.matrix.fromArray(p.transform.matrix),m.matrix.decompose(m.position,m.rotation,m.scale),m.matrixWorldNeedsUpdate=!0,m.jointRadius=p.radius),m.visible=p!==null}const u=c.joints["index-finger-tip"],f=c.joints["thumb-tip"],h=u.position.distanceTo(f.position),d=.02,_=.005;c.inputState.pinching&&h>d+_?(c.inputState.pinching=!1,this.dispatchEvent({type:"pinchend",handedness:e.handedness,target:this})):!c.inputState.pinching&&h<=d-_&&(c.inputState.pinching=!0,this.dispatchEvent({type:"pinchstart",handedness:e.handedness,target:this}))}else l!==null&&e.gripSpace&&(s=t.getPose(e.gripSpace,n),s!==null&&(l.matrix.fromArray(s.transform.matrix),l.matrix.decompose(l.position,l.rotation,l.scale),l.matrixWorldNeedsUpdate=!0,s.linearVelocity?(l.hasLinearVelocity=!0,l.linearVelocity.copy(s.linearVelocity)):l.hasLinearVelocity=!1,s.angularVelocity?(l.hasAngularVelocity=!0,l.angularVelocity.copy(s.angularVelocity)):l.hasAngularVelocity=!1));o!==null&&(i=t.getPose(e.targetRaySpace,n),i===null&&s!==null&&(i=s),i!==null&&(o.matrix.fromArray(i.transform.matrix),o.matrix.decompose(o.position,o.rotation,o.scale),o.matrixWorldNeedsUpdate=!0,i.linearVelocity?(o.hasLinearVelocity=!0,o.linearVelocity.copy(i.linearVelocity)):o.hasLinearVelocity=!1,i.angularVelocity?(o.hasAngularVelocity=!0,o.angularVelocity.copy(i.angularVelocity)):o.hasAngularVelocity=!1,this.dispatchEvent(vp)))}return o!==null&&(o.visible=i!==null),l!==null&&(l.visible=s!==null),c!==null&&(c.visible=a!==null),this}_getHandJoint(e,t){if(e.joints[t.jointName]===void 0){const n=new Si;n.matrixAutoUpdate=!1,n.visible=!1,e.joints[t.jointName]=n,e.add(n)}return e.joints[t.jointName]}}const Yh={aliceblue:15792383,antiquewhite:16444375,aqua:65535,aquamarine:8388564,azure:15794175,beige:16119260,bisque:16770244,black:0,blanchedalmond:16772045,blue:255,blueviolet:9055202,brown:10824234,burlywood:14596231,cadetblue:6266528,chartreuse:8388352,chocolate:13789470,coral:16744272,cornflowerblue:6591981,cornsilk:16775388,crimson:14423100,cyan:65535,darkblue:139,darkcyan:35723,darkgoldenrod:12092939,darkgray:11119017,darkgreen:25600,darkgrey:11119017,darkkhaki:12433259,darkmagenta:9109643,darkolivegreen:5597999,darkorange:16747520,darkorchid:10040012,darkred:9109504,darksalmon:15308410,darkseagreen:9419919,darkslateblue:4734347,darkslategray:3100495,darkslategrey:3100495,darkturquoise:52945,darkviolet:9699539,deeppink:16716947,deepskyblue:49151,dimgray:6908265,dimgrey:6908265,dodgerblue:2003199,firebrick:11674146,floralwhite:16775920,forestgreen:2263842,fuchsia:16711935,gainsboro:14474460,ghostwhite:16316671,gold:16766720,goldenrod:14329120,gray:8421504,green:32768,greenyellow:11403055,grey:8421504,honeydew:15794160,hotpink:16738740,indianred:13458524,indigo:4915330,ivory:16777200,khaki:15787660,lavender:15132410,lavenderblush:16773365,lawngreen:8190976,lemonchiffon:16775885,lightblue:11393254,lightcoral:15761536,lightcyan:14745599,lightgoldenrodyellow:16448210,lightgray:13882323,lightgreen:9498256,lightgrey:13882323,lightpink:16758465,lightsalmon:16752762,lightseagreen:2142890,lightskyblue:8900346,lightslategray:7833753,lightslategrey:7833753,lightsteelblue:11584734,lightyellow:16777184,lime:65280,limegreen:3329330,linen:16445670,magenta:16711935,maroon:8388608,mediumaquamarine:6737322,mediumblue:205,mediumorchid:12211667,mediumpurple:9662683,mediumseagreen:3978097,mediumslateblue:8087790,mediumspringgreen:64154,mediumturquoise:4772300,mediumvioletred:13047173,midnightblue:1644912,mintcream:16121850,mistyrose:16770273,moccasin:16770229,navajowhite:16768685,navy:128,oldlace:16643558,olive:8421376,olivedrab:7048739,orange:16753920,orangered:16729344,orchid:14315734,palegoldenrod:15657130,palegreen:10025880,paleturquoise:11529966,palevioletred:14381203,papayawhip:16773077,peachpuff:16767673,peru:13468991,pink:16761035,plum:14524637,powderblue:11591910,purple:8388736,rebeccapurple:6697881,red:16711680,rosybrown:12357519,royalblue:4286945,saddlebrown:9127187,salmon:16416882,sandybrown:16032864,seagreen:3050327,seashell:16774638,sienna:10506797,silver:12632256,skyblue:8900331,slateblue:6970061,slategray:7372944,slategrey:7372944,snow:16775930,springgreen:65407,steelblue:4620980,tan:13808780,teal:32896,thistle:14204888,tomato:16737095,turquoise:4251856,violet:15631086,wheat:16113331,white:16777215,whitesmoke:16119285,yellow:16776960,yellowgreen:10145074},Ii={h:0,s:0,l:0},ia={h:0,s:0,l:0};function Mo(r,e,t){return t<0&&(t+=1),t>1&&(t-=1),t<1/6?r+(e-r)*6*t:t<1/2?e:t<2/3?r+(e-r)*6*(2/3-t):r}class it{constructor(e,t,n){return this.isColor=!0,this.r=1,this.g=1,this.b=1,this.set(e,t,n)}set(e,t,n){if(t===void 0&&n===void 0){const i=e;i&&i.isColor?this.copy(i):typeof i=="number"?this.setHex(i):typeof i=="string"&&this.setStyle(i)}else this.setRGB(e,t,n);return this}setScalar(e){return this.r=e,this.g=e,this.b=e,this}setHex(e,t=Tn){return e=Math.floor(e),this.r=(e>>16&255)/255,this.g=(e>>8&255)/255,this.b=(e&255)/255,ht.colorSpaceToWorking(this,t),this}setRGB(e,t,n,i=ht.workingColorSpace){return this.r=e,this.g=t,this.b=n,ht.colorSpaceToWorking(this,i),this}setHSL(e,t,n,i=ht.workingColorSpace){if(e=rp(e,1),t=nt(t,0,1),n=nt(n,0,1),t===0)this.r=this.g=this.b=n;else{const s=n<=.5?n*(1+t):n+t-n*t,a=2*n-s;this.r=Mo(a,s,e+1/3),this.g=Mo(a,s,e),this.b=Mo(a,s,e-1/3)}return ht.colorSpaceToWorking(this,i),this}setStyle(e,t=Tn){function n(s){s!==void 0&&parseFloat(s)<1&&He("Color: Alpha component of "+e+" will be ignored.")}let i;if(i=/^(\w+)\(([^\)]*)\)/.exec(e)){let s;const a=i[1],o=i[2];switch(a){case"rgb":case"rgba":if(s=/^\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setRGB(Math.min(255,parseInt(s[1],10))/255,Math.min(255,parseInt(s[2],10))/255,Math.min(255,parseInt(s[3],10))/255,t);if(s=/^\s*(\d+)\%\s*,\s*(\d+)\%\s*,\s*(\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setRGB(Math.min(100,parseInt(s[1],10))/100,Math.min(100,parseInt(s[2],10))/100,Math.min(100,parseInt(s[3],10))/100,t);break;case"hsl":case"hsla":if(s=/^\s*(\d*\.?\d+)\s*,\s*(\d*\.?\d+)\%\s*,\s*(\d*\.?\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setHSL(parseFloat(s[1])/360,parseFloat(s[2])/100,parseFloat(s[3])/100,t);break;default:He("Color: Unknown color model "+e)}}else if(i=/^\#([A-Fa-f\d]+)$/.exec(e)){const s=i[1],a=s.length;if(a===3)return this.setRGB(parseInt(s.charAt(0),16)/15,parseInt(s.charAt(1),16)/15,parseInt(s.charAt(2),16)/15,t);if(a===6)return this.setHex(parseInt(s,16),t);He("Color: Invalid hex color "+e)}else if(e&&e.length>0)return this.setColorName(e,t);return this}setColorName(e,t=Tn){const n=Yh[e.toLowerCase()];return n!==void 0?this.setHex(n,t):He("Color: Unknown color "+e),this}clone(){return new this.constructor(this.r,this.g,this.b)}copy(e){return this.r=e.r,this.g=e.g,this.b=e.b,this}copySRGBToLinear(e){return this.r=Ei(e.r),this.g=Ei(e.g),this.b=Ei(e.b),this}copyLinearToSRGB(e){return this.r=Zr(e.r),this.g=Zr(e.g),this.b=Zr(e.b),this}convertSRGBToLinear(){return this.copySRGBToLinear(this),this}convertLinearToSRGB(){return this.copyLinearToSRGB(this),this}getHex(e=Tn){return ht.workingToColorSpace(Jt.copy(this),e),Math.round(nt(Jt.r*255,0,255))*65536+Math.round(nt(Jt.g*255,0,255))*256+Math.round(nt(Jt.b*255,0,255))}getHexString(e=Tn){return("000000"+this.getHex(e).toString(16)).slice(-6)}getHSL(e,t=ht.workingColorSpace){ht.workingToColorSpace(Jt.copy(this),t);const n=Jt.r,i=Jt.g,s=Jt.b,a=Math.max(n,i,s),o=Math.min(n,i,s);let l,c;const u=(o+a)/2;if(o===a)l=0,c=0;else{const f=a-o;switch(c=u<=.5?f/(a+o):f/(2-a-o),a){case n:l=(i-s)/f+(i<s?6:0);break;case i:l=(s-n)/f+2;break;case s:l=(n-i)/f+4;break}l/=6}return e.h=l,e.s=c,e.l=u,e}getRGB(e,t=ht.workingColorSpace){return ht.workingToColorSpace(Jt.copy(this),t),e.r=Jt.r,e.g=Jt.g,e.b=Jt.b,e}getStyle(e=Tn){ht.workingToColorSpace(Jt.copy(this),e);const t=Jt.r,n=Jt.g,i=Jt.b;return e!==Tn?`color(${e} ${t.toFixed(3)} ${n.toFixed(3)} ${i.toFixed(3)})`:`rgb(${Math.round(t*255)},${Math.round(n*255)},${Math.round(i*255)})`}offsetHSL(e,t,n){return this.getHSL(Ii),this.setHSL(Ii.h+e,Ii.s+t,Ii.l+n)}add(e){return this.r+=e.r,this.g+=e.g,this.b+=e.b,this}addColors(e,t){return this.r=e.r+t.r,this.g=e.g+t.g,this.b=e.b+t.b,this}addScalar(e){return this.r+=e,this.g+=e,this.b+=e,this}sub(e){return this.r=Math.max(0,this.r-e.r),this.g=Math.max(0,this.g-e.g),this.b=Math.max(0,this.b-e.b),this}multiply(e){return this.r*=e.r,this.g*=e.g,this.b*=e.b,this}multiplyScalar(e){return this.r*=e,this.g*=e,this.b*=e,this}lerp(e,t){return this.r+=(e.r-this.r)*t,this.g+=(e.g-this.g)*t,this.b+=(e.b-this.b)*t,this}lerpColors(e,t,n){return this.r=e.r+(t.r-e.r)*n,this.g=e.g+(t.g-e.g)*n,this.b=e.b+(t.b-e.b)*n,this}lerpHSL(e,t){this.getHSL(Ii),e.getHSL(ia);const n=fo(Ii.h,ia.h,t),i=fo(Ii.s,ia.s,t),s=fo(Ii.l,ia.l,t);return this.setHSL(n,i,s),this}setFromVector3(e){return this.r=e.x,this.g=e.y,this.b=e.z,this}applyMatrix3(e){const t=this.r,n=this.g,i=this.b,s=e.elements;return this.r=s[0]*t+s[3]*n+s[6]*i,this.g=s[1]*t+s[4]*n+s[7]*i,this.b=s[2]*t+s[5]*n+s[8]*i,this}equals(e){return e.r===this.r&&e.g===this.g&&e.b===this.b}fromArray(e,t=0){return this.r=e[t],this.g=e[t+1],this.b=e[t+2],this}toArray(e=[],t=0){return e[t]=this.r,e[t+1]=this.g,e[t+2]=this.b,e}fromBufferAttribute(e,t){return this.r=e.getX(t),this.g=e.getY(t),this.b=e.getZ(t),this}toJSON(){return this.getHex()}*[Symbol.iterator](){yield this.r,yield this.g,yield this.b}}const Jt=new it;it.NAMES=Yh;class Ec{constructor(e,t=25e-5){this.isFogExp2=!0,this.name="",this.color=new it(e),this.density=t}clone(){return new Ec(this.color,this.density)}toJSON(){return{type:"FogExp2",name:this.name,color:this.color.getHex(),density:this.density}}}class pu extends kt{constructor(){super(),this.isScene=!0,this.type="Scene",this.background=null,this.environment=null,this.fog=null,this.backgroundBlurriness=0,this.backgroundIntensity=1,this.backgroundRotation=new li,this.environmentIntensity=1,this.environmentRotation=new li,this.overrideMaterial=null,typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}copy(e,t){return super.copy(e,t),e.background!==null&&(this.background=e.background.clone()),e.environment!==null&&(this.environment=e.environment.clone()),e.fog!==null&&(this.fog=e.fog.clone()),this.backgroundBlurriness=e.backgroundBlurriness,this.backgroundIntensity=e.backgroundIntensity,this.backgroundRotation.copy(e.backgroundRotation),this.environmentIntensity=e.environmentIntensity,this.environmentRotation.copy(e.environmentRotation),e.overrideMaterial!==null&&(this.overrideMaterial=e.overrideMaterial.clone()),this.matrixAutoUpdate=e.matrixAutoUpdate,this}toJSON(e){const t=super.toJSON(e);return this.fog!==null&&(t.object.fog=this.fog.toJSON()),this.backgroundBlurriness>0&&(t.object.backgroundBlurriness=this.backgroundBlurriness),this.backgroundIntensity!==1&&(t.object.backgroundIntensity=this.backgroundIntensity),t.object.backgroundRotation=this.backgroundRotation.toArray(),this.environmentIntensity!==1&&(t.object.environmentIntensity=this.environmentIntensity),t.object.environmentRotation=this.environmentRotation.toArray(),t}}const Hn=new V,mi=new V,So=new V,_i=new V,Ir=new V,Ur=new V,mu=new V,yo=new V,bo=new V,Eo=new V,To=new Pt,Ao=new Pt,wo=new Pt;class kn{constructor(e=new V,t=new V,n=new V){this.a=e,this.b=t,this.c=n}static getNormal(e,t,n,i){i.subVectors(n,t),Hn.subVectors(e,t),i.cross(Hn);const s=i.lengthSq();return s>0?i.multiplyScalar(1/Math.sqrt(s)):i.set(0,0,0)}static getBarycoord(e,t,n,i,s){Hn.subVectors(i,t),mi.subVectors(n,t),So.subVectors(e,t);const a=Hn.dot(Hn),o=Hn.dot(mi),l=Hn.dot(So),c=mi.dot(mi),u=mi.dot(So),f=a*c-o*o;if(f===0)return s.set(0,0,0),null;const h=1/f,d=(c*l-o*u)*h,_=(a*u-o*l)*h;return s.set(1-d-_,_,d)}static containsPoint(e,t,n,i){return this.getBarycoord(e,t,n,i,_i)===null?!1:_i.x>=0&&_i.y>=0&&_i.x+_i.y<=1}static getInterpolation(e,t,n,i,s,a,o,l){return this.getBarycoord(e,t,n,i,_i)===null?(l.x=0,l.y=0,"z"in l&&(l.z=0),"w"in l&&(l.w=0),null):(l.setScalar(0),l.addScaledVector(s,_i.x),l.addScaledVector(a,_i.y),l.addScaledVector(o,_i.z),l)}static getInterpolatedAttribute(e,t,n,i,s,a){return To.setScalar(0),Ao.setScalar(0),wo.setScalar(0),To.fromBufferAttribute(e,t),Ao.fromBufferAttribute(e,n),wo.fromBufferAttribute(e,i),a.setScalar(0),a.addScaledVector(To,s.x),a.addScaledVector(Ao,s.y),a.addScaledVector(wo,s.z),a}static isFrontFacing(e,t,n,i){return Hn.subVectors(n,t),mi.subVectors(e,t),Hn.cross(mi).dot(i)<0}set(e,t,n){return this.a.copy(e),this.b.copy(t),this.c.copy(n),this}setFromPointsAndIndices(e,t,n,i){return this.a.copy(e[t]),this.b.copy(e[n]),this.c.copy(e[i]),this}setFromAttributeAndIndices(e,t,n,i){return this.a.fromBufferAttribute(e,t),this.b.fromBufferAttribute(e,n),this.c.fromBufferAttribute(e,i),this}clone(){return new this.constructor().copy(this)}copy(e){return this.a.copy(e.a),this.b.copy(e.b),this.c.copy(e.c),this}getArea(){return Hn.subVectors(this.c,this.b),mi.subVectors(this.a,this.b),Hn.cross(mi).length()*.5}getMidpoint(e){return e.addVectors(this.a,this.b).add(this.c).multiplyScalar(1/3)}getNormal(e){return kn.getNormal(this.a,this.b,this.c,e)}getPlane(e){return e.setFromCoplanarPoints(this.a,this.b,this.c)}getBarycoord(e,t){return kn.getBarycoord(e,this.a,this.b,this.c,t)}getInterpolation(e,t,n,i,s){return kn.getInterpolation(e,this.a,this.b,this.c,t,n,i,s)}containsPoint(e){return kn.containsPoint(e,this.a,this.b,this.c)}isFrontFacing(e){return kn.isFrontFacing(this.a,this.b,this.c,e)}intersectsBox(e){return e.intersectsTriangle(this)}closestPointToPoint(e,t){const n=this.a,i=this.b,s=this.c;let a,o;Ir.subVectors(i,n),Ur.subVectors(s,n),yo.subVectors(e,n);const l=Ir.dot(yo),c=Ur.dot(yo);if(l<=0&&c<=0)return t.copy(n);bo.subVectors(e,i);const u=Ir.dot(bo),f=Ur.dot(bo);if(u>=0&&f<=u)return t.copy(i);const h=l*f-u*c;if(h<=0&&l>=0&&u<=0)return a=l/(l-u),t.copy(n).addScaledVector(Ir,a);Eo.subVectors(e,s);const d=Ir.dot(Eo),_=Ur.dot(Eo);if(_>=0&&d<=_)return t.copy(s);const g=d*c-l*_;if(g<=0&&c>=0&&_<=0)return o=c/(c-_),t.copy(n).addScaledVector(Ur,o);const p=u*_-d*f;if(p<=0&&f-u>=0&&d-_>=0)return mu.subVectors(s,i),o=(f-u)/(f-u+(d-_)),t.copy(i).addScaledVector(mu,o);const m=1/(p+g+h);return a=g*m,o=h*m,t.copy(n).addScaledVector(Ir,a).addScaledVector(Ur,o)}equals(e){return e.a.equals(this.a)&&e.b.equals(this.b)&&e.c.equals(this.c)}}class hs{constructor(e=new V(1/0,1/0,1/0),t=new V(-1/0,-1/0,-1/0)){this.isBox3=!0,this.min=e,this.max=t}set(e,t){return this.min.copy(e),this.max.copy(t),this}setFromArray(e){this.makeEmpty();for(let t=0,n=e.length;t<n;t+=3)this.expandByPoint(Wn.fromArray(e,t));return this}setFromBufferAttribute(e){this.makeEmpty();for(let t=0,n=e.count;t<n;t++)this.expandByPoint(Wn.fromBufferAttribute(e,t));return this}setFromPoints(e){this.makeEmpty();for(let t=0,n=e.length;t<n;t++)this.expandByPoint(e[t]);return this}setFromCenterAndSize(e,t){const n=Wn.copy(t).multiplyScalar(.5);return this.min.copy(e).sub(n),this.max.copy(e).add(n),this}setFromObject(e,t=!1){return this.makeEmpty(),this.expandByObject(e,t)}clone(){return new this.constructor().copy(this)}copy(e){return this.min.copy(e.min),this.max.copy(e.max),this}makeEmpty(){return this.min.x=this.min.y=this.min.z=1/0,this.max.x=this.max.y=this.max.z=-1/0,this}isEmpty(){return this.max.x<this.min.x||this.max.y<this.min.y||this.max.z<this.min.z}getCenter(e){return this.isEmpty()?e.set(0,0,0):e.addVectors(this.min,this.max).multiplyScalar(.5)}getSize(e){return this.isEmpty()?e.set(0,0,0):e.subVectors(this.max,this.min)}expandByPoint(e){return this.min.min(e),this.max.max(e),this}expandByVector(e){return this.min.sub(e),this.max.add(e),this}expandByScalar(e){return this.min.addScalar(-e),this.max.addScalar(e),this}expandByObject(e,t=!1){e.updateWorldMatrix(!1,!1);const n=e.geometry;if(n!==void 0){const s=n.getAttribute("position");if(t===!0&&s!==void 0&&e.isInstancedMesh!==!0)for(let a=0,o=s.count;a<o;a++)e.isMesh===!0?e.getVertexPosition(a,Wn):Wn.fromBufferAttribute(s,a),Wn.applyMatrix4(e.matrixWorld),this.expandByPoint(Wn);else e.boundingBox!==void 0?(e.boundingBox===null&&e.computeBoundingBox(),ra.copy(e.boundingBox)):(n.boundingBox===null&&n.computeBoundingBox(),ra.copy(n.boundingBox)),ra.applyMatrix4(e.matrixWorld),this.union(ra)}const i=e.children;for(let s=0,a=i.length;s<a;s++)this.expandByObject(i[s],t);return this}containsPoint(e){return e.x>=this.min.x&&e.x<=this.max.x&&e.y>=this.min.y&&e.y<=this.max.y&&e.z>=this.min.z&&e.z<=this.max.z}containsBox(e){return this.min.x<=e.min.x&&e.max.x<=this.max.x&&this.min.y<=e.min.y&&e.max.y<=this.max.y&&this.min.z<=e.min.z&&e.max.z<=this.max.z}getParameter(e,t){return t.set((e.x-this.min.x)/(this.max.x-this.min.x),(e.y-this.min.y)/(this.max.y-this.min.y),(e.z-this.min.z)/(this.max.z-this.min.z))}intersectsBox(e){return e.max.x>=this.min.x&&e.min.x<=this.max.x&&e.max.y>=this.min.y&&e.min.y<=this.max.y&&e.max.z>=this.min.z&&e.min.z<=this.max.z}intersectsSphere(e){return this.clampPoint(e.center,Wn),Wn.distanceToSquared(e.center)<=e.radius*e.radius}intersectsPlane(e){let t,n;return e.normal.x>0?(t=e.normal.x*this.min.x,n=e.normal.x*this.max.x):(t=e.normal.x*this.max.x,n=e.normal.x*this.min.x),e.normal.y>0?(t+=e.normal.y*this.min.y,n+=e.normal.y*this.max.y):(t+=e.normal.y*this.max.y,n+=e.normal.y*this.min.y),e.normal.z>0?(t+=e.normal.z*this.min.z,n+=e.normal.z*this.max.z):(t+=e.normal.z*this.max.z,n+=e.normal.z*this.min.z),t<=-e.constant&&n>=-e.constant}intersectsTriangle(e){if(this.isEmpty())return!1;this.getCenter(xs),sa.subVectors(this.max,xs),Nr.subVectors(e.a,xs),Fr.subVectors(e.b,xs),Or.subVectors(e.c,xs),Ui.subVectors(Fr,Nr),Ni.subVectors(Or,Fr),rr.subVectors(Nr,Or);let t=[0,-Ui.z,Ui.y,0,-Ni.z,Ni.y,0,-rr.z,rr.y,Ui.z,0,-Ui.x,Ni.z,0,-Ni.x,rr.z,0,-rr.x,-Ui.y,Ui.x,0,-Ni.y,Ni.x,0,-rr.y,rr.x,0];return!Ro(t,Nr,Fr,Or,sa)||(t=[1,0,0,0,1,0,0,0,1],!Ro(t,Nr,Fr,Or,sa))?!1:(aa.crossVectors(Ui,Ni),t=[aa.x,aa.y,aa.z],Ro(t,Nr,Fr,Or,sa))}clampPoint(e,t){return t.copy(e).clamp(this.min,this.max)}distanceToPoint(e){return this.clampPoint(e,Wn).distanceTo(e)}getBoundingSphere(e){return this.isEmpty()?e.makeEmpty():(this.getCenter(e.center),e.radius=this.getSize(Wn).length()*.5),e}intersect(e){return this.min.max(e.min),this.max.min(e.max),this.isEmpty()&&this.makeEmpty(),this}union(e){return this.min.min(e.min),this.max.max(e.max),this}applyMatrix4(e){return this.isEmpty()?this:(gi[0].set(this.min.x,this.min.y,this.min.z).applyMatrix4(e),gi[1].set(this.min.x,this.min.y,this.max.z).applyMatrix4(e),gi[2].set(this.min.x,this.max.y,this.min.z).applyMatrix4(e),gi[3].set(this.min.x,this.max.y,this.max.z).applyMatrix4(e),gi[4].set(this.max.x,this.min.y,this.min.z).applyMatrix4(e),gi[5].set(this.max.x,this.min.y,this.max.z).applyMatrix4(e),gi[6].set(this.max.x,this.max.y,this.min.z).applyMatrix4(e),gi[7].set(this.max.x,this.max.y,this.max.z).applyMatrix4(e),this.setFromPoints(gi),this)}translate(e){return this.min.add(e),this.max.add(e),this}equals(e){return e.min.equals(this.min)&&e.max.equals(this.max)}toJSON(){return{min:this.min.toArray(),max:this.max.toArray()}}fromJSON(e){return this.min.fromArray(e.min),this.max.fromArray(e.max),this}}const gi=[new V,new V,new V,new V,new V,new V,new V,new V],Wn=new V,ra=new hs,Nr=new V,Fr=new V,Or=new V,Ui=new V,Ni=new V,rr=new V,xs=new V,sa=new V,aa=new V,sr=new V;function Ro(r,e,t,n,i){for(let s=0,a=r.length-3;s<=a;s+=3){sr.fromArray(r,s);const o=i.x*Math.abs(sr.x)+i.y*Math.abs(sr.y)+i.z*Math.abs(sr.z),l=e.dot(sr),c=t.dot(sr),u=n.dot(sr);if(Math.max(-Math.max(l,c,u),Math.min(l,c,u))>o)return!1}return!0}const Nt=new V,oa=new qe;let xp=0;class Yn{constructor(e,t,n=!1){if(Array.isArray(e))throw new TypeError("THREE.BufferAttribute: array should be a Typed Array.");this.isBufferAttribute=!0,Object.defineProperty(this,"id",{value:xp++}),this.name="",this.array=e,this.itemSize=t,this.count=e!==void 0?e.length/t:0,this.normalized=n,this.usage=eu,this.updateRanges=[],this.gpuType=ni,this.version=0}onUploadCallback(){}set needsUpdate(e){e===!0&&this.version++}setUsage(e){return this.usage=e,this}addUpdateRange(e,t){this.updateRanges.push({start:e,count:t})}clearUpdateRanges(){this.updateRanges.length=0}copy(e){return this.name=e.name,this.array=new e.array.constructor(e.array),this.itemSize=e.itemSize,this.count=e.count,this.normalized=e.normalized,this.usage=e.usage,this.gpuType=e.gpuType,this}copyAt(e,t,n){e*=this.itemSize,n*=t.itemSize;for(let i=0,s=this.itemSize;i<s;i++)this.array[e+i]=t.array[n+i];return this}copyArray(e){return this.array.set(e),this}applyMatrix3(e){if(this.itemSize===2)for(let t=0,n=this.count;t<n;t++)oa.fromBufferAttribute(this,t),oa.applyMatrix3(e),this.setXY(t,oa.x,oa.y);else if(this.itemSize===3)for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.applyMatrix3(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}applyMatrix4(e){for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.applyMatrix4(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}applyNormalMatrix(e){for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.applyNormalMatrix(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}transformDirection(e){for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.transformDirection(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}set(e,t=0){return this.array.set(e,t),this}getComponent(e,t){let n=this.array[e*this.itemSize+t];return this.normalized&&(n=gs(n,this.array)),n}setComponent(e,t,n){return this.normalized&&(n=un(n,this.array)),this.array[e*this.itemSize+t]=n,this}getX(e){let t=this.array[e*this.itemSize];return this.normalized&&(t=gs(t,this.array)),t}setX(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize]=t,this}getY(e){let t=this.array[e*this.itemSize+1];return this.normalized&&(t=gs(t,this.array)),t}setY(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize+1]=t,this}getZ(e){let t=this.array[e*this.itemSize+2];return this.normalized&&(t=gs(t,this.array)),t}setZ(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize+2]=t,this}getW(e){let t=this.array[e*this.itemSize+3];return this.normalized&&(t=gs(t,this.array)),t}setW(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize+3]=t,this}setXY(e,t,n){return e*=this.itemSize,this.normalized&&(t=un(t,this.array),n=un(n,this.array)),this.array[e+0]=t,this.array[e+1]=n,this}setXYZ(e,t,n,i){return e*=this.itemSize,this.normalized&&(t=un(t,this.array),n=un(n,this.array),i=un(i,this.array)),this.array[e+0]=t,this.array[e+1]=n,this.array[e+2]=i,this}setXYZW(e,t,n,i,s){return e*=this.itemSize,this.normalized&&(t=un(t,this.array),n=un(n,this.array),i=un(i,this.array),s=un(s,this.array)),this.array[e+0]=t,this.array[e+1]=n,this.array[e+2]=i,this.array[e+3]=s,this}onUpload(e){return this.onUploadCallback=e,this}clone(){return new this.constructor(this.array,this.itemSize).copy(this)}toJSON(){const e={itemSize:this.itemSize,type:this.array.constructor.name,array:Array.from(this.array),normalized:this.normalized};return this.name!==""&&(e.name=this.name),this.usage!==eu&&(e.usage=this.usage),e}}class $h extends Yn{constructor(e,t,n){super(new Uint16Array(e),t,n)}}class Kh extends Yn{constructor(e,t,n){super(new Uint32Array(e),t,n)}}class Yt extends Yn{constructor(e,t,n){super(new Float32Array(e),t,n)}}const Mp=new hs,Ms=new V,Co=new V;class Ys{constructor(e=new V,t=-1){this.isSphere=!0,this.center=e,this.radius=t}set(e,t){return this.center.copy(e),this.radius=t,this}setFromPoints(e,t){const n=this.center;t!==void 0?n.copy(t):Mp.setFromPoints(e).getCenter(n);let i=0;for(let s=0,a=e.length;s<a;s++)i=Math.max(i,n.distanceToSquared(e[s]));return this.radius=Math.sqrt(i),this}copy(e){return this.center.copy(e.center),this.radius=e.radius,this}isEmpty(){return this.radius<0}makeEmpty(){return this.center.set(0,0,0),this.radius=-1,this}containsPoint(e){return e.distanceToSquared(this.center)<=this.radius*this.radius}distanceToPoint(e){return e.distanceTo(this.center)-this.radius}intersectsSphere(e){const t=this.radius+e.radius;return e.center.distanceToSquared(this.center)<=t*t}intersectsBox(e){return e.intersectsSphere(this)}intersectsPlane(e){return Math.abs(e.distanceToPoint(this.center))<=this.radius}clampPoint(e,t){const n=this.center.distanceToSquared(e);return t.copy(e),n>this.radius*this.radius&&(t.sub(this.center).normalize(),t.multiplyScalar(this.radius).add(this.center)),t}getBoundingBox(e){return this.isEmpty()?(e.makeEmpty(),e):(e.set(this.center,this.center),e.expandByScalar(this.radius),e)}applyMatrix4(e){return this.center.applyMatrix4(e),this.radius=this.radius*e.getMaxScaleOnAxis(),this}translate(e){return this.center.add(e),this}expandByPoint(e){if(this.isEmpty())return this.center.copy(e),this.radius=0,this;Ms.subVectors(e,this.center);const t=Ms.lengthSq();if(t>this.radius*this.radius){const n=Math.sqrt(t),i=(n-this.radius)*.5;this.center.addScaledVector(Ms,i/n),this.radius+=i}return this}union(e){return e.isEmpty()?this:this.isEmpty()?(this.copy(e),this):(this.center.equals(e.center)===!0?this.radius=Math.max(this.radius,e.radius):(Co.subVectors(e.center,this.center).setLength(e.radius),this.expandByPoint(Ms.copy(e.center).add(Co)),this.expandByPoint(Ms.copy(e.center).sub(Co))),this)}equals(e){return e.center.equals(this.center)&&e.radius===this.radius}clone(){return new this.constructor().copy(this)}toJSON(){return{radius:this.radius,center:this.center.toArray()}}fromJSON(e){return this.radius=e.radius,this.center.fromArray(e.center),this}}let Sp=0;const On=new St,Po=new kt,Br=new V,En=new hs,Ss=new hs,Ht=new V;class ln extends br{constructor(){super(),this.isBufferGeometry=!0,Object.defineProperty(this,"id",{value:Sp++}),this.uuid=qs(),this.name="",this.type="BufferGeometry",this.index=null,this.indirect=null,this.indirectOffset=0,this.attributes={},this.morphAttributes={},this.morphTargetsRelative=!1,this.groups=[],this.boundingBox=null,this.boundingSphere=null,this.drawRange={start:0,count:1/0},this.userData={}}getIndex(){return this.index}setIndex(e){return Array.isArray(e)?this.index=new(ep(e)?Kh:$h)(e,1):this.index=e,this}setIndirect(e,t=0){return this.indirect=e,this.indirectOffset=t,this}getIndirect(){return this.indirect}getAttribute(e){return this.attributes[e]}setAttribute(e,t){return this.attributes[e]=t,this}deleteAttribute(e){return delete this.attributes[e],this}hasAttribute(e){return this.attributes[e]!==void 0}addGroup(e,t,n=0){this.groups.push({start:e,count:t,materialIndex:n})}clearGroups(){this.groups=[]}setDrawRange(e,t){this.drawRange.start=e,this.drawRange.count=t}applyMatrix4(e){const t=this.attributes.position;t!==void 0&&(t.applyMatrix4(e),t.needsUpdate=!0);const n=this.attributes.normal;if(n!==void 0){const s=new Ke().getNormalMatrix(e);n.applyNormalMatrix(s),n.needsUpdate=!0}const i=this.attributes.tangent;return i!==void 0&&(i.transformDirection(e),i.needsUpdate=!0),this.boundingBox!==null&&this.computeBoundingBox(),this.boundingSphere!==null&&this.computeBoundingSphere(),this}applyQuaternion(e){return On.makeRotationFromQuaternion(e),this.applyMatrix4(On),this}rotateX(e){return On.makeRotationX(e),this.applyMatrix4(On),this}rotateY(e){return On.makeRotationY(e),this.applyMatrix4(On),this}rotateZ(e){return On.makeRotationZ(e),this.applyMatrix4(On),this}translate(e,t,n){return On.makeTranslation(e,t,n),this.applyMatrix4(On),this}scale(e,t,n){return On.makeScale(e,t,n),this.applyMatrix4(On),this}lookAt(e){return Po.lookAt(e),Po.updateMatrix(),this.applyMatrix4(Po.matrix),this}center(){return this.computeBoundingBox(),this.boundingBox.getCenter(Br).negate(),this.translate(Br.x,Br.y,Br.z),this}setFromPoints(e){const t=this.getAttribute("position");if(t===void 0){const n=[];for(let i=0,s=e.length;i<s;i++){const a=e[i];n.push(a.x,a.y,a.z||0)}this.setAttribute("position",new Yt(n,3))}else{const n=Math.min(e.length,t.count);for(let i=0;i<n;i++){const s=e[i];t.setXYZ(i,s.x,s.y,s.z||0)}e.length>t.count&&He("BufferGeometry: Buffer size too small for points data. Use .dispose() and create a new geometry."),t.needsUpdate=!0}return this}computeBoundingBox(){this.boundingBox===null&&(this.boundingBox=new hs);const e=this.attributes.position,t=this.morphAttributes.position;if(e&&e.isGLBufferAttribute){ut("BufferGeometry.computeBoundingBox(): GLBufferAttribute requires a manual bounding box.",this),this.boundingBox.set(new V(-1/0,-1/0,-1/0),new V(1/0,1/0,1/0));return}if(e!==void 0){if(this.boundingBox.setFromBufferAttribute(e),t)for(let n=0,i=t.length;n<i;n++){const s=t[n];En.setFromBufferAttribute(s),this.morphTargetsRelative?(Ht.addVectors(this.boundingBox.min,En.min),this.boundingBox.expandByPoint(Ht),Ht.addVectors(this.boundingBox.max,En.max),this.boundingBox.expandByPoint(Ht)):(this.boundingBox.expandByPoint(En.min),this.boundingBox.expandByPoint(En.max))}}else this.boundingBox.makeEmpty();(isNaN(this.boundingBox.min.x)||isNaN(this.boundingBox.min.y)||isNaN(this.boundingBox.min.z))&&ut('BufferGeometry.computeBoundingBox(): Computed min/max have NaN values. The "position" attribute is likely to have NaN values.',this)}computeBoundingSphere(){this.boundingSphere===null&&(this.boundingSphere=new Ys);const e=this.attributes.position,t=this.morphAttributes.position;if(e&&e.isGLBufferAttribute){ut("BufferGeometry.computeBoundingSphere(): GLBufferAttribute requires a manual bounding sphere.",this),this.boundingSphere.set(new V,1/0);return}if(e){const n=this.boundingSphere.center;if(En.setFromBufferAttribute(e),t)for(let s=0,a=t.length;s<a;s++){const o=t[s];Ss.setFromBufferAttribute(o),this.morphTargetsRelative?(Ht.addVectors(En.min,Ss.min),En.expandByPoint(Ht),Ht.addVectors(En.max,Ss.max),En.expandByPoint(Ht)):(En.expandByPoint(Ss.min),En.expandByPoint(Ss.max))}En.getCenter(n);let i=0;for(let s=0,a=e.count;s<a;s++)Ht.fromBufferAttribute(e,s),i=Math.max(i,n.distanceToSquared(Ht));if(t)for(let s=0,a=t.length;s<a;s++){const o=t[s],l=this.morphTargetsRelative;for(let c=0,u=o.count;c<u;c++)Ht.fromBufferAttribute(o,c),l&&(Br.fromBufferAttribute(e,c),Ht.add(Br)),i=Math.max(i,n.distanceToSquared(Ht))}this.boundingSphere.radius=Math.sqrt(i),isNaN(this.boundingSphere.radius)&&ut('BufferGeometry.computeBoundingSphere(): Computed radius is NaN. The "position" attribute is likely to have NaN values.',this)}}computeTangents(){const e=this.index,t=this.attributes;if(e===null||t.position===void 0||t.normal===void 0||t.uv===void 0){ut("BufferGeometry: .computeTangents() failed. Missing required attributes (index, position, normal or uv)");return}const n=t.position,i=t.normal,s=t.uv;this.hasAttribute("tangent")===!1&&this.setAttribute("tangent",new Yn(new Float32Array(4*n.count),4));const a=this.getAttribute("tangent"),o=[],l=[];for(let x=0;x<n.count;x++)o[x]=new V,l[x]=new V;const c=new V,u=new V,f=new V,h=new qe,d=new qe,_=new qe,g=new V,p=new V;function m(x,b,W){c.fromBufferAttribute(n,x),u.fromBufferAttribute(n,b),f.fromBufferAttribute(n,W),h.fromBufferAttribute(s,x),d.fromBufferAttribute(s,b),_.fromBufferAttribute(s,W),u.sub(c),f.sub(c),d.sub(h),_.sub(h);const L=1/(d.x*_.y-_.x*d.y);isFinite(L)&&(g.copy(u).multiplyScalar(_.y).addScaledVector(f,-d.y).multiplyScalar(L),p.copy(f).multiplyScalar(d.x).addScaledVector(u,-_.x).multiplyScalar(L),o[x].add(g),o[b].add(g),o[W].add(g),l[x].add(p),l[b].add(p),l[W].add(p))}let M=this.groups;M.length===0&&(M=[{start:0,count:e.count}]);for(let x=0,b=M.length;x<b;++x){const W=M[x],L=W.start,G=W.count;for(let z=L,q=L+G;z<q;z+=3)m(e.getX(z+0),e.getX(z+1),e.getX(z+2))}const S=new V,y=new V,A=new V,w=new V;function C(x){A.fromBufferAttribute(i,x),w.copy(A);const b=o[x];S.copy(b),S.sub(A.multiplyScalar(A.dot(b))).normalize(),y.crossVectors(w,b);const L=y.dot(l[x])<0?-1:1;a.setXYZW(x,S.x,S.y,S.z,L)}for(let x=0,b=M.length;x<b;++x){const W=M[x],L=W.start,G=W.count;for(let z=L,q=L+G;z<q;z+=3)C(e.getX(z+0)),C(e.getX(z+1)),C(e.getX(z+2))}}computeVertexNormals(){const e=this.index,t=this.getAttribute("position");if(t!==void 0){let n=this.getAttribute("normal");if(n===void 0)n=new Yn(new Float32Array(t.count*3),3),this.setAttribute("normal",n);else for(let h=0,d=n.count;h<d;h++)n.setXYZ(h,0,0,0);const i=new V,s=new V,a=new V,o=new V,l=new V,c=new V,u=new V,f=new V;if(e)for(let h=0,d=e.count;h<d;h+=3){const _=e.getX(h+0),g=e.getX(h+1),p=e.getX(h+2);i.fromBufferAttribute(t,_),s.fromBufferAttribute(t,g),a.fromBufferAttribute(t,p),u.subVectors(a,s),f.subVectors(i,s),u.cross(f),o.fromBufferAttribute(n,_),l.fromBufferAttribute(n,g),c.fromBufferAttribute(n,p),o.add(u),l.add(u),c.add(u),n.setXYZ(_,o.x,o.y,o.z),n.setXYZ(g,l.x,l.y,l.z),n.setXYZ(p,c.x,c.y,c.z)}else for(let h=0,d=t.count;h<d;h+=3)i.fromBufferAttribute(t,h+0),s.fromBufferAttribute(t,h+1),a.fromBufferAttribute(t,h+2),u.subVectors(a,s),f.subVectors(i,s),u.cross(f),n.setXYZ(h+0,u.x,u.y,u.z),n.setXYZ(h+1,u.x,u.y,u.z),n.setXYZ(h+2,u.x,u.y,u.z);this.normalizeNormals(),n.needsUpdate=!0}}normalizeNormals(){const e=this.attributes.normal;for(let t=0,n=e.count;t<n;t++)Ht.fromBufferAttribute(e,t),Ht.normalize(),e.setXYZ(t,Ht.x,Ht.y,Ht.z)}toNonIndexed(){function e(o,l){const c=o.array,u=o.itemSize,f=o.normalized,h=new c.constructor(l.length*u);let d=0,_=0;for(let g=0,p=l.length;g<p;g++){o.isInterleavedBufferAttribute?d=l[g]*o.data.stride+o.offset:d=l[g]*u;for(let m=0;m<u;m++)h[_++]=c[d++]}return new Yn(h,u,f)}if(this.index===null)return He("BufferGeometry.toNonIndexed(): BufferGeometry is already non-indexed."),this;const t=new ln,n=this.index.array,i=this.attributes;for(const o in i){const l=i[o],c=e(l,n);t.setAttribute(o,c)}const s=this.morphAttributes;for(const o in s){const l=[],c=s[o];for(let u=0,f=c.length;u<f;u++){const h=c[u],d=e(h,n);l.push(d)}t.morphAttributes[o]=l}t.morphTargetsRelative=this.morphTargetsRelative;const a=this.groups;for(let o=0,l=a.length;o<l;o++){const c=a[o];t.addGroup(c.start,c.count,c.materialIndex)}return t}toJSON(){const e={metadata:{version:4.7,type:"BufferGeometry",generator:"BufferGeometry.toJSON"}};if(e.uuid=this.uuid,e.type=this.type,this.name!==""&&(e.name=this.name),Object.keys(this.userData).length>0&&(e.userData=this.userData),this.parameters!==void 0){const l=this.parameters;for(const c in l)l[c]!==void 0&&(e[c]=l[c]);return e}e.data={attributes:{}};const t=this.index;t!==null&&(e.data.index={type:t.array.constructor.name,array:Array.prototype.slice.call(t.array)});const n=this.attributes;for(const l in n){const c=n[l];e.data.attributes[l]=c.toJSON(e.data)}const i={};let s=!1;for(const l in this.morphAttributes){const c=this.morphAttributes[l],u=[];for(let f=0,h=c.length;f<h;f++){const d=c[f];u.push(d.toJSON(e.data))}u.length>0&&(i[l]=u,s=!0)}s&&(e.data.morphAttributes=i,e.data.morphTargetsRelative=this.morphTargetsRelative);const a=this.groups;a.length>0&&(e.data.groups=JSON.parse(JSON.stringify(a)));const o=this.boundingSphere;return o!==null&&(e.data.boundingSphere=o.toJSON()),e}clone(){return new this.constructor().copy(this)}copy(e){this.index=null,this.attributes={},this.morphAttributes={},this.groups=[],this.boundingBox=null,this.boundingSphere=null;const t={};this.name=e.name;const n=e.index;n!==null&&this.setIndex(n.clone());const i=e.attributes;for(const c in i){const u=i[c];this.setAttribute(c,u.clone(t))}const s=e.morphAttributes;for(const c in s){const u=[],f=s[c];for(let h=0,d=f.length;h<d;h++)u.push(f[h].clone(t));this.morphAttributes[c]=u}this.morphTargetsRelative=e.morphTargetsRelative;const a=e.groups;for(let c=0,u=a.length;c<u;c++){const f=a[c];this.addGroup(f.start,f.count,f.materialIndex)}const o=e.boundingBox;o!==null&&(this.boundingBox=o.clone());const l=e.boundingSphere;return l!==null&&(this.boundingSphere=l.clone()),this.drawRange.start=e.drawRange.start,this.drawRange.count=e.drawRange.count,this.userData=e.userData,this}dispose(){this.dispatchEvent({type:"dispose"})}}let yp=0;class Er extends br{constructor(){super(),this.isMaterial=!0,Object.defineProperty(this,"id",{value:yp++}),this.uuid=qs(),this.name="",this.type="Material",this.blending=Kr,this.side=Zi,this.vertexColors=!1,this.opacity=1,this.transparent=!1,this.alphaHash=!1,this.blendSrc=tl,this.blendDst=nl,this.blendEquation=dr,this.blendSrcAlpha=null,this.blendDstAlpha=null,this.blendEquationAlpha=null,this.blendColor=new it(0,0,0),this.blendAlpha=0,this.depthFunc=es,this.depthTest=!0,this.depthWrite=!0,this.stencilWriteMask=255,this.stencilFunc=Qc,this.stencilRef=0,this.stencilFuncMask=255,this.stencilFail=Rr,this.stencilZFail=Rr,this.stencilZPass=Rr,this.stencilWrite=!1,this.clippingPlanes=null,this.clipIntersection=!1,this.clipShadows=!1,this.shadowSide=null,this.colorWrite=!0,this.precision=null,this.polygonOffset=!1,this.polygonOffsetFactor=0,this.polygonOffsetUnits=0,this.dithering=!1,this.alphaToCoverage=!1,this.premultipliedAlpha=!1,this.forceSinglePass=!1,this.allowOverride=!0,this.visible=!0,this.toneMapped=!0,this.userData={},this.version=0,this._alphaTest=0}get alphaTest(){return this._alphaTest}set alphaTest(e){this._alphaTest>0!=e>0&&this.version++,this._alphaTest=e}onBeforeRender(){}onBeforeCompile(){}customProgramCacheKey(){return this.onBeforeCompile.toString()}setValues(e){if(e!==void 0)for(const t in e){const n=e[t];if(n===void 0){He(`Material: parameter '${t}' has value of undefined.`);continue}const i=this[t];if(i===void 0){He(`Material: '${t}' is not a property of THREE.${this.type}.`);continue}i&&i.isColor?i.set(n):i&&i.isVector3&&n&&n.isVector3?i.copy(n):this[t]=n}}toJSON(e){const t=e===void 0||typeof e=="string";t&&(e={textures:{},images:{}});const n={metadata:{version:4.7,type:"Material",generator:"Material.toJSON"}};n.uuid=this.uuid,n.type=this.type,this.name!==""&&(n.name=this.name),this.color&&this.color.isColor&&(n.color=this.color.getHex()),this.roughness!==void 0&&(n.roughness=this.roughness),this.metalness!==void 0&&(n.metalness=this.metalness),this.sheen!==void 0&&(n.sheen=this.sheen),this.sheenColor&&this.sheenColor.isColor&&(n.sheenColor=this.sheenColor.getHex()),this.sheenRoughness!==void 0&&(n.sheenRoughness=this.sheenRoughness),this.emissive&&this.emissive.isColor&&(n.emissive=this.emissive.getHex()),this.emissiveIntensity!==void 0&&this.emissiveIntensity!==1&&(n.emissiveIntensity=this.emissiveIntensity),this.specular&&this.specular.isColor&&(n.specular=this.specular.getHex()),this.specularIntensity!==void 0&&(n.specularIntensity=this.specularIntensity),this.specularColor&&this.specularColor.isColor&&(n.specularColor=this.specularColor.getHex()),this.shininess!==void 0&&(n.shininess=this.shininess),this.clearcoat!==void 0&&(n.clearcoat=this.clearcoat),this.clearcoatRoughness!==void 0&&(n.clearcoatRoughness=this.clearcoatRoughness),this.clearcoatMap&&this.clearcoatMap.isTexture&&(n.clearcoatMap=this.clearcoatMap.toJSON(e).uuid),this.clearcoatRoughnessMap&&this.clearcoatRoughnessMap.isTexture&&(n.clearcoatRoughnessMap=this.clearcoatRoughnessMap.toJSON(e).uuid),this.clearcoatNormalMap&&this.clearcoatNormalMap.isTexture&&(n.clearcoatNormalMap=this.clearcoatNormalMap.toJSON(e).uuid,n.clearcoatNormalScale=this.clearcoatNormalScale.toArray()),this.sheenColorMap&&this.sheenColorMap.isTexture&&(n.sheenColorMap=this.sheenColorMap.toJSON(e).uuid),this.sheenRoughnessMap&&this.sheenRoughnessMap.isTexture&&(n.sheenRoughnessMap=this.sheenRoughnessMap.toJSON(e).uuid),this.dispersion!==void 0&&(n.dispersion=this.dispersion),this.iridescence!==void 0&&(n.iridescence=this.iridescence),this.iridescenceIOR!==void 0&&(n.iridescenceIOR=this.iridescenceIOR),this.iridescenceThicknessRange!==void 0&&(n.iridescenceThicknessRange=this.iridescenceThicknessRange),this.iridescenceMap&&this.iridescenceMap.isTexture&&(n.iridescenceMap=this.iridescenceMap.toJSON(e).uuid),this.iridescenceThicknessMap&&this.iridescenceThicknessMap.isTexture&&(n.iridescenceThicknessMap=this.iridescenceThicknessMap.toJSON(e).uuid),this.anisotropy!==void 0&&(n.anisotropy=this.anisotropy),this.anisotropyRotation!==void 0&&(n.anisotropyRotation=this.anisotropyRotation),this.anisotropyMap&&this.anisotropyMap.isTexture&&(n.anisotropyMap=this.anisotropyMap.toJSON(e).uuid),this.map&&this.map.isTexture&&(n.map=this.map.toJSON(e).uuid),this.matcap&&this.matcap.isTexture&&(n.matcap=this.matcap.toJSON(e).uuid),this.alphaMap&&this.alphaMap.isTexture&&(n.alphaMap=this.alphaMap.toJSON(e).uuid),this.lightMap&&this.lightMap.isTexture&&(n.lightMap=this.lightMap.toJSON(e).uuid,n.lightMapIntensity=this.lightMapIntensity),this.aoMap&&this.aoMap.isTexture&&(n.aoMap=this.aoMap.toJSON(e).uuid,n.aoMapIntensity=this.aoMapIntensity),this.bumpMap&&this.bumpMap.isTexture&&(n.bumpMap=this.bumpMap.toJSON(e).uuid,n.bumpScale=this.bumpScale),this.normalMap&&this.normalMap.isTexture&&(n.normalMap=this.normalMap.toJSON(e).uuid,n.normalMapType=this.normalMapType,n.normalScale=this.normalScale.toArray()),this.displacementMap&&this.displacementMap.isTexture&&(n.displacementMap=this.displacementMap.toJSON(e).uuid,n.displacementScale=this.displacementScale,n.displacementBias=this.displacementBias),this.roughnessMap&&this.roughnessMap.isTexture&&(n.roughnessMap=this.roughnessMap.toJSON(e).uuid),this.metalnessMap&&this.metalnessMap.isTexture&&(n.metalnessMap=this.metalnessMap.toJSON(e).uuid),this.emissiveMap&&this.emissiveMap.isTexture&&(n.emissiveMap=this.emissiveMap.toJSON(e).uuid),this.specularMap&&this.specularMap.isTexture&&(n.specularMap=this.specularMap.toJSON(e).uuid),this.specularIntensityMap&&this.specularIntensityMap.isTexture&&(n.specularIntensityMap=this.specularIntensityMap.toJSON(e).uuid),this.specularColorMap&&this.specularColorMap.isTexture&&(n.specularColorMap=this.specularColorMap.toJSON(e).uuid),this.envMap&&this.envMap.isTexture&&(n.envMap=this.envMap.toJSON(e).uuid,this.combine!==void 0&&(n.combine=this.combine)),this.envMapRotation!==void 0&&(n.envMapRotation=this.envMapRotation.toArray()),this.envMapIntensity!==void 0&&(n.envMapIntensity=this.envMapIntensity),this.reflectivity!==void 0&&(n.reflectivity=this.reflectivity),this.refractionRatio!==void 0&&(n.refractionRatio=this.refractionRatio),this.gradientMap&&this.gradientMap.isTexture&&(n.gradientMap=this.gradientMap.toJSON(e).uuid),this.transmission!==void 0&&(n.transmission=this.transmission),this.transmissionMap&&this.transmissionMap.isTexture&&(n.transmissionMap=this.transmissionMap.toJSON(e).uuid),this.thickness!==void 0&&(n.thickness=this.thickness),this.thicknessMap&&this.thicknessMap.isTexture&&(n.thicknessMap=this.thicknessMap.toJSON(e).uuid),this.attenuationDistance!==void 0&&this.attenuationDistance!==1/0&&(n.attenuationDistance=this.attenuationDistance),this.attenuationColor!==void 0&&(n.attenuationColor=this.attenuationColor.getHex()),this.size!==void 0&&(n.size=this.size),this.shadowSide!==null&&(n.shadowSide=this.shadowSide),this.sizeAttenuation!==void 0&&(n.sizeAttenuation=this.sizeAttenuation),this.blending!==Kr&&(n.blending=this.blending),this.side!==Zi&&(n.side=this.side),this.vertexColors===!0&&(n.vertexColors=!0),this.opacity<1&&(n.opacity=this.opacity),this.transparent===!0&&(n.transparent=!0),this.blendSrc!==tl&&(n.blendSrc=this.blendSrc),this.blendDst!==nl&&(n.blendDst=this.blendDst),this.blendEquation!==dr&&(n.blendEquation=this.blendEquation),this.blendSrcAlpha!==null&&(n.blendSrcAlpha=this.blendSrcAlpha),this.blendDstAlpha!==null&&(n.blendDstAlpha=this.blendDstAlpha),this.blendEquationAlpha!==null&&(n.blendEquationAlpha=this.blendEquationAlpha),this.blendColor&&this.blendColor.isColor&&(n.blendColor=this.blendColor.getHex()),this.blendAlpha!==0&&(n.blendAlpha=this.blendAlpha),this.depthFunc!==es&&(n.depthFunc=this.depthFunc),this.depthTest===!1&&(n.depthTest=this.depthTest),this.depthWrite===!1&&(n.depthWrite=this.depthWrite),this.colorWrite===!1&&(n.colorWrite=this.colorWrite),this.stencilWriteMask!==255&&(n.stencilWriteMask=this.stencilWriteMask),this.stencilFunc!==Qc&&(n.stencilFunc=this.stencilFunc),this.stencilRef!==0&&(n.stencilRef=this.stencilRef),this.stencilFuncMask!==255&&(n.stencilFuncMask=this.stencilFuncMask),this.stencilFail!==Rr&&(n.stencilFail=this.stencilFail),this.stencilZFail!==Rr&&(n.stencilZFail=this.stencilZFail),this.stencilZPass!==Rr&&(n.stencilZPass=this.stencilZPass),this.stencilWrite===!0&&(n.stencilWrite=this.stencilWrite),this.rotation!==void 0&&this.rotation!==0&&(n.rotation=this.rotation),this.polygonOffset===!0&&(n.polygonOffset=!0),this.polygonOffsetFactor!==0&&(n.polygonOffsetFactor=this.polygonOffsetFactor),this.polygonOffsetUnits!==0&&(n.polygonOffsetUnits=this.polygonOffsetUnits),this.linewidth!==void 0&&this.linewidth!==1&&(n.linewidth=this.linewidth),this.dashSize!==void 0&&(n.dashSize=this.dashSize),this.gapSize!==void 0&&(n.gapSize=this.gapSize),this.scale!==void 0&&(n.scale=this.scale),this.dithering===!0&&(n.dithering=!0),this.alphaTest>0&&(n.alphaTest=this.alphaTest),this.alphaHash===!0&&(n.alphaHash=!0),this.alphaToCoverage===!0&&(n.alphaToCoverage=!0),this.premultipliedAlpha===!0&&(n.premultipliedAlpha=!0),this.forceSinglePass===!0&&(n.forceSinglePass=!0),this.allowOverride===!1&&(n.allowOverride=!1),this.wireframe===!0&&(n.wireframe=!0),this.wireframeLinewidth>1&&(n.wireframeLinewidth=this.wireframeLinewidth),this.wireframeLinecap!=="round"&&(n.wireframeLinecap=this.wireframeLinecap),this.wireframeLinejoin!=="round"&&(n.wireframeLinejoin=this.wireframeLinejoin),this.flatShading===!0&&(n.flatShading=!0),this.visible===!1&&(n.visible=!1),this.toneMapped===!1&&(n.toneMapped=!1),this.fog===!1&&(n.fog=!1),Object.keys(this.userData).length>0&&(n.userData=this.userData);function i(s){const a=[];for(const o in s){const l=s[o];delete l.metadata,a.push(l)}return a}if(t){const s=i(e.textures),a=i(e.images);s.length>0&&(n.textures=s),a.length>0&&(n.images=a)}return n}clone(){return new this.constructor().copy(this)}copy(e){this.name=e.name,this.blending=e.blending,this.side=e.side,this.vertexColors=e.vertexColors,this.opacity=e.opacity,this.transparent=e.transparent,this.blendSrc=e.blendSrc,this.blendDst=e.blendDst,this.blendEquation=e.blendEquation,this.blendSrcAlpha=e.blendSrcAlpha,this.blendDstAlpha=e.blendDstAlpha,this.blendEquationAlpha=e.blendEquationAlpha,this.blendColor.copy(e.blendColor),this.blendAlpha=e.blendAlpha,this.depthFunc=e.depthFunc,this.depthTest=e.depthTest,this.depthWrite=e.depthWrite,this.stencilWriteMask=e.stencilWriteMask,this.stencilFunc=e.stencilFunc,this.stencilRef=e.stencilRef,this.stencilFuncMask=e.stencilFuncMask,this.stencilFail=e.stencilFail,this.stencilZFail=e.stencilZFail,this.stencilZPass=e.stencilZPass,this.stencilWrite=e.stencilWrite;const t=e.clippingPlanes;let n=null;if(t!==null){const i=t.length;n=new Array(i);for(let s=0;s!==i;++s)n[s]=t[s].clone()}return this.clippingPlanes=n,this.clipIntersection=e.clipIntersection,this.clipShadows=e.clipShadows,this.shadowSide=e.shadowSide,this.colorWrite=e.colorWrite,this.precision=e.precision,this.polygonOffset=e.polygonOffset,this.polygonOffsetFactor=e.polygonOffsetFactor,this.polygonOffsetUnits=e.polygonOffsetUnits,this.dithering=e.dithering,this.alphaTest=e.alphaTest,this.alphaHash=e.alphaHash,this.alphaToCoverage=e.alphaToCoverage,this.premultipliedAlpha=e.premultipliedAlpha,this.forceSinglePass=e.forceSinglePass,this.allowOverride=e.allowOverride,this.visible=e.visible,this.toneMapped=e.toneMapped,this.userData=JSON.parse(JSON.stringify(e.userData)),this}dispose(){this.dispatchEvent({type:"dispose"})}set needsUpdate(e){e===!0&&this.version++}}const vi=new V,Do=new V,la=new V,Fi=new V,Lo=new V,ca=new V,Io=new V;class $s{constructor(e=new V,t=new V(0,0,-1)){this.origin=e,this.direction=t}set(e,t){return this.origin.copy(e),this.direction.copy(t),this}copy(e){return this.origin.copy(e.origin),this.direction.copy(e.direction),this}at(e,t){return t.copy(this.origin).addScaledVector(this.direction,e)}lookAt(e){return this.direction.copy(e).sub(this.origin).normalize(),this}recast(e){return this.origin.copy(this.at(e,vi)),this}closestPointToPoint(e,t){t.subVectors(e,this.origin);const n=t.dot(this.direction);return n<0?t.copy(this.origin):t.copy(this.origin).addScaledVector(this.direction,n)}distanceToPoint(e){return Math.sqrt(this.distanceSqToPoint(e))}distanceSqToPoint(e){const t=vi.subVectors(e,this.origin).dot(this.direction);return t<0?this.origin.distanceToSquared(e):(vi.copy(this.origin).addScaledVector(this.direction,t),vi.distanceToSquared(e))}distanceSqToSegment(e,t,n,i){Do.copy(e).add(t).multiplyScalar(.5),la.copy(t).sub(e).normalize(),Fi.copy(this.origin).sub(Do);const s=e.distanceTo(t)*.5,a=-this.direction.dot(la),o=Fi.dot(this.direction),l=-Fi.dot(la),c=Fi.lengthSq(),u=Math.abs(1-a*a);let f,h,d,_;if(u>0)if(f=a*l-o,h=a*o-l,_=s*u,f>=0)if(h>=-_)if(h<=_){const g=1/u;f*=g,h*=g,d=f*(f+a*h+2*o)+h*(a*f+h+2*l)+c}else h=s,f=Math.max(0,-(a*h+o)),d=-f*f+h*(h+2*l)+c;else h=-s,f=Math.max(0,-(a*h+o)),d=-f*f+h*(h+2*l)+c;else h<=-_?(f=Math.max(0,-(-a*s+o)),h=f>0?-s:Math.min(Math.max(-s,-l),s),d=-f*f+h*(h+2*l)+c):h<=_?(f=0,h=Math.min(Math.max(-s,-l),s),d=h*(h+2*l)+c):(f=Math.max(0,-(a*s+o)),h=f>0?s:Math.min(Math.max(-s,-l),s),d=-f*f+h*(h+2*l)+c);else h=a>0?-s:s,f=Math.max(0,-(a*h+o)),d=-f*f+h*(h+2*l)+c;return n&&n.copy(this.origin).addScaledVector(this.direction,f),i&&i.copy(Do).addScaledVector(la,h),d}intersectSphere(e,t){vi.subVectors(e.center,this.origin);const n=vi.dot(this.direction),i=vi.dot(vi)-n*n,s=e.radius*e.radius;if(i>s)return null;const a=Math.sqrt(s-i),o=n-a,l=n+a;return l<0?null:o<0?this.at(l,t):this.at(o,t)}intersectsSphere(e){return e.radius<0?!1:this.distanceSqToPoint(e.center)<=e.radius*e.radius}distanceToPlane(e){const t=e.normal.dot(this.direction);if(t===0)return e.distanceToPoint(this.origin)===0?0:null;const n=-(this.origin.dot(e.normal)+e.constant)/t;return n>=0?n:null}intersectPlane(e,t){const n=this.distanceToPlane(e);return n===null?null:this.at(n,t)}intersectsPlane(e){const t=e.distanceToPoint(this.origin);return t===0||e.normal.dot(this.direction)*t<0}intersectBox(e,t){let n,i,s,a,o,l;const c=1/this.direction.x,u=1/this.direction.y,f=1/this.direction.z,h=this.origin;return c>=0?(n=(e.min.x-h.x)*c,i=(e.max.x-h.x)*c):(n=(e.max.x-h.x)*c,i=(e.min.x-h.x)*c),u>=0?(s=(e.min.y-h.y)*u,a=(e.max.y-h.y)*u):(s=(e.max.y-h.y)*u,a=(e.min.y-h.y)*u),n>a||s>i||((s>n||isNaN(n))&&(n=s),(a<i||isNaN(i))&&(i=a),f>=0?(o=(e.min.z-h.z)*f,l=(e.max.z-h.z)*f):(o=(e.max.z-h.z)*f,l=(e.min.z-h.z)*f),n>l||o>i)||((o>n||n!==n)&&(n=o),(l<i||i!==i)&&(i=l),i<0)?null:this.at(n>=0?n:i,t)}intersectsBox(e){return this.intersectBox(e,vi)!==null}intersectTriangle(e,t,n,i,s){Lo.subVectors(t,e),ca.subVectors(n,e),Io.crossVectors(Lo,ca);let a=this.direction.dot(Io),o;if(a>0){if(i)return null;o=1}else if(a<0)o=-1,a=-a;else return null;Fi.subVectors(this.origin,e);const l=o*this.direction.dot(ca.crossVectors(Fi,ca));if(l<0)return null;const c=o*this.direction.dot(Lo.cross(Fi));if(c<0||l+c>a)return null;const u=-o*Fi.dot(Io);return u<0?null:this.at(u/a,s)}applyMatrix4(e){return this.origin.applyMatrix4(e),this.direction.transformDirection(e),this}equals(e){return e.origin.equals(this.origin)&&e.direction.equals(this.direction)}clone(){return new this.constructor().copy(this)}}class Wa extends Er{constructor(e){super(),this.isMeshBasicMaterial=!0,this.type="MeshBasicMaterial",this.color=new it(16777215),this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.specularMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new li,this.combine=Ch,this.reflectivity=1,this.refractionRatio=.98,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.lightMap=e.lightMap,this.lightMapIntensity=e.lightMapIntensity,this.aoMap=e.aoMap,this.aoMapIntensity=e.aoMapIntensity,this.specularMap=e.specularMap,this.alphaMap=e.alphaMap,this.envMap=e.envMap,this.envMapRotation.copy(e.envMapRotation),this.combine=e.combine,this.reflectivity=e.reflectivity,this.refractionRatio=e.refractionRatio,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.wireframeLinecap=e.wireframeLinecap,this.wireframeLinejoin=e.wireframeLinejoin,this.fog=e.fog,this}}const _u=new St,ar=new $s,ua=new Ys,gu=new V,ha=new V,fa=new V,da=new V,Uo=new V,pa=new V,vu=new V,ma=new V;class Xt extends kt{constructor(e=new ln,t=new Wa){super(),this.isMesh=!0,this.type="Mesh",this.geometry=e,this.material=t,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.count=1,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),e.morphTargetInfluences!==void 0&&(this.morphTargetInfluences=e.morphTargetInfluences.slice()),e.morphTargetDictionary!==void 0&&(this.morphTargetDictionary=Object.assign({},e.morphTargetDictionary)),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const i=t[n[0]];if(i!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=i.length;s<a;s++){const o=i[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}getVertexPosition(e,t){const n=this.geometry,i=n.attributes.position,s=n.morphAttributes.position,a=n.morphTargetsRelative;t.fromBufferAttribute(i,e);const o=this.morphTargetInfluences;if(s&&o){pa.set(0,0,0);for(let l=0,c=s.length;l<c;l++){const u=o[l],f=s[l];u!==0&&(Uo.fromBufferAttribute(f,e),a?pa.addScaledVector(Uo,u):pa.addScaledVector(Uo.sub(t),u))}t.add(pa)}return t}raycast(e,t){const n=this.geometry,i=this.material,s=this.matrixWorld;i!==void 0&&(n.boundingSphere===null&&n.computeBoundingSphere(),ua.copy(n.boundingSphere),ua.applyMatrix4(s),ar.copy(e.ray).recast(e.near),!(ua.containsPoint(ar.origin)===!1&&(ar.intersectSphere(ua,gu)===null||ar.origin.distanceToSquared(gu)>(e.far-e.near)**2))&&(_u.copy(s).invert(),ar.copy(e.ray).applyMatrix4(_u),!(n.boundingBox!==null&&ar.intersectsBox(n.boundingBox)===!1)&&this._computeIntersections(e,t,ar)))}_computeIntersections(e,t,n){let i;const s=this.geometry,a=this.material,o=s.index,l=s.attributes.position,c=s.attributes.uv,u=s.attributes.uv1,f=s.attributes.normal,h=s.groups,d=s.drawRange;if(o!==null)if(Array.isArray(a))for(let _=0,g=h.length;_<g;_++){const p=h[_],m=a[p.materialIndex],M=Math.max(p.start,d.start),S=Math.min(o.count,Math.min(p.start+p.count,d.start+d.count));for(let y=M,A=S;y<A;y+=3){const w=o.getX(y),C=o.getX(y+1),x=o.getX(y+2);i=_a(this,m,e,n,c,u,f,w,C,x),i&&(i.faceIndex=Math.floor(y/3),i.face.materialIndex=p.materialIndex,t.push(i))}}else{const _=Math.max(0,d.start),g=Math.min(o.count,d.start+d.count);for(let p=_,m=g;p<m;p+=3){const M=o.getX(p),S=o.getX(p+1),y=o.getX(p+2);i=_a(this,a,e,n,c,u,f,M,S,y),i&&(i.faceIndex=Math.floor(p/3),t.push(i))}}else if(l!==void 0)if(Array.isArray(a))for(let _=0,g=h.length;_<g;_++){const p=h[_],m=a[p.materialIndex],M=Math.max(p.start,d.start),S=Math.min(l.count,Math.min(p.start+p.count,d.start+d.count));for(let y=M,A=S;y<A;y+=3){const w=y,C=y+1,x=y+2;i=_a(this,m,e,n,c,u,f,w,C,x),i&&(i.faceIndex=Math.floor(y/3),i.face.materialIndex=p.materialIndex,t.push(i))}}else{const _=Math.max(0,d.start),g=Math.min(l.count,d.start+d.count);for(let p=_,m=g;p<m;p+=3){const M=p,S=p+1,y=p+2;i=_a(this,a,e,n,c,u,f,M,S,y),i&&(i.faceIndex=Math.floor(p/3),t.push(i))}}}}function bp(r,e,t,n,i,s,a,o){let l;if(e.side===mn?l=n.intersectTriangle(a,s,i,!0,o):l=n.intersectTriangle(i,s,a,e.side===Zi,o),l===null)return null;ma.copy(o),ma.applyMatrix4(r.matrixWorld);const c=t.ray.origin.distanceTo(ma);return c<t.near||c>t.far?null:{distance:c,point:ma.clone(),object:r}}function _a(r,e,t,n,i,s,a,o,l,c){r.getVertexPosition(o,ha),r.getVertexPosition(l,fa),r.getVertexPosition(c,da);const u=bp(r,e,t,n,ha,fa,da,vu);if(u){const f=new V;kn.getBarycoord(vu,ha,fa,da,f),i&&(u.uv=kn.getInterpolatedAttribute(i,o,l,c,f,new qe)),s&&(u.uv1=kn.getInterpolatedAttribute(s,o,l,c,f,new qe)),a&&(u.normal=kn.getInterpolatedAttribute(a,o,l,c,f,new V),u.normal.dot(n.direction)>0&&u.normal.multiplyScalar(-1));const h={a:o,b:l,c,normal:new V,materialIndex:0};kn.getNormal(ha,fa,da,h.normal),u.face=h,u.barycoord=f}return u}class Ep extends on{constructor(e=null,t=1,n=1,i,s,a,o,l,c=qt,u=qt,f,h){super(null,a,o,l,c,u,i,s,f,h),this.isDataTexture=!0,this.image={data:e,width:t,height:n},this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}const No=new V,Tp=new V,Ap=new Ke;class Vi{constructor(e=new V(1,0,0),t=0){this.isPlane=!0,this.normal=e,this.constant=t}set(e,t){return this.normal.copy(e),this.constant=t,this}setComponents(e,t,n,i){return this.normal.set(e,t,n),this.constant=i,this}setFromNormalAndCoplanarPoint(e,t){return this.normal.copy(e),this.constant=-t.dot(this.normal),this}setFromCoplanarPoints(e,t,n){const i=No.subVectors(n,t).cross(Tp.subVectors(e,t)).normalize();return this.setFromNormalAndCoplanarPoint(i,e),this}copy(e){return this.normal.copy(e.normal),this.constant=e.constant,this}normalize(){const e=1/this.normal.length();return this.normal.multiplyScalar(e),this.constant*=e,this}negate(){return this.constant*=-1,this.normal.negate(),this}distanceToPoint(e){return this.normal.dot(e)+this.constant}distanceToSphere(e){return this.distanceToPoint(e.center)-e.radius}projectPoint(e,t){return t.copy(e).addScaledVector(this.normal,-this.distanceToPoint(e))}intersectLine(e,t){const n=e.delta(No),i=this.normal.dot(n);if(i===0)return this.distanceToPoint(e.start)===0?t.copy(e.start):null;const s=-(e.start.dot(this.normal)+this.constant)/i;return s<0||s>1?null:t.copy(e.start).addScaledVector(n,s)}intersectsLine(e){const t=this.distanceToPoint(e.start),n=this.distanceToPoint(e.end);return t<0&&n>0||n<0&&t>0}intersectsBox(e){return e.intersectsPlane(this)}intersectsSphere(e){return e.intersectsPlane(this)}coplanarPoint(e){return e.copy(this.normal).multiplyScalar(-this.constant)}applyMatrix4(e,t){const n=t||Ap.getNormalMatrix(e),i=this.coplanarPoint(No).applyMatrix4(e),s=this.normal.applyMatrix3(n).normalize();return this.constant=-i.dot(s),this}translate(e){return this.constant-=e.dot(this.normal),this}equals(e){return e.normal.equals(this.normal)&&e.constant===this.constant}clone(){return new this.constructor().copy(this)}}const or=new Ys,wp=new qe(.5,.5),ga=new V;class Tc{constructor(e=new Vi,t=new Vi,n=new Vi,i=new Vi,s=new Vi,a=new Vi){this.planes=[e,t,n,i,s,a]}set(e,t,n,i,s,a){const o=this.planes;return o[0].copy(e),o[1].copy(t),o[2].copy(n),o[3].copy(i),o[4].copy(s),o[5].copy(a),this}copy(e){const t=this.planes;for(let n=0;n<6;n++)t[n].copy(e.planes[n]);return this}setFromProjectionMatrix(e,t=ii,n=!1){const i=this.planes,s=e.elements,a=s[0],o=s[1],l=s[2],c=s[3],u=s[4],f=s[5],h=s[6],d=s[7],_=s[8],g=s[9],p=s[10],m=s[11],M=s[12],S=s[13],y=s[14],A=s[15];if(i[0].setComponents(c-a,d-u,m-_,A-M).normalize(),i[1].setComponents(c+a,d+u,m+_,A+M).normalize(),i[2].setComponents(c+o,d+f,m+g,A+S).normalize(),i[3].setComponents(c-o,d-f,m-g,A-S).normalize(),n)i[4].setComponents(l,h,p,y).normalize(),i[5].setComponents(c-l,d-h,m-p,A-y).normalize();else if(i[4].setComponents(c-l,d-h,m-p,A-y).normalize(),t===ii)i[5].setComponents(c+l,d+h,m+p,A+y).normalize();else if(t===Os)i[5].setComponents(l,h,p,y).normalize();else throw new Error("THREE.Frustum.setFromProjectionMatrix(): Invalid coordinate system: "+t);return this}intersectsObject(e){if(e.boundingSphere!==void 0)e.boundingSphere===null&&e.computeBoundingSphere(),or.copy(e.boundingSphere).applyMatrix4(e.matrixWorld);else{const t=e.geometry;t.boundingSphere===null&&t.computeBoundingSphere(),or.copy(t.boundingSphere).applyMatrix4(e.matrixWorld)}return this.intersectsSphere(or)}intersectsSprite(e){or.center.set(0,0,0);const t=wp.distanceTo(e.center);return or.radius=.7071067811865476+t,or.applyMatrix4(e.matrixWorld),this.intersectsSphere(or)}intersectsSphere(e){const t=this.planes,n=e.center,i=-e.radius;for(let s=0;s<6;s++)if(t[s].distanceToPoint(n)<i)return!1;return!0}intersectsBox(e){const t=this.planes;for(let n=0;n<6;n++){const i=t[n];if(ga.x=i.normal.x>0?e.max.x:e.min.x,ga.y=i.normal.y>0?e.max.y:e.min.y,ga.z=i.normal.z>0?e.max.z:e.min.z,i.distanceToPoint(ga)<0)return!1}return!0}containsPoint(e){const t=this.planes;for(let n=0;n<6;n++)if(t[n].distanceToPoint(e)<0)return!1;return!0}clone(){return new this.constructor().copy(this)}}class Ds extends Er{constructor(e){super(),this.isLineBasicMaterial=!0,this.type="LineBasicMaterial",this.color=new it(16777215),this.map=null,this.linewidth=1,this.linecap="round",this.linejoin="round",this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.linewidth=e.linewidth,this.linecap=e.linecap,this.linejoin=e.linejoin,this.fog=e.fog,this}}const Xa=new V,qa=new V,xu=new St,ys=new $s,va=new Ys,Fo=new V,Mu=new V;class Rp extends kt{constructor(e=new ln,t=new Ds){super(),this.isLine=!0,this.type="Line",this.geometry=e,this.material=t,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}computeLineDistances(){const e=this.geometry;if(e.index===null){const t=e.attributes.position,n=[0];for(let i=1,s=t.count;i<s;i++)Xa.fromBufferAttribute(t,i-1),qa.fromBufferAttribute(t,i),n[i]=n[i-1],n[i]+=Xa.distanceTo(qa);e.setAttribute("lineDistance",new Yt(n,1))}else He("Line.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}raycast(e,t){const n=this.geometry,i=this.matrixWorld,s=e.params.Line.threshold,a=n.drawRange;if(n.boundingSphere===null&&n.computeBoundingSphere(),va.copy(n.boundingSphere),va.applyMatrix4(i),va.radius+=s,e.ray.intersectsSphere(va)===!1)return;xu.copy(i).invert(),ys.copy(e.ray).applyMatrix4(xu);const o=s/((this.scale.x+this.scale.y+this.scale.z)/3),l=o*o,c=this.isLineSegments?2:1,u=n.index,h=n.attributes.position;if(u!==null){const d=Math.max(0,a.start),_=Math.min(u.count,a.start+a.count);for(let g=d,p=_-1;g<p;g+=c){const m=u.getX(g),M=u.getX(g+1),S=xa(this,e,ys,l,m,M,g);S&&t.push(S)}if(this.isLineLoop){const g=u.getX(_-1),p=u.getX(d),m=xa(this,e,ys,l,g,p,_-1);m&&t.push(m)}}else{const d=Math.max(0,a.start),_=Math.min(h.count,a.start+a.count);for(let g=d,p=_-1;g<p;g+=c){const m=xa(this,e,ys,l,g,g+1,g);m&&t.push(m)}if(this.isLineLoop){const g=xa(this,e,ys,l,_-1,d,_-1);g&&t.push(g)}}}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const i=t[n[0]];if(i!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=i.length;s<a;s++){const o=i[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}}function xa(r,e,t,n,i,s,a){const o=r.geometry.attributes.position;if(Xa.fromBufferAttribute(o,i),qa.fromBufferAttribute(o,s),t.distanceSqToSegment(Xa,qa,Fo,Mu)>n)return;Fo.applyMatrix4(r.matrixWorld);const c=e.ray.origin.distanceTo(Fo);if(!(c<e.near||c>e.far))return{distance:c,point:Mu.clone().applyMatrix4(r.matrixWorld),index:a,face:null,faceIndex:null,barycoord:null,object:r}}const Su=new V,yu=new V;class Fa extends Rp{constructor(e,t){super(e,t),this.isLineSegments=!0,this.type="LineSegments"}computeLineDistances(){const e=this.geometry;if(e.index===null){const t=e.attributes.position,n=[];for(let i=0,s=t.count;i<s;i+=2)Su.fromBufferAttribute(t,i),yu.fromBufferAttribute(t,i+1),n[i]=i===0?0:n[i-1],n[i+1]=n[i]+Su.distanceTo(yu);e.setAttribute("lineDistance",new Yt(n,1))}else He("LineSegments.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}}class Zh extends Er{constructor(e){super(),this.isPointsMaterial=!0,this.type="PointsMaterial",this.color=new it(16777215),this.map=null,this.alphaMap=null,this.size=1,this.sizeAttenuation=!0,this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.alphaMap=e.alphaMap,this.size=e.size,this.sizeAttenuation=e.sizeAttenuation,this.fog=e.fog,this}}const bu=new St,Xl=new $s,Ma=new Ys,Sa=new V;class Cp extends kt{constructor(e=new ln,t=new Zh){super(),this.isPoints=!0,this.type="Points",this.geometry=e,this.material=t,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}raycast(e,t){const n=this.geometry,i=this.matrixWorld,s=e.params.Points.threshold,a=n.drawRange;if(n.boundingSphere===null&&n.computeBoundingSphere(),Ma.copy(n.boundingSphere),Ma.applyMatrix4(i),Ma.radius+=s,e.ray.intersectsSphere(Ma)===!1)return;bu.copy(i).invert(),Xl.copy(e.ray).applyMatrix4(bu);const o=s/((this.scale.x+this.scale.y+this.scale.z)/3),l=o*o,c=n.index,f=n.attributes.position;if(c!==null){const h=Math.max(0,a.start),d=Math.min(c.count,a.start+a.count);for(let _=h,g=d;_<g;_++){const p=c.getX(_);Sa.fromBufferAttribute(f,p),Eu(Sa,p,l,i,e,t,this)}}else{const h=Math.max(0,a.start),d=Math.min(f.count,a.start+a.count);for(let _=h,g=d;_<g;_++)Sa.fromBufferAttribute(f,_),Eu(Sa,_,l,i,e,t,this)}}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const i=t[n[0]];if(i!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=i.length;s<a;s++){const o=i[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}}function Eu(r,e,t,n,i,s,a){const o=Xl.distanceSqToPoint(r);if(o<t){const l=new V;Xl.closestPointToPoint(r,l),l.applyMatrix4(n);const c=i.ray.origin.distanceTo(l);if(c<i.near||c>i.far)return;s.push({distance:c,distanceToRay:Math.sqrt(o),point:l,index:e,face:null,faceIndex:null,barycoord:null,object:a})}}class jh extends on{constructor(e=[],t=yr,n,i,s,a,o,l,c,u){super(e,t,n,i,s,a,o,l,c,u),this.isCubeTexture=!0,this.flipY=!1}get images(){return this.image}set images(e){this.image=e}}class Bs extends on{constructor(e,t,n=oi,i,s,a,o=qt,l=qt,c,u=Ai,f=1){if(u!==Ai&&u!==_r)throw new Error("DepthTexture format must be either THREE.DepthFormat or THREE.DepthStencilFormat");const h={width:e,height:t,depth:f};super(h,i,s,a,o,l,u,n,c),this.isDepthTexture=!0,this.flipY=!1,this.generateMipmaps=!1,this.compareFunction=null}copy(e){return super.copy(e),this.source=new yc(Object.assign({},e.image)),this.compareFunction=e.compareFunction,this}toJSON(e){const t=super.toJSON(e);return this.compareFunction!==null&&(t.compareFunction=this.compareFunction),t}}class Pp extends Bs{constructor(e,t=oi,n=yr,i,s,a=qt,o=qt,l,c=Ai){const u={width:e,height:e,depth:1},f=[u,u,u,u,u,u];super(e,e,t,n,i,s,a,o,l,c),this.image=f,this.isCubeDepthTexture=!0,this.isCubeTexture=!0}get images(){return this.image}set images(e){this.image=e}}class Jh extends on{constructor(e=null){super(),this.sourceTexture=e,this.isExternalTexture=!0}copy(e){return super.copy(e),this.sourceTexture=e.sourceTexture,this}}class pn extends ln{constructor(e=1,t=1,n=1,i=1,s=1,a=1){super(),this.type="BoxGeometry",this.parameters={width:e,height:t,depth:n,widthSegments:i,heightSegments:s,depthSegments:a};const o=this;i=Math.floor(i),s=Math.floor(s),a=Math.floor(a);const l=[],c=[],u=[],f=[];let h=0,d=0;_("z","y","x",-1,-1,n,t,e,a,s,0),_("z","y","x",1,-1,n,t,-e,a,s,1),_("x","z","y",1,1,e,n,t,i,a,2),_("x","z","y",1,-1,e,n,-t,i,a,3),_("x","y","z",1,-1,e,t,n,i,s,4),_("x","y","z",-1,-1,e,t,-n,i,s,5),this.setIndex(l),this.setAttribute("position",new Yt(c,3)),this.setAttribute("normal",new Yt(u,3)),this.setAttribute("uv",new Yt(f,2));function _(g,p,m,M,S,y,A,w,C,x,b){const W=y/C,L=A/x,G=y/2,z=A/2,q=w/2,H=C+1,N=x+1;let F=0,ne=0;const ie=new V;for(let me=0;me<N;me++){const Te=me*L-z;for(let le=0;le<H;le++){const Oe=le*W-G;ie[g]=Oe*M,ie[p]=Te*S,ie[m]=q,c.push(ie.x,ie.y,ie.z),ie[g]=0,ie[p]=0,ie[m]=w>0?1:-1,u.push(ie.x,ie.y,ie.z),f.push(le/C),f.push(1-me/x),F+=1}}for(let me=0;me<x;me++)for(let Te=0;Te<C;Te++){const le=h+Te+H*me,Oe=h+Te+H*(me+1),Je=h+(Te+1)+H*(me+1),$e=h+(Te+1)+H*me;l.push(le,Oe,$e),l.push(Oe,Je,$e),ne+=6}o.addGroup(d,ne,b),d+=ne,h+=F}}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new pn(e.width,e.height,e.depth,e.widthSegments,e.heightSegments,e.depthSegments)}}const ya=new V,ba=new V,Oo=new V,Ea=new kn;class Bo extends ln{constructor(e=null,t=1){if(super(),this.type="EdgesGeometry",this.parameters={geometry:e,thresholdAngle:t},e!==null){const i=Math.pow(10,4),s=Math.cos(Ps*t),a=e.getIndex(),o=e.getAttribute("position"),l=a?a.count:o.count,c=[0,0,0],u=["a","b","c"],f=new Array(3),h={},d=[];for(let _=0;_<l;_+=3){a?(c[0]=a.getX(_),c[1]=a.getX(_+1),c[2]=a.getX(_+2)):(c[0]=_,c[1]=_+1,c[2]=_+2);const{a:g,b:p,c:m}=Ea;if(g.fromBufferAttribute(o,c[0]),p.fromBufferAttribute(o,c[1]),m.fromBufferAttribute(o,c[2]),Ea.getNormal(Oo),f[0]=`${Math.round(g.x*i)},${Math.round(g.y*i)},${Math.round(g.z*i)}`,f[1]=`${Math.round(p.x*i)},${Math.round(p.y*i)},${Math.round(p.z*i)}`,f[2]=`${Math.round(m.x*i)},${Math.round(m.y*i)},${Math.round(m.z*i)}`,!(f[0]===f[1]||f[1]===f[2]||f[2]===f[0]))for(let M=0;M<3;M++){const S=(M+1)%3,y=f[M],A=f[S],w=Ea[u[M]],C=Ea[u[S]],x=`${y}_${A}`,b=`${A}_${y}`;b in h&&h[b]?(Oo.dot(h[b].normal)<=s&&(d.push(w.x,w.y,w.z),d.push(C.x,C.y,C.z)),h[b]=null):x in h||(h[x]={index0:c[M],index1:c[S],normal:Oo.clone()})}}for(const _ in h)if(h[_]){const{index0:g,index1:p}=h[_];ya.fromBufferAttribute(o,g),ba.fromBufferAttribute(o,p),d.push(ya.x,ya.y,ya.z),d.push(ba.x,ba.y,ba.z)}this.setAttribute("position",new Yt(d,3))}}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}}class Ks extends ln{constructor(e=1,t=1,n=1,i=1){super(),this.type="PlaneGeometry",this.parameters={width:e,height:t,widthSegments:n,heightSegments:i};const s=e/2,a=t/2,o=Math.floor(n),l=Math.floor(i),c=o+1,u=l+1,f=e/o,h=t/l,d=[],_=[],g=[],p=[];for(let m=0;m<u;m++){const M=m*h-a;for(let S=0;S<c;S++){const y=S*f-s;_.push(y,-M,0),g.push(0,0,1),p.push(S/o),p.push(1-m/l)}}for(let m=0;m<l;m++)for(let M=0;M<o;M++){const S=M+c*m,y=M+c*(m+1),A=M+1+c*(m+1),w=M+1+c*m;d.push(S,y,w),d.push(y,A,w)}this.setIndex(d),this.setAttribute("position",new Yt(_,3)),this.setAttribute("normal",new Yt(g,3)),this.setAttribute("uv",new Yt(p,2))}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new Ks(e.width,e.height,e.widthSegments,e.heightSegments)}}class Ac extends ln{constructor(e=.5,t=1,n=32,i=1,s=0,a=Math.PI*2){super(),this.type="RingGeometry",this.parameters={innerRadius:e,outerRadius:t,thetaSegments:n,phiSegments:i,thetaStart:s,thetaLength:a},n=Math.max(3,n),i=Math.max(1,i);const o=[],l=[],c=[],u=[];let f=e;const h=(t-e)/i,d=new V,_=new qe;for(let g=0;g<=i;g++){for(let p=0;p<=n;p++){const m=s+p/n*a;d.x=f*Math.cos(m),d.y=f*Math.sin(m),l.push(d.x,d.y,d.z),c.push(0,0,1),_.x=(d.x/t+1)/2,_.y=(d.y/t+1)/2,u.push(_.x,_.y)}f+=h}for(let g=0;g<i;g++){const p=g*(n+1);for(let m=0;m<n;m++){const M=m+p,S=M,y=M+n+1,A=M+n+2,w=M+1;o.push(S,y,w),o.push(y,A,w)}}this.setIndex(o),this.setAttribute("position",new Yt(l,3)),this.setAttribute("normal",new Yt(c,3)),this.setAttribute("uv",new Yt(u,2))}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new Ac(e.innerRadius,e.outerRadius,e.thetaSegments,e.phiSegments,e.thetaStart,e.thetaLength)}}function rs(r){const e={};for(const t in r){e[t]={};for(const n in r[t]){const i=r[t][n];i&&(i.isColor||i.isMatrix3||i.isMatrix4||i.isVector2||i.isVector3||i.isVector4||i.isTexture||i.isQuaternion)?i.isRenderTargetTexture?(He("UniformsUtils: Textures of render targets cannot be cloned via cloneUniforms() or mergeUniforms()."),e[t][n]=null):e[t][n]=i.clone():Array.isArray(i)?e[t][n]=i.slice():e[t][n]=i}}return e}function sn(r){const e={};for(let t=0;t<r.length;t++){const n=rs(r[t]);for(const i in n)e[i]=n[i]}return e}function Dp(r){const e=[];for(let t=0;t<r.length;t++)e.push(r[t].clone());return e}function Qh(r){const e=r.getRenderTarget();return e===null?r.outputColorSpace:e.isXRRenderTarget===!0?e.texture.colorSpace:ht.workingColorSpace}const Lp={clone:rs,merge:sn};var Ip=`void main() {
	gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
}`,Up=`void main() {
	gl_FragColor = vec4( 1.0, 0.0, 0.0, 1.0 );
}`;class ci extends Er{constructor(e){super(),this.isShaderMaterial=!0,this.type="ShaderMaterial",this.defines={},this.uniforms={},this.uniformsGroups=[],this.vertexShader=Ip,this.fragmentShader=Up,this.linewidth=1,this.wireframe=!1,this.wireframeLinewidth=1,this.fog=!1,this.lights=!1,this.clipping=!1,this.forceSinglePass=!0,this.extensions={clipCullDistance:!1,multiDraw:!1},this.defaultAttributeValues={color:[1,1,1],uv:[0,0],uv1:[0,0]},this.index0AttributeName=void 0,this.uniformsNeedUpdate=!1,this.glslVersion=null,e!==void 0&&this.setValues(e)}copy(e){return super.copy(e),this.fragmentShader=e.fragmentShader,this.vertexShader=e.vertexShader,this.uniforms=rs(e.uniforms),this.uniformsGroups=Dp(e.uniformsGroups),this.defines=Object.assign({},e.defines),this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.fog=e.fog,this.lights=e.lights,this.clipping=e.clipping,this.extensions=Object.assign({},e.extensions),this.glslVersion=e.glslVersion,this.defaultAttributeValues=Object.assign({},e.defaultAttributeValues),this.index0AttributeName=e.index0AttributeName,this.uniformsNeedUpdate=e.uniformsNeedUpdate,this}toJSON(e){const t=super.toJSON(e);t.glslVersion=this.glslVersion,t.uniforms={};for(const i in this.uniforms){const a=this.uniforms[i].value;a&&a.isTexture?t.uniforms[i]={type:"t",value:a.toJSON(e).uuid}:a&&a.isColor?t.uniforms[i]={type:"c",value:a.getHex()}:a&&a.isVector2?t.uniforms[i]={type:"v2",value:a.toArray()}:a&&a.isVector3?t.uniforms[i]={type:"v3",value:a.toArray()}:a&&a.isVector4?t.uniforms[i]={type:"v4",value:a.toArray()}:a&&a.isMatrix3?t.uniforms[i]={type:"m3",value:a.toArray()}:a&&a.isMatrix4?t.uniforms[i]={type:"m4",value:a.toArray()}:t.uniforms[i]={value:a}}Object.keys(this.defines).length>0&&(t.defines=this.defines),t.vertexShader=this.vertexShader,t.fragmentShader=this.fragmentShader,t.lights=this.lights,t.clipping=this.clipping;const n={};for(const i in this.extensions)this.extensions[i]===!0&&(n[i]=!0);return Object.keys(n).length>0&&(t.extensions=n),t}}class Np extends ci{constructor(e){super(e),this.isRawShaderMaterial=!0,this.type="RawShaderMaterial"}}class hn extends Er{constructor(e){super(),this.isMeshStandardMaterial=!0,this.type="MeshStandardMaterial",this.defines={STANDARD:""},this.color=new it(16777215),this.roughness=1,this.metalness=0,this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.emissive=new it(0),this.emissiveIntensity=1,this.emissiveMap=null,this.bumpMap=null,this.bumpScale=1,this.normalMap=null,this.normalMapType=Wh,this.normalScale=new qe(1,1),this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.roughnessMap=null,this.metalnessMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new li,this.envMapIntensity=1,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.flatShading=!1,this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.defines={STANDARD:""},this.color.copy(e.color),this.roughness=e.roughness,this.metalness=e.metalness,this.map=e.map,this.lightMap=e.lightMap,this.lightMapIntensity=e.lightMapIntensity,this.aoMap=e.aoMap,this.aoMapIntensity=e.aoMapIntensity,this.emissive.copy(e.emissive),this.emissiveMap=e.emissiveMap,this.emissiveIntensity=e.emissiveIntensity,this.bumpMap=e.bumpMap,this.bumpScale=e.bumpScale,this.normalMap=e.normalMap,this.normalMapType=e.normalMapType,this.normalScale.copy(e.normalScale),this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this.roughnessMap=e.roughnessMap,this.metalnessMap=e.metalnessMap,this.alphaMap=e.alphaMap,this.envMap=e.envMap,this.envMapRotation.copy(e.envMapRotation),this.envMapIntensity=e.envMapIntensity,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.wireframeLinecap=e.wireframeLinecap,this.wireframeLinejoin=e.wireframeLinejoin,this.flatShading=e.flatShading,this.fog=e.fog,this}}class Fp extends Er{constructor(e){super(),this.isMeshDepthMaterial=!0,this.type="MeshDepthMaterial",this.depthPacking=qd,this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.wireframe=!1,this.wireframeLinewidth=1,this.setValues(e)}copy(e){return super.copy(e),this.depthPacking=e.depthPacking,this.map=e.map,this.alphaMap=e.alphaMap,this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this}}class Op extends Er{constructor(e){super(),this.isMeshDistanceMaterial=!0,this.type="MeshDistanceMaterial",this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.setValues(e)}copy(e){return super.copy(e),this.map=e.map,this.alphaMap=e.alphaMap,this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this}}class wc extends kt{constructor(e,t=1){super(),this.isLight=!0,this.type="Light",this.color=new it(e),this.intensity=t}dispose(){this.dispatchEvent({type:"dispose"})}copy(e,t){return super.copy(e,t),this.color.copy(e.color),this.intensity=e.intensity,this}toJSON(e){const t=super.toJSON(e);return t.object.color=this.color.getHex(),t.object.intensity=this.intensity,t}}const ko=new St,Tu=new V,Au=new V;class ef{constructor(e){this.camera=e,this.intensity=1,this.bias=0,this.biasNode=null,this.normalBias=0,this.radius=1,this.blurSamples=8,this.mapSize=new qe(512,512),this.mapType=Rn,this.map=null,this.mapPass=null,this.matrix=new St,this.autoUpdate=!0,this.needsUpdate=!1,this._frustum=new Tc,this._frameExtents=new qe(1,1),this._viewportCount=1,this._viewports=[new Pt(0,0,1,1)]}getViewportCount(){return this._viewportCount}getFrustum(){return this._frustum}updateMatrices(e){const t=this.camera,n=this.matrix;Tu.setFromMatrixPosition(e.matrixWorld),t.position.copy(Tu),Au.setFromMatrixPosition(e.target.matrixWorld),t.lookAt(Au),t.updateMatrixWorld(),ko.multiplyMatrices(t.projectionMatrix,t.matrixWorldInverse),this._frustum.setFromProjectionMatrix(ko,t.coordinateSystem,t.reversedDepth),t.coordinateSystem===Os||t.reversedDepth?n.set(.5,0,0,.5,0,.5,0,.5,0,0,1,0,0,0,0,1):n.set(.5,0,0,.5,0,.5,0,.5,0,0,.5,.5,0,0,0,1),n.multiply(ko)}getViewport(e){return this._viewports[e]}getFrameExtents(){return this._frameExtents}dispose(){this.map&&this.map.dispose(),this.mapPass&&this.mapPass.dispose()}copy(e){return this.camera=e.camera.clone(),this.intensity=e.intensity,this.bias=e.bias,this.radius=e.radius,this.autoUpdate=e.autoUpdate,this.needsUpdate=e.needsUpdate,this.normalBias=e.normalBias,this.blurSamples=e.blurSamples,this.mapSize.copy(e.mapSize),this.biasNode=e.biasNode,this}clone(){return new this.constructor().copy(this)}toJSON(){const e={};return this.intensity!==1&&(e.intensity=this.intensity),this.bias!==0&&(e.bias=this.bias),this.normalBias!==0&&(e.normalBias=this.normalBias),this.radius!==1&&(e.radius=this.radius),(this.mapSize.x!==512||this.mapSize.y!==512)&&(e.mapSize=this.mapSize.toArray()),e.camera=this.camera.toJSON(!1).object,delete e.camera.matrix,e}}const Ta=new V,Aa=new ji,jn=new V;class tf extends kt{constructor(){super(),this.isCamera=!0,this.type="Camera",this.matrixWorldInverse=new St,this.projectionMatrix=new St,this.projectionMatrixInverse=new St,this.coordinateSystem=ii,this._reversedDepth=!1}get reversedDepth(){return this._reversedDepth}copy(e,t){return super.copy(e,t),this.matrixWorldInverse.copy(e.matrixWorldInverse),this.projectionMatrix.copy(e.projectionMatrix),this.projectionMatrixInverse.copy(e.projectionMatrixInverse),this.coordinateSystem=e.coordinateSystem,this}getWorldDirection(e){return super.getWorldDirection(e).negate()}updateMatrixWorld(e){super.updateMatrixWorld(e),this.matrixWorld.decompose(Ta,Aa,jn),jn.x===1&&jn.y===1&&jn.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(Ta,Aa,jn.set(1,1,1)).invert()}updateWorldMatrix(e,t){super.updateWorldMatrix(e,t),this.matrixWorld.decompose(Ta,Aa,jn),jn.x===1&&jn.y===1&&jn.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(Ta,Aa,jn.set(1,1,1)).invert()}clone(){return new this.constructor().copy(this)}}const Oi=new V,wu=new qe,Ru=new qe;class wn extends tf{constructor(e=50,t=1,n=.1,i=2e3){super(),this.isPerspectiveCamera=!0,this.type="PerspectiveCamera",this.fov=e,this.zoom=1,this.near=n,this.far=i,this.focus=10,this.aspect=t,this.view=null,this.filmGauge=35,this.filmOffset=0,this.updateProjectionMatrix()}copy(e,t){return super.copy(e,t),this.fov=e.fov,this.zoom=e.zoom,this.near=e.near,this.far=e.far,this.focus=e.focus,this.aspect=e.aspect,this.view=e.view===null?null:Object.assign({},e.view),this.filmGauge=e.filmGauge,this.filmOffset=e.filmOffset,this}setFocalLength(e){const t=.5*this.getFilmHeight()/e;this.fov=Wl*2*Math.atan(t),this.updateProjectionMatrix()}getFocalLength(){const e=Math.tan(Ps*.5*this.fov);return .5*this.getFilmHeight()/e}getEffectiveFOV(){return Wl*2*Math.atan(Math.tan(Ps*.5*this.fov)/this.zoom)}getFilmWidth(){return this.filmGauge*Math.min(this.aspect,1)}getFilmHeight(){return this.filmGauge/Math.max(this.aspect,1)}getViewBounds(e,t,n){Oi.set(-1,-1,.5).applyMatrix4(this.projectionMatrixInverse),t.set(Oi.x,Oi.y).multiplyScalar(-e/Oi.z),Oi.set(1,1,.5).applyMatrix4(this.projectionMatrixInverse),n.set(Oi.x,Oi.y).multiplyScalar(-e/Oi.z)}getViewSize(e,t){return this.getViewBounds(e,wu,Ru),t.subVectors(Ru,wu)}setViewOffset(e,t,n,i,s,a){this.aspect=e/t,this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=e,this.view.fullHeight=t,this.view.offsetX=n,this.view.offsetY=i,this.view.width=s,this.view.height=a,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const e=this.near;let t=e*Math.tan(Ps*.5*this.fov)/this.zoom,n=2*t,i=this.aspect*n,s=-.5*i;const a=this.view;if(this.view!==null&&this.view.enabled){const l=a.fullWidth,c=a.fullHeight;s+=a.offsetX*i/l,t-=a.offsetY*n/c,i*=a.width/l,n*=a.height/c}const o=this.filmOffset;o!==0&&(s+=e*o/this.getFilmWidth()),this.projectionMatrix.makePerspective(s,s+i,t,t-n,e,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(e){const t=super.toJSON(e);return t.object.fov=this.fov,t.object.zoom=this.zoom,t.object.near=this.near,t.object.far=this.far,t.object.focus=this.focus,t.object.aspect=this.aspect,this.view!==null&&(t.object.view=Object.assign({},this.view)),t.object.filmGauge=this.filmGauge,t.object.filmOffset=this.filmOffset,t}}class Bp extends ef{constructor(){super(new wn(90,1,.5,500)),this.isPointLightShadow=!0}}class kp extends wc{constructor(e,t,n=0,i=2){super(e,t),this.isPointLight=!0,this.type="PointLight",this.distance=n,this.decay=i,this.shadow=new Bp}get power(){return this.intensity*4*Math.PI}set power(e){this.intensity=e/(4*Math.PI)}dispose(){super.dispose(),this.shadow.dispose()}copy(e,t){return super.copy(e,t),this.distance=e.distance,this.decay=e.decay,this.shadow=e.shadow.clone(),this}toJSON(e){const t=super.toJSON(e);return t.object.distance=this.distance,t.object.decay=this.decay,t.object.shadow=this.shadow.toJSON(),t}}class Rc extends tf{constructor(e=-1,t=1,n=1,i=-1,s=.1,a=2e3){super(),this.isOrthographicCamera=!0,this.type="OrthographicCamera",this.zoom=1,this.view=null,this.left=e,this.right=t,this.top=n,this.bottom=i,this.near=s,this.far=a,this.updateProjectionMatrix()}copy(e,t){return super.copy(e,t),this.left=e.left,this.right=e.right,this.top=e.top,this.bottom=e.bottom,this.near=e.near,this.far=e.far,this.zoom=e.zoom,this.view=e.view===null?null:Object.assign({},e.view),this}setViewOffset(e,t,n,i,s,a){this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=e,this.view.fullHeight=t,this.view.offsetX=n,this.view.offsetY=i,this.view.width=s,this.view.height=a,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const e=(this.right-this.left)/(2*this.zoom),t=(this.top-this.bottom)/(2*this.zoom),n=(this.right+this.left)/2,i=(this.top+this.bottom)/2;let s=n-e,a=n+e,o=i+t,l=i-t;if(this.view!==null&&this.view.enabled){const c=(this.right-this.left)/this.view.fullWidth/this.zoom,u=(this.top-this.bottom)/this.view.fullHeight/this.zoom;s+=c*this.view.offsetX,a=s+c*this.view.width,o-=u*this.view.offsetY,l=o-u*this.view.height}this.projectionMatrix.makeOrthographic(s,a,o,l,this.near,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(e){const t=super.toJSON(e);return t.object.zoom=this.zoom,t.object.left=this.left,t.object.right=this.right,t.object.top=this.top,t.object.bottom=this.bottom,t.object.near=this.near,t.object.far=this.far,this.view!==null&&(t.object.view=Object.assign({},this.view)),t}}class zp extends ef{constructor(){super(new Rc(-5,5,5,-5,.5,500)),this.isDirectionalLightShadow=!0}}class Cu extends wc{constructor(e,t){super(e,t),this.isDirectionalLight=!0,this.type="DirectionalLight",this.position.copy(kt.DEFAULT_UP),this.updateMatrix(),this.target=new kt,this.shadow=new zp}dispose(){super.dispose(),this.shadow.dispose()}copy(e){return super.copy(e),this.target=e.target.clone(),this.shadow=e.shadow.clone(),this}toJSON(e){const t=super.toJSON(e);return t.object.shadow=this.shadow.toJSON(),t.object.target=this.target.uuid,t}}class Vp extends wc{constructor(e,t){super(e,t),this.isAmbientLight=!0,this.type="AmbientLight"}}const kr=-90,zr=1;class Gp extends kt{constructor(e,t,n){super(),this.type="CubeCamera",this.renderTarget=n,this.coordinateSystem=null,this.activeMipmapLevel=0;const i=new wn(kr,zr,e,t);i.layers=this.layers,this.add(i);const s=new wn(kr,zr,e,t);s.layers=this.layers,this.add(s);const a=new wn(kr,zr,e,t);a.layers=this.layers,this.add(a);const o=new wn(kr,zr,e,t);o.layers=this.layers,this.add(o);const l=new wn(kr,zr,e,t);l.layers=this.layers,this.add(l);const c=new wn(kr,zr,e,t);c.layers=this.layers,this.add(c)}updateCoordinateSystem(){const e=this.coordinateSystem,t=this.children.concat(),[n,i,s,a,o,l]=t;for(const c of t)this.remove(c);if(e===ii)n.up.set(0,1,0),n.lookAt(1,0,0),i.up.set(0,1,0),i.lookAt(-1,0,0),s.up.set(0,0,-1),s.lookAt(0,1,0),a.up.set(0,0,1),a.lookAt(0,-1,0),o.up.set(0,1,0),o.lookAt(0,0,1),l.up.set(0,1,0),l.lookAt(0,0,-1);else if(e===Os)n.up.set(0,-1,0),n.lookAt(-1,0,0),i.up.set(0,-1,0),i.lookAt(1,0,0),s.up.set(0,0,1),s.lookAt(0,1,0),a.up.set(0,0,-1),a.lookAt(0,-1,0),o.up.set(0,-1,0),o.lookAt(0,0,1),l.up.set(0,-1,0),l.lookAt(0,0,-1);else throw new Error("THREE.CubeCamera.updateCoordinateSystem(): Invalid coordinate system: "+e);for(const c of t)this.add(c),c.updateMatrixWorld()}update(e,t){this.parent===null&&this.updateMatrixWorld();const{renderTarget:n,activeMipmapLevel:i}=this;this.coordinateSystem!==e.coordinateSystem&&(this.coordinateSystem=e.coordinateSystem,this.updateCoordinateSystem());const[s,a,o,l,c,u]=this.children,f=e.getRenderTarget(),h=e.getActiveCubeFace(),d=e.getActiveMipmapLevel(),_=e.xr.enabled;e.xr.enabled=!1;const g=n.texture.generateMipmaps;n.texture.generateMipmaps=!1;let p=!1;e.isWebGLRenderer===!0?p=e.state.buffers.depth.getReversed():p=e.reversedDepthBuffer,e.setRenderTarget(n,0,i),p&&e.autoClear===!1&&e.clearDepth(),e.render(t,s),e.setRenderTarget(n,1,i),p&&e.autoClear===!1&&e.clearDepth(),e.render(t,a),e.setRenderTarget(n,2,i),p&&e.autoClear===!1&&e.clearDepth(),e.render(t,o),e.setRenderTarget(n,3,i),p&&e.autoClear===!1&&e.clearDepth(),e.render(t,l),e.setRenderTarget(n,4,i),p&&e.autoClear===!1&&e.clearDepth(),e.render(t,c),n.texture.generateMipmaps=g,e.setRenderTarget(n,5,i),p&&e.autoClear===!1&&e.clearDepth(),e.render(t,u),e.setRenderTarget(f,h,d),e.xr.enabled=_,n.texture.needsPMREMUpdate=!0}}class Hp extends wn{constructor(e=[]){super(),this.isArrayCamera=!0,this.isMultiViewCamera=!1,this.cameras=e}}const Pu=new St;class Wp{constructor(e,t,n=0,i=1/0){this.ray=new $s(e,t),this.near=n,this.far=i,this.camera=null,this.layers=new bc,this.params={Mesh:{},Line:{threshold:1},LOD:{},Points:{threshold:1},Sprite:{}}}set(e,t){this.ray.set(e,t)}setFromCamera(e,t){t.isPerspectiveCamera?(this.ray.origin.setFromMatrixPosition(t.matrixWorld),this.ray.direction.set(e.x,e.y,.5).unproject(t).sub(this.ray.origin).normalize(),this.camera=t):t.isOrthographicCamera?(this.ray.origin.set(e.x,e.y,(t.near+t.far)/(t.near-t.far)).unproject(t),this.ray.direction.set(0,0,-1).transformDirection(t.matrixWorld),this.camera=t):ut("Raycaster: Unsupported camera type: "+t.type)}setFromXRController(e){return Pu.identity().extractRotation(e.matrixWorld),this.ray.origin.setFromMatrixPosition(e.matrixWorld),this.ray.direction.set(0,0,-1).applyMatrix4(Pu),this}intersectObject(e,t=!0,n=[]){return ql(e,this,n,t),n.sort(Du),n}intersectObjects(e,t=!0,n=[]){for(let i=0,s=e.length;i<s;i++)ql(e[i],this,n,t);return n.sort(Du),n}}function Du(r,e){return r.distance-e.distance}function ql(r,e,t,n){let i=!0;if(r.layers.test(e.layers)&&r.raycast(e,t)===!1&&(i=!1),i===!0&&n===!0){const s=r.children;for(let a=0,o=s.length;a<o;a++)ql(s[a],e,t,!0)}}class Lu{constructor(e=1,t=0,n=0){this.radius=e,this.phi=t,this.theta=n}set(e,t,n){return this.radius=e,this.phi=t,this.theta=n,this}copy(e){return this.radius=e.radius,this.phi=e.phi,this.theta=e.theta,this}makeSafe(){return this.phi=nt(this.phi,1e-6,Math.PI-1e-6),this}setFromVector3(e){return this.setFromCartesianCoords(e.x,e.y,e.z)}setFromCartesianCoords(e,t,n){return this.radius=Math.sqrt(e*e+t*t+n*n),this.radius===0?(this.theta=0,this.phi=0):(this.theta=Math.atan2(e,n),this.phi=Math.acos(nt(t/this.radius,-1,1))),this}clone(){return new this.constructor().copy(this)}}class Xp extends Fa{constructor(e=10,t=10,n=4473924,i=8947848){n=new it(n),i=new it(i);const s=t/2,a=e/t,o=e/2,l=[],c=[];for(let h=0,d=0,_=-o;h<=t;h++,_+=a){l.push(-o,0,_,o,0,_),l.push(_,0,-o,_,0,o);const g=h===s?n:i;g.toArray(c,d),d+=3,g.toArray(c,d),d+=3,g.toArray(c,d),d+=3,g.toArray(c,d),d+=3}const u=new ln;u.setAttribute("position",new Yt(l,3)),u.setAttribute("color",new Yt(c,3));const f=new Ds({vertexColors:!0,toneMapped:!1});super(u,f),this.type="GridHelper"}dispose(){this.geometry.dispose(),this.material.dispose()}}class qp extends br{constructor(e,t=null){super(),this.object=e,this.domElement=t,this.enabled=!0,this.state=-1,this.keys={},this.mouseButtons={LEFT:null,MIDDLE:null,RIGHT:null},this.touches={ONE:null,TWO:null}}connect(e){if(e===void 0){He("Controls: connect() now requires an element.");return}this.domElement!==null&&this.disconnect(),this.domElement=e}disconnect(){}dispose(){}update(){}}function Iu(r,e,t,n){const i=Yp(n);switch(t){case Vh:return r*e;case Hh:return r*e/i.components*i.byteLength;case gc:return r*e/i.components*i.byteLength;case ns:return r*e*2/i.components*i.byteLength;case vc:return r*e*2/i.components*i.byteLength;case Gh:return r*e*3/i.components*i.byteLength;case qn:return r*e*4/i.components*i.byteLength;case xc:return r*e*4/i.components*i.byteLength;case La:case Ia:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*8;case Ua:case Na:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*16;case dl:case ml:return Math.max(r,16)*Math.max(e,8)/4;case fl:case pl:return Math.max(r,8)*Math.max(e,8)/2;case _l:case gl:case xl:case Ml:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*8;case vl:case Sl:case yl:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*16;case bl:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*16;case El:return Math.floor((r+4)/5)*Math.floor((e+3)/4)*16;case Tl:return Math.floor((r+4)/5)*Math.floor((e+4)/5)*16;case Al:return Math.floor((r+5)/6)*Math.floor((e+4)/5)*16;case wl:return Math.floor((r+5)/6)*Math.floor((e+5)/6)*16;case Rl:return Math.floor((r+7)/8)*Math.floor((e+4)/5)*16;case Cl:return Math.floor((r+7)/8)*Math.floor((e+5)/6)*16;case Pl:return Math.floor((r+7)/8)*Math.floor((e+7)/8)*16;case Dl:return Math.floor((r+9)/10)*Math.floor((e+4)/5)*16;case Ll:return Math.floor((r+9)/10)*Math.floor((e+5)/6)*16;case Il:return Math.floor((r+9)/10)*Math.floor((e+7)/8)*16;case Ul:return Math.floor((r+9)/10)*Math.floor((e+9)/10)*16;case Nl:return Math.floor((r+11)/12)*Math.floor((e+9)/10)*16;case Fl:return Math.floor((r+11)/12)*Math.floor((e+11)/12)*16;case Ol:case Bl:case kl:return Math.ceil(r/4)*Math.ceil(e/4)*16;case zl:case Vl:return Math.ceil(r/4)*Math.ceil(e/4)*8;case Gl:case Hl:return Math.ceil(r/4)*Math.ceil(e/4)*16}throw new Error(`Unable to determine texture byte length for ${t} format.`)}function Yp(r){switch(r){case Rn:case Oh:return{byteLength:1,components:1};case Ns:case Bh:case Ti:return{byteLength:2,components:1};case mc:case _c:return{byteLength:2,components:4};case oi:case pc:case ni:return{byteLength:4,components:1};case kh:case zh:return{byteLength:4,components:3}}throw new Error(`Unknown texture type ${r}.`)}typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("register",{detail:{revision:fc}}));typeof window<"u"&&(window.__THREE__?He("WARNING: Multiple instances of Three.js being imported."):window.__THREE__=fc);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */function nf(){let r=null,e=!1,t=null,n=null;function i(s,a){t(s,a),n=r.requestAnimationFrame(i)}return{start:function(){e!==!0&&t!==null&&(n=r.requestAnimationFrame(i),e=!0)},stop:function(){r.cancelAnimationFrame(n),e=!1},setAnimationLoop:function(s){t=s},setContext:function(s){r=s}}}function $p(r){const e=new WeakMap;function t(o,l){const c=o.array,u=o.usage,f=c.byteLength,h=r.createBuffer();r.bindBuffer(l,h),r.bufferData(l,c,u),o.onUploadCallback();let d;if(c instanceof Float32Array)d=r.FLOAT;else if(typeof Float16Array<"u"&&c instanceof Float16Array)d=r.HALF_FLOAT;else if(c instanceof Uint16Array)o.isFloat16BufferAttribute?d=r.HALF_FLOAT:d=r.UNSIGNED_SHORT;else if(c instanceof Int16Array)d=r.SHORT;else if(c instanceof Uint32Array)d=r.UNSIGNED_INT;else if(c instanceof Int32Array)d=r.INT;else if(c instanceof Int8Array)d=r.BYTE;else if(c instanceof Uint8Array)d=r.UNSIGNED_BYTE;else if(c instanceof Uint8ClampedArray)d=r.UNSIGNED_BYTE;else throw new Error("THREE.WebGLAttributes: Unsupported buffer data format: "+c);return{buffer:h,type:d,bytesPerElement:c.BYTES_PER_ELEMENT,version:o.version,size:f}}function n(o,l,c){const u=l.array,f=l.updateRanges;if(r.bindBuffer(c,o),f.length===0)r.bufferSubData(c,0,u);else{f.sort((d,_)=>d.start-_.start);let h=0;for(let d=1;d<f.length;d++){const _=f[h],g=f[d];g.start<=_.start+_.count+1?_.count=Math.max(_.count,g.start+g.count-_.start):(++h,f[h]=g)}f.length=h+1;for(let d=0,_=f.length;d<_;d++){const g=f[d];r.bufferSubData(c,g.start*u.BYTES_PER_ELEMENT,u,g.start,g.count)}l.clearUpdateRanges()}l.onUploadCallback()}function i(o){return o.isInterleavedBufferAttribute&&(o=o.data),e.get(o)}function s(o){o.isInterleavedBufferAttribute&&(o=o.data);const l=e.get(o);l&&(r.deleteBuffer(l.buffer),e.delete(o))}function a(o,l){if(o.isInterleavedBufferAttribute&&(o=o.data),o.isGLBufferAttribute){const u=e.get(o);(!u||u.version<o.version)&&e.set(o,{buffer:o.buffer,type:o.type,bytesPerElement:o.elementSize,version:o.version});return}const c=e.get(o);if(c===void 0)e.set(o,t(o,l));else if(c.version<o.version){if(c.size!==o.array.byteLength)throw new Error("THREE.WebGLAttributes: The size of the buffer attribute's array buffer does not match the original size. Resizing buffer attributes is not supported.");n(c.buffer,o,l),c.version=o.version}}return{get:i,remove:s,update:a}}var Kp=`#ifdef USE_ALPHAHASH
	if ( diffuseColor.a < getAlphaHashThreshold( vPosition ) ) discard;
#endif`,Zp=`#ifdef USE_ALPHAHASH
	const float ALPHA_HASH_SCALE = 0.05;
	float hash2D( vec2 value ) {
		return fract( 1.0e4 * sin( 17.0 * value.x + 0.1 * value.y ) * ( 0.1 + abs( sin( 13.0 * value.y + value.x ) ) ) );
	}
	float hash3D( vec3 value ) {
		return hash2D( vec2( hash2D( value.xy ), value.z ) );
	}
	float getAlphaHashThreshold( vec3 position ) {
		float maxDeriv = max(
			length( dFdx( position.xyz ) ),
			length( dFdy( position.xyz ) )
		);
		float pixScale = 1.0 / ( ALPHA_HASH_SCALE * maxDeriv );
		vec2 pixScales = vec2(
			exp2( floor( log2( pixScale ) ) ),
			exp2( ceil( log2( pixScale ) ) )
		);
		vec2 alpha = vec2(
			hash3D( floor( pixScales.x * position.xyz ) ),
			hash3D( floor( pixScales.y * position.xyz ) )
		);
		float lerpFactor = fract( log2( pixScale ) );
		float x = ( 1.0 - lerpFactor ) * alpha.x + lerpFactor * alpha.y;
		float a = min( lerpFactor, 1.0 - lerpFactor );
		vec3 cases = vec3(
			x * x / ( 2.0 * a * ( 1.0 - a ) ),
			( x - 0.5 * a ) / ( 1.0 - a ),
			1.0 - ( ( 1.0 - x ) * ( 1.0 - x ) / ( 2.0 * a * ( 1.0 - a ) ) )
		);
		float threshold = ( x < ( 1.0 - a ) )
			? ( ( x < a ) ? cases.x : cases.y )
			: cases.z;
		return clamp( threshold , 1.0e-6, 1.0 );
	}
#endif`,jp=`#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, vAlphaMapUv ).g;
#endif`,Jp=`#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,Qp=`#ifdef USE_ALPHATEST
	#ifdef ALPHA_TO_COVERAGE
	diffuseColor.a = smoothstep( alphaTest, alphaTest + fwidth( diffuseColor.a ), diffuseColor.a );
	if ( diffuseColor.a == 0.0 ) discard;
	#else
	if ( diffuseColor.a < alphaTest ) discard;
	#endif
#endif`,em=`#ifdef USE_ALPHATEST
	uniform float alphaTest;
#endif`,tm=`#ifdef USE_AOMAP
	float ambientOcclusion = ( texture2D( aoMap, vAoMapUv ).r - 1.0 ) * aoMapIntensity + 1.0;
	reflectedLight.indirectDiffuse *= ambientOcclusion;
	#if defined( USE_CLEARCOAT ) 
		clearcoatSpecularIndirect *= ambientOcclusion;
	#endif
	#if defined( USE_SHEEN ) 
		sheenSpecularIndirect *= ambientOcclusion;
	#endif
	#if defined( USE_ENVMAP ) && defined( STANDARD )
		float dotNV = saturate( dot( geometryNormal, geometryViewDir ) );
		reflectedLight.indirectSpecular *= computeSpecularOcclusion( dotNV, ambientOcclusion, material.roughness );
	#endif
#endif`,nm=`#ifdef USE_AOMAP
	uniform sampler2D aoMap;
	uniform float aoMapIntensity;
#endif`,im=`#ifdef USE_BATCHING
	#if ! defined( GL_ANGLE_multi_draw )
	#define gl_DrawID _gl_DrawID
	uniform int _gl_DrawID;
	#endif
	uniform highp sampler2D batchingTexture;
	uniform highp usampler2D batchingIdTexture;
	mat4 getBatchingMatrix( const in float i ) {
		int size = textureSize( batchingTexture, 0 ).x;
		int j = int( i ) * 4;
		int x = j % size;
		int y = j / size;
		vec4 v1 = texelFetch( batchingTexture, ivec2( x, y ), 0 );
		vec4 v2 = texelFetch( batchingTexture, ivec2( x + 1, y ), 0 );
		vec4 v3 = texelFetch( batchingTexture, ivec2( x + 2, y ), 0 );
		vec4 v4 = texelFetch( batchingTexture, ivec2( x + 3, y ), 0 );
		return mat4( v1, v2, v3, v4 );
	}
	float getIndirectIndex( const in int i ) {
		int size = textureSize( batchingIdTexture, 0 ).x;
		int x = i % size;
		int y = i / size;
		return float( texelFetch( batchingIdTexture, ivec2( x, y ), 0 ).r );
	}
#endif
#ifdef USE_BATCHING_COLOR
	uniform sampler2D batchingColorTexture;
	vec4 getBatchingColor( const in float i ) {
		int size = textureSize( batchingColorTexture, 0 ).x;
		int j = int( i );
		int x = j % size;
		int y = j / size;
		return texelFetch( batchingColorTexture, ivec2( x, y ), 0 );
	}
#endif`,rm=`#ifdef USE_BATCHING
	mat4 batchingMatrix = getBatchingMatrix( getIndirectIndex( gl_DrawID ) );
#endif`,sm=`vec3 transformed = vec3( position );
#ifdef USE_ALPHAHASH
	vPosition = vec3( position );
#endif`,am=`vec3 objectNormal = vec3( normal );
#ifdef USE_TANGENT
	vec3 objectTangent = vec3( tangent.xyz );
#endif`,om=`float G_BlinnPhong_Implicit( ) {
	return 0.25;
}
float D_BlinnPhong( const in float shininess, const in float dotNH ) {
	return RECIPROCAL_PI * ( shininess * 0.5 + 1.0 ) * pow( dotNH, shininess );
}
vec3 BRDF_BlinnPhong( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in vec3 specularColor, const in float shininess ) {
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNH = saturate( dot( normal, halfDir ) );
	float dotVH = saturate( dot( viewDir, halfDir ) );
	vec3 F = F_Schlick( specularColor, 1.0, dotVH );
	float G = G_BlinnPhong_Implicit( );
	float D = D_BlinnPhong( shininess, dotNH );
	return F * ( G * D );
} // validated`,lm=`#ifdef USE_IRIDESCENCE
	const mat3 XYZ_TO_REC709 = mat3(
		 3.2404542, -0.9692660,  0.0556434,
		-1.5371385,  1.8760108, -0.2040259,
		-0.4985314,  0.0415560,  1.0572252
	);
	vec3 Fresnel0ToIor( vec3 fresnel0 ) {
		vec3 sqrtF0 = sqrt( fresnel0 );
		return ( vec3( 1.0 ) + sqrtF0 ) / ( vec3( 1.0 ) - sqrtF0 );
	}
	vec3 IorToFresnel0( vec3 transmittedIor, float incidentIor ) {
		return pow2( ( transmittedIor - vec3( incidentIor ) ) / ( transmittedIor + vec3( incidentIor ) ) );
	}
	float IorToFresnel0( float transmittedIor, float incidentIor ) {
		return pow2( ( transmittedIor - incidentIor ) / ( transmittedIor + incidentIor ));
	}
	vec3 evalSensitivity( float OPD, vec3 shift ) {
		float phase = 2.0 * PI * OPD * 1.0e-9;
		vec3 val = vec3( 5.4856e-13, 4.4201e-13, 5.2481e-13 );
		vec3 pos = vec3( 1.6810e+06, 1.7953e+06, 2.2084e+06 );
		vec3 var = vec3( 4.3278e+09, 9.3046e+09, 6.6121e+09 );
		vec3 xyz = val * sqrt( 2.0 * PI * var ) * cos( pos * phase + shift ) * exp( - pow2( phase ) * var );
		xyz.x += 9.7470e-14 * sqrt( 2.0 * PI * 4.5282e+09 ) * cos( 2.2399e+06 * phase + shift[ 0 ] ) * exp( - 4.5282e+09 * pow2( phase ) );
		xyz /= 1.0685e-7;
		vec3 rgb = XYZ_TO_REC709 * xyz;
		return rgb;
	}
	vec3 evalIridescence( float outsideIOR, float eta2, float cosTheta1, float thinFilmThickness, vec3 baseF0 ) {
		vec3 I;
		float iridescenceIOR = mix( outsideIOR, eta2, smoothstep( 0.0, 0.03, thinFilmThickness ) );
		float sinTheta2Sq = pow2( outsideIOR / iridescenceIOR ) * ( 1.0 - pow2( cosTheta1 ) );
		float cosTheta2Sq = 1.0 - sinTheta2Sq;
		if ( cosTheta2Sq < 0.0 ) {
			return vec3( 1.0 );
		}
		float cosTheta2 = sqrt( cosTheta2Sq );
		float R0 = IorToFresnel0( iridescenceIOR, outsideIOR );
		float R12 = F_Schlick( R0, 1.0, cosTheta1 );
		float T121 = 1.0 - R12;
		float phi12 = 0.0;
		if ( iridescenceIOR < outsideIOR ) phi12 = PI;
		float phi21 = PI - phi12;
		vec3 baseIOR = Fresnel0ToIor( clamp( baseF0, 0.0, 0.9999 ) );		vec3 R1 = IorToFresnel0( baseIOR, iridescenceIOR );
		vec3 R23 = F_Schlick( R1, 1.0, cosTheta2 );
		vec3 phi23 = vec3( 0.0 );
		if ( baseIOR[ 0 ] < iridescenceIOR ) phi23[ 0 ] = PI;
		if ( baseIOR[ 1 ] < iridescenceIOR ) phi23[ 1 ] = PI;
		if ( baseIOR[ 2 ] < iridescenceIOR ) phi23[ 2 ] = PI;
		float OPD = 2.0 * iridescenceIOR * thinFilmThickness * cosTheta2;
		vec3 phi = vec3( phi21 ) + phi23;
		vec3 R123 = clamp( R12 * R23, 1e-5, 0.9999 );
		vec3 r123 = sqrt( R123 );
		vec3 Rs = pow2( T121 ) * R23 / ( vec3( 1.0 ) - R123 );
		vec3 C0 = R12 + Rs;
		I = C0;
		vec3 Cm = Rs - T121;
		for ( int m = 1; m <= 2; ++ m ) {
			Cm *= r123;
			vec3 Sm = 2.0 * evalSensitivity( float( m ) * OPD, float( m ) * phi );
			I += Cm * Sm;
		}
		return max( I, vec3( 0.0 ) );
	}
#endif`,cm=`#ifdef USE_BUMPMAP
	uniform sampler2D bumpMap;
	uniform float bumpScale;
	vec2 dHdxy_fwd() {
		vec2 dSTdx = dFdx( vBumpMapUv );
		vec2 dSTdy = dFdy( vBumpMapUv );
		float Hll = bumpScale * texture2D( bumpMap, vBumpMapUv ).x;
		float dBx = bumpScale * texture2D( bumpMap, vBumpMapUv + dSTdx ).x - Hll;
		float dBy = bumpScale * texture2D( bumpMap, vBumpMapUv + dSTdy ).x - Hll;
		return vec2( dBx, dBy );
	}
	vec3 perturbNormalArb( vec3 surf_pos, vec3 surf_norm, vec2 dHdxy, float faceDirection ) {
		vec3 vSigmaX = normalize( dFdx( surf_pos.xyz ) );
		vec3 vSigmaY = normalize( dFdy( surf_pos.xyz ) );
		vec3 vN = surf_norm;
		vec3 R1 = cross( vSigmaY, vN );
		vec3 R2 = cross( vN, vSigmaX );
		float fDet = dot( vSigmaX, R1 ) * faceDirection;
		vec3 vGrad = sign( fDet ) * ( dHdxy.x * R1 + dHdxy.y * R2 );
		return normalize( abs( fDet ) * surf_norm - vGrad );
	}
#endif`,um=`#if NUM_CLIPPING_PLANES > 0
	vec4 plane;
	#ifdef ALPHA_TO_COVERAGE
		float distanceToPlane, distanceGradient;
		float clipOpacity = 1.0;
		#pragma unroll_loop_start
		for ( int i = 0; i < UNION_CLIPPING_PLANES; i ++ ) {
			plane = clippingPlanes[ i ];
			distanceToPlane = - dot( vClipPosition, plane.xyz ) + plane.w;
			distanceGradient = fwidth( distanceToPlane ) / 2.0;
			clipOpacity *= smoothstep( - distanceGradient, distanceGradient, distanceToPlane );
			if ( clipOpacity == 0.0 ) discard;
		}
		#pragma unroll_loop_end
		#if UNION_CLIPPING_PLANES < NUM_CLIPPING_PLANES
			float unionClipOpacity = 1.0;
			#pragma unroll_loop_start
			for ( int i = UNION_CLIPPING_PLANES; i < NUM_CLIPPING_PLANES; i ++ ) {
				plane = clippingPlanes[ i ];
				distanceToPlane = - dot( vClipPosition, plane.xyz ) + plane.w;
				distanceGradient = fwidth( distanceToPlane ) / 2.0;
				unionClipOpacity *= 1.0 - smoothstep( - distanceGradient, distanceGradient, distanceToPlane );
			}
			#pragma unroll_loop_end
			clipOpacity *= 1.0 - unionClipOpacity;
		#endif
		diffuseColor.a *= clipOpacity;
		if ( diffuseColor.a == 0.0 ) discard;
	#else
		#pragma unroll_loop_start
		for ( int i = 0; i < UNION_CLIPPING_PLANES; i ++ ) {
			plane = clippingPlanes[ i ];
			if ( dot( vClipPosition, plane.xyz ) > plane.w ) discard;
		}
		#pragma unroll_loop_end
		#if UNION_CLIPPING_PLANES < NUM_CLIPPING_PLANES
			bool clipped = true;
			#pragma unroll_loop_start
			for ( int i = UNION_CLIPPING_PLANES; i < NUM_CLIPPING_PLANES; i ++ ) {
				plane = clippingPlanes[ i ];
				clipped = ( dot( vClipPosition, plane.xyz ) > plane.w ) && clipped;
			}
			#pragma unroll_loop_end
			if ( clipped ) discard;
		#endif
	#endif
#endif`,hm=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
	uniform vec4 clippingPlanes[ NUM_CLIPPING_PLANES ];
#endif`,fm=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
#endif`,dm=`#if NUM_CLIPPING_PLANES > 0
	vClipPosition = - mvPosition.xyz;
#endif`,pm=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	diffuseColor *= vColor;
#endif`,mm=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	varying vec4 vColor;
#endif`,_m=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	varying vec4 vColor;
#endif`,gm=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	vColor = vec4( 1.0 );
#endif
#ifdef USE_COLOR_ALPHA
	vColor *= color;
#elif defined( USE_COLOR )
	vColor.rgb *= color;
#endif
#ifdef USE_INSTANCING_COLOR
	vColor.rgb *= instanceColor.rgb;
#endif
#ifdef USE_BATCHING_COLOR
	vColor *= getBatchingColor( getIndirectIndex( gl_DrawID ) );
#endif`,vm=`#define PI 3.141592653589793
#define PI2 6.283185307179586
#define PI_HALF 1.5707963267948966
#define RECIPROCAL_PI 0.3183098861837907
#define RECIPROCAL_PI2 0.15915494309189535
#define EPSILON 1e-6
#ifndef saturate
#define saturate( a ) clamp( a, 0.0, 1.0 )
#endif
#define whiteComplement( a ) ( 1.0 - saturate( a ) )
float pow2( const in float x ) { return x*x; }
vec3 pow2( const in vec3 x ) { return x*x; }
float pow3( const in float x ) { return x*x*x; }
float pow4( const in float x ) { float x2 = x*x; return x2*x2; }
float max3( const in vec3 v ) { return max( max( v.x, v.y ), v.z ); }
float average( const in vec3 v ) { return dot( v, vec3( 0.3333333 ) ); }
highp float rand( const in vec2 uv ) {
	const highp float a = 12.9898, b = 78.233, c = 43758.5453;
	highp float dt = dot( uv.xy, vec2( a,b ) ), sn = mod( dt, PI );
	return fract( sin( sn ) * c );
}
#ifdef HIGH_PRECISION
	float precisionSafeLength( vec3 v ) { return length( v ); }
#else
	float precisionSafeLength( vec3 v ) {
		float maxComponent = max3( abs( v ) );
		return length( v / maxComponent ) * maxComponent;
	}
#endif
struct IncidentLight {
	vec3 color;
	vec3 direction;
	bool visible;
};
struct ReflectedLight {
	vec3 directDiffuse;
	vec3 directSpecular;
	vec3 indirectDiffuse;
	vec3 indirectSpecular;
};
#ifdef USE_ALPHAHASH
	varying vec3 vPosition;
#endif
vec3 transformDirection( in vec3 dir, in mat4 matrix ) {
	return normalize( ( matrix * vec4( dir, 0.0 ) ).xyz );
}
vec3 inverseTransformDirection( in vec3 dir, in mat4 matrix ) {
	return normalize( ( vec4( dir, 0.0 ) * matrix ).xyz );
}
bool isPerspectiveMatrix( mat4 m ) {
	return m[ 2 ][ 3 ] == - 1.0;
}
vec2 equirectUv( in vec3 dir ) {
	float u = atan( dir.z, dir.x ) * RECIPROCAL_PI2 + 0.5;
	float v = asin( clamp( dir.y, - 1.0, 1.0 ) ) * RECIPROCAL_PI + 0.5;
	return vec2( u, v );
}
vec3 BRDF_Lambert( const in vec3 diffuseColor ) {
	return RECIPROCAL_PI * diffuseColor;
}
vec3 F_Schlick( const in vec3 f0, const in float f90, const in float dotVH ) {
	float fresnel = exp2( ( - 5.55473 * dotVH - 6.98316 ) * dotVH );
	return f0 * ( 1.0 - fresnel ) + ( f90 * fresnel );
}
float F_Schlick( const in float f0, const in float f90, const in float dotVH ) {
	float fresnel = exp2( ( - 5.55473 * dotVH - 6.98316 ) * dotVH );
	return f0 * ( 1.0 - fresnel ) + ( f90 * fresnel );
} // validated`,xm=`#ifdef ENVMAP_TYPE_CUBE_UV
	#define cubeUV_minMipLevel 4.0
	#define cubeUV_minTileSize 16.0
	float getFace( vec3 direction ) {
		vec3 absDirection = abs( direction );
		float face = - 1.0;
		if ( absDirection.x > absDirection.z ) {
			if ( absDirection.x > absDirection.y )
				face = direction.x > 0.0 ? 0.0 : 3.0;
			else
				face = direction.y > 0.0 ? 1.0 : 4.0;
		} else {
			if ( absDirection.z > absDirection.y )
				face = direction.z > 0.0 ? 2.0 : 5.0;
			else
				face = direction.y > 0.0 ? 1.0 : 4.0;
		}
		return face;
	}
	vec2 getUV( vec3 direction, float face ) {
		vec2 uv;
		if ( face == 0.0 ) {
			uv = vec2( direction.z, direction.y ) / abs( direction.x );
		} else if ( face == 1.0 ) {
			uv = vec2( - direction.x, - direction.z ) / abs( direction.y );
		} else if ( face == 2.0 ) {
			uv = vec2( - direction.x, direction.y ) / abs( direction.z );
		} else if ( face == 3.0 ) {
			uv = vec2( - direction.z, direction.y ) / abs( direction.x );
		} else if ( face == 4.0 ) {
			uv = vec2( - direction.x, direction.z ) / abs( direction.y );
		} else {
			uv = vec2( direction.x, direction.y ) / abs( direction.z );
		}
		return 0.5 * ( uv + 1.0 );
	}
	vec3 bilinearCubeUV( sampler2D envMap, vec3 direction, float mipInt ) {
		float face = getFace( direction );
		float filterInt = max( cubeUV_minMipLevel - mipInt, 0.0 );
		mipInt = max( mipInt, cubeUV_minMipLevel );
		float faceSize = exp2( mipInt );
		highp vec2 uv = getUV( direction, face ) * ( faceSize - 2.0 ) + 1.0;
		if ( face > 2.0 ) {
			uv.y += faceSize;
			face -= 3.0;
		}
		uv.x += face * faceSize;
		uv.x += filterInt * 3.0 * cubeUV_minTileSize;
		uv.y += 4.0 * ( exp2( CUBEUV_MAX_MIP ) - faceSize );
		uv.x *= CUBEUV_TEXEL_WIDTH;
		uv.y *= CUBEUV_TEXEL_HEIGHT;
		#ifdef texture2DGradEXT
			return texture2DGradEXT( envMap, uv, vec2( 0.0 ), vec2( 0.0 ) ).rgb;
		#else
			return texture2D( envMap, uv ).rgb;
		#endif
	}
	#define cubeUV_r0 1.0
	#define cubeUV_m0 - 2.0
	#define cubeUV_r1 0.8
	#define cubeUV_m1 - 1.0
	#define cubeUV_r4 0.4
	#define cubeUV_m4 2.0
	#define cubeUV_r5 0.305
	#define cubeUV_m5 3.0
	#define cubeUV_r6 0.21
	#define cubeUV_m6 4.0
	float roughnessToMip( float roughness ) {
		float mip = 0.0;
		if ( roughness >= cubeUV_r1 ) {
			mip = ( cubeUV_r0 - roughness ) * ( cubeUV_m1 - cubeUV_m0 ) / ( cubeUV_r0 - cubeUV_r1 ) + cubeUV_m0;
		} else if ( roughness >= cubeUV_r4 ) {
			mip = ( cubeUV_r1 - roughness ) * ( cubeUV_m4 - cubeUV_m1 ) / ( cubeUV_r1 - cubeUV_r4 ) + cubeUV_m1;
		} else if ( roughness >= cubeUV_r5 ) {
			mip = ( cubeUV_r4 - roughness ) * ( cubeUV_m5 - cubeUV_m4 ) / ( cubeUV_r4 - cubeUV_r5 ) + cubeUV_m4;
		} else if ( roughness >= cubeUV_r6 ) {
			mip = ( cubeUV_r5 - roughness ) * ( cubeUV_m6 - cubeUV_m5 ) / ( cubeUV_r5 - cubeUV_r6 ) + cubeUV_m5;
		} else {
			mip = - 2.0 * log2( 1.16 * roughness );		}
		return mip;
	}
	vec4 textureCubeUV( sampler2D envMap, vec3 sampleDir, float roughness ) {
		float mip = clamp( roughnessToMip( roughness ), cubeUV_m0, CUBEUV_MAX_MIP );
		float mipF = fract( mip );
		float mipInt = floor( mip );
		vec3 color0 = bilinearCubeUV( envMap, sampleDir, mipInt );
		if ( mipF == 0.0 ) {
			return vec4( color0, 1.0 );
		} else {
			vec3 color1 = bilinearCubeUV( envMap, sampleDir, mipInt + 1.0 );
			return vec4( mix( color0, color1, mipF ), 1.0 );
		}
	}
#endif`,Mm=`vec3 transformedNormal = objectNormal;
#ifdef USE_TANGENT
	vec3 transformedTangent = objectTangent;
#endif
#ifdef USE_BATCHING
	mat3 bm = mat3( batchingMatrix );
	transformedNormal /= vec3( dot( bm[ 0 ], bm[ 0 ] ), dot( bm[ 1 ], bm[ 1 ] ), dot( bm[ 2 ], bm[ 2 ] ) );
	transformedNormal = bm * transformedNormal;
	#ifdef USE_TANGENT
		transformedTangent = bm * transformedTangent;
	#endif
#endif
#ifdef USE_INSTANCING
	mat3 im = mat3( instanceMatrix );
	transformedNormal /= vec3( dot( im[ 0 ], im[ 0 ] ), dot( im[ 1 ], im[ 1 ] ), dot( im[ 2 ], im[ 2 ] ) );
	transformedNormal = im * transformedNormal;
	#ifdef USE_TANGENT
		transformedTangent = im * transformedTangent;
	#endif
#endif
transformedNormal = normalMatrix * transformedNormal;
#ifdef FLIP_SIDED
	transformedNormal = - transformedNormal;
#endif
#ifdef USE_TANGENT
	transformedTangent = ( modelViewMatrix * vec4( transformedTangent, 0.0 ) ).xyz;
	#ifdef FLIP_SIDED
		transformedTangent = - transformedTangent;
	#endif
#endif`,Sm=`#ifdef USE_DISPLACEMENTMAP
	uniform sampler2D displacementMap;
	uniform float displacementScale;
	uniform float displacementBias;
#endif`,ym=`#ifdef USE_DISPLACEMENTMAP
	transformed += normalize( objectNormal ) * ( texture2D( displacementMap, vDisplacementMapUv ).x * displacementScale + displacementBias );
#endif`,bm=`#ifdef USE_EMISSIVEMAP
	vec4 emissiveColor = texture2D( emissiveMap, vEmissiveMapUv );
	#ifdef DECODE_VIDEO_TEXTURE_EMISSIVE
		emissiveColor = sRGBTransferEOTF( emissiveColor );
	#endif
	totalEmissiveRadiance *= emissiveColor.rgb;
#endif`,Em=`#ifdef USE_EMISSIVEMAP
	uniform sampler2D emissiveMap;
#endif`,Tm="gl_FragColor = linearToOutputTexel( gl_FragColor );",Am=`vec4 LinearTransferOETF( in vec4 value ) {
	return value;
}
vec4 sRGBTransferEOTF( in vec4 value ) {
	return vec4( mix( pow( value.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), value.rgb * 0.0773993808, vec3( lessThanEqual( value.rgb, vec3( 0.04045 ) ) ) ), value.a );
}
vec4 sRGBTransferOETF( in vec4 value ) {
	return vec4( mix( pow( value.rgb, vec3( 0.41666 ) ) * 1.055 - vec3( 0.055 ), value.rgb * 12.92, vec3( lessThanEqual( value.rgb, vec3( 0.0031308 ) ) ) ), value.a );
}`,wm=`#ifdef USE_ENVMAP
	#ifdef ENV_WORLDPOS
		vec3 cameraToFrag;
		if ( isOrthographic ) {
			cameraToFrag = normalize( vec3( - viewMatrix[ 0 ][ 2 ], - viewMatrix[ 1 ][ 2 ], - viewMatrix[ 2 ][ 2 ] ) );
		} else {
			cameraToFrag = normalize( vWorldPosition - cameraPosition );
		}
		vec3 worldNormal = inverseTransformDirection( normal, viewMatrix );
		#ifdef ENVMAP_MODE_REFLECTION
			vec3 reflectVec = reflect( cameraToFrag, worldNormal );
		#else
			vec3 reflectVec = refract( cameraToFrag, worldNormal, refractionRatio );
		#endif
	#else
		vec3 reflectVec = vReflect;
	#endif
	#ifdef ENVMAP_TYPE_CUBE
		vec4 envColor = textureCube( envMap, envMapRotation * vec3( flipEnvMap * reflectVec.x, reflectVec.yz ) );
		#ifdef ENVMAP_BLENDING_MULTIPLY
			outgoingLight = mix( outgoingLight, outgoingLight * envColor.xyz, specularStrength * reflectivity );
		#elif defined( ENVMAP_BLENDING_MIX )
			outgoingLight = mix( outgoingLight, envColor.xyz, specularStrength * reflectivity );
		#elif defined( ENVMAP_BLENDING_ADD )
			outgoingLight += envColor.xyz * specularStrength * reflectivity;
		#endif
	#endif
#endif`,Rm=`#ifdef USE_ENVMAP
	uniform float envMapIntensity;
	uniform float flipEnvMap;
	uniform mat3 envMapRotation;
	#ifdef ENVMAP_TYPE_CUBE
		uniform samplerCube envMap;
	#else
		uniform sampler2D envMap;
	#endif
#endif`,Cm=`#ifdef USE_ENVMAP
	uniform float reflectivity;
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		varying vec3 vWorldPosition;
		uniform float refractionRatio;
	#else
		varying vec3 vReflect;
	#endif
#endif`,Pm=`#ifdef USE_ENVMAP
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		
		varying vec3 vWorldPosition;
	#else
		varying vec3 vReflect;
		uniform float refractionRatio;
	#endif
#endif`,Dm=`#ifdef USE_ENVMAP
	#ifdef ENV_WORLDPOS
		vWorldPosition = worldPosition.xyz;
	#else
		vec3 cameraToVertex;
		if ( isOrthographic ) {
			cameraToVertex = normalize( vec3( - viewMatrix[ 0 ][ 2 ], - viewMatrix[ 1 ][ 2 ], - viewMatrix[ 2 ][ 2 ] ) );
		} else {
			cameraToVertex = normalize( worldPosition.xyz - cameraPosition );
		}
		vec3 worldNormal = inverseTransformDirection( transformedNormal, viewMatrix );
		#ifdef ENVMAP_MODE_REFLECTION
			vReflect = reflect( cameraToVertex, worldNormal );
		#else
			vReflect = refract( cameraToVertex, worldNormal, refractionRatio );
		#endif
	#endif
#endif`,Lm=`#ifdef USE_FOG
	vFogDepth = - mvPosition.z;
#endif`,Im=`#ifdef USE_FOG
	varying float vFogDepth;
#endif`,Um=`#ifdef USE_FOG
	#ifdef FOG_EXP2
		float fogFactor = 1.0 - exp( - fogDensity * fogDensity * vFogDepth * vFogDepth );
	#else
		float fogFactor = smoothstep( fogNear, fogFar, vFogDepth );
	#endif
	gl_FragColor.rgb = mix( gl_FragColor.rgb, fogColor, fogFactor );
#endif`,Nm=`#ifdef USE_FOG
	uniform vec3 fogColor;
	varying float vFogDepth;
	#ifdef FOG_EXP2
		uniform float fogDensity;
	#else
		uniform float fogNear;
		uniform float fogFar;
	#endif
#endif`,Fm=`#ifdef USE_GRADIENTMAP
	uniform sampler2D gradientMap;
#endif
vec3 getGradientIrradiance( vec3 normal, vec3 lightDirection ) {
	float dotNL = dot( normal, lightDirection );
	vec2 coord = vec2( dotNL * 0.5 + 0.5, 0.0 );
	#ifdef USE_GRADIENTMAP
		return vec3( texture2D( gradientMap, coord ).r );
	#else
		vec2 fw = fwidth( coord ) * 0.5;
		return mix( vec3( 0.7 ), vec3( 1.0 ), smoothstep( 0.7 - fw.x, 0.7 + fw.x, coord.x ) );
	#endif
}`,Om=`#ifdef USE_LIGHTMAP
	uniform sampler2D lightMap;
	uniform float lightMapIntensity;
#endif`,Bm=`LambertMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularStrength = specularStrength;`,km=`varying vec3 vViewPosition;
struct LambertMaterial {
	vec3 diffuseColor;
	float specularStrength;
};
void RE_Direct_Lambert( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in LambertMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectDiffuse_Lambert( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in LambertMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_Lambert
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Lambert`,zm=`uniform bool receiveShadow;
uniform vec3 ambientLightColor;
#if defined( USE_LIGHT_PROBES )
	uniform vec3 lightProbe[ 9 ];
#endif
vec3 shGetIrradianceAt( in vec3 normal, in vec3 shCoefficients[ 9 ] ) {
	float x = normal.x, y = normal.y, z = normal.z;
	vec3 result = shCoefficients[ 0 ] * 0.886227;
	result += shCoefficients[ 1 ] * 2.0 * 0.511664 * y;
	result += shCoefficients[ 2 ] * 2.0 * 0.511664 * z;
	result += shCoefficients[ 3 ] * 2.0 * 0.511664 * x;
	result += shCoefficients[ 4 ] * 2.0 * 0.429043 * x * y;
	result += shCoefficients[ 5 ] * 2.0 * 0.429043 * y * z;
	result += shCoefficients[ 6 ] * ( 0.743125 * z * z - 0.247708 );
	result += shCoefficients[ 7 ] * 2.0 * 0.429043 * x * z;
	result += shCoefficients[ 8 ] * 0.429043 * ( x * x - y * y );
	return result;
}
vec3 getLightProbeIrradiance( const in vec3 lightProbe[ 9 ], const in vec3 normal ) {
	vec3 worldNormal = inverseTransformDirection( normal, viewMatrix );
	vec3 irradiance = shGetIrradianceAt( worldNormal, lightProbe );
	return irradiance;
}
vec3 getAmbientLightIrradiance( const in vec3 ambientLightColor ) {
	vec3 irradiance = ambientLightColor;
	return irradiance;
}
float getDistanceAttenuation( const in float lightDistance, const in float cutoffDistance, const in float decayExponent ) {
	float distanceFalloff = 1.0 / max( pow( lightDistance, decayExponent ), 0.01 );
	if ( cutoffDistance > 0.0 ) {
		distanceFalloff *= pow2( saturate( 1.0 - pow4( lightDistance / cutoffDistance ) ) );
	}
	return distanceFalloff;
}
float getSpotAttenuation( const in float coneCosine, const in float penumbraCosine, const in float angleCosine ) {
	return smoothstep( coneCosine, penumbraCosine, angleCosine );
}
#if NUM_DIR_LIGHTS > 0
	struct DirectionalLight {
		vec3 direction;
		vec3 color;
	};
	uniform DirectionalLight directionalLights[ NUM_DIR_LIGHTS ];
	void getDirectionalLightInfo( const in DirectionalLight directionalLight, out IncidentLight light ) {
		light.color = directionalLight.color;
		light.direction = directionalLight.direction;
		light.visible = true;
	}
#endif
#if NUM_POINT_LIGHTS > 0
	struct PointLight {
		vec3 position;
		vec3 color;
		float distance;
		float decay;
	};
	uniform PointLight pointLights[ NUM_POINT_LIGHTS ];
	void getPointLightInfo( const in PointLight pointLight, const in vec3 geometryPosition, out IncidentLight light ) {
		vec3 lVector = pointLight.position - geometryPosition;
		light.direction = normalize( lVector );
		float lightDistance = length( lVector );
		light.color = pointLight.color;
		light.color *= getDistanceAttenuation( lightDistance, pointLight.distance, pointLight.decay );
		light.visible = ( light.color != vec3( 0.0 ) );
	}
#endif
#if NUM_SPOT_LIGHTS > 0
	struct SpotLight {
		vec3 position;
		vec3 direction;
		vec3 color;
		float distance;
		float decay;
		float coneCos;
		float penumbraCos;
	};
	uniform SpotLight spotLights[ NUM_SPOT_LIGHTS ];
	void getSpotLightInfo( const in SpotLight spotLight, const in vec3 geometryPosition, out IncidentLight light ) {
		vec3 lVector = spotLight.position - geometryPosition;
		light.direction = normalize( lVector );
		float angleCos = dot( light.direction, spotLight.direction );
		float spotAttenuation = getSpotAttenuation( spotLight.coneCos, spotLight.penumbraCos, angleCos );
		if ( spotAttenuation > 0.0 ) {
			float lightDistance = length( lVector );
			light.color = spotLight.color * spotAttenuation;
			light.color *= getDistanceAttenuation( lightDistance, spotLight.distance, spotLight.decay );
			light.visible = ( light.color != vec3( 0.0 ) );
		} else {
			light.color = vec3( 0.0 );
			light.visible = false;
		}
	}
#endif
#if NUM_RECT_AREA_LIGHTS > 0
	struct RectAreaLight {
		vec3 color;
		vec3 position;
		vec3 halfWidth;
		vec3 halfHeight;
	};
	uniform sampler2D ltc_1;	uniform sampler2D ltc_2;
	uniform RectAreaLight rectAreaLights[ NUM_RECT_AREA_LIGHTS ];
#endif
#if NUM_HEMI_LIGHTS > 0
	struct HemisphereLight {
		vec3 direction;
		vec3 skyColor;
		vec3 groundColor;
	};
	uniform HemisphereLight hemisphereLights[ NUM_HEMI_LIGHTS ];
	vec3 getHemisphereLightIrradiance( const in HemisphereLight hemiLight, const in vec3 normal ) {
		float dotNL = dot( normal, hemiLight.direction );
		float hemiDiffuseWeight = 0.5 * dotNL + 0.5;
		vec3 irradiance = mix( hemiLight.groundColor, hemiLight.skyColor, hemiDiffuseWeight );
		return irradiance;
	}
#endif`,Vm=`#ifdef USE_ENVMAP
	vec3 getIBLIrradiance( const in vec3 normal ) {
		#ifdef ENVMAP_TYPE_CUBE_UV
			vec3 worldNormal = inverseTransformDirection( normal, viewMatrix );
			vec4 envMapColor = textureCubeUV( envMap, envMapRotation * worldNormal, 1.0 );
			return PI * envMapColor.rgb * envMapIntensity;
		#else
			return vec3( 0.0 );
		#endif
	}
	vec3 getIBLRadiance( const in vec3 viewDir, const in vec3 normal, const in float roughness ) {
		#ifdef ENVMAP_TYPE_CUBE_UV
			vec3 reflectVec = reflect( - viewDir, normal );
			reflectVec = normalize( mix( reflectVec, normal, pow4( roughness ) ) );
			reflectVec = inverseTransformDirection( reflectVec, viewMatrix );
			vec4 envMapColor = textureCubeUV( envMap, envMapRotation * reflectVec, roughness );
			return envMapColor.rgb * envMapIntensity;
		#else
			return vec3( 0.0 );
		#endif
	}
	#ifdef USE_ANISOTROPY
		vec3 getIBLAnisotropyRadiance( const in vec3 viewDir, const in vec3 normal, const in float roughness, const in vec3 bitangent, const in float anisotropy ) {
			#ifdef ENVMAP_TYPE_CUBE_UV
				vec3 bentNormal = cross( bitangent, viewDir );
				bentNormal = normalize( cross( bentNormal, bitangent ) );
				bentNormal = normalize( mix( bentNormal, normal, pow2( pow2( 1.0 - anisotropy * ( 1.0 - roughness ) ) ) ) );
				return getIBLRadiance( viewDir, bentNormal, roughness );
			#else
				return vec3( 0.0 );
			#endif
		}
	#endif
#endif`,Gm=`ToonMaterial material;
material.diffuseColor = diffuseColor.rgb;`,Hm=`varying vec3 vViewPosition;
struct ToonMaterial {
	vec3 diffuseColor;
};
void RE_Direct_Toon( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in ToonMaterial material, inout ReflectedLight reflectedLight ) {
	vec3 irradiance = getGradientIrradiance( geometryNormal, directLight.direction ) * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
void RE_IndirectDiffuse_Toon( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in ToonMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_Toon
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Toon`,Wm=`BlinnPhongMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularColor = specular;
material.specularShininess = shininess;
material.specularStrength = specularStrength;`,Xm=`varying vec3 vViewPosition;
struct BlinnPhongMaterial {
	vec3 diffuseColor;
	vec3 specularColor;
	float specularShininess;
	float specularStrength;
};
void RE_Direct_BlinnPhong( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in BlinnPhongMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
	reflectedLight.directSpecular += irradiance * BRDF_BlinnPhong( directLight.direction, geometryViewDir, geometryNormal, material.specularColor, material.specularShininess ) * material.specularStrength;
}
void RE_IndirectDiffuse_BlinnPhong( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in BlinnPhongMaterial material, inout ReflectedLight reflectedLight ) {
	reflectedLight.indirectDiffuse += irradiance * BRDF_Lambert( material.diffuseColor );
}
#define RE_Direct				RE_Direct_BlinnPhong
#define RE_IndirectDiffuse		RE_IndirectDiffuse_BlinnPhong`,qm=`PhysicalMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.diffuseContribution = diffuseColor.rgb * ( 1.0 - metalnessFactor );
material.metalness = metalnessFactor;
vec3 dxy = max( abs( dFdx( nonPerturbedNormal ) ), abs( dFdy( nonPerturbedNormal ) ) );
float geometryRoughness = max( max( dxy.x, dxy.y ), dxy.z );
material.roughness = max( roughnessFactor, 0.0525 );material.roughness += geometryRoughness;
material.roughness = min( material.roughness, 1.0 );
#ifdef IOR
	material.ior = ior;
	#ifdef USE_SPECULAR
		float specularIntensityFactor = specularIntensity;
		vec3 specularColorFactor = specularColor;
		#ifdef USE_SPECULAR_COLORMAP
			specularColorFactor *= texture2D( specularColorMap, vSpecularColorMapUv ).rgb;
		#endif
		#ifdef USE_SPECULAR_INTENSITYMAP
			specularIntensityFactor *= texture2D( specularIntensityMap, vSpecularIntensityMapUv ).a;
		#endif
		material.specularF90 = mix( specularIntensityFactor, 1.0, metalnessFactor );
	#else
		float specularIntensityFactor = 1.0;
		vec3 specularColorFactor = vec3( 1.0 );
		material.specularF90 = 1.0;
	#endif
	material.specularColor = min( pow2( ( material.ior - 1.0 ) / ( material.ior + 1.0 ) ) * specularColorFactor, vec3( 1.0 ) ) * specularIntensityFactor;
	material.specularColorBlended = mix( material.specularColor, diffuseColor.rgb, metalnessFactor );
#else
	material.specularColor = vec3( 0.04 );
	material.specularColorBlended = mix( material.specularColor, diffuseColor.rgb, metalnessFactor );
	material.specularF90 = 1.0;
#endif
#ifdef USE_CLEARCOAT
	material.clearcoat = clearcoat;
	material.clearcoatRoughness = clearcoatRoughness;
	material.clearcoatF0 = vec3( 0.04 );
	material.clearcoatF90 = 1.0;
	#ifdef USE_CLEARCOATMAP
		material.clearcoat *= texture2D( clearcoatMap, vClearcoatMapUv ).x;
	#endif
	#ifdef USE_CLEARCOAT_ROUGHNESSMAP
		material.clearcoatRoughness *= texture2D( clearcoatRoughnessMap, vClearcoatRoughnessMapUv ).y;
	#endif
	material.clearcoat = saturate( material.clearcoat );	material.clearcoatRoughness = max( material.clearcoatRoughness, 0.0525 );
	material.clearcoatRoughness += geometryRoughness;
	material.clearcoatRoughness = min( material.clearcoatRoughness, 1.0 );
#endif
#ifdef USE_DISPERSION
	material.dispersion = dispersion;
#endif
#ifdef USE_IRIDESCENCE
	material.iridescence = iridescence;
	material.iridescenceIOR = iridescenceIOR;
	#ifdef USE_IRIDESCENCEMAP
		material.iridescence *= texture2D( iridescenceMap, vIridescenceMapUv ).r;
	#endif
	#ifdef USE_IRIDESCENCE_THICKNESSMAP
		material.iridescenceThickness = (iridescenceThicknessMaximum - iridescenceThicknessMinimum) * texture2D( iridescenceThicknessMap, vIridescenceThicknessMapUv ).g + iridescenceThicknessMinimum;
	#else
		material.iridescenceThickness = iridescenceThicknessMaximum;
	#endif
#endif
#ifdef USE_SHEEN
	material.sheenColor = sheenColor;
	#ifdef USE_SHEEN_COLORMAP
		material.sheenColor *= texture2D( sheenColorMap, vSheenColorMapUv ).rgb;
	#endif
	material.sheenRoughness = clamp( sheenRoughness, 0.0001, 1.0 );
	#ifdef USE_SHEEN_ROUGHNESSMAP
		material.sheenRoughness *= texture2D( sheenRoughnessMap, vSheenRoughnessMapUv ).a;
	#endif
#endif
#ifdef USE_ANISOTROPY
	#ifdef USE_ANISOTROPYMAP
		mat2 anisotropyMat = mat2( anisotropyVector.x, anisotropyVector.y, - anisotropyVector.y, anisotropyVector.x );
		vec3 anisotropyPolar = texture2D( anisotropyMap, vAnisotropyMapUv ).rgb;
		vec2 anisotropyV = anisotropyMat * normalize( 2.0 * anisotropyPolar.rg - vec2( 1.0 ) ) * anisotropyPolar.b;
	#else
		vec2 anisotropyV = anisotropyVector;
	#endif
	material.anisotropy = length( anisotropyV );
	if( material.anisotropy == 0.0 ) {
		anisotropyV = vec2( 1.0, 0.0 );
	} else {
		anisotropyV /= material.anisotropy;
		material.anisotropy = saturate( material.anisotropy );
	}
	material.alphaT = mix( pow2( material.roughness ), 1.0, pow2( material.anisotropy ) );
	material.anisotropyT = tbn[ 0 ] * anisotropyV.x + tbn[ 1 ] * anisotropyV.y;
	material.anisotropyB = tbn[ 1 ] * anisotropyV.x - tbn[ 0 ] * anisotropyV.y;
#endif`,Ym=`uniform sampler2D dfgLUT;
struct PhysicalMaterial {
	vec3 diffuseColor;
	vec3 diffuseContribution;
	vec3 specularColor;
	vec3 specularColorBlended;
	float roughness;
	float metalness;
	float specularF90;
	float dispersion;
	#ifdef USE_CLEARCOAT
		float clearcoat;
		float clearcoatRoughness;
		vec3 clearcoatF0;
		float clearcoatF90;
	#endif
	#ifdef USE_IRIDESCENCE
		float iridescence;
		float iridescenceIOR;
		float iridescenceThickness;
		vec3 iridescenceFresnel;
		vec3 iridescenceF0;
		vec3 iridescenceFresnelDielectric;
		vec3 iridescenceFresnelMetallic;
	#endif
	#ifdef USE_SHEEN
		vec3 sheenColor;
		float sheenRoughness;
	#endif
	#ifdef IOR
		float ior;
	#endif
	#ifdef USE_TRANSMISSION
		float transmission;
		float transmissionAlpha;
		float thickness;
		float attenuationDistance;
		vec3 attenuationColor;
	#endif
	#ifdef USE_ANISOTROPY
		float anisotropy;
		float alphaT;
		vec3 anisotropyT;
		vec3 anisotropyB;
	#endif
};
vec3 clearcoatSpecularDirect = vec3( 0.0 );
vec3 clearcoatSpecularIndirect = vec3( 0.0 );
vec3 sheenSpecularDirect = vec3( 0.0 );
vec3 sheenSpecularIndirect = vec3(0.0 );
vec3 Schlick_to_F0( const in vec3 f, const in float f90, const in float dotVH ) {
    float x = clamp( 1.0 - dotVH, 0.0, 1.0 );
    float x2 = x * x;
    float x5 = clamp( x * x2 * x2, 0.0, 0.9999 );
    return ( f - vec3( f90 ) * x5 ) / ( 1.0 - x5 );
}
float V_GGX_SmithCorrelated( const in float alpha, const in float dotNL, const in float dotNV ) {
	float a2 = pow2( alpha );
	float gv = dotNL * sqrt( a2 + ( 1.0 - a2 ) * pow2( dotNV ) );
	float gl = dotNV * sqrt( a2 + ( 1.0 - a2 ) * pow2( dotNL ) );
	return 0.5 / max( gv + gl, EPSILON );
}
float D_GGX( const in float alpha, const in float dotNH ) {
	float a2 = pow2( alpha );
	float denom = pow2( dotNH ) * ( a2 - 1.0 ) + 1.0;
	return RECIPROCAL_PI * a2 / pow2( denom );
}
#ifdef USE_ANISOTROPY
	float V_GGX_SmithCorrelated_Anisotropic( const in float alphaT, const in float alphaB, const in float dotTV, const in float dotBV, const in float dotTL, const in float dotBL, const in float dotNV, const in float dotNL ) {
		float gv = dotNL * length( vec3( alphaT * dotTV, alphaB * dotBV, dotNV ) );
		float gl = dotNV * length( vec3( alphaT * dotTL, alphaB * dotBL, dotNL ) );
		float v = 0.5 / ( gv + gl );
		return v;
	}
	float D_GGX_Anisotropic( const in float alphaT, const in float alphaB, const in float dotNH, const in float dotTH, const in float dotBH ) {
		float a2 = alphaT * alphaB;
		highp vec3 v = vec3( alphaB * dotTH, alphaT * dotBH, a2 * dotNH );
		highp float v2 = dot( v, v );
		float w2 = a2 / v2;
		return RECIPROCAL_PI * a2 * pow2 ( w2 );
	}
#endif
#ifdef USE_CLEARCOAT
	vec3 BRDF_GGX_Clearcoat( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material) {
		vec3 f0 = material.clearcoatF0;
		float f90 = material.clearcoatF90;
		float roughness = material.clearcoatRoughness;
		float alpha = pow2( roughness );
		vec3 halfDir = normalize( lightDir + viewDir );
		float dotNL = saturate( dot( normal, lightDir ) );
		float dotNV = saturate( dot( normal, viewDir ) );
		float dotNH = saturate( dot( normal, halfDir ) );
		float dotVH = saturate( dot( viewDir, halfDir ) );
		vec3 F = F_Schlick( f0, f90, dotVH );
		float V = V_GGX_SmithCorrelated( alpha, dotNL, dotNV );
		float D = D_GGX( alpha, dotNH );
		return F * ( V * D );
	}
#endif
vec3 BRDF_GGX( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material ) {
	vec3 f0 = material.specularColorBlended;
	float f90 = material.specularF90;
	float roughness = material.roughness;
	float alpha = pow2( roughness );
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	float dotNH = saturate( dot( normal, halfDir ) );
	float dotVH = saturate( dot( viewDir, halfDir ) );
	vec3 F = F_Schlick( f0, f90, dotVH );
	#ifdef USE_IRIDESCENCE
		F = mix( F, material.iridescenceFresnel, material.iridescence );
	#endif
	#ifdef USE_ANISOTROPY
		float dotTL = dot( material.anisotropyT, lightDir );
		float dotTV = dot( material.anisotropyT, viewDir );
		float dotTH = dot( material.anisotropyT, halfDir );
		float dotBL = dot( material.anisotropyB, lightDir );
		float dotBV = dot( material.anisotropyB, viewDir );
		float dotBH = dot( material.anisotropyB, halfDir );
		float V = V_GGX_SmithCorrelated_Anisotropic( material.alphaT, alpha, dotTV, dotBV, dotTL, dotBL, dotNV, dotNL );
		float D = D_GGX_Anisotropic( material.alphaT, alpha, dotNH, dotTH, dotBH );
	#else
		float V = V_GGX_SmithCorrelated( alpha, dotNL, dotNV );
		float D = D_GGX( alpha, dotNH );
	#endif
	return F * ( V * D );
}
vec2 LTC_Uv( const in vec3 N, const in vec3 V, const in float roughness ) {
	const float LUT_SIZE = 64.0;
	const float LUT_SCALE = ( LUT_SIZE - 1.0 ) / LUT_SIZE;
	const float LUT_BIAS = 0.5 / LUT_SIZE;
	float dotNV = saturate( dot( N, V ) );
	vec2 uv = vec2( roughness, sqrt( 1.0 - dotNV ) );
	uv = uv * LUT_SCALE + LUT_BIAS;
	return uv;
}
float LTC_ClippedSphereFormFactor( const in vec3 f ) {
	float l = length( f );
	return max( ( l * l + f.z ) / ( l + 1.0 ), 0.0 );
}
vec3 LTC_EdgeVectorFormFactor( const in vec3 v1, const in vec3 v2 ) {
	float x = dot( v1, v2 );
	float y = abs( x );
	float a = 0.8543985 + ( 0.4965155 + 0.0145206 * y ) * y;
	float b = 3.4175940 + ( 4.1616724 + y ) * y;
	float v = a / b;
	float theta_sintheta = ( x > 0.0 ) ? v : 0.5 * inversesqrt( max( 1.0 - x * x, 1e-7 ) ) - v;
	return cross( v1, v2 ) * theta_sintheta;
}
vec3 LTC_Evaluate( const in vec3 N, const in vec3 V, const in vec3 P, const in mat3 mInv, const in vec3 rectCoords[ 4 ] ) {
	vec3 v1 = rectCoords[ 1 ] - rectCoords[ 0 ];
	vec3 v2 = rectCoords[ 3 ] - rectCoords[ 0 ];
	vec3 lightNormal = cross( v1, v2 );
	if( dot( lightNormal, P - rectCoords[ 0 ] ) < 0.0 ) return vec3( 0.0 );
	vec3 T1, T2;
	T1 = normalize( V - N * dot( V, N ) );
	T2 = - cross( N, T1 );
	mat3 mat = mInv * transpose( mat3( T1, T2, N ) );
	vec3 coords[ 4 ];
	coords[ 0 ] = mat * ( rectCoords[ 0 ] - P );
	coords[ 1 ] = mat * ( rectCoords[ 1 ] - P );
	coords[ 2 ] = mat * ( rectCoords[ 2 ] - P );
	coords[ 3 ] = mat * ( rectCoords[ 3 ] - P );
	coords[ 0 ] = normalize( coords[ 0 ] );
	coords[ 1 ] = normalize( coords[ 1 ] );
	coords[ 2 ] = normalize( coords[ 2 ] );
	coords[ 3 ] = normalize( coords[ 3 ] );
	vec3 vectorFormFactor = vec3( 0.0 );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 0 ], coords[ 1 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 1 ], coords[ 2 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 2 ], coords[ 3 ] );
	vectorFormFactor += LTC_EdgeVectorFormFactor( coords[ 3 ], coords[ 0 ] );
	float result = LTC_ClippedSphereFormFactor( vectorFormFactor );
	return vec3( result );
}
#if defined( USE_SHEEN )
float D_Charlie( float roughness, float dotNH ) {
	float alpha = pow2( roughness );
	float invAlpha = 1.0 / alpha;
	float cos2h = dotNH * dotNH;
	float sin2h = max( 1.0 - cos2h, 0.0078125 );
	return ( 2.0 + invAlpha ) * pow( sin2h, invAlpha * 0.5 ) / ( 2.0 * PI );
}
float V_Neubelt( float dotNV, float dotNL ) {
	return saturate( 1.0 / ( 4.0 * ( dotNL + dotNV - dotNL * dotNV ) ) );
}
vec3 BRDF_Sheen( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, vec3 sheenColor, const in float sheenRoughness ) {
	vec3 halfDir = normalize( lightDir + viewDir );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	float dotNH = saturate( dot( normal, halfDir ) );
	float D = D_Charlie( sheenRoughness, dotNH );
	float V = V_Neubelt( dotNV, dotNL );
	return sheenColor * ( D * V );
}
#endif
float IBLSheenBRDF( const in vec3 normal, const in vec3 viewDir, const in float roughness ) {
	float dotNV = saturate( dot( normal, viewDir ) );
	float r2 = roughness * roughness;
	float rInv = 1.0 / ( roughness + 0.1 );
	float a = -1.9362 + 1.0678 * roughness + 0.4573 * r2 - 0.8469 * rInv;
	float b = -0.6014 + 0.5538 * roughness - 0.4670 * r2 - 0.1255 * rInv;
	float DG = exp( a * dotNV + b );
	return saturate( DG );
}
vec3 EnvironmentBRDF( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float roughness ) {
	float dotNV = saturate( dot( normal, viewDir ) );
	vec2 fab = texture2D( dfgLUT, vec2( roughness, dotNV ) ).rg;
	return specularColor * fab.x + specularF90 * fab.y;
}
#ifdef USE_IRIDESCENCE
void computeMultiscatteringIridescence( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float iridescence, const in vec3 iridescenceF0, const in float roughness, inout vec3 singleScatter, inout vec3 multiScatter ) {
#else
void computeMultiscattering( const in vec3 normal, const in vec3 viewDir, const in vec3 specularColor, const in float specularF90, const in float roughness, inout vec3 singleScatter, inout vec3 multiScatter ) {
#endif
	float dotNV = saturate( dot( normal, viewDir ) );
	vec2 fab = texture2D( dfgLUT, vec2( roughness, dotNV ) ).rg;
	#ifdef USE_IRIDESCENCE
		vec3 Fr = mix( specularColor, iridescenceF0, iridescence );
	#else
		vec3 Fr = specularColor;
	#endif
	vec3 FssEss = Fr * fab.x + specularF90 * fab.y;
	float Ess = fab.x + fab.y;
	float Ems = 1.0 - Ess;
	vec3 Favg = Fr + ( 1.0 - Fr ) * 0.047619;	vec3 Fms = FssEss * Favg / ( 1.0 - Ems * Favg );
	singleScatter += FssEss;
	multiScatter += Fms * Ems;
}
vec3 BRDF_GGX_Multiscatter( const in vec3 lightDir, const in vec3 viewDir, const in vec3 normal, const in PhysicalMaterial material ) {
	vec3 singleScatter = BRDF_GGX( lightDir, viewDir, normal, material );
	float dotNL = saturate( dot( normal, lightDir ) );
	float dotNV = saturate( dot( normal, viewDir ) );
	vec2 dfgV = texture2D( dfgLUT, vec2( material.roughness, dotNV ) ).rg;
	vec2 dfgL = texture2D( dfgLUT, vec2( material.roughness, dotNL ) ).rg;
	vec3 FssEss_V = material.specularColorBlended * dfgV.x + material.specularF90 * dfgV.y;
	vec3 FssEss_L = material.specularColorBlended * dfgL.x + material.specularF90 * dfgL.y;
	float Ess_V = dfgV.x + dfgV.y;
	float Ess_L = dfgL.x + dfgL.y;
	float Ems_V = 1.0 - Ess_V;
	float Ems_L = 1.0 - Ess_L;
	vec3 Favg = material.specularColorBlended + ( 1.0 - material.specularColorBlended ) * 0.047619;
	vec3 Fms = FssEss_V * FssEss_L * Favg / ( 1.0 - Ems_V * Ems_L * Favg + EPSILON );
	float compensationFactor = Ems_V * Ems_L;
	vec3 multiScatter = Fms * compensationFactor;
	return singleScatter + multiScatter;
}
#if NUM_RECT_AREA_LIGHTS > 0
	void RE_Direct_RectArea_Physical( const in RectAreaLight rectAreaLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
		vec3 normal = geometryNormal;
		vec3 viewDir = geometryViewDir;
		vec3 position = geometryPosition;
		vec3 lightPos = rectAreaLight.position;
		vec3 halfWidth = rectAreaLight.halfWidth;
		vec3 halfHeight = rectAreaLight.halfHeight;
		vec3 lightColor = rectAreaLight.color;
		float roughness = material.roughness;
		vec3 rectCoords[ 4 ];
		rectCoords[ 0 ] = lightPos + halfWidth - halfHeight;		rectCoords[ 1 ] = lightPos - halfWidth - halfHeight;
		rectCoords[ 2 ] = lightPos - halfWidth + halfHeight;
		rectCoords[ 3 ] = lightPos + halfWidth + halfHeight;
		vec2 uv = LTC_Uv( normal, viewDir, roughness );
		vec4 t1 = texture2D( ltc_1, uv );
		vec4 t2 = texture2D( ltc_2, uv );
		mat3 mInv = mat3(
			vec3( t1.x, 0, t1.y ),
			vec3(    0, 1,    0 ),
			vec3( t1.z, 0, t1.w )
		);
		vec3 fresnel = ( material.specularColorBlended * t2.x + ( material.specularF90 - material.specularColorBlended ) * t2.y );
		reflectedLight.directSpecular += lightColor * fresnel * LTC_Evaluate( normal, viewDir, position, mInv, rectCoords );
		reflectedLight.directDiffuse += lightColor * material.diffuseContribution * LTC_Evaluate( normal, viewDir, position, mat3( 1.0 ), rectCoords );
		#ifdef USE_CLEARCOAT
			vec3 Ncc = geometryClearcoatNormal;
			vec2 uvClearcoat = LTC_Uv( Ncc, viewDir, material.clearcoatRoughness );
			vec4 t1Clearcoat = texture2D( ltc_1, uvClearcoat );
			vec4 t2Clearcoat = texture2D( ltc_2, uvClearcoat );
			mat3 mInvClearcoat = mat3(
				vec3( t1Clearcoat.x, 0, t1Clearcoat.y ),
				vec3(             0, 1,             0 ),
				vec3( t1Clearcoat.z, 0, t1Clearcoat.w )
			);
			vec3 fresnelClearcoat = material.clearcoatF0 * t2Clearcoat.x + ( material.clearcoatF90 - material.clearcoatF0 ) * t2Clearcoat.y;
			clearcoatSpecularDirect += lightColor * fresnelClearcoat * LTC_Evaluate( Ncc, viewDir, position, mInvClearcoat, rectCoords );
		#endif
	}
#endif
void RE_Direct_Physical( const in IncidentLight directLight, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
	float dotNL = saturate( dot( geometryNormal, directLight.direction ) );
	vec3 irradiance = dotNL * directLight.color;
	#ifdef USE_CLEARCOAT
		float dotNLcc = saturate( dot( geometryClearcoatNormal, directLight.direction ) );
		vec3 ccIrradiance = dotNLcc * directLight.color;
		clearcoatSpecularDirect += ccIrradiance * BRDF_GGX_Clearcoat( directLight.direction, geometryViewDir, geometryClearcoatNormal, material );
	#endif
	#ifdef USE_SHEEN
 
 		sheenSpecularDirect += irradiance * BRDF_Sheen( directLight.direction, geometryViewDir, geometryNormal, material.sheenColor, material.sheenRoughness );
 
 		float sheenAlbedoV = IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness );
 		float sheenAlbedoL = IBLSheenBRDF( geometryNormal, directLight.direction, material.sheenRoughness );
 
 		float sheenEnergyComp = 1.0 - max3( material.sheenColor ) * max( sheenAlbedoV, sheenAlbedoL );
 
 		irradiance *= sheenEnergyComp;
 
 	#endif
	reflectedLight.directSpecular += irradiance * BRDF_GGX_Multiscatter( directLight.direction, geometryViewDir, geometryNormal, material );
	reflectedLight.directDiffuse += irradiance * BRDF_Lambert( material.diffuseContribution );
}
void RE_IndirectDiffuse_Physical( const in vec3 irradiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight ) {
	vec3 diffuse = irradiance * BRDF_Lambert( material.diffuseContribution );
	#ifdef USE_SHEEN
		float sheenAlbedo = IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness );
		float sheenEnergyComp = 1.0 - max3( material.sheenColor ) * sheenAlbedo;
		diffuse *= sheenEnergyComp;
	#endif
	reflectedLight.indirectDiffuse += diffuse;
}
void RE_IndirectSpecular_Physical( const in vec3 radiance, const in vec3 irradiance, const in vec3 clearcoatRadiance, const in vec3 geometryPosition, const in vec3 geometryNormal, const in vec3 geometryViewDir, const in vec3 geometryClearcoatNormal, const in PhysicalMaterial material, inout ReflectedLight reflectedLight) {
	#ifdef USE_CLEARCOAT
		clearcoatSpecularIndirect += clearcoatRadiance * EnvironmentBRDF( geometryClearcoatNormal, geometryViewDir, material.clearcoatF0, material.clearcoatF90, material.clearcoatRoughness );
	#endif
	#ifdef USE_SHEEN
		sheenSpecularIndirect += irradiance * material.sheenColor * IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness ) * RECIPROCAL_PI;
 	#endif
	vec3 singleScatteringDielectric = vec3( 0.0 );
	vec3 multiScatteringDielectric = vec3( 0.0 );
	vec3 singleScatteringMetallic = vec3( 0.0 );
	vec3 multiScatteringMetallic = vec3( 0.0 );
	#ifdef USE_IRIDESCENCE
		computeMultiscatteringIridescence( geometryNormal, geometryViewDir, material.specularColor, material.specularF90, material.iridescence, material.iridescenceFresnelDielectric, material.roughness, singleScatteringDielectric, multiScatteringDielectric );
		computeMultiscatteringIridescence( geometryNormal, geometryViewDir, material.diffuseColor, material.specularF90, material.iridescence, material.iridescenceFresnelMetallic, material.roughness, singleScatteringMetallic, multiScatteringMetallic );
	#else
		computeMultiscattering( geometryNormal, geometryViewDir, material.specularColor, material.specularF90, material.roughness, singleScatteringDielectric, multiScatteringDielectric );
		computeMultiscattering( geometryNormal, geometryViewDir, material.diffuseColor, material.specularF90, material.roughness, singleScatteringMetallic, multiScatteringMetallic );
	#endif
	vec3 singleScattering = mix( singleScatteringDielectric, singleScatteringMetallic, material.metalness );
	vec3 multiScattering = mix( multiScatteringDielectric, multiScatteringMetallic, material.metalness );
	vec3 totalScatteringDielectric = singleScatteringDielectric + multiScatteringDielectric;
	vec3 diffuse = material.diffuseContribution * ( 1.0 - totalScatteringDielectric );
	vec3 cosineWeightedIrradiance = irradiance * RECIPROCAL_PI;
	vec3 indirectSpecular = radiance * singleScattering;
	indirectSpecular += multiScattering * cosineWeightedIrradiance;
	vec3 indirectDiffuse = diffuse * cosineWeightedIrradiance;
	#ifdef USE_SHEEN
		float sheenAlbedo = IBLSheenBRDF( geometryNormal, geometryViewDir, material.sheenRoughness );
		float sheenEnergyComp = 1.0 - max3( material.sheenColor ) * sheenAlbedo;
		indirectSpecular *= sheenEnergyComp;
		indirectDiffuse *= sheenEnergyComp;
	#endif
	reflectedLight.indirectSpecular += indirectSpecular;
	reflectedLight.indirectDiffuse += indirectDiffuse;
}
#define RE_Direct				RE_Direct_Physical
#define RE_Direct_RectArea		RE_Direct_RectArea_Physical
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Physical
#define RE_IndirectSpecular		RE_IndirectSpecular_Physical
float computeSpecularOcclusion( const in float dotNV, const in float ambientOcclusion, const in float roughness ) {
	return saturate( pow( dotNV + ambientOcclusion, exp2( - 16.0 * roughness - 1.0 ) ) - 1.0 + ambientOcclusion );
}`,$m=`
vec3 geometryPosition = - vViewPosition;
vec3 geometryNormal = normal;
vec3 geometryViewDir = ( isOrthographic ) ? vec3( 0, 0, 1 ) : normalize( vViewPosition );
vec3 geometryClearcoatNormal = vec3( 0.0 );
#ifdef USE_CLEARCOAT
	geometryClearcoatNormal = clearcoatNormal;
#endif
#ifdef USE_IRIDESCENCE
	float dotNVi = saturate( dot( normal, geometryViewDir ) );
	if ( material.iridescenceThickness == 0.0 ) {
		material.iridescence = 0.0;
	} else {
		material.iridescence = saturate( material.iridescence );
	}
	if ( material.iridescence > 0.0 ) {
		material.iridescenceFresnelDielectric = evalIridescence( 1.0, material.iridescenceIOR, dotNVi, material.iridescenceThickness, material.specularColor );
		material.iridescenceFresnelMetallic = evalIridescence( 1.0, material.iridescenceIOR, dotNVi, material.iridescenceThickness, material.diffuseColor );
		material.iridescenceFresnel = mix( material.iridescenceFresnelDielectric, material.iridescenceFresnelMetallic, material.metalness );
		material.iridescenceF0 = Schlick_to_F0( material.iridescenceFresnel, 1.0, dotNVi );
	}
#endif
IncidentLight directLight;
#if ( NUM_POINT_LIGHTS > 0 ) && defined( RE_Direct )
	PointLight pointLight;
	#if defined( USE_SHADOWMAP ) && NUM_POINT_LIGHT_SHADOWS > 0
	PointLightShadow pointLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_POINT_LIGHTS; i ++ ) {
		pointLight = pointLights[ i ];
		getPointLightInfo( pointLight, geometryPosition, directLight );
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_POINT_LIGHT_SHADOWS ) && ( defined( SHADOWMAP_TYPE_PCF ) || defined( SHADOWMAP_TYPE_BASIC ) )
		pointLightShadow = pointLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getPointShadow( pointShadowMap[ i ], pointLightShadow.shadowMapSize, pointLightShadow.shadowIntensity, pointLightShadow.shadowBias, pointLightShadow.shadowRadius, vPointShadowCoord[ i ], pointLightShadow.shadowCameraNear, pointLightShadow.shadowCameraFar ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_SPOT_LIGHTS > 0 ) && defined( RE_Direct )
	SpotLight spotLight;
	vec4 spotColor;
	vec3 spotLightCoord;
	bool inSpotLightMap;
	#if defined( USE_SHADOWMAP ) && NUM_SPOT_LIGHT_SHADOWS > 0
	SpotLightShadow spotLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHTS; i ++ ) {
		spotLight = spotLights[ i ];
		getSpotLightInfo( spotLight, geometryPosition, directLight );
		#if ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS )
		#define SPOT_LIGHT_MAP_INDEX UNROLLED_LOOP_INDEX
		#elif ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
		#define SPOT_LIGHT_MAP_INDEX NUM_SPOT_LIGHT_MAPS
		#else
		#define SPOT_LIGHT_MAP_INDEX ( UNROLLED_LOOP_INDEX - NUM_SPOT_LIGHT_SHADOWS + NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS )
		#endif
		#if ( SPOT_LIGHT_MAP_INDEX < NUM_SPOT_LIGHT_MAPS )
			spotLightCoord = vSpotLightCoord[ i ].xyz / vSpotLightCoord[ i ].w;
			inSpotLightMap = all( lessThan( abs( spotLightCoord * 2. - 1. ), vec3( 1.0 ) ) );
			spotColor = texture2D( spotLightMap[ SPOT_LIGHT_MAP_INDEX ], spotLightCoord.xy );
			directLight.color = inSpotLightMap ? directLight.color * spotColor.rgb : directLight.color;
		#endif
		#undef SPOT_LIGHT_MAP_INDEX
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
		spotLightShadow = spotLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getShadow( spotShadowMap[ i ], spotLightShadow.shadowMapSize, spotLightShadow.shadowIntensity, spotLightShadow.shadowBias, spotLightShadow.shadowRadius, vSpotLightCoord[ i ] ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_DIR_LIGHTS > 0 ) && defined( RE_Direct )
	DirectionalLight directionalLight;
	#if defined( USE_SHADOWMAP ) && NUM_DIR_LIGHT_SHADOWS > 0
	DirectionalLightShadow directionalLightShadow;
	#endif
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_DIR_LIGHTS; i ++ ) {
		directionalLight = directionalLights[ i ];
		getDirectionalLightInfo( directionalLight, directLight );
		#if defined( USE_SHADOWMAP ) && ( UNROLLED_LOOP_INDEX < NUM_DIR_LIGHT_SHADOWS )
		directionalLightShadow = directionalLightShadows[ i ];
		directLight.color *= ( directLight.visible && receiveShadow ) ? getShadow( directionalShadowMap[ i ], directionalLightShadow.shadowMapSize, directionalLightShadow.shadowIntensity, directionalLightShadow.shadowBias, directionalLightShadow.shadowRadius, vDirectionalShadowCoord[ i ] ) : 1.0;
		#endif
		RE_Direct( directLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if ( NUM_RECT_AREA_LIGHTS > 0 ) && defined( RE_Direct_RectArea )
	RectAreaLight rectAreaLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_RECT_AREA_LIGHTS; i ++ ) {
		rectAreaLight = rectAreaLights[ i ];
		RE_Direct_RectArea( rectAreaLight, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
	}
	#pragma unroll_loop_end
#endif
#if defined( RE_IndirectDiffuse )
	vec3 iblIrradiance = vec3( 0.0 );
	vec3 irradiance = getAmbientLightIrradiance( ambientLightColor );
	#if defined( USE_LIGHT_PROBES )
		irradiance += getLightProbeIrradiance( lightProbe, geometryNormal );
	#endif
	#if ( NUM_HEMI_LIGHTS > 0 )
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_HEMI_LIGHTS; i ++ ) {
			irradiance += getHemisphereLightIrradiance( hemisphereLights[ i ], geometryNormal );
		}
		#pragma unroll_loop_end
	#endif
#endif
#if defined( RE_IndirectSpecular )
	vec3 radiance = vec3( 0.0 );
	vec3 clearcoatRadiance = vec3( 0.0 );
#endif`,Km=`#if defined( RE_IndirectDiffuse )
	#ifdef USE_LIGHTMAP
		vec4 lightMapTexel = texture2D( lightMap, vLightMapUv );
		vec3 lightMapIrradiance = lightMapTexel.rgb * lightMapIntensity;
		irradiance += lightMapIrradiance;
	#endif
	#if defined( USE_ENVMAP ) && defined( ENVMAP_TYPE_CUBE_UV )
		#if defined( STANDARD ) || defined( LAMBERT ) || defined( PHONG )
			iblIrradiance += getIBLIrradiance( geometryNormal );
		#endif
	#endif
#endif
#if defined( USE_ENVMAP ) && defined( RE_IndirectSpecular )
	#ifdef USE_ANISOTROPY
		radiance += getIBLAnisotropyRadiance( geometryViewDir, geometryNormal, material.roughness, material.anisotropyB, material.anisotropy );
	#else
		radiance += getIBLRadiance( geometryViewDir, geometryNormal, material.roughness );
	#endif
	#ifdef USE_CLEARCOAT
		clearcoatRadiance += getIBLRadiance( geometryViewDir, geometryClearcoatNormal, material.clearcoatRoughness );
	#endif
#endif`,Zm=`#if defined( RE_IndirectDiffuse )
	#if defined( LAMBERT ) || defined( PHONG )
		irradiance += iblIrradiance;
	#endif
	RE_IndirectDiffuse( irradiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif
#if defined( RE_IndirectSpecular )
	RE_IndirectSpecular( radiance, iblIrradiance, clearcoatRadiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif`,jm=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	gl_FragDepth = vIsPerspective == 0.0 ? gl_FragCoord.z : log2( vFragDepth ) * logDepthBufFC * 0.5;
#endif`,Jm=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	uniform float logDepthBufFC;
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,Qm=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,e_=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	vFragDepth = 1.0 + gl_Position.w;
	vIsPerspective = float( isPerspectiveMatrix( projectionMatrix ) );
#endif`,t_=`#ifdef USE_MAP
	vec4 sampledDiffuseColor = texture2D( map, vMapUv );
	#ifdef DECODE_VIDEO_TEXTURE
		sampledDiffuseColor = sRGBTransferEOTF( sampledDiffuseColor );
	#endif
	diffuseColor *= sampledDiffuseColor;
#endif`,n_=`#ifdef USE_MAP
	uniform sampler2D map;
#endif`,i_=`#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
	#if defined( USE_POINTS_UV )
		vec2 uv = vUv;
	#else
		vec2 uv = ( uvTransform * vec3( gl_PointCoord.x, 1.0 - gl_PointCoord.y, 1 ) ).xy;
	#endif
#endif
#ifdef USE_MAP
	diffuseColor *= texture2D( map, uv );
#endif
#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, uv ).g;
#endif`,r_=`#if defined( USE_POINTS_UV )
	varying vec2 vUv;
#else
	#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
		uniform mat3 uvTransform;
	#endif
#endif
#ifdef USE_MAP
	uniform sampler2D map;
#endif
#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,s_=`float metalnessFactor = metalness;
#ifdef USE_METALNESSMAP
	vec4 texelMetalness = texture2D( metalnessMap, vMetalnessMapUv );
	metalnessFactor *= texelMetalness.b;
#endif`,a_=`#ifdef USE_METALNESSMAP
	uniform sampler2D metalnessMap;
#endif`,o_=`#ifdef USE_INSTANCING_MORPH
	float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	float morphTargetBaseInfluence = texelFetch( morphTexture, ivec2( 0, gl_InstanceID ), 0 ).r;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		morphTargetInfluences[i] =  texelFetch( morphTexture, ivec2( i + 1, gl_InstanceID ), 0 ).r;
	}
#endif`,l_=`#if defined( USE_MORPHCOLORS )
	vColor *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		#if defined( USE_COLOR_ALPHA )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ) * morphTargetInfluences[ i ];
		#elif defined( USE_COLOR )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ).rgb * morphTargetInfluences[ i ];
		#endif
	}
#endif`,c_=`#ifdef USE_MORPHNORMALS
	objectNormal *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) objectNormal += getMorph( gl_VertexID, i, 1 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,u_=`#ifdef USE_MORPHTARGETS
	#ifndef USE_INSTANCING_MORPH
		uniform float morphTargetBaseInfluence;
		uniform float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	#endif
	uniform sampler2DArray morphTargetsTexture;
	uniform ivec2 morphTargetsTextureSize;
	vec4 getMorph( const in int vertexIndex, const in int morphTargetIndex, const in int offset ) {
		int texelIndex = vertexIndex * MORPHTARGETS_TEXTURE_STRIDE + offset;
		int y = texelIndex / morphTargetsTextureSize.x;
		int x = texelIndex - y * morphTargetsTextureSize.x;
		ivec3 morphUV = ivec3( x, y, morphTargetIndex );
		return texelFetch( morphTargetsTexture, morphUV, 0 );
	}
#endif`,h_=`#ifdef USE_MORPHTARGETS
	transformed *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) transformed += getMorph( gl_VertexID, i, 0 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,f_=`float faceDirection = gl_FrontFacing ? 1.0 : - 1.0;
#ifdef FLAT_SHADED
	vec3 fdx = dFdx( vViewPosition );
	vec3 fdy = dFdy( vViewPosition );
	vec3 normal = normalize( cross( fdx, fdy ) );
#else
	vec3 normal = normalize( vNormal );
	#ifdef DOUBLE_SIDED
		normal *= faceDirection;
	#endif
#endif
#if defined( USE_NORMALMAP_TANGENTSPACE ) || defined( USE_CLEARCOAT_NORMALMAP ) || defined( USE_ANISOTROPY )
	#ifdef USE_TANGENT
		mat3 tbn = mat3( normalize( vTangent ), normalize( vBitangent ), normal );
	#else
		mat3 tbn = getTangentFrame( - vViewPosition, normal,
		#if defined( USE_NORMALMAP )
			vNormalMapUv
		#elif defined( USE_CLEARCOAT_NORMALMAP )
			vClearcoatNormalMapUv
		#else
			vUv
		#endif
		);
	#endif
	#if defined( DOUBLE_SIDED ) && ! defined( FLAT_SHADED )
		tbn[0] *= faceDirection;
		tbn[1] *= faceDirection;
	#endif
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	#ifdef USE_TANGENT
		mat3 tbn2 = mat3( normalize( vTangent ), normalize( vBitangent ), normal );
	#else
		mat3 tbn2 = getTangentFrame( - vViewPosition, normal, vClearcoatNormalMapUv );
	#endif
	#if defined( DOUBLE_SIDED ) && ! defined( FLAT_SHADED )
		tbn2[0] *= faceDirection;
		tbn2[1] *= faceDirection;
	#endif
#endif
vec3 nonPerturbedNormal = normal;`,d_=`#ifdef USE_NORMALMAP_OBJECTSPACE
	normal = texture2D( normalMap, vNormalMapUv ).xyz * 2.0 - 1.0;
	#ifdef FLIP_SIDED
		normal = - normal;
	#endif
	#ifdef DOUBLE_SIDED
		normal = normal * faceDirection;
	#endif
	normal = normalize( normalMatrix * normal );
#elif defined( USE_NORMALMAP_TANGENTSPACE )
	vec3 mapN = texture2D( normalMap, vNormalMapUv ).xyz * 2.0 - 1.0;
	mapN.xy *= normalScale;
	normal = normalize( tbn * mapN );
#elif defined( USE_BUMPMAP )
	normal = perturbNormalArb( - vViewPosition, normal, dHdxy_fwd(), faceDirection );
#endif`,p_=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,m_=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,__=`#ifndef FLAT_SHADED
	vNormal = normalize( transformedNormal );
	#ifdef USE_TANGENT
		vTangent = normalize( transformedTangent );
		vBitangent = normalize( cross( vNormal, vTangent ) * tangent.w );
	#endif
#endif`,g_=`#ifdef USE_NORMALMAP
	uniform sampler2D normalMap;
	uniform vec2 normalScale;
#endif
#ifdef USE_NORMALMAP_OBJECTSPACE
	uniform mat3 normalMatrix;
#endif
#if ! defined ( USE_TANGENT ) && ( defined ( USE_NORMALMAP_TANGENTSPACE ) || defined ( USE_CLEARCOAT_NORMALMAP ) || defined( USE_ANISOTROPY ) )
	mat3 getTangentFrame( vec3 eye_pos, vec3 surf_norm, vec2 uv ) {
		vec3 q0 = dFdx( eye_pos.xyz );
		vec3 q1 = dFdy( eye_pos.xyz );
		vec2 st0 = dFdx( uv.st );
		vec2 st1 = dFdy( uv.st );
		vec3 N = surf_norm;
		vec3 q1perp = cross( q1, N );
		vec3 q0perp = cross( N, q0 );
		vec3 T = q1perp * st0.x + q0perp * st1.x;
		vec3 B = q1perp * st0.y + q0perp * st1.y;
		float det = max( dot( T, T ), dot( B, B ) );
		float scale = ( det == 0.0 ) ? 0.0 : inversesqrt( det );
		return mat3( T * scale, B * scale, N );
	}
#endif`,v_=`#ifdef USE_CLEARCOAT
	vec3 clearcoatNormal = nonPerturbedNormal;
#endif`,x_=`#ifdef USE_CLEARCOAT_NORMALMAP
	vec3 clearcoatMapN = texture2D( clearcoatNormalMap, vClearcoatNormalMapUv ).xyz * 2.0 - 1.0;
	clearcoatMapN.xy *= clearcoatNormalScale;
	clearcoatNormal = normalize( tbn2 * clearcoatMapN );
#endif`,M_=`#ifdef USE_CLEARCOATMAP
	uniform sampler2D clearcoatMap;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform sampler2D clearcoatNormalMap;
	uniform vec2 clearcoatNormalScale;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform sampler2D clearcoatRoughnessMap;
#endif`,S_=`#ifdef USE_IRIDESCENCEMAP
	uniform sampler2D iridescenceMap;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform sampler2D iridescenceThicknessMap;
#endif`,y_=`#ifdef OPAQUE
diffuseColor.a = 1.0;
#endif
#ifdef USE_TRANSMISSION
diffuseColor.a *= material.transmissionAlpha;
#endif
gl_FragColor = vec4( outgoingLight, diffuseColor.a );`,b_=`vec3 packNormalToRGB( const in vec3 normal ) {
	return normalize( normal ) * 0.5 + 0.5;
}
vec3 unpackRGBToNormal( const in vec3 rgb ) {
	return 2.0 * rgb.xyz - 1.0;
}
const float PackUpscale = 256. / 255.;const float UnpackDownscale = 255. / 256.;const float ShiftRight8 = 1. / 256.;
const float Inv255 = 1. / 255.;
const vec4 PackFactors = vec4( 1.0, 256.0, 256.0 * 256.0, 256.0 * 256.0 * 256.0 );
const vec2 UnpackFactors2 = vec2( UnpackDownscale, 1.0 / PackFactors.g );
const vec3 UnpackFactors3 = vec3( UnpackDownscale / PackFactors.rg, 1.0 / PackFactors.b );
const vec4 UnpackFactors4 = vec4( UnpackDownscale / PackFactors.rgb, 1.0 / PackFactors.a );
vec4 packDepthToRGBA( const in float v ) {
	if( v <= 0.0 )
		return vec4( 0., 0., 0., 0. );
	if( v >= 1.0 )
		return vec4( 1., 1., 1., 1. );
	float vuf;
	float af = modf( v * PackFactors.a, vuf );
	float bf = modf( vuf * ShiftRight8, vuf );
	float gf = modf( vuf * ShiftRight8, vuf );
	return vec4( vuf * Inv255, gf * PackUpscale, bf * PackUpscale, af );
}
vec3 packDepthToRGB( const in float v ) {
	if( v <= 0.0 )
		return vec3( 0., 0., 0. );
	if( v >= 1.0 )
		return vec3( 1., 1., 1. );
	float vuf;
	float bf = modf( v * PackFactors.b, vuf );
	float gf = modf( vuf * ShiftRight8, vuf );
	return vec3( vuf * Inv255, gf * PackUpscale, bf );
}
vec2 packDepthToRG( const in float v ) {
	if( v <= 0.0 )
		return vec2( 0., 0. );
	if( v >= 1.0 )
		return vec2( 1., 1. );
	float vuf;
	float gf = modf( v * 256., vuf );
	return vec2( vuf * Inv255, gf );
}
float unpackRGBAToDepth( const in vec4 v ) {
	return dot( v, UnpackFactors4 );
}
float unpackRGBToDepth( const in vec3 v ) {
	return dot( v, UnpackFactors3 );
}
float unpackRGToDepth( const in vec2 v ) {
	return v.r * UnpackFactors2.r + v.g * UnpackFactors2.g;
}
vec4 pack2HalfToRGBA( const in vec2 v ) {
	vec4 r = vec4( v.x, fract( v.x * 255.0 ), v.y, fract( v.y * 255.0 ) );
	return vec4( r.x - r.y / 255.0, r.y, r.z - r.w / 255.0, r.w );
}
vec2 unpackRGBATo2Half( const in vec4 v ) {
	return vec2( v.x + ( v.y / 255.0 ), v.z + ( v.w / 255.0 ) );
}
float viewZToOrthographicDepth( const in float viewZ, const in float near, const in float far ) {
	return ( viewZ + near ) / ( near - far );
}
float orthographicDepthToViewZ( const in float depth, const in float near, const in float far ) {
	#ifdef USE_REVERSED_DEPTH_BUFFER
	
		return depth * ( far - near ) - far;
	#else
		return depth * ( near - far ) - near;
	#endif
}
float viewZToPerspectiveDepth( const in float viewZ, const in float near, const in float far ) {
	return ( ( near + viewZ ) * far ) / ( ( far - near ) * viewZ );
}
float perspectiveDepthToViewZ( const in float depth, const in float near, const in float far ) {
	
	#ifdef USE_REVERSED_DEPTH_BUFFER
		return ( near * far ) / ( ( near - far ) * depth - near );
	#else
		return ( near * far ) / ( ( far - near ) * depth - far );
	#endif
}`,E_=`#ifdef PREMULTIPLIED_ALPHA
	gl_FragColor.rgb *= gl_FragColor.a;
#endif`,T_=`vec4 mvPosition = vec4( transformed, 1.0 );
#ifdef USE_BATCHING
	mvPosition = batchingMatrix * mvPosition;
#endif
#ifdef USE_INSTANCING
	mvPosition = instanceMatrix * mvPosition;
#endif
mvPosition = modelViewMatrix * mvPosition;
gl_Position = projectionMatrix * mvPosition;`,A_=`#ifdef DITHERING
	gl_FragColor.rgb = dithering( gl_FragColor.rgb );
#endif`,w_=`#ifdef DITHERING
	vec3 dithering( vec3 color ) {
		float grid_position = rand( gl_FragCoord.xy );
		vec3 dither_shift_RGB = vec3( 0.25 / 255.0, -0.25 / 255.0, 0.25 / 255.0 );
		dither_shift_RGB = mix( 2.0 * dither_shift_RGB, -2.0 * dither_shift_RGB, grid_position );
		return color + dither_shift_RGB;
	}
#endif`,R_=`float roughnessFactor = roughness;
#ifdef USE_ROUGHNESSMAP
	vec4 texelRoughness = texture2D( roughnessMap, vRoughnessMapUv );
	roughnessFactor *= texelRoughness.g;
#endif`,C_=`#ifdef USE_ROUGHNESSMAP
	uniform sampler2D roughnessMap;
#endif`,P_=`#if NUM_SPOT_LIGHT_COORDS > 0
	varying vec4 vSpotLightCoord[ NUM_SPOT_LIGHT_COORDS ];
#endif
#if NUM_SPOT_LIGHT_MAPS > 0
	uniform sampler2D spotLightMap[ NUM_SPOT_LIGHT_MAPS ];
#endif
#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
		#if defined( SHADOWMAP_TYPE_PCF )
			uniform sampler2DShadow directionalShadowMap[ NUM_DIR_LIGHT_SHADOWS ];
		#else
			uniform sampler2D directionalShadowMap[ NUM_DIR_LIGHT_SHADOWS ];
		#endif
		varying vec4 vDirectionalShadowCoord[ NUM_DIR_LIGHT_SHADOWS ];
		struct DirectionalLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform DirectionalLightShadow directionalLightShadows[ NUM_DIR_LIGHT_SHADOWS ];
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
		#if defined( SHADOWMAP_TYPE_PCF )
			uniform sampler2DShadow spotShadowMap[ NUM_SPOT_LIGHT_SHADOWS ];
		#else
			uniform sampler2D spotShadowMap[ NUM_SPOT_LIGHT_SHADOWS ];
		#endif
		struct SpotLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform SpotLightShadow spotLightShadows[ NUM_SPOT_LIGHT_SHADOWS ];
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		#if defined( SHADOWMAP_TYPE_PCF )
			uniform samplerCubeShadow pointShadowMap[ NUM_POINT_LIGHT_SHADOWS ];
		#elif defined( SHADOWMAP_TYPE_BASIC )
			uniform samplerCube pointShadowMap[ NUM_POINT_LIGHT_SHADOWS ];
		#endif
		varying vec4 vPointShadowCoord[ NUM_POINT_LIGHT_SHADOWS ];
		struct PointLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
			float shadowCameraNear;
			float shadowCameraFar;
		};
		uniform PointLightShadow pointLightShadows[ NUM_POINT_LIGHT_SHADOWS ];
	#endif
	#if defined( SHADOWMAP_TYPE_PCF )
		float interleavedGradientNoise( vec2 position ) {
			return fract( 52.9829189 * fract( dot( position, vec2( 0.06711056, 0.00583715 ) ) ) );
		}
		vec2 vogelDiskSample( int sampleIndex, int samplesCount, float phi ) {
			const float goldenAngle = 2.399963229728653;
			float r = sqrt( ( float( sampleIndex ) + 0.5 ) / float( samplesCount ) );
			float theta = float( sampleIndex ) * goldenAngle + phi;
			return vec2( cos( theta ), sin( theta ) ) * r;
		}
	#endif
	#if defined( SHADOWMAP_TYPE_PCF )
		float getShadow( sampler2DShadow shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord ) {
			float shadow = 1.0;
			shadowCoord.xyz /= shadowCoord.w;
			shadowCoord.z += shadowBias;
			bool inFrustum = shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0;
			bool frustumTest = inFrustum && shadowCoord.z <= 1.0;
			if ( frustumTest ) {
				vec2 texelSize = vec2( 1.0 ) / shadowMapSize;
				float radius = shadowRadius * texelSize.x;
				float phi = interleavedGradientNoise( gl_FragCoord.xy ) * PI2;
				shadow = (
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 0, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 1, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 2, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 3, 5, phi ) * radius, shadowCoord.z ) ) +
					texture( shadowMap, vec3( shadowCoord.xy + vogelDiskSample( 4, 5, phi ) * radius, shadowCoord.z ) )
				) * 0.2;
			}
			return mix( 1.0, shadow, shadowIntensity );
		}
	#elif defined( SHADOWMAP_TYPE_VSM )
		float getShadow( sampler2D shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord ) {
			float shadow = 1.0;
			shadowCoord.xyz /= shadowCoord.w;
			#ifdef USE_REVERSED_DEPTH_BUFFER
				shadowCoord.z -= shadowBias;
			#else
				shadowCoord.z += shadowBias;
			#endif
			bool inFrustum = shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0;
			bool frustumTest = inFrustum && shadowCoord.z <= 1.0;
			if ( frustumTest ) {
				vec2 distribution = texture2D( shadowMap, shadowCoord.xy ).rg;
				float mean = distribution.x;
				float variance = distribution.y * distribution.y;
				#ifdef USE_REVERSED_DEPTH_BUFFER
					float hard_shadow = step( mean, shadowCoord.z );
				#else
					float hard_shadow = step( shadowCoord.z, mean );
				#endif
				
				if ( hard_shadow == 1.0 ) {
					shadow = 1.0;
				} else {
					variance = max( variance, 0.0000001 );
					float d = shadowCoord.z - mean;
					float p_max = variance / ( variance + d * d );
					p_max = clamp( ( p_max - 0.3 ) / 0.65, 0.0, 1.0 );
					shadow = max( hard_shadow, p_max );
				}
			}
			return mix( 1.0, shadow, shadowIntensity );
		}
	#else
		float getShadow( sampler2D shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord ) {
			float shadow = 1.0;
			shadowCoord.xyz /= shadowCoord.w;
			#ifdef USE_REVERSED_DEPTH_BUFFER
				shadowCoord.z -= shadowBias;
			#else
				shadowCoord.z += shadowBias;
			#endif
			bool inFrustum = shadowCoord.x >= 0.0 && shadowCoord.x <= 1.0 && shadowCoord.y >= 0.0 && shadowCoord.y <= 1.0;
			bool frustumTest = inFrustum && shadowCoord.z <= 1.0;
			if ( frustumTest ) {
				float depth = texture2D( shadowMap, shadowCoord.xy ).r;
				#ifdef USE_REVERSED_DEPTH_BUFFER
					shadow = step( depth, shadowCoord.z );
				#else
					shadow = step( shadowCoord.z, depth );
				#endif
			}
			return mix( 1.0, shadow, shadowIntensity );
		}
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
	#if defined( SHADOWMAP_TYPE_PCF )
	float getPointShadow( samplerCubeShadow shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord, float shadowCameraNear, float shadowCameraFar ) {
		float shadow = 1.0;
		vec3 lightToPosition = shadowCoord.xyz;
		vec3 bd3D = normalize( lightToPosition );
		vec3 absVec = abs( lightToPosition );
		float viewSpaceZ = max( max( absVec.x, absVec.y ), absVec.z );
		if ( viewSpaceZ - shadowCameraFar <= 0.0 && viewSpaceZ - shadowCameraNear >= 0.0 ) {
			#ifdef USE_REVERSED_DEPTH_BUFFER
				float dp = ( shadowCameraNear * ( shadowCameraFar - viewSpaceZ ) ) / ( viewSpaceZ * ( shadowCameraFar - shadowCameraNear ) );
				dp -= shadowBias;
			#else
				float dp = ( shadowCameraFar * ( viewSpaceZ - shadowCameraNear ) ) / ( viewSpaceZ * ( shadowCameraFar - shadowCameraNear ) );
				dp += shadowBias;
			#endif
			float texelSize = shadowRadius / shadowMapSize.x;
			vec3 absDir = abs( bd3D );
			vec3 tangent = absDir.x > absDir.z ? vec3( 0.0, 1.0, 0.0 ) : vec3( 1.0, 0.0, 0.0 );
			tangent = normalize( cross( bd3D, tangent ) );
			vec3 bitangent = cross( bd3D, tangent );
			float phi = interleavedGradientNoise( gl_FragCoord.xy ) * PI2;
			vec2 sample0 = vogelDiskSample( 0, 5, phi );
			vec2 sample1 = vogelDiskSample( 1, 5, phi );
			vec2 sample2 = vogelDiskSample( 2, 5, phi );
			vec2 sample3 = vogelDiskSample( 3, 5, phi );
			vec2 sample4 = vogelDiskSample( 4, 5, phi );
			shadow = (
				texture( shadowMap, vec4( bd3D + ( tangent * sample0.x + bitangent * sample0.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample1.x + bitangent * sample1.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample2.x + bitangent * sample2.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample3.x + bitangent * sample3.y ) * texelSize, dp ) ) +
				texture( shadowMap, vec4( bd3D + ( tangent * sample4.x + bitangent * sample4.y ) * texelSize, dp ) )
			) * 0.2;
		}
		return mix( 1.0, shadow, shadowIntensity );
	}
	#elif defined( SHADOWMAP_TYPE_BASIC )
	float getPointShadow( samplerCube shadowMap, vec2 shadowMapSize, float shadowIntensity, float shadowBias, float shadowRadius, vec4 shadowCoord, float shadowCameraNear, float shadowCameraFar ) {
		float shadow = 1.0;
		vec3 lightToPosition = shadowCoord.xyz;
		vec3 absVec = abs( lightToPosition );
		float viewSpaceZ = max( max( absVec.x, absVec.y ), absVec.z );
		if ( viewSpaceZ - shadowCameraFar <= 0.0 && viewSpaceZ - shadowCameraNear >= 0.0 ) {
			float dp = ( shadowCameraFar * ( viewSpaceZ - shadowCameraNear ) ) / ( viewSpaceZ * ( shadowCameraFar - shadowCameraNear ) );
			dp += shadowBias;
			vec3 bd3D = normalize( lightToPosition );
			float depth = textureCube( shadowMap, bd3D ).r;
			#ifdef USE_REVERSED_DEPTH_BUFFER
				depth = 1.0 - depth;
			#endif
			shadow = step( dp, depth );
		}
		return mix( 1.0, shadow, shadowIntensity );
	}
	#endif
	#endif
#endif`,D_=`#if NUM_SPOT_LIGHT_COORDS > 0
	uniform mat4 spotLightMatrix[ NUM_SPOT_LIGHT_COORDS ];
	varying vec4 vSpotLightCoord[ NUM_SPOT_LIGHT_COORDS ];
#endif
#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
		uniform mat4 directionalShadowMatrix[ NUM_DIR_LIGHT_SHADOWS ];
		varying vec4 vDirectionalShadowCoord[ NUM_DIR_LIGHT_SHADOWS ];
		struct DirectionalLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform DirectionalLightShadow directionalLightShadows[ NUM_DIR_LIGHT_SHADOWS ];
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
		struct SpotLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
		};
		uniform SpotLightShadow spotLightShadows[ NUM_SPOT_LIGHT_SHADOWS ];
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		uniform mat4 pointShadowMatrix[ NUM_POINT_LIGHT_SHADOWS ];
		varying vec4 vPointShadowCoord[ NUM_POINT_LIGHT_SHADOWS ];
		struct PointLightShadow {
			float shadowIntensity;
			float shadowBias;
			float shadowNormalBias;
			float shadowRadius;
			vec2 shadowMapSize;
			float shadowCameraNear;
			float shadowCameraFar;
		};
		uniform PointLightShadow pointLightShadows[ NUM_POINT_LIGHT_SHADOWS ];
	#endif
#endif`,L_=`#if ( defined( USE_SHADOWMAP ) && ( NUM_DIR_LIGHT_SHADOWS > 0 || NUM_POINT_LIGHT_SHADOWS > 0 ) ) || ( NUM_SPOT_LIGHT_COORDS > 0 )
	vec3 shadowWorldNormal = inverseTransformDirection( transformedNormal, viewMatrix );
	vec4 shadowWorldPosition;
#endif
#if defined( USE_SHADOWMAP )
	#if NUM_DIR_LIGHT_SHADOWS > 0
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_DIR_LIGHT_SHADOWS; i ++ ) {
			shadowWorldPosition = worldPosition + vec4( shadowWorldNormal * directionalLightShadows[ i ].shadowNormalBias, 0 );
			vDirectionalShadowCoord[ i ] = directionalShadowMatrix[ i ] * shadowWorldPosition;
		}
		#pragma unroll_loop_end
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0
		#pragma unroll_loop_start
		for ( int i = 0; i < NUM_POINT_LIGHT_SHADOWS; i ++ ) {
			shadowWorldPosition = worldPosition + vec4( shadowWorldNormal * pointLightShadows[ i ].shadowNormalBias, 0 );
			vPointShadowCoord[ i ] = pointShadowMatrix[ i ] * shadowWorldPosition;
		}
		#pragma unroll_loop_end
	#endif
#endif
#if NUM_SPOT_LIGHT_COORDS > 0
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHT_COORDS; i ++ ) {
		shadowWorldPosition = worldPosition;
		#if ( defined( USE_SHADOWMAP ) && UNROLLED_LOOP_INDEX < NUM_SPOT_LIGHT_SHADOWS )
			shadowWorldPosition.xyz += shadowWorldNormal * spotLightShadows[ i ].shadowNormalBias;
		#endif
		vSpotLightCoord[ i ] = spotLightMatrix[ i ] * shadowWorldPosition;
	}
	#pragma unroll_loop_end
#endif`,I_=`float getShadowMask() {
	float shadow = 1.0;
	#ifdef USE_SHADOWMAP
	#if NUM_DIR_LIGHT_SHADOWS > 0
	DirectionalLightShadow directionalLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_DIR_LIGHT_SHADOWS; i ++ ) {
		directionalLight = directionalLightShadows[ i ];
		shadow *= receiveShadow ? getShadow( directionalShadowMap[ i ], directionalLight.shadowMapSize, directionalLight.shadowIntensity, directionalLight.shadowBias, directionalLight.shadowRadius, vDirectionalShadowCoord[ i ] ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#if NUM_SPOT_LIGHT_SHADOWS > 0
	SpotLightShadow spotLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_SPOT_LIGHT_SHADOWS; i ++ ) {
		spotLight = spotLightShadows[ i ];
		shadow *= receiveShadow ? getShadow( spotShadowMap[ i ], spotLight.shadowMapSize, spotLight.shadowIntensity, spotLight.shadowBias, spotLight.shadowRadius, vSpotLightCoord[ i ] ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#if NUM_POINT_LIGHT_SHADOWS > 0 && ( defined( SHADOWMAP_TYPE_PCF ) || defined( SHADOWMAP_TYPE_BASIC ) )
	PointLightShadow pointLight;
	#pragma unroll_loop_start
	for ( int i = 0; i < NUM_POINT_LIGHT_SHADOWS; i ++ ) {
		pointLight = pointLightShadows[ i ];
		shadow *= receiveShadow ? getPointShadow( pointShadowMap[ i ], pointLight.shadowMapSize, pointLight.shadowIntensity, pointLight.shadowBias, pointLight.shadowRadius, vPointShadowCoord[ i ], pointLight.shadowCameraNear, pointLight.shadowCameraFar ) : 1.0;
	}
	#pragma unroll_loop_end
	#endif
	#endif
	return shadow;
}`,U_=`#ifdef USE_SKINNING
	mat4 boneMatX = getBoneMatrix( skinIndex.x );
	mat4 boneMatY = getBoneMatrix( skinIndex.y );
	mat4 boneMatZ = getBoneMatrix( skinIndex.z );
	mat4 boneMatW = getBoneMatrix( skinIndex.w );
#endif`,N_=`#ifdef USE_SKINNING
	uniform mat4 bindMatrix;
	uniform mat4 bindMatrixInverse;
	uniform highp sampler2D boneTexture;
	mat4 getBoneMatrix( const in float i ) {
		int size = textureSize( boneTexture, 0 ).x;
		int j = int( i ) * 4;
		int x = j % size;
		int y = j / size;
		vec4 v1 = texelFetch( boneTexture, ivec2( x, y ), 0 );
		vec4 v2 = texelFetch( boneTexture, ivec2( x + 1, y ), 0 );
		vec4 v3 = texelFetch( boneTexture, ivec2( x + 2, y ), 0 );
		vec4 v4 = texelFetch( boneTexture, ivec2( x + 3, y ), 0 );
		return mat4( v1, v2, v3, v4 );
	}
#endif`,F_=`#ifdef USE_SKINNING
	vec4 skinVertex = bindMatrix * vec4( transformed, 1.0 );
	vec4 skinned = vec4( 0.0 );
	skinned += boneMatX * skinVertex * skinWeight.x;
	skinned += boneMatY * skinVertex * skinWeight.y;
	skinned += boneMatZ * skinVertex * skinWeight.z;
	skinned += boneMatW * skinVertex * skinWeight.w;
	transformed = ( bindMatrixInverse * skinned ).xyz;
#endif`,O_=`#ifdef USE_SKINNING
	mat4 skinMatrix = mat4( 0.0 );
	skinMatrix += skinWeight.x * boneMatX;
	skinMatrix += skinWeight.y * boneMatY;
	skinMatrix += skinWeight.z * boneMatZ;
	skinMatrix += skinWeight.w * boneMatW;
	skinMatrix = bindMatrixInverse * skinMatrix * bindMatrix;
	objectNormal = vec4( skinMatrix * vec4( objectNormal, 0.0 ) ).xyz;
	#ifdef USE_TANGENT
		objectTangent = vec4( skinMatrix * vec4( objectTangent, 0.0 ) ).xyz;
	#endif
#endif`,B_=`float specularStrength;
#ifdef USE_SPECULARMAP
	vec4 texelSpecular = texture2D( specularMap, vSpecularMapUv );
	specularStrength = texelSpecular.r;
#else
	specularStrength = 1.0;
#endif`,k_=`#ifdef USE_SPECULARMAP
	uniform sampler2D specularMap;
#endif`,z_=`#if defined( TONE_MAPPING )
	gl_FragColor.rgb = toneMapping( gl_FragColor.rgb );
#endif`,V_=`#ifndef saturate
#define saturate( a ) clamp( a, 0.0, 1.0 )
#endif
uniform float toneMappingExposure;
vec3 LinearToneMapping( vec3 color ) {
	return saturate( toneMappingExposure * color );
}
vec3 ReinhardToneMapping( vec3 color ) {
	color *= toneMappingExposure;
	return saturate( color / ( vec3( 1.0 ) + color ) );
}
vec3 CineonToneMapping( vec3 color ) {
	color *= toneMappingExposure;
	color = max( vec3( 0.0 ), color - 0.004 );
	return pow( ( color * ( 6.2 * color + 0.5 ) ) / ( color * ( 6.2 * color + 1.7 ) + 0.06 ), vec3( 2.2 ) );
}
vec3 RRTAndODTFit( vec3 v ) {
	vec3 a = v * ( v + 0.0245786 ) - 0.000090537;
	vec3 b = v * ( 0.983729 * v + 0.4329510 ) + 0.238081;
	return a / b;
}
vec3 ACESFilmicToneMapping( vec3 color ) {
	const mat3 ACESInputMat = mat3(
		vec3( 0.59719, 0.07600, 0.02840 ),		vec3( 0.35458, 0.90834, 0.13383 ),
		vec3( 0.04823, 0.01566, 0.83777 )
	);
	const mat3 ACESOutputMat = mat3(
		vec3(  1.60475, -0.10208, -0.00327 ),		vec3( -0.53108,  1.10813, -0.07276 ),
		vec3( -0.07367, -0.00605,  1.07602 )
	);
	color *= toneMappingExposure / 0.6;
	color = ACESInputMat * color;
	color = RRTAndODTFit( color );
	color = ACESOutputMat * color;
	return saturate( color );
}
const mat3 LINEAR_REC2020_TO_LINEAR_SRGB = mat3(
	vec3( 1.6605, - 0.1246, - 0.0182 ),
	vec3( - 0.5876, 1.1329, - 0.1006 ),
	vec3( - 0.0728, - 0.0083, 1.1187 )
);
const mat3 LINEAR_SRGB_TO_LINEAR_REC2020 = mat3(
	vec3( 0.6274, 0.0691, 0.0164 ),
	vec3( 0.3293, 0.9195, 0.0880 ),
	vec3( 0.0433, 0.0113, 0.8956 )
);
vec3 agxDefaultContrastApprox( vec3 x ) {
	vec3 x2 = x * x;
	vec3 x4 = x2 * x2;
	return + 15.5 * x4 * x2
		- 40.14 * x4 * x
		+ 31.96 * x4
		- 6.868 * x2 * x
		+ 0.4298 * x2
		+ 0.1191 * x
		- 0.00232;
}
vec3 AgXToneMapping( vec3 color ) {
	const mat3 AgXInsetMatrix = mat3(
		vec3( 0.856627153315983, 0.137318972929847, 0.11189821299995 ),
		vec3( 0.0951212405381588, 0.761241990602591, 0.0767994186031903 ),
		vec3( 0.0482516061458583, 0.101439036467562, 0.811302368396859 )
	);
	const mat3 AgXOutsetMatrix = mat3(
		vec3( 1.1271005818144368, - 0.1413297634984383, - 0.14132976349843826 ),
		vec3( - 0.11060664309660323, 1.157823702216272, - 0.11060664309660294 ),
		vec3( - 0.016493938717834573, - 0.016493938717834257, 1.2519364065950405 )
	);
	const float AgxMinEv = - 12.47393;	const float AgxMaxEv = 4.026069;
	color *= toneMappingExposure;
	color = LINEAR_SRGB_TO_LINEAR_REC2020 * color;
	color = AgXInsetMatrix * color;
	color = max( color, 1e-10 );	color = log2( color );
	color = ( color - AgxMinEv ) / ( AgxMaxEv - AgxMinEv );
	color = clamp( color, 0.0, 1.0 );
	color = agxDefaultContrastApprox( color );
	color = AgXOutsetMatrix * color;
	color = pow( max( vec3( 0.0 ), color ), vec3( 2.2 ) );
	color = LINEAR_REC2020_TO_LINEAR_SRGB * color;
	color = clamp( color, 0.0, 1.0 );
	return color;
}
vec3 NeutralToneMapping( vec3 color ) {
	const float StartCompression = 0.8 - 0.04;
	const float Desaturation = 0.15;
	color *= toneMappingExposure;
	float x = min( color.r, min( color.g, color.b ) );
	float offset = x < 0.08 ? x - 6.25 * x * x : 0.04;
	color -= offset;
	float peak = max( color.r, max( color.g, color.b ) );
	if ( peak < StartCompression ) return color;
	float d = 1. - StartCompression;
	float newPeak = 1. - d * d / ( peak + d - StartCompression );
	color *= newPeak / peak;
	float g = 1. - 1. / ( Desaturation * ( peak - newPeak ) + 1. );
	return mix( color, vec3( newPeak ), g );
}
vec3 CustomToneMapping( vec3 color ) { return color; }`,G_=`#ifdef USE_TRANSMISSION
	material.transmission = transmission;
	material.transmissionAlpha = 1.0;
	material.thickness = thickness;
	material.attenuationDistance = attenuationDistance;
	material.attenuationColor = attenuationColor;
	#ifdef USE_TRANSMISSIONMAP
		material.transmission *= texture2D( transmissionMap, vTransmissionMapUv ).r;
	#endif
	#ifdef USE_THICKNESSMAP
		material.thickness *= texture2D( thicknessMap, vThicknessMapUv ).g;
	#endif
	vec3 pos = vWorldPosition;
	vec3 v = normalize( cameraPosition - pos );
	vec3 n = inverseTransformDirection( normal, viewMatrix );
	vec4 transmitted = getIBLVolumeRefraction(
		n, v, material.roughness, material.diffuseContribution, material.specularColorBlended, material.specularF90,
		pos, modelMatrix, viewMatrix, projectionMatrix, material.dispersion, material.ior, material.thickness,
		material.attenuationColor, material.attenuationDistance );
	material.transmissionAlpha = mix( material.transmissionAlpha, transmitted.a, material.transmission );
	totalDiffuse = mix( totalDiffuse, transmitted.rgb, material.transmission );
#endif`,H_=`#ifdef USE_TRANSMISSION
	uniform float transmission;
	uniform float thickness;
	uniform float attenuationDistance;
	uniform vec3 attenuationColor;
	#ifdef USE_TRANSMISSIONMAP
		uniform sampler2D transmissionMap;
	#endif
	#ifdef USE_THICKNESSMAP
		uniform sampler2D thicknessMap;
	#endif
	uniform vec2 transmissionSamplerSize;
	uniform sampler2D transmissionSamplerMap;
	uniform mat4 modelMatrix;
	uniform mat4 projectionMatrix;
	varying vec3 vWorldPosition;
	float w0( float a ) {
		return ( 1.0 / 6.0 ) * ( a * ( a * ( - a + 3.0 ) - 3.0 ) + 1.0 );
	}
	float w1( float a ) {
		return ( 1.0 / 6.0 ) * ( a *  a * ( 3.0 * a - 6.0 ) + 4.0 );
	}
	float w2( float a ){
		return ( 1.0 / 6.0 ) * ( a * ( a * ( - 3.0 * a + 3.0 ) + 3.0 ) + 1.0 );
	}
	float w3( float a ) {
		return ( 1.0 / 6.0 ) * ( a * a * a );
	}
	float g0( float a ) {
		return w0( a ) + w1( a );
	}
	float g1( float a ) {
		return w2( a ) + w3( a );
	}
	float h0( float a ) {
		return - 1.0 + w1( a ) / ( w0( a ) + w1( a ) );
	}
	float h1( float a ) {
		return 1.0 + w3( a ) / ( w2( a ) + w3( a ) );
	}
	vec4 bicubic( sampler2D tex, vec2 uv, vec4 texelSize, float lod ) {
		uv = uv * texelSize.zw + 0.5;
		vec2 iuv = floor( uv );
		vec2 fuv = fract( uv );
		float g0x = g0( fuv.x );
		float g1x = g1( fuv.x );
		float h0x = h0( fuv.x );
		float h1x = h1( fuv.x );
		float h0y = h0( fuv.y );
		float h1y = h1( fuv.y );
		vec2 p0 = ( vec2( iuv.x + h0x, iuv.y + h0y ) - 0.5 ) * texelSize.xy;
		vec2 p1 = ( vec2( iuv.x + h1x, iuv.y + h0y ) - 0.5 ) * texelSize.xy;
		vec2 p2 = ( vec2( iuv.x + h0x, iuv.y + h1y ) - 0.5 ) * texelSize.xy;
		vec2 p3 = ( vec2( iuv.x + h1x, iuv.y + h1y ) - 0.5 ) * texelSize.xy;
		return g0( fuv.y ) * ( g0x * textureLod( tex, p0, lod ) + g1x * textureLod( tex, p1, lod ) ) +
			g1( fuv.y ) * ( g0x * textureLod( tex, p2, lod ) + g1x * textureLod( tex, p3, lod ) );
	}
	vec4 textureBicubic( sampler2D sampler, vec2 uv, float lod ) {
		vec2 fLodSize = vec2( textureSize( sampler, int( lod ) ) );
		vec2 cLodSize = vec2( textureSize( sampler, int( lod + 1.0 ) ) );
		vec2 fLodSizeInv = 1.0 / fLodSize;
		vec2 cLodSizeInv = 1.0 / cLodSize;
		vec4 fSample = bicubic( sampler, uv, vec4( fLodSizeInv, fLodSize ), floor( lod ) );
		vec4 cSample = bicubic( sampler, uv, vec4( cLodSizeInv, cLodSize ), ceil( lod ) );
		return mix( fSample, cSample, fract( lod ) );
	}
	vec3 getVolumeTransmissionRay( const in vec3 n, const in vec3 v, const in float thickness, const in float ior, const in mat4 modelMatrix ) {
		vec3 refractionVector = refract( - v, normalize( n ), 1.0 / ior );
		vec3 modelScale;
		modelScale.x = length( vec3( modelMatrix[ 0 ].xyz ) );
		modelScale.y = length( vec3( modelMatrix[ 1 ].xyz ) );
		modelScale.z = length( vec3( modelMatrix[ 2 ].xyz ) );
		return normalize( refractionVector ) * thickness * modelScale;
	}
	float applyIorToRoughness( const in float roughness, const in float ior ) {
		return roughness * clamp( ior * 2.0 - 2.0, 0.0, 1.0 );
	}
	vec4 getTransmissionSample( const in vec2 fragCoord, const in float roughness, const in float ior ) {
		float lod = log2( transmissionSamplerSize.x ) * applyIorToRoughness( roughness, ior );
		return textureBicubic( transmissionSamplerMap, fragCoord.xy, lod );
	}
	vec3 volumeAttenuation( const in float transmissionDistance, const in vec3 attenuationColor, const in float attenuationDistance ) {
		if ( isinf( attenuationDistance ) ) {
			return vec3( 1.0 );
		} else {
			vec3 attenuationCoefficient = -log( attenuationColor ) / attenuationDistance;
			vec3 transmittance = exp( - attenuationCoefficient * transmissionDistance );			return transmittance;
		}
	}
	vec4 getIBLVolumeRefraction( const in vec3 n, const in vec3 v, const in float roughness, const in vec3 diffuseColor,
		const in vec3 specularColor, const in float specularF90, const in vec3 position, const in mat4 modelMatrix,
		const in mat4 viewMatrix, const in mat4 projMatrix, const in float dispersion, const in float ior, const in float thickness,
		const in vec3 attenuationColor, const in float attenuationDistance ) {
		vec4 transmittedLight;
		vec3 transmittance;
		#ifdef USE_DISPERSION
			float halfSpread = ( ior - 1.0 ) * 0.025 * dispersion;
			vec3 iors = vec3( ior - halfSpread, ior, ior + halfSpread );
			for ( int i = 0; i < 3; i ++ ) {
				vec3 transmissionRay = getVolumeTransmissionRay( n, v, thickness, iors[ i ], modelMatrix );
				vec3 refractedRayExit = position + transmissionRay;
				vec4 ndcPos = projMatrix * viewMatrix * vec4( refractedRayExit, 1.0 );
				vec2 refractionCoords = ndcPos.xy / ndcPos.w;
				refractionCoords += 1.0;
				refractionCoords /= 2.0;
				vec4 transmissionSample = getTransmissionSample( refractionCoords, roughness, iors[ i ] );
				transmittedLight[ i ] = transmissionSample[ i ];
				transmittedLight.a += transmissionSample.a;
				transmittance[ i ] = diffuseColor[ i ] * volumeAttenuation( length( transmissionRay ), attenuationColor, attenuationDistance )[ i ];
			}
			transmittedLight.a /= 3.0;
		#else
			vec3 transmissionRay = getVolumeTransmissionRay( n, v, thickness, ior, modelMatrix );
			vec3 refractedRayExit = position + transmissionRay;
			vec4 ndcPos = projMatrix * viewMatrix * vec4( refractedRayExit, 1.0 );
			vec2 refractionCoords = ndcPos.xy / ndcPos.w;
			refractionCoords += 1.0;
			refractionCoords /= 2.0;
			transmittedLight = getTransmissionSample( refractionCoords, roughness, ior );
			transmittance = diffuseColor * volumeAttenuation( length( transmissionRay ), attenuationColor, attenuationDistance );
		#endif
		vec3 attenuatedColor = transmittance * transmittedLight.rgb;
		vec3 F = EnvironmentBRDF( n, v, specularColor, specularF90, roughness );
		float transmittanceFactor = ( transmittance.r + transmittance.g + transmittance.b ) / 3.0;
		return vec4( ( 1.0 - F ) * attenuatedColor, 1.0 - ( 1.0 - transmittedLight.a ) * transmittanceFactor );
	}
#endif`,W_=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	varying vec2 vUv;
#endif
#ifdef USE_MAP
	varying vec2 vMapUv;
#endif
#ifdef USE_ALPHAMAP
	varying vec2 vAlphaMapUv;
#endif
#ifdef USE_LIGHTMAP
	varying vec2 vLightMapUv;
#endif
#ifdef USE_AOMAP
	varying vec2 vAoMapUv;
#endif
#ifdef USE_BUMPMAP
	varying vec2 vBumpMapUv;
#endif
#ifdef USE_NORMALMAP
	varying vec2 vNormalMapUv;
#endif
#ifdef USE_EMISSIVEMAP
	varying vec2 vEmissiveMapUv;
#endif
#ifdef USE_METALNESSMAP
	varying vec2 vMetalnessMapUv;
#endif
#ifdef USE_ROUGHNESSMAP
	varying vec2 vRoughnessMapUv;
#endif
#ifdef USE_ANISOTROPYMAP
	varying vec2 vAnisotropyMapUv;
#endif
#ifdef USE_CLEARCOATMAP
	varying vec2 vClearcoatMapUv;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	varying vec2 vClearcoatNormalMapUv;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	varying vec2 vClearcoatRoughnessMapUv;
#endif
#ifdef USE_IRIDESCENCEMAP
	varying vec2 vIridescenceMapUv;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	varying vec2 vIridescenceThicknessMapUv;
#endif
#ifdef USE_SHEEN_COLORMAP
	varying vec2 vSheenColorMapUv;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	varying vec2 vSheenRoughnessMapUv;
#endif
#ifdef USE_SPECULARMAP
	varying vec2 vSpecularMapUv;
#endif
#ifdef USE_SPECULAR_COLORMAP
	varying vec2 vSpecularColorMapUv;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	varying vec2 vSpecularIntensityMapUv;
#endif
#ifdef USE_TRANSMISSIONMAP
	uniform mat3 transmissionMapTransform;
	varying vec2 vTransmissionMapUv;
#endif
#ifdef USE_THICKNESSMAP
	uniform mat3 thicknessMapTransform;
	varying vec2 vThicknessMapUv;
#endif`,X_=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	varying vec2 vUv;
#endif
#ifdef USE_MAP
	uniform mat3 mapTransform;
	varying vec2 vMapUv;
#endif
#ifdef USE_ALPHAMAP
	uniform mat3 alphaMapTransform;
	varying vec2 vAlphaMapUv;
#endif
#ifdef USE_LIGHTMAP
	uniform mat3 lightMapTransform;
	varying vec2 vLightMapUv;
#endif
#ifdef USE_AOMAP
	uniform mat3 aoMapTransform;
	varying vec2 vAoMapUv;
#endif
#ifdef USE_BUMPMAP
	uniform mat3 bumpMapTransform;
	varying vec2 vBumpMapUv;
#endif
#ifdef USE_NORMALMAP
	uniform mat3 normalMapTransform;
	varying vec2 vNormalMapUv;
#endif
#ifdef USE_DISPLACEMENTMAP
	uniform mat3 displacementMapTransform;
	varying vec2 vDisplacementMapUv;
#endif
#ifdef USE_EMISSIVEMAP
	uniform mat3 emissiveMapTransform;
	varying vec2 vEmissiveMapUv;
#endif
#ifdef USE_METALNESSMAP
	uniform mat3 metalnessMapTransform;
	varying vec2 vMetalnessMapUv;
#endif
#ifdef USE_ROUGHNESSMAP
	uniform mat3 roughnessMapTransform;
	varying vec2 vRoughnessMapUv;
#endif
#ifdef USE_ANISOTROPYMAP
	uniform mat3 anisotropyMapTransform;
	varying vec2 vAnisotropyMapUv;
#endif
#ifdef USE_CLEARCOATMAP
	uniform mat3 clearcoatMapTransform;
	varying vec2 vClearcoatMapUv;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform mat3 clearcoatNormalMapTransform;
	varying vec2 vClearcoatNormalMapUv;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform mat3 clearcoatRoughnessMapTransform;
	varying vec2 vClearcoatRoughnessMapUv;
#endif
#ifdef USE_SHEEN_COLORMAP
	uniform mat3 sheenColorMapTransform;
	varying vec2 vSheenColorMapUv;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	uniform mat3 sheenRoughnessMapTransform;
	varying vec2 vSheenRoughnessMapUv;
#endif
#ifdef USE_IRIDESCENCEMAP
	uniform mat3 iridescenceMapTransform;
	varying vec2 vIridescenceMapUv;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform mat3 iridescenceThicknessMapTransform;
	varying vec2 vIridescenceThicknessMapUv;
#endif
#ifdef USE_SPECULARMAP
	uniform mat3 specularMapTransform;
	varying vec2 vSpecularMapUv;
#endif
#ifdef USE_SPECULAR_COLORMAP
	uniform mat3 specularColorMapTransform;
	varying vec2 vSpecularColorMapUv;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	uniform mat3 specularIntensityMapTransform;
	varying vec2 vSpecularIntensityMapUv;
#endif
#ifdef USE_TRANSMISSIONMAP
	uniform mat3 transmissionMapTransform;
	varying vec2 vTransmissionMapUv;
#endif
#ifdef USE_THICKNESSMAP
	uniform mat3 thicknessMapTransform;
	varying vec2 vThicknessMapUv;
#endif`,q_=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
	vUv = vec3( uv, 1 ).xy;
#endif
#ifdef USE_MAP
	vMapUv = ( mapTransform * vec3( MAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ALPHAMAP
	vAlphaMapUv = ( alphaMapTransform * vec3( ALPHAMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_LIGHTMAP
	vLightMapUv = ( lightMapTransform * vec3( LIGHTMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_AOMAP
	vAoMapUv = ( aoMapTransform * vec3( AOMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_BUMPMAP
	vBumpMapUv = ( bumpMapTransform * vec3( BUMPMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_NORMALMAP
	vNormalMapUv = ( normalMapTransform * vec3( NORMALMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_DISPLACEMENTMAP
	vDisplacementMapUv = ( displacementMapTransform * vec3( DISPLACEMENTMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_EMISSIVEMAP
	vEmissiveMapUv = ( emissiveMapTransform * vec3( EMISSIVEMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_METALNESSMAP
	vMetalnessMapUv = ( metalnessMapTransform * vec3( METALNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ROUGHNESSMAP
	vRoughnessMapUv = ( roughnessMapTransform * vec3( ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_ANISOTROPYMAP
	vAnisotropyMapUv = ( anisotropyMapTransform * vec3( ANISOTROPYMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOATMAP
	vClearcoatMapUv = ( clearcoatMapTransform * vec3( CLEARCOATMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	vClearcoatNormalMapUv = ( clearcoatNormalMapTransform * vec3( CLEARCOAT_NORMALMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	vClearcoatRoughnessMapUv = ( clearcoatRoughnessMapTransform * vec3( CLEARCOAT_ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_IRIDESCENCEMAP
	vIridescenceMapUv = ( iridescenceMapTransform * vec3( IRIDESCENCEMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	vIridescenceThicknessMapUv = ( iridescenceThicknessMapTransform * vec3( IRIDESCENCE_THICKNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SHEEN_COLORMAP
	vSheenColorMapUv = ( sheenColorMapTransform * vec3( SHEEN_COLORMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SHEEN_ROUGHNESSMAP
	vSheenRoughnessMapUv = ( sheenRoughnessMapTransform * vec3( SHEEN_ROUGHNESSMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULARMAP
	vSpecularMapUv = ( specularMapTransform * vec3( SPECULARMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULAR_COLORMAP
	vSpecularColorMapUv = ( specularColorMapTransform * vec3( SPECULAR_COLORMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_SPECULAR_INTENSITYMAP
	vSpecularIntensityMapUv = ( specularIntensityMapTransform * vec3( SPECULAR_INTENSITYMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_TRANSMISSIONMAP
	vTransmissionMapUv = ( transmissionMapTransform * vec3( TRANSMISSIONMAP_UV, 1 ) ).xy;
#endif
#ifdef USE_THICKNESSMAP
	vThicknessMapUv = ( thicknessMapTransform * vec3( THICKNESSMAP_UV, 1 ) ).xy;
#endif`,Y_=`#if defined( USE_ENVMAP ) || defined( DISTANCE ) || defined ( USE_SHADOWMAP ) || defined ( USE_TRANSMISSION ) || NUM_SPOT_LIGHT_COORDS > 0
	vec4 worldPosition = vec4( transformed, 1.0 );
	#ifdef USE_BATCHING
		worldPosition = batchingMatrix * worldPosition;
	#endif
	#ifdef USE_INSTANCING
		worldPosition = instanceMatrix * worldPosition;
	#endif
	worldPosition = modelMatrix * worldPosition;
#endif`;const $_=`varying vec2 vUv;
uniform mat3 uvTransform;
void main() {
	vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	gl_Position = vec4( position.xy, 1.0, 1.0 );
}`,K_=`uniform sampler2D t2D;
uniform float backgroundIntensity;
varying vec2 vUv;
void main() {
	vec4 texColor = texture2D( t2D, vUv );
	#ifdef DECODE_VIDEO_TEXTURE
		texColor = vec4( mix( pow( texColor.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), texColor.rgb * 0.0773993808, vec3( lessThanEqual( texColor.rgb, vec3( 0.04045 ) ) ) ), texColor.w );
	#endif
	texColor.rgb *= backgroundIntensity;
	gl_FragColor = texColor;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,Z_=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,j_=`#ifdef ENVMAP_TYPE_CUBE
	uniform samplerCube envMap;
#elif defined( ENVMAP_TYPE_CUBE_UV )
	uniform sampler2D envMap;
#endif
uniform float flipEnvMap;
uniform float backgroundBlurriness;
uniform float backgroundIntensity;
uniform mat3 backgroundRotation;
varying vec3 vWorldDirection;
#include <cube_uv_reflection_fragment>
void main() {
	#ifdef ENVMAP_TYPE_CUBE
		vec4 texColor = textureCube( envMap, backgroundRotation * vec3( flipEnvMap * vWorldDirection.x, vWorldDirection.yz ) );
	#elif defined( ENVMAP_TYPE_CUBE_UV )
		vec4 texColor = textureCubeUV( envMap, backgroundRotation * vWorldDirection, backgroundBlurriness );
	#else
		vec4 texColor = vec4( 0.0, 0.0, 0.0, 1.0 );
	#endif
	texColor.rgb *= backgroundIntensity;
	gl_FragColor = texColor;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,J_=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,Q_=`uniform samplerCube tCube;
uniform float tFlip;
uniform float opacity;
varying vec3 vWorldDirection;
void main() {
	vec4 texColor = textureCube( tCube, vec3( tFlip * vWorldDirection.x, vWorldDirection.yz ) );
	gl_FragColor = texColor;
	gl_FragColor.a *= opacity;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,eg=`#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
varying vec2 vHighPrecisionZW;
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <skinbase_vertex>
	#include <morphinstance_vertex>
	#ifdef USE_DISPLACEMENTMAP
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vHighPrecisionZW = gl_Position.zw;
}`,tg=`#if DEPTH_PACKING == 3200
	uniform float opacity;
#endif
#include <common>
#include <packing>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
varying vec2 vHighPrecisionZW;
void main() {
	vec4 diffuseColor = vec4( 1.0 );
	#include <clipping_planes_fragment>
	#if DEPTH_PACKING == 3200
		diffuseColor.a = opacity;
	#endif
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <logdepthbuf_fragment>
	#ifdef USE_REVERSED_DEPTH_BUFFER
		float fragCoordZ = vHighPrecisionZW[ 0 ] / vHighPrecisionZW[ 1 ];
	#else
		float fragCoordZ = 0.5 * vHighPrecisionZW[ 0 ] / vHighPrecisionZW[ 1 ] + 0.5;
	#endif
	#if DEPTH_PACKING == 3200
		gl_FragColor = vec4( vec3( 1.0 - fragCoordZ ), opacity );
	#elif DEPTH_PACKING == 3201
		gl_FragColor = packDepthToRGBA( fragCoordZ );
	#elif DEPTH_PACKING == 3202
		gl_FragColor = vec4( packDepthToRGB( fragCoordZ ), 1.0 );
	#elif DEPTH_PACKING == 3203
		gl_FragColor = vec4( packDepthToRG( fragCoordZ ), 0.0, 1.0 );
	#endif
}`,ng=`#define DISTANCE
varying vec3 vWorldPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <skinbase_vertex>
	#include <morphinstance_vertex>
	#ifdef USE_DISPLACEMENTMAP
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <worldpos_vertex>
	#include <clipping_planes_vertex>
	vWorldPosition = worldPosition.xyz;
}`,ig=`#define DISTANCE
uniform vec3 referencePosition;
uniform float nearDistance;
uniform float farDistance;
varying vec3 vWorldPosition;
#include <common>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <clipping_planes_pars_fragment>
void main () {
	vec4 diffuseColor = vec4( 1.0 );
	#include <clipping_planes_fragment>
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	float dist = length( vWorldPosition - referencePosition );
	dist = ( dist - nearDistance ) / ( farDistance - nearDistance );
	dist = saturate( dist );
	gl_FragColor = vec4( dist, 0.0, 0.0, 1.0 );
}`,rg=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
}`,sg=`uniform sampler2D tEquirect;
varying vec3 vWorldDirection;
#include <common>
void main() {
	vec3 direction = normalize( vWorldDirection );
	vec2 sampleUV = equirectUv( direction );
	gl_FragColor = texture2D( tEquirect, sampleUV );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,ag=`uniform float scale;
attribute float lineDistance;
varying float vLineDistance;
#include <common>
#include <uv_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	vLineDistance = scale * lineDistance;
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
}`,og=`uniform vec3 diffuse;
uniform float opacity;
uniform float dashSize;
uniform float totalSize;
varying float vLineDistance;
#include <common>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	if ( mod( vLineDistance, totalSize ) > dashSize ) {
		discard;
	}
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,lg=`#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#if defined ( USE_ENVMAP ) || defined ( USE_SKINNING )
		#include <beginnormal_vertex>
		#include <morphnormal_vertex>
		#include <skinbase_vertex>
		#include <skinnormal_vertex>
		#include <defaultnormal_vertex>
	#endif
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <fog_vertex>
}`,cg=`uniform vec3 diffuse;
uniform float opacity;
#ifndef FLAT_SHADED
	varying vec3 vNormal;
#endif
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <fog_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	#ifdef USE_LIGHTMAP
		vec4 lightMapTexel = texture2D( lightMap, vLightMapUv );
		reflectedLight.indirectDiffuse += lightMapTexel.rgb * lightMapIntensity * RECIPROCAL_PI;
	#else
		reflectedLight.indirectDiffuse += vec3( 1.0 );
	#endif
	#include <aomap_fragment>
	reflectedLight.indirectDiffuse *= diffuseColor.rgb;
	vec3 outgoingLight = reflectedLight.indirectDiffuse;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,ug=`#define LAMBERT
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,hg=`#define LAMBERT
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float opacity;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_lambert_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_lambert_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + totalEmissiveRadiance;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,fg=`#define MATCAP
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <color_pars_vertex>
#include <displacementmap_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
	vViewPosition = - mvPosition.xyz;
}`,dg=`#define MATCAP
uniform vec3 diffuse;
uniform float opacity;
uniform sampler2D matcap;
varying vec3 vViewPosition;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <normal_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	vec3 viewDir = normalize( vViewPosition );
	vec3 x = normalize( vec3( viewDir.z, 0.0, - viewDir.x ) );
	vec3 y = cross( viewDir, x );
	vec2 uv = vec2( dot( x, normal ), dot( y, normal ) ) * 0.495 + 0.5;
	#ifdef USE_MATCAP
		vec4 matcapColor = texture2D( matcap, uv );
	#else
		vec4 matcapColor = vec4( vec3( mix( 0.2, 0.8, uv.y ) ), 1.0 );
	#endif
	vec3 outgoingLight = diffuseColor.rgb * matcapColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,pg=`#define NORMAL
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	varying vec3 vViewPosition;
#endif
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	vViewPosition = - mvPosition.xyz;
#endif
}`,mg=`#define NORMAL
uniform float opacity;
#if defined( FLAT_SHADED ) || defined( USE_BUMPMAP ) || defined( USE_NORMALMAP_TANGENTSPACE )
	varying vec3 vViewPosition;
#endif
#include <uv_pars_fragment>
#include <normal_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( 0.0, 0.0, 0.0, opacity );
	#include <clipping_planes_fragment>
	#include <logdepthbuf_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	gl_FragColor = vec4( normalize( normal ) * 0.5 + 0.5, diffuseColor.a );
	#ifdef OPAQUE
		gl_FragColor.a = 1.0;
	#endif
}`,_g=`#define PHONG
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <envmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <envmap_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,gg=`#define PHONG
uniform vec3 diffuse;
uniform vec3 emissive;
uniform vec3 specular;
uniform float shininess;
uniform float opacity;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_phong_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <specularmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <specularmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_phong_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + reflectedLight.directSpecular + reflectedLight.indirectSpecular + totalEmissiveRadiance;
	#include <envmap_fragment>
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,vg=`#define STANDARD
varying vec3 vViewPosition;
#ifdef USE_TRANSMISSION
	varying vec3 vWorldPosition;
#endif
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
#ifdef USE_TRANSMISSION
	vWorldPosition = worldPosition.xyz;
#endif
}`,xg=`#define STANDARD
#ifdef PHYSICAL
	#define IOR
	#define USE_SPECULAR
#endif
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float roughness;
uniform float metalness;
uniform float opacity;
#ifdef IOR
	uniform float ior;
#endif
#ifdef USE_SPECULAR
	uniform float specularIntensity;
	uniform vec3 specularColor;
	#ifdef USE_SPECULAR_COLORMAP
		uniform sampler2D specularColorMap;
	#endif
	#ifdef USE_SPECULAR_INTENSITYMAP
		uniform sampler2D specularIntensityMap;
	#endif
#endif
#ifdef USE_CLEARCOAT
	uniform float clearcoat;
	uniform float clearcoatRoughness;
#endif
#ifdef USE_DISPERSION
	uniform float dispersion;
#endif
#ifdef USE_IRIDESCENCE
	uniform float iridescence;
	uniform float iridescenceIOR;
	uniform float iridescenceThicknessMinimum;
	uniform float iridescenceThicknessMaximum;
#endif
#ifdef USE_SHEEN
	uniform vec3 sheenColor;
	uniform float sheenRoughness;
	#ifdef USE_SHEEN_COLORMAP
		uniform sampler2D sheenColorMap;
	#endif
	#ifdef USE_SHEEN_ROUGHNESSMAP
		uniform sampler2D sheenRoughnessMap;
	#endif
#endif
#ifdef USE_ANISOTROPY
	uniform vec2 anisotropyVector;
	#ifdef USE_ANISOTROPYMAP
		uniform sampler2D anisotropyMap;
	#endif
#endif
varying vec3 vViewPosition;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <iridescence_fragment>
#include <cube_uv_reflection_fragment>
#include <envmap_common_pars_fragment>
#include <envmap_physical_pars_fragment>
#include <fog_pars_fragment>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_physical_pars_fragment>
#include <transmission_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <clearcoat_pars_fragment>
#include <iridescence_pars_fragment>
#include <roughnessmap_pars_fragment>
#include <metalnessmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <roughnessmap_fragment>
	#include <metalnessmap_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <clearcoat_normal_fragment_begin>
	#include <clearcoat_normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_physical_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 totalDiffuse = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse;
	vec3 totalSpecular = reflectedLight.directSpecular + reflectedLight.indirectSpecular;
	#include <transmission_fragment>
	vec3 outgoingLight = totalDiffuse + totalSpecular + totalEmissiveRadiance;
	#ifdef USE_SHEEN
 
		outgoingLight = outgoingLight + sheenSpecularDirect + sheenSpecularIndirect;
 
 	#endif
	#ifdef USE_CLEARCOAT
		float dotNVcc = saturate( dot( geometryClearcoatNormal, geometryViewDir ) );
		vec3 Fcc = F_Schlick( material.clearcoatF0, material.clearcoatF90, dotNVcc );
		outgoingLight = outgoingLight * ( 1.0 - material.clearcoat * Fcc ) + ( clearcoatSpecularDirect + clearcoatSpecularIndirect ) * material.clearcoat;
	#endif
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,Mg=`#define TOON
varying vec3 vViewPosition;
#include <common>
#include <batching_pars_vertex>
#include <uv_pars_vertex>
#include <displacementmap_pars_vertex>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <normal_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <shadowmap_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <normal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <displacementmap_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	vViewPosition = - mvPosition.xyz;
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,Sg=`#define TOON
uniform vec3 diffuse;
uniform vec3 emissive;
uniform float opacity;
#include <common>
#include <dithering_pars_fragment>
#include <color_pars_fragment>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <aomap_pars_fragment>
#include <lightmap_pars_fragment>
#include <emissivemap_pars_fragment>
#include <gradientmap_pars_fragment>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <normal_pars_fragment>
#include <lights_toon_pars_fragment>
#include <shadowmap_pars_fragment>
#include <bumpmap_pars_fragment>
#include <normalmap_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	ReflectedLight reflectedLight = ReflectedLight( vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ), vec3( 0.0 ) );
	vec3 totalEmissiveRadiance = emissive;
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <color_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	#include <normal_fragment_begin>
	#include <normal_fragment_maps>
	#include <emissivemap_fragment>
	#include <lights_toon_fragment>
	#include <lights_fragment_begin>
	#include <lights_fragment_maps>
	#include <lights_fragment_end>
	#include <aomap_fragment>
	vec3 outgoingLight = reflectedLight.directDiffuse + reflectedLight.indirectDiffuse + totalEmissiveRadiance;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
	#include <dithering_fragment>
}`,yg=`uniform float size;
uniform float scale;
#include <common>
#include <color_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
#ifdef USE_POINTS_UV
	varying vec2 vUv;
	uniform mat3 uvTransform;
#endif
void main() {
	#ifdef USE_POINTS_UV
		vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	#endif
	#include <color_vertex>
	#include <morphinstance_vertex>
	#include <morphcolor_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <project_vertex>
	gl_PointSize = size;
	#ifdef USE_SIZEATTENUATION
		bool isPerspective = isPerspectiveMatrix( projectionMatrix );
		if ( isPerspective ) gl_PointSize *= ( scale / - mvPosition.z );
	#endif
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <worldpos_vertex>
	#include <fog_vertex>
}`,bg=`uniform vec3 diffuse;
uniform float opacity;
#include <common>
#include <color_pars_fragment>
#include <map_particle_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_particle_fragment>
	#include <color_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,Eg=`#include <common>
#include <batching_pars_vertex>
#include <fog_pars_vertex>
#include <morphtarget_pars_vertex>
#include <skinning_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <shadowmap_pars_vertex>
void main() {
	#include <batching_vertex>
	#include <beginnormal_vertex>
	#include <morphinstance_vertex>
	#include <morphnormal_vertex>
	#include <skinbase_vertex>
	#include <skinnormal_vertex>
	#include <defaultnormal_vertex>
	#include <begin_vertex>
	#include <morphtarget_vertex>
	#include <skinning_vertex>
	#include <project_vertex>
	#include <logdepthbuf_vertex>
	#include <worldpos_vertex>
	#include <shadowmap_vertex>
	#include <fog_vertex>
}`,Tg=`uniform vec3 color;
uniform float opacity;
#include <common>
#include <fog_pars_fragment>
#include <bsdfs>
#include <lights_pars_begin>
#include <logdepthbuf_pars_fragment>
#include <shadowmap_pars_fragment>
#include <shadowmask_pars_fragment>
void main() {
	#include <logdepthbuf_fragment>
	gl_FragColor = vec4( color, opacity * ( 1.0 - getShadowMask() ) );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
	#include <premultiplied_alpha_fragment>
}`,Ag=`uniform float rotation;
uniform vec2 center;
#include <common>
#include <uv_pars_vertex>
#include <fog_pars_vertex>
#include <logdepthbuf_pars_vertex>
#include <clipping_planes_pars_vertex>
void main() {
	#include <uv_vertex>
	vec4 mvPosition = modelViewMatrix[ 3 ];
	vec2 scale = vec2( length( modelMatrix[ 0 ].xyz ), length( modelMatrix[ 1 ].xyz ) );
	#ifndef USE_SIZEATTENUATION
		bool isPerspective = isPerspectiveMatrix( projectionMatrix );
		if ( isPerspective ) scale *= - mvPosition.z;
	#endif
	vec2 alignedPosition = ( position.xy - ( center - vec2( 0.5 ) ) ) * scale;
	vec2 rotatedPosition;
	rotatedPosition.x = cos( rotation ) * alignedPosition.x - sin( rotation ) * alignedPosition.y;
	rotatedPosition.y = sin( rotation ) * alignedPosition.x + cos( rotation ) * alignedPosition.y;
	mvPosition.xy += rotatedPosition;
	gl_Position = projectionMatrix * mvPosition;
	#include <logdepthbuf_vertex>
	#include <clipping_planes_vertex>
	#include <fog_vertex>
}`,wg=`uniform vec3 diffuse;
uniform float opacity;
#include <common>
#include <uv_pars_fragment>
#include <map_pars_fragment>
#include <alphamap_pars_fragment>
#include <alphatest_pars_fragment>
#include <alphahash_pars_fragment>
#include <fog_pars_fragment>
#include <logdepthbuf_pars_fragment>
#include <clipping_planes_pars_fragment>
void main() {
	vec4 diffuseColor = vec4( diffuse, opacity );
	#include <clipping_planes_fragment>
	vec3 outgoingLight = vec3( 0.0 );
	#include <logdepthbuf_fragment>
	#include <map_fragment>
	#include <alphamap_fragment>
	#include <alphatest_fragment>
	#include <alphahash_fragment>
	outgoingLight = diffuseColor.rgb;
	#include <opaque_fragment>
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
	#include <fog_fragment>
}`,je={alphahash_fragment:Kp,alphahash_pars_fragment:Zp,alphamap_fragment:jp,alphamap_pars_fragment:Jp,alphatest_fragment:Qp,alphatest_pars_fragment:em,aomap_fragment:tm,aomap_pars_fragment:nm,batching_pars_vertex:im,batching_vertex:rm,begin_vertex:sm,beginnormal_vertex:am,bsdfs:om,iridescence_fragment:lm,bumpmap_pars_fragment:cm,clipping_planes_fragment:um,clipping_planes_pars_fragment:hm,clipping_planes_pars_vertex:fm,clipping_planes_vertex:dm,color_fragment:pm,color_pars_fragment:mm,color_pars_vertex:_m,color_vertex:gm,common:vm,cube_uv_reflection_fragment:xm,defaultnormal_vertex:Mm,displacementmap_pars_vertex:Sm,displacementmap_vertex:ym,emissivemap_fragment:bm,emissivemap_pars_fragment:Em,colorspace_fragment:Tm,colorspace_pars_fragment:Am,envmap_fragment:wm,envmap_common_pars_fragment:Rm,envmap_pars_fragment:Cm,envmap_pars_vertex:Pm,envmap_physical_pars_fragment:Vm,envmap_vertex:Dm,fog_vertex:Lm,fog_pars_vertex:Im,fog_fragment:Um,fog_pars_fragment:Nm,gradientmap_pars_fragment:Fm,lightmap_pars_fragment:Om,lights_lambert_fragment:Bm,lights_lambert_pars_fragment:km,lights_pars_begin:zm,lights_toon_fragment:Gm,lights_toon_pars_fragment:Hm,lights_phong_fragment:Wm,lights_phong_pars_fragment:Xm,lights_physical_fragment:qm,lights_physical_pars_fragment:Ym,lights_fragment_begin:$m,lights_fragment_maps:Km,lights_fragment_end:Zm,logdepthbuf_fragment:jm,logdepthbuf_pars_fragment:Jm,logdepthbuf_pars_vertex:Qm,logdepthbuf_vertex:e_,map_fragment:t_,map_pars_fragment:n_,map_particle_fragment:i_,map_particle_pars_fragment:r_,metalnessmap_fragment:s_,metalnessmap_pars_fragment:a_,morphinstance_vertex:o_,morphcolor_vertex:l_,morphnormal_vertex:c_,morphtarget_pars_vertex:u_,morphtarget_vertex:h_,normal_fragment_begin:f_,normal_fragment_maps:d_,normal_pars_fragment:p_,normal_pars_vertex:m_,normal_vertex:__,normalmap_pars_fragment:g_,clearcoat_normal_fragment_begin:v_,clearcoat_normal_fragment_maps:x_,clearcoat_pars_fragment:M_,iridescence_pars_fragment:S_,opaque_fragment:y_,packing:b_,premultiplied_alpha_fragment:E_,project_vertex:T_,dithering_fragment:A_,dithering_pars_fragment:w_,roughnessmap_fragment:R_,roughnessmap_pars_fragment:C_,shadowmap_pars_fragment:P_,shadowmap_pars_vertex:D_,shadowmap_vertex:L_,shadowmask_pars_fragment:I_,skinbase_vertex:U_,skinning_pars_vertex:N_,skinning_vertex:F_,skinnormal_vertex:O_,specularmap_fragment:B_,specularmap_pars_fragment:k_,tonemapping_fragment:z_,tonemapping_pars_fragment:V_,transmission_fragment:G_,transmission_pars_fragment:H_,uv_pars_fragment:W_,uv_pars_vertex:X_,uv_vertex:q_,worldpos_vertex:Y_,background_vert:$_,background_frag:K_,backgroundCube_vert:Z_,backgroundCube_frag:j_,cube_vert:J_,cube_frag:Q_,depth_vert:eg,depth_frag:tg,distance_vert:ng,distance_frag:ig,equirect_vert:rg,equirect_frag:sg,linedashed_vert:ag,linedashed_frag:og,meshbasic_vert:lg,meshbasic_frag:cg,meshlambert_vert:ug,meshlambert_frag:hg,meshmatcap_vert:fg,meshmatcap_frag:dg,meshnormal_vert:pg,meshnormal_frag:mg,meshphong_vert:_g,meshphong_frag:gg,meshphysical_vert:vg,meshphysical_frag:xg,meshtoon_vert:Mg,meshtoon_frag:Sg,points_vert:yg,points_frag:bg,shadow_vert:Eg,shadow_frag:Tg,sprite_vert:Ag,sprite_frag:wg},ye={common:{diffuse:{value:new it(16777215)},opacity:{value:1},map:{value:null},mapTransform:{value:new Ke},alphaMap:{value:null},alphaMapTransform:{value:new Ke},alphaTest:{value:0}},specularmap:{specularMap:{value:null},specularMapTransform:{value:new Ke}},envmap:{envMap:{value:null},envMapRotation:{value:new Ke},flipEnvMap:{value:-1},reflectivity:{value:1},ior:{value:1.5},refractionRatio:{value:.98},dfgLUT:{value:null}},aomap:{aoMap:{value:null},aoMapIntensity:{value:1},aoMapTransform:{value:new Ke}},lightmap:{lightMap:{value:null},lightMapIntensity:{value:1},lightMapTransform:{value:new Ke}},bumpmap:{bumpMap:{value:null},bumpMapTransform:{value:new Ke},bumpScale:{value:1}},normalmap:{normalMap:{value:null},normalMapTransform:{value:new Ke},normalScale:{value:new qe(1,1)}},displacementmap:{displacementMap:{value:null},displacementMapTransform:{value:new Ke},displacementScale:{value:1},displacementBias:{value:0}},emissivemap:{emissiveMap:{value:null},emissiveMapTransform:{value:new Ke}},metalnessmap:{metalnessMap:{value:null},metalnessMapTransform:{value:new Ke}},roughnessmap:{roughnessMap:{value:null},roughnessMapTransform:{value:new Ke}},gradientmap:{gradientMap:{value:null}},fog:{fogDensity:{value:25e-5},fogNear:{value:1},fogFar:{value:2e3},fogColor:{value:new it(16777215)}},lights:{ambientLightColor:{value:[]},lightProbe:{value:[]},directionalLights:{value:[],properties:{direction:{},color:{}}},directionalLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},directionalShadowMatrix:{value:[]},spotLights:{value:[],properties:{color:{},position:{},direction:{},distance:{},coneCos:{},penumbraCos:{},decay:{}}},spotLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},spotLightMap:{value:[]},spotLightMatrix:{value:[]},pointLights:{value:[],properties:{color:{},position:{},decay:{},distance:{}}},pointLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{},shadowCameraNear:{},shadowCameraFar:{}}},pointShadowMatrix:{value:[]},hemisphereLights:{value:[],properties:{direction:{},skyColor:{},groundColor:{}}},rectAreaLights:{value:[],properties:{color:{},position:{},width:{},height:{}}},ltc_1:{value:null},ltc_2:{value:null}},points:{diffuse:{value:new it(16777215)},opacity:{value:1},size:{value:1},scale:{value:1},map:{value:null},alphaMap:{value:null},alphaMapTransform:{value:new Ke},alphaTest:{value:0},uvTransform:{value:new Ke}},sprite:{diffuse:{value:new it(16777215)},opacity:{value:1},center:{value:new qe(.5,.5)},rotation:{value:0},map:{value:null},mapTransform:{value:new Ke},alphaMap:{value:null},alphaMapTransform:{value:new Ke},alphaTest:{value:0}}},ei={basic:{uniforms:sn([ye.common,ye.specularmap,ye.envmap,ye.aomap,ye.lightmap,ye.fog]),vertexShader:je.meshbasic_vert,fragmentShader:je.meshbasic_frag},lambert:{uniforms:sn([ye.common,ye.specularmap,ye.envmap,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.fog,ye.lights,{emissive:{value:new it(0)},envMapIntensity:{value:1}}]),vertexShader:je.meshlambert_vert,fragmentShader:je.meshlambert_frag},phong:{uniforms:sn([ye.common,ye.specularmap,ye.envmap,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.fog,ye.lights,{emissive:{value:new it(0)},specular:{value:new it(1118481)},shininess:{value:30},envMapIntensity:{value:1}}]),vertexShader:je.meshphong_vert,fragmentShader:je.meshphong_frag},standard:{uniforms:sn([ye.common,ye.envmap,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.roughnessmap,ye.metalnessmap,ye.fog,ye.lights,{emissive:{value:new it(0)},roughness:{value:1},metalness:{value:0},envMapIntensity:{value:1}}]),vertexShader:je.meshphysical_vert,fragmentShader:je.meshphysical_frag},toon:{uniforms:sn([ye.common,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.gradientmap,ye.fog,ye.lights,{emissive:{value:new it(0)}}]),vertexShader:je.meshtoon_vert,fragmentShader:je.meshtoon_frag},matcap:{uniforms:sn([ye.common,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.fog,{matcap:{value:null}}]),vertexShader:je.meshmatcap_vert,fragmentShader:je.meshmatcap_frag},points:{uniforms:sn([ye.points,ye.fog]),vertexShader:je.points_vert,fragmentShader:je.points_frag},dashed:{uniforms:sn([ye.common,ye.fog,{scale:{value:1},dashSize:{value:1},totalSize:{value:2}}]),vertexShader:je.linedashed_vert,fragmentShader:je.linedashed_frag},depth:{uniforms:sn([ye.common,ye.displacementmap]),vertexShader:je.depth_vert,fragmentShader:je.depth_frag},normal:{uniforms:sn([ye.common,ye.bumpmap,ye.normalmap,ye.displacementmap,{opacity:{value:1}}]),vertexShader:je.meshnormal_vert,fragmentShader:je.meshnormal_frag},sprite:{uniforms:sn([ye.sprite,ye.fog]),vertexShader:je.sprite_vert,fragmentShader:je.sprite_frag},background:{uniforms:{uvTransform:{value:new Ke},t2D:{value:null},backgroundIntensity:{value:1}},vertexShader:je.background_vert,fragmentShader:je.background_frag},backgroundCube:{uniforms:{envMap:{value:null},flipEnvMap:{value:-1},backgroundBlurriness:{value:0},backgroundIntensity:{value:1},backgroundRotation:{value:new Ke}},vertexShader:je.backgroundCube_vert,fragmentShader:je.backgroundCube_frag},cube:{uniforms:{tCube:{value:null},tFlip:{value:-1},opacity:{value:1}},vertexShader:je.cube_vert,fragmentShader:je.cube_frag},equirect:{uniforms:{tEquirect:{value:null}},vertexShader:je.equirect_vert,fragmentShader:je.equirect_frag},distance:{uniforms:sn([ye.common,ye.displacementmap,{referencePosition:{value:new V},nearDistance:{value:1},farDistance:{value:1e3}}]),vertexShader:je.distance_vert,fragmentShader:je.distance_frag},shadow:{uniforms:sn([ye.lights,ye.fog,{color:{value:new it(0)},opacity:{value:1}}]),vertexShader:je.shadow_vert,fragmentShader:je.shadow_frag}};ei.physical={uniforms:sn([ei.standard.uniforms,{clearcoat:{value:0},clearcoatMap:{value:null},clearcoatMapTransform:{value:new Ke},clearcoatNormalMap:{value:null},clearcoatNormalMapTransform:{value:new Ke},clearcoatNormalScale:{value:new qe(1,1)},clearcoatRoughness:{value:0},clearcoatRoughnessMap:{value:null},clearcoatRoughnessMapTransform:{value:new Ke},dispersion:{value:0},iridescence:{value:0},iridescenceMap:{value:null},iridescenceMapTransform:{value:new Ke},iridescenceIOR:{value:1.3},iridescenceThicknessMinimum:{value:100},iridescenceThicknessMaximum:{value:400},iridescenceThicknessMap:{value:null},iridescenceThicknessMapTransform:{value:new Ke},sheen:{value:0},sheenColor:{value:new it(0)},sheenColorMap:{value:null},sheenColorMapTransform:{value:new Ke},sheenRoughness:{value:1},sheenRoughnessMap:{value:null},sheenRoughnessMapTransform:{value:new Ke},transmission:{value:0},transmissionMap:{value:null},transmissionMapTransform:{value:new Ke},transmissionSamplerSize:{value:new qe},transmissionSamplerMap:{value:null},thickness:{value:0},thicknessMap:{value:null},thicknessMapTransform:{value:new Ke},attenuationDistance:{value:0},attenuationColor:{value:new it(0)},specularColor:{value:new it(1,1,1)},specularColorMap:{value:null},specularColorMapTransform:{value:new Ke},specularIntensity:{value:1},specularIntensityMap:{value:null},specularIntensityMapTransform:{value:new Ke},anisotropyVector:{value:new qe},anisotropyMap:{value:null},anisotropyMapTransform:{value:new Ke}}]),vertexShader:je.meshphysical_vert,fragmentShader:je.meshphysical_frag};const wa={r:0,b:0,g:0},lr=new li,Rg=new St;function Cg(r,e,t,n,i,s){const a=new it(0);let o=i===!0?0:1,l,c,u=null,f=0,h=null;function d(M){let S=M.isScene===!0?M.background:null;if(S&&S.isTexture){const y=M.backgroundBlurriness>0;S=e.get(S,y)}return S}function _(M){let S=!1;const y=d(M);y===null?p(a,o):y&&y.isColor&&(p(y,1),S=!0);const A=r.xr.getEnvironmentBlendMode();A==="additive"?t.buffers.color.setClear(0,0,0,1,s):A==="alpha-blend"&&t.buffers.color.setClear(0,0,0,0,s),(r.autoClear||S)&&(t.buffers.depth.setTest(!0),t.buffers.depth.setMask(!0),t.buffers.color.setMask(!0),r.clear(r.autoClearColor,r.autoClearDepth,r.autoClearStencil))}function g(M,S){const y=d(S);y&&(y.isCubeTexture||y.mapping===eo)?(c===void 0&&(c=new Xt(new pn(1,1,1),new ci({name:"BackgroundCubeMaterial",uniforms:rs(ei.backgroundCube.uniforms),vertexShader:ei.backgroundCube.vertexShader,fragmentShader:ei.backgroundCube.fragmentShader,side:mn,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),c.geometry.deleteAttribute("normal"),c.geometry.deleteAttribute("uv"),c.onBeforeRender=function(A,w,C){this.matrixWorld.copyPosition(C.matrixWorld)},Object.defineProperty(c.material,"envMap",{get:function(){return this.uniforms.envMap.value}}),n.update(c)),lr.copy(S.backgroundRotation),lr.x*=-1,lr.y*=-1,lr.z*=-1,y.isCubeTexture&&y.isRenderTargetTexture===!1&&(lr.y*=-1,lr.z*=-1),c.material.uniforms.envMap.value=y,c.material.uniforms.flipEnvMap.value=y.isCubeTexture&&y.isRenderTargetTexture===!1?-1:1,c.material.uniforms.backgroundBlurriness.value=S.backgroundBlurriness,c.material.uniforms.backgroundIntensity.value=S.backgroundIntensity,c.material.uniforms.backgroundRotation.value.setFromMatrix4(Rg.makeRotationFromEuler(lr)),c.material.toneMapped=ht.getTransfer(y.colorSpace)!==_t,(u!==y||f!==y.version||h!==r.toneMapping)&&(c.material.needsUpdate=!0,u=y,f=y.version,h=r.toneMapping),c.layers.enableAll(),M.unshift(c,c.geometry,c.material,0,0,null)):y&&y.isTexture&&(l===void 0&&(l=new Xt(new Ks(2,2),new ci({name:"BackgroundMaterial",uniforms:rs(ei.background.uniforms),vertexShader:ei.background.vertexShader,fragmentShader:ei.background.fragmentShader,side:Zi,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),l.geometry.deleteAttribute("normal"),Object.defineProperty(l.material,"map",{get:function(){return this.uniforms.t2D.value}}),n.update(l)),l.material.uniforms.t2D.value=y,l.material.uniforms.backgroundIntensity.value=S.backgroundIntensity,l.material.toneMapped=ht.getTransfer(y.colorSpace)!==_t,y.matrixAutoUpdate===!0&&y.updateMatrix(),l.material.uniforms.uvTransform.value.copy(y.matrix),(u!==y||f!==y.version||h!==r.toneMapping)&&(l.material.needsUpdate=!0,u=y,f=y.version,h=r.toneMapping),l.layers.enableAll(),M.unshift(l,l.geometry,l.material,0,0,null))}function p(M,S){M.getRGB(wa,Qh(r)),t.buffers.color.setClear(wa.r,wa.g,wa.b,S,s)}function m(){c!==void 0&&(c.geometry.dispose(),c.material.dispose(),c=void 0),l!==void 0&&(l.geometry.dispose(),l.material.dispose(),l=void 0)}return{getClearColor:function(){return a},setClearColor:function(M,S=1){a.set(M),o=S,p(a,o)},getClearAlpha:function(){return o},setClearAlpha:function(M){o=M,p(a,o)},render:_,addToRenderList:g,dispose:m}}function Pg(r,e){const t=r.getParameter(r.MAX_VERTEX_ATTRIBS),n={},i=h(null);let s=i,a=!1;function o(L,G,z,q,H){let N=!1;const F=f(L,q,z,G);s!==F&&(s=F,c(s.object)),N=d(L,q,z,H),N&&_(L,q,z,H),H!==null&&e.update(H,r.ELEMENT_ARRAY_BUFFER),(N||a)&&(a=!1,y(L,G,z,q),H!==null&&r.bindBuffer(r.ELEMENT_ARRAY_BUFFER,e.get(H).buffer))}function l(){return r.createVertexArray()}function c(L){return r.bindVertexArray(L)}function u(L){return r.deleteVertexArray(L)}function f(L,G,z,q){const H=q.wireframe===!0;let N=n[G.id];N===void 0&&(N={},n[G.id]=N);const F=L.isInstancedMesh===!0?L.id:0;let ne=N[F];ne===void 0&&(ne={},N[F]=ne);let ie=ne[z.id];ie===void 0&&(ie={},ne[z.id]=ie);let me=ie[H];return me===void 0&&(me=h(l()),ie[H]=me),me}function h(L){const G=[],z=[],q=[];for(let H=0;H<t;H++)G[H]=0,z[H]=0,q[H]=0;return{geometry:null,program:null,wireframe:!1,newAttributes:G,enabledAttributes:z,attributeDivisors:q,object:L,attributes:{},index:null}}function d(L,G,z,q){const H=s.attributes,N=G.attributes;let F=0;const ne=z.getAttributes();for(const ie in ne)if(ne[ie].location>=0){const Te=H[ie];let le=N[ie];if(le===void 0&&(ie==="instanceMatrix"&&L.instanceMatrix&&(le=L.instanceMatrix),ie==="instanceColor"&&L.instanceColor&&(le=L.instanceColor)),Te===void 0||Te.attribute!==le||le&&Te.data!==le.data)return!0;F++}return s.attributesNum!==F||s.index!==q}function _(L,G,z,q){const H={},N=G.attributes;let F=0;const ne=z.getAttributes();for(const ie in ne)if(ne[ie].location>=0){let Te=N[ie];Te===void 0&&(ie==="instanceMatrix"&&L.instanceMatrix&&(Te=L.instanceMatrix),ie==="instanceColor"&&L.instanceColor&&(Te=L.instanceColor));const le={};le.attribute=Te,Te&&Te.data&&(le.data=Te.data),H[ie]=le,F++}s.attributes=H,s.attributesNum=F,s.index=q}function g(){const L=s.newAttributes;for(let G=0,z=L.length;G<z;G++)L[G]=0}function p(L){m(L,0)}function m(L,G){const z=s.newAttributes,q=s.enabledAttributes,H=s.attributeDivisors;z[L]=1,q[L]===0&&(r.enableVertexAttribArray(L),q[L]=1),H[L]!==G&&(r.vertexAttribDivisor(L,G),H[L]=G)}function M(){const L=s.newAttributes,G=s.enabledAttributes;for(let z=0,q=G.length;z<q;z++)G[z]!==L[z]&&(r.disableVertexAttribArray(z),G[z]=0)}function S(L,G,z,q,H,N,F){F===!0?r.vertexAttribIPointer(L,G,z,H,N):r.vertexAttribPointer(L,G,z,q,H,N)}function y(L,G,z,q){g();const H=q.attributes,N=z.getAttributes(),F=G.defaultAttributeValues;for(const ne in N){const ie=N[ne];if(ie.location>=0){let me=H[ne];if(me===void 0&&(ne==="instanceMatrix"&&L.instanceMatrix&&(me=L.instanceMatrix),ne==="instanceColor"&&L.instanceColor&&(me=L.instanceColor)),me!==void 0){const Te=me.normalized,le=me.itemSize,Oe=e.get(me);if(Oe===void 0)continue;const Je=Oe.buffer,$e=Oe.type,ee=Oe.bytesPerElement,de=$e===r.INT||$e===r.UNSIGNED_INT||me.gpuType===pc;if(me.isInterleavedBufferAttribute){const pe=me.data,Ve=pe.stride,Ne=me.offset;if(pe.isInstancedInterleavedBuffer){for(let Fe=0;Fe<ie.locationSize;Fe++)m(ie.location+Fe,pe.meshPerAttribute);L.isInstancedMesh!==!0&&q._maxInstanceCount===void 0&&(q._maxInstanceCount=pe.meshPerAttribute*pe.count)}else for(let Fe=0;Fe<ie.locationSize;Fe++)p(ie.location+Fe);r.bindBuffer(r.ARRAY_BUFFER,Je);for(let Fe=0;Fe<ie.locationSize;Fe++)S(ie.location+Fe,le/ie.locationSize,$e,Te,Ve*ee,(Ne+le/ie.locationSize*Fe)*ee,de)}else{if(me.isInstancedBufferAttribute){for(let pe=0;pe<ie.locationSize;pe++)m(ie.location+pe,me.meshPerAttribute);L.isInstancedMesh!==!0&&q._maxInstanceCount===void 0&&(q._maxInstanceCount=me.meshPerAttribute*me.count)}else for(let pe=0;pe<ie.locationSize;pe++)p(ie.location+pe);r.bindBuffer(r.ARRAY_BUFFER,Je);for(let pe=0;pe<ie.locationSize;pe++)S(ie.location+pe,le/ie.locationSize,$e,Te,le*ee,le/ie.locationSize*pe*ee,de)}}else if(F!==void 0){const Te=F[ne];if(Te!==void 0)switch(Te.length){case 2:r.vertexAttrib2fv(ie.location,Te);break;case 3:r.vertexAttrib3fv(ie.location,Te);break;case 4:r.vertexAttrib4fv(ie.location,Te);break;default:r.vertexAttrib1fv(ie.location,Te)}}}}M()}function A(){b();for(const L in n){const G=n[L];for(const z in G){const q=G[z];for(const H in q){const N=q[H];for(const F in N)u(N[F].object),delete N[F];delete q[H]}}delete n[L]}}function w(L){if(n[L.id]===void 0)return;const G=n[L.id];for(const z in G){const q=G[z];for(const H in q){const N=q[H];for(const F in N)u(N[F].object),delete N[F];delete q[H]}}delete n[L.id]}function C(L){for(const G in n){const z=n[G];for(const q in z){const H=z[q];if(H[L.id]===void 0)continue;const N=H[L.id];for(const F in N)u(N[F].object),delete N[F];delete H[L.id]}}}function x(L){for(const G in n){const z=n[G],q=L.isInstancedMesh===!0?L.id:0,H=z[q];if(H!==void 0){for(const N in H){const F=H[N];for(const ne in F)u(F[ne].object),delete F[ne];delete H[N]}delete z[q],Object.keys(z).length===0&&delete n[G]}}}function b(){W(),a=!0,s!==i&&(s=i,c(s.object))}function W(){i.geometry=null,i.program=null,i.wireframe=!1}return{setup:o,reset:b,resetDefaultState:W,dispose:A,releaseStatesOfGeometry:w,releaseStatesOfObject:x,releaseStatesOfProgram:C,initAttributes:g,enableAttribute:p,disableUnusedAttributes:M}}function Dg(r,e,t){let n;function i(c){n=c}function s(c,u){r.drawArrays(n,c,u),t.update(u,n,1)}function a(c,u,f){f!==0&&(r.drawArraysInstanced(n,c,u,f),t.update(u,n,f))}function o(c,u,f){if(f===0)return;e.get("WEBGL_multi_draw").multiDrawArraysWEBGL(n,c,0,u,0,f);let d=0;for(let _=0;_<f;_++)d+=u[_];t.update(d,n,1)}function l(c,u,f,h){if(f===0)return;const d=e.get("WEBGL_multi_draw");if(d===null)for(let _=0;_<c.length;_++)a(c[_],u[_],h[_]);else{d.multiDrawArraysInstancedWEBGL(n,c,0,u,0,h,0,f);let _=0;for(let g=0;g<f;g++)_+=u[g]*h[g];t.update(_,n,1)}}this.setMode=i,this.render=s,this.renderInstances=a,this.renderMultiDraw=o,this.renderMultiDrawInstances=l}function Lg(r,e,t,n){let i;function s(){if(i!==void 0)return i;if(e.has("EXT_texture_filter_anisotropic")===!0){const C=e.get("EXT_texture_filter_anisotropic");i=r.getParameter(C.MAX_TEXTURE_MAX_ANISOTROPY_EXT)}else i=0;return i}function a(C){return!(C!==qn&&n.convert(C)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_FORMAT))}function o(C){const x=C===Ti&&(e.has("EXT_color_buffer_half_float")||e.has("EXT_color_buffer_float"));return!(C!==Rn&&n.convert(C)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_TYPE)&&C!==ni&&!x)}function l(C){if(C==="highp"){if(r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.HIGH_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.HIGH_FLOAT).precision>0)return"highp";C="mediump"}return C==="mediump"&&r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.MEDIUM_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.MEDIUM_FLOAT).precision>0?"mediump":"lowp"}let c=t.precision!==void 0?t.precision:"highp";const u=l(c);u!==c&&(He("WebGLRenderer:",c,"not supported, using",u,"instead."),c=u);const f=t.logarithmicDepthBuffer===!0,h=t.reversedDepthBuffer===!0&&e.has("EXT_clip_control"),d=r.getParameter(r.MAX_TEXTURE_IMAGE_UNITS),_=r.getParameter(r.MAX_VERTEX_TEXTURE_IMAGE_UNITS),g=r.getParameter(r.MAX_TEXTURE_SIZE),p=r.getParameter(r.MAX_CUBE_MAP_TEXTURE_SIZE),m=r.getParameter(r.MAX_VERTEX_ATTRIBS),M=r.getParameter(r.MAX_VERTEX_UNIFORM_VECTORS),S=r.getParameter(r.MAX_VARYING_VECTORS),y=r.getParameter(r.MAX_FRAGMENT_UNIFORM_VECTORS),A=r.getParameter(r.MAX_SAMPLES),w=r.getParameter(r.SAMPLES);return{isWebGL2:!0,getMaxAnisotropy:s,getMaxPrecision:l,textureFormatReadable:a,textureTypeReadable:o,precision:c,logarithmicDepthBuffer:f,reversedDepthBuffer:h,maxTextures:d,maxVertexTextures:_,maxTextureSize:g,maxCubemapSize:p,maxAttributes:m,maxVertexUniforms:M,maxVaryings:S,maxFragmentUniforms:y,maxSamples:A,samples:w}}function Ig(r){const e=this;let t=null,n=0,i=!1,s=!1;const a=new Vi,o=new Ke,l={value:null,needsUpdate:!1};this.uniform=l,this.numPlanes=0,this.numIntersection=0,this.init=function(f,h){const d=f.length!==0||h||n!==0||i;return i=h,n=f.length,d},this.beginShadows=function(){s=!0,u(null)},this.endShadows=function(){s=!1},this.setGlobalState=function(f,h){t=u(f,h,0)},this.setState=function(f,h,d){const _=f.clippingPlanes,g=f.clipIntersection,p=f.clipShadows,m=r.get(f);if(!i||_===null||_.length===0||s&&!p)s?u(null):c();else{const M=s?0:n,S=M*4;let y=m.clippingState||null;l.value=y,y=u(_,h,S,d);for(let A=0;A!==S;++A)y[A]=t[A];m.clippingState=y,this.numIntersection=g?this.numPlanes:0,this.numPlanes+=M}};function c(){l.value!==t&&(l.value=t,l.needsUpdate=n>0),e.numPlanes=n,e.numIntersection=0}function u(f,h,d,_){const g=f!==null?f.length:0;let p=null;if(g!==0){if(p=l.value,_!==!0||p===null){const m=d+g*4,M=h.matrixWorldInverse;o.getNormalMatrix(M),(p===null||p.length<m)&&(p=new Float32Array(m));for(let S=0,y=d;S!==g;++S,y+=4)a.copy(f[S]).applyMatrix4(M,o),a.normal.toArray(p,y),p[y+3]=a.constant}l.value=p,l.needsUpdate=!0}return e.numPlanes=g,e.numIntersection=0,p}}const Wi=4,Uu=[.125,.215,.35,.446,.526,.582],pr=20,Ug=256,bs=new Rc,Nu=new it;let zo=null,Vo=0,Go=0,Ho=!1;const Ng=new V;class Fu{constructor(e){this._renderer=e,this._pingPongRenderTarget=null,this._lodMax=0,this._cubeSize=0,this._sizeLods=[],this._sigmas=[],this._lodMeshes=[],this._backgroundBox=null,this._cubemapMaterial=null,this._equirectMaterial=null,this._blurMaterial=null,this._ggxMaterial=null}fromScene(e,t=0,n=.1,i=100,s={}){const{size:a=256,position:o=Ng}=s;zo=this._renderer.getRenderTarget(),Vo=this._renderer.getActiveCubeFace(),Go=this._renderer.getActiveMipmapLevel(),Ho=this._renderer.xr.enabled,this._renderer.xr.enabled=!1,this._setSize(a);const l=this._allocateTargets();return l.depthBuffer=!0,this._sceneToCubeUV(e,n,i,l,o),t>0&&this._blur(l,0,0,t),this._applyPMREM(l),this._cleanup(l),l}fromEquirectangular(e,t=null){return this._fromTexture(e,t)}fromCubemap(e,t=null){return this._fromTexture(e,t)}compileCubemapShader(){this._cubemapMaterial===null&&(this._cubemapMaterial=ku(),this._compileMaterial(this._cubemapMaterial))}compileEquirectangularShader(){this._equirectMaterial===null&&(this._equirectMaterial=Bu(),this._compileMaterial(this._equirectMaterial))}dispose(){this._dispose(),this._cubemapMaterial!==null&&this._cubemapMaterial.dispose(),this._equirectMaterial!==null&&this._equirectMaterial.dispose(),this._backgroundBox!==null&&(this._backgroundBox.geometry.dispose(),this._backgroundBox.material.dispose())}_setSize(e){this._lodMax=Math.floor(Math.log2(e)),this._cubeSize=Math.pow(2,this._lodMax)}_dispose(){this._blurMaterial!==null&&this._blurMaterial.dispose(),this._ggxMaterial!==null&&this._ggxMaterial.dispose(),this._pingPongRenderTarget!==null&&this._pingPongRenderTarget.dispose();for(let e=0;e<this._lodMeshes.length;e++)this._lodMeshes[e].geometry.dispose()}_cleanup(e){this._renderer.setRenderTarget(zo,Vo,Go),this._renderer.xr.enabled=Ho,e.scissorTest=!1,Vr(e,0,0,e.width,e.height)}_fromTexture(e,t){e.mapping===yr||e.mapping===ts?this._setSize(e.image.length===0?16:e.image[0].width||e.image[0].image.width):this._setSize(e.image.width/4),zo=this._renderer.getRenderTarget(),Vo=this._renderer.getActiveCubeFace(),Go=this._renderer.getActiveMipmapLevel(),Ho=this._renderer.xr.enabled,this._renderer.xr.enabled=!1;const n=t||this._allocateTargets();return this._textureToCubeUV(e,n),this._applyPMREM(n),this._cleanup(n),n}_allocateTargets(){const e=3*Math.max(this._cubeSize,112),t=4*this._cubeSize,n={magFilter:en,minFilter:en,generateMipmaps:!1,type:Ti,format:qn,colorSpace:is,depthBuffer:!1},i=Ou(e,t,n);if(this._pingPongRenderTarget===null||this._pingPongRenderTarget.width!==e||this._pingPongRenderTarget.height!==t){this._pingPongRenderTarget!==null&&this._dispose(),this._pingPongRenderTarget=Ou(e,t,n);const{_lodMax:s}=this;({lodMeshes:this._lodMeshes,sizeLods:this._sizeLods,sigmas:this._sigmas}=Fg(s)),this._blurMaterial=Bg(s,e,t),this._ggxMaterial=Og(s,e,t)}return i}_compileMaterial(e){const t=new Xt(new ln,e);this._renderer.compile(t,bs)}_sceneToCubeUV(e,t,n,i,s){const l=new wn(90,1,t,n),c=[1,-1,1,1,1,1],u=[1,1,1,-1,-1,-1],f=this._renderer,h=f.autoClear,d=f.toneMapping;f.getClearColor(Nu),f.toneMapping=si,f.autoClear=!1,f.state.buffers.depth.getReversed()&&(f.setRenderTarget(i),f.clearDepth(),f.setRenderTarget(null)),this._backgroundBox===null&&(this._backgroundBox=new Xt(new pn,new Wa({name:"PMREM.Background",side:mn,depthWrite:!1,depthTest:!1})));const g=this._backgroundBox,p=g.material;let m=!1;const M=e.background;M?M.isColor&&(p.color.copy(M),e.background=null,m=!0):(p.color.copy(Nu),m=!0);for(let S=0;S<6;S++){const y=S%3;y===0?(l.up.set(0,c[S],0),l.position.set(s.x,s.y,s.z),l.lookAt(s.x+u[S],s.y,s.z)):y===1?(l.up.set(0,0,c[S]),l.position.set(s.x,s.y,s.z),l.lookAt(s.x,s.y+u[S],s.z)):(l.up.set(0,c[S],0),l.position.set(s.x,s.y,s.z),l.lookAt(s.x,s.y,s.z+u[S]));const A=this._cubeSize;Vr(i,y*A,S>2?A:0,A,A),f.setRenderTarget(i),m&&f.render(g,l),f.render(e,l)}f.toneMapping=d,f.autoClear=h,e.background=M}_textureToCubeUV(e,t){const n=this._renderer,i=e.mapping===yr||e.mapping===ts;i?(this._cubemapMaterial===null&&(this._cubemapMaterial=ku()),this._cubemapMaterial.uniforms.flipEnvMap.value=e.isRenderTargetTexture===!1?-1:1):this._equirectMaterial===null&&(this._equirectMaterial=Bu());const s=i?this._cubemapMaterial:this._equirectMaterial,a=this._lodMeshes[0];a.material=s;const o=s.uniforms;o.envMap.value=e;const l=this._cubeSize;Vr(t,0,0,3*l,2*l),n.setRenderTarget(t),n.render(a,bs)}_applyPMREM(e){const t=this._renderer,n=t.autoClear;t.autoClear=!1;const i=this._lodMeshes.length;for(let s=1;s<i;s++)this._applyGGXFilter(e,s-1,s);t.autoClear=n}_applyGGXFilter(e,t,n){const i=this._renderer,s=this._pingPongRenderTarget,a=this._ggxMaterial,o=this._lodMeshes[n];o.material=a;const l=a.uniforms,c=n/(this._lodMeshes.length-1),u=t/(this._lodMeshes.length-1),f=Math.sqrt(c*c-u*u),h=0+c*1.25,d=f*h,{_lodMax:_}=this,g=this._sizeLods[n],p=3*g*(n>_-Wi?n-_+Wi:0),m=4*(this._cubeSize-g);l.envMap.value=e.texture,l.roughness.value=d,l.mipInt.value=_-t,Vr(s,p,m,3*g,2*g),i.setRenderTarget(s),i.render(o,bs),l.envMap.value=s.texture,l.roughness.value=0,l.mipInt.value=_-n,Vr(e,p,m,3*g,2*g),i.setRenderTarget(e),i.render(o,bs)}_blur(e,t,n,i,s){const a=this._pingPongRenderTarget;this._halfBlur(e,a,t,n,i,"latitudinal",s),this._halfBlur(a,e,n,n,i,"longitudinal",s)}_halfBlur(e,t,n,i,s,a,o){const l=this._renderer,c=this._blurMaterial;a!=="latitudinal"&&a!=="longitudinal"&&ut("blur direction must be either latitudinal or longitudinal!");const u=3,f=this._lodMeshes[i];f.material=c;const h=c.uniforms,d=this._sizeLods[n]-1,_=isFinite(s)?Math.PI/(2*d):2*Math.PI/(2*pr-1),g=s/_,p=isFinite(s)?1+Math.floor(u*g):pr;p>pr&&He(`sigmaRadians, ${s}, is too large and will clip, as it requested ${p} samples when the maximum is set to ${pr}`);const m=[];let M=0;for(let C=0;C<pr;++C){const x=C/g,b=Math.exp(-x*x/2);m.push(b),C===0?M+=b:C<p&&(M+=2*b)}for(let C=0;C<m.length;C++)m[C]=m[C]/M;h.envMap.value=e.texture,h.samples.value=p,h.weights.value=m,h.latitudinal.value=a==="latitudinal",o&&(h.poleAxis.value=o);const{_lodMax:S}=this;h.dTheta.value=_,h.mipInt.value=S-n;const y=this._sizeLods[i],A=3*y*(i>S-Wi?i-S+Wi:0),w=4*(this._cubeSize-y);Vr(t,A,w,3*y,2*y),l.setRenderTarget(t),l.render(f,bs)}}function Fg(r){const e=[],t=[],n=[];let i=r;const s=r-Wi+1+Uu.length;for(let a=0;a<s;a++){const o=Math.pow(2,i);e.push(o);let l=1/o;a>r-Wi?l=Uu[a-r+Wi-1]:a===0&&(l=0),t.push(l);const c=1/(o-2),u=-c,f=1+c,h=[u,u,f,u,f,f,u,u,f,f,u,f],d=6,_=6,g=3,p=2,m=1,M=new Float32Array(g*_*d),S=new Float32Array(p*_*d),y=new Float32Array(m*_*d);for(let w=0;w<d;w++){const C=w%3*2/3-1,x=w>2?0:-1,b=[C,x,0,C+2/3,x,0,C+2/3,x+1,0,C,x,0,C+2/3,x+1,0,C,x+1,0];M.set(b,g*_*w),S.set(h,p*_*w);const W=[w,w,w,w,w,w];y.set(W,m*_*w)}const A=new ln;A.setAttribute("position",new Yn(M,g)),A.setAttribute("uv",new Yn(S,p)),A.setAttribute("faceIndex",new Yn(y,m)),n.push(new Xt(A,null)),i>Wi&&i--}return{lodMeshes:n,sizeLods:e,sigmas:t}}function Ou(r,e,t){const n=new ai(r,e,t);return n.texture.mapping=eo,n.texture.name="PMREM.cubeUv",n.scissorTest=!0,n}function Vr(r,e,t,n,i){r.viewport.set(e,t,n,i),r.scissor.set(e,t,n,i)}function Og(r,e,t){return new ci({name:"PMREMGGXConvolution",defines:{GGX_SAMPLES:Ug,CUBEUV_TEXEL_WIDTH:1/e,CUBEUV_TEXEL_HEIGHT:1/t,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},roughness:{value:0},mipInt:{value:0}},vertexShader:to(),fragmentShader:`

			precision highp float;
			precision highp int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;
			uniform float roughness;
			uniform float mipInt;

			#define ENVMAP_TYPE_CUBE_UV
			#include <cube_uv_reflection_fragment>

			#define PI 3.14159265359

			// Van der Corput radical inverse
			float radicalInverse_VdC(uint bits) {
				bits = (bits << 16u) | (bits >> 16u);
				bits = ((bits & 0x55555555u) << 1u) | ((bits & 0xAAAAAAAAu) >> 1u);
				bits = ((bits & 0x33333333u) << 2u) | ((bits & 0xCCCCCCCCu) >> 2u);
				bits = ((bits & 0x0F0F0F0Fu) << 4u) | ((bits & 0xF0F0F0F0u) >> 4u);
				bits = ((bits & 0x00FF00FFu) << 8u) | ((bits & 0xFF00FF00u) >> 8u);
				return float(bits) * 2.3283064365386963e-10; // / 0x100000000
			}

			// Hammersley sequence
			vec2 hammersley(uint i, uint N) {
				return vec2(float(i) / float(N), radicalInverse_VdC(i));
			}

			// GGX VNDF importance sampling (Eric Heitz 2018)
			// "Sampling the GGX Distribution of Visible Normals"
			// https://jcgt.org/published/0007/04/01/
			vec3 importanceSampleGGX_VNDF(vec2 Xi, vec3 V, float roughness) {
				float alpha = roughness * roughness;

				// Section 4.1: Orthonormal basis
				vec3 T1 = vec3(1.0, 0.0, 0.0);
				vec3 T2 = cross(V, T1);

				// Section 4.2: Parameterization of projected area
				float r = sqrt(Xi.x);
				float phi = 2.0 * PI * Xi.y;
				float t1 = r * cos(phi);
				float t2 = r * sin(phi);
				float s = 0.5 * (1.0 + V.z);
				t2 = (1.0 - s) * sqrt(1.0 - t1 * t1) + s * t2;

				// Section 4.3: Reprojection onto hemisphere
				vec3 Nh = t1 * T1 + t2 * T2 + sqrt(max(0.0, 1.0 - t1 * t1 - t2 * t2)) * V;

				// Section 3.4: Transform back to ellipsoid configuration
				return normalize(vec3(alpha * Nh.x, alpha * Nh.y, max(0.0, Nh.z)));
			}

			void main() {
				vec3 N = normalize(vOutputDirection);
				vec3 V = N; // Assume view direction equals normal for pre-filtering

				vec3 prefilteredColor = vec3(0.0);
				float totalWeight = 0.0;

				// For very low roughness, just sample the environment directly
				if (roughness < 0.001) {
					gl_FragColor = vec4(bilinearCubeUV(envMap, N, mipInt), 1.0);
					return;
				}

				// Tangent space basis for VNDF sampling
				vec3 up = abs(N.z) < 0.999 ? vec3(0.0, 0.0, 1.0) : vec3(1.0, 0.0, 0.0);
				vec3 tangent = normalize(cross(up, N));
				vec3 bitangent = cross(N, tangent);

				for(uint i = 0u; i < uint(GGX_SAMPLES); i++) {
					vec2 Xi = hammersley(i, uint(GGX_SAMPLES));

					// For PMREM, V = N, so in tangent space V is always (0, 0, 1)
					vec3 H_tangent = importanceSampleGGX_VNDF(Xi, vec3(0.0, 0.0, 1.0), roughness);

					// Transform H back to world space
					vec3 H = normalize(tangent * H_tangent.x + bitangent * H_tangent.y + N * H_tangent.z);
					vec3 L = normalize(2.0 * dot(V, H) * H - V);

					float NdotL = max(dot(N, L), 0.0);

					if(NdotL > 0.0) {
						// Sample environment at fixed mip level
						// VNDF importance sampling handles the distribution filtering
						vec3 sampleColor = bilinearCubeUV(envMap, L, mipInt);

						// Weight by NdotL for the split-sum approximation
						// VNDF PDF naturally accounts for the visible microfacet distribution
						prefilteredColor += sampleColor * NdotL;
						totalWeight += NdotL;
					}
				}

				if (totalWeight > 0.0) {
					prefilteredColor = prefilteredColor / totalWeight;
				}

				gl_FragColor = vec4(prefilteredColor, 1.0);
			}
		`,blending:bi,depthTest:!1,depthWrite:!1})}function Bg(r,e,t){const n=new Float32Array(pr),i=new V(0,1,0);return new ci({name:"SphericalGaussianBlur",defines:{n:pr,CUBEUV_TEXEL_WIDTH:1/e,CUBEUV_TEXEL_HEIGHT:1/t,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},samples:{value:1},weights:{value:n},latitudinal:{value:!1},dTheta:{value:0},mipInt:{value:0},poleAxis:{value:i}},vertexShader:to(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;
			uniform int samples;
			uniform float weights[ n ];
			uniform bool latitudinal;
			uniform float dTheta;
			uniform float mipInt;
			uniform vec3 poleAxis;

			#define ENVMAP_TYPE_CUBE_UV
			#include <cube_uv_reflection_fragment>

			vec3 getSample( float theta, vec3 axis ) {

				float cosTheta = cos( theta );
				// Rodrigues' axis-angle rotation
				vec3 sampleDirection = vOutputDirection * cosTheta
					+ cross( axis, vOutputDirection ) * sin( theta )
					+ axis * dot( axis, vOutputDirection ) * ( 1.0 - cosTheta );

				return bilinearCubeUV( envMap, sampleDirection, mipInt );

			}

			void main() {

				vec3 axis = latitudinal ? poleAxis : cross( poleAxis, vOutputDirection );

				if ( all( equal( axis, vec3( 0.0 ) ) ) ) {

					axis = vec3( vOutputDirection.z, 0.0, - vOutputDirection.x );

				}

				axis = normalize( axis );

				gl_FragColor = vec4( 0.0, 0.0, 0.0, 1.0 );
				gl_FragColor.rgb += weights[ 0 ] * getSample( 0.0, axis );

				for ( int i = 1; i < n; i++ ) {

					if ( i >= samples ) {

						break;

					}

					float theta = dTheta * float( i );
					gl_FragColor.rgb += weights[ i ] * getSample( -1.0 * theta, axis );
					gl_FragColor.rgb += weights[ i ] * getSample( theta, axis );

				}

			}
		`,blending:bi,depthTest:!1,depthWrite:!1})}function Bu(){return new ci({name:"EquirectangularToCubeUV",uniforms:{envMap:{value:null}},vertexShader:to(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			varying vec3 vOutputDirection;

			uniform sampler2D envMap;

			#include <common>

			void main() {

				vec3 outputDirection = normalize( vOutputDirection );
				vec2 uv = equirectUv( outputDirection );

				gl_FragColor = vec4( texture2D ( envMap, uv ).rgb, 1.0 );

			}
		`,blending:bi,depthTest:!1,depthWrite:!1})}function ku(){return new ci({name:"CubemapToCubeUV",uniforms:{envMap:{value:null},flipEnvMap:{value:-1}},vertexShader:to(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			uniform float flipEnvMap;

			varying vec3 vOutputDirection;

			uniform samplerCube envMap;

			void main() {

				gl_FragColor = textureCube( envMap, vec3( flipEnvMap * vOutputDirection.x, vOutputDirection.yz ) );

			}
		`,blending:bi,depthTest:!1,depthWrite:!1})}function to(){return`

		precision mediump float;
		precision mediump int;

		attribute float faceIndex;

		varying vec3 vOutputDirection;

		// RH coordinate system; PMREM face-indexing convention
		vec3 getDirection( vec2 uv, float face ) {

			uv = 2.0 * uv - 1.0;

			vec3 direction = vec3( uv, 1.0 );

			if ( face == 0.0 ) {

				direction = direction.zyx; // ( 1, v, u ) pos x

			} else if ( face == 1.0 ) {

				direction = direction.xzy;
				direction.xz *= -1.0; // ( -u, 1, -v ) pos y

			} else if ( face == 2.0 ) {

				direction.x *= -1.0; // ( -u, v, 1 ) pos z

			} else if ( face == 3.0 ) {

				direction = direction.zyx;
				direction.xz *= -1.0; // ( -1, v, -u ) neg x

			} else if ( face == 4.0 ) {

				direction = direction.xzy;
				direction.xy *= -1.0; // ( -u, -1, v ) neg y

			} else if ( face == 5.0 ) {

				direction.z *= -1.0; // ( u, v, -1 ) neg z

			}

			return direction;

		}

		void main() {

			vOutputDirection = getDirection( uv, faceIndex );
			gl_Position = vec4( position, 1.0 );

		}
	`}class rf extends ai{constructor(e=1,t={}){super(e,e,t),this.isWebGLCubeRenderTarget=!0;const n={width:e,height:e,depth:1},i=[n,n,n,n,n,n];this.texture=new jh(i),this._setTextureOptions(t),this.texture.isRenderTargetTexture=!0}fromEquirectangularTexture(e,t){this.texture.type=t.type,this.texture.colorSpace=t.colorSpace,this.texture.generateMipmaps=t.generateMipmaps,this.texture.minFilter=t.minFilter,this.texture.magFilter=t.magFilter;const n={uniforms:{tEquirect:{value:null}},vertexShader:`

				varying vec3 vWorldDirection;

				vec3 transformDirection( in vec3 dir, in mat4 matrix ) {

					return normalize( ( matrix * vec4( dir, 0.0 ) ).xyz );

				}

				void main() {

					vWorldDirection = transformDirection( position, modelMatrix );

					#include <begin_vertex>
					#include <project_vertex>

				}
			`,fragmentShader:`

				uniform sampler2D tEquirect;

				varying vec3 vWorldDirection;

				#include <common>

				void main() {

					vec3 direction = normalize( vWorldDirection );

					vec2 sampleUV = equirectUv( direction );

					gl_FragColor = texture2D( tEquirect, sampleUV );

				}
			`},i=new pn(5,5,5),s=new ci({name:"CubemapFromEquirect",uniforms:rs(n.uniforms),vertexShader:n.vertexShader,fragmentShader:n.fragmentShader,side:mn,blending:bi});s.uniforms.tEquirect.value=t;const a=new Xt(i,s),o=t.minFilter;return t.minFilter===mr&&(t.minFilter=en),new Gp(1,10,this).update(e,a),t.minFilter=o,a.geometry.dispose(),a.material.dispose(),this}clear(e,t=!0,n=!0,i=!0){const s=e.getRenderTarget();for(let a=0;a<6;a++)e.setRenderTarget(this,a),e.clear(t,n,i);e.setRenderTarget(s)}}function kg(r){let e=new WeakMap,t=new WeakMap,n=null;function i(h,d=!1){return h==null?null:d?a(h):s(h)}function s(h){if(h&&h.isTexture){const d=h.mapping;if(d===co||d===uo)if(e.has(h)){const _=e.get(h).texture;return o(_,h.mapping)}else{const _=h.image;if(_&&_.height>0){const g=new rf(_.height);return g.fromEquirectangularTexture(r,h),e.set(h,g),h.addEventListener("dispose",c),o(g.texture,h.mapping)}else return null}}return h}function a(h){if(h&&h.isTexture){const d=h.mapping,_=d===co||d===uo,g=d===yr||d===ts;if(_||g){let p=t.get(h);const m=p!==void 0?p.texture.pmremVersion:0;if(h.isRenderTargetTexture&&h.pmremVersion!==m)return n===null&&(n=new Fu(r)),p=_?n.fromEquirectangular(h,p):n.fromCubemap(h,p),p.texture.pmremVersion=h.pmremVersion,t.set(h,p),p.texture;if(p!==void 0)return p.texture;{const M=h.image;return _&&M&&M.height>0||g&&M&&l(M)?(n===null&&(n=new Fu(r)),p=_?n.fromEquirectangular(h):n.fromCubemap(h),p.texture.pmremVersion=h.pmremVersion,t.set(h,p),h.addEventListener("dispose",u),p.texture):null}}}return h}function o(h,d){return d===co?h.mapping=yr:d===uo&&(h.mapping=ts),h}function l(h){let d=0;const _=6;for(let g=0;g<_;g++)h[g]!==void 0&&d++;return d===_}function c(h){const d=h.target;d.removeEventListener("dispose",c);const _=e.get(d);_!==void 0&&(e.delete(d),_.dispose())}function u(h){const d=h.target;d.removeEventListener("dispose",u);const _=t.get(d);_!==void 0&&(t.delete(d),_.dispose())}function f(){e=new WeakMap,t=new WeakMap,n!==null&&(n.dispose(),n=null)}return{get:i,dispose:f}}function zg(r){const e={};function t(n){if(e[n]!==void 0)return e[n];const i=r.getExtension(n);return e[n]=i,i}return{has:function(n){return t(n)!==null},init:function(){t("EXT_color_buffer_float"),t("WEBGL_clip_cull_distance"),t("OES_texture_float_linear"),t("EXT_color_buffer_half_float"),t("WEBGL_multisampled_render_to_texture"),t("WEBGL_render_shared_exponent")},get:function(n){const i=t(n);return i===null&&Ha("WebGLRenderer: "+n+" extension not supported."),i}}}function Vg(r,e,t,n){const i={},s=new WeakMap;function a(f){const h=f.target;h.index!==null&&e.remove(h.index);for(const _ in h.attributes)e.remove(h.attributes[_]);h.removeEventListener("dispose",a),delete i[h.id];const d=s.get(h);d&&(e.remove(d),s.delete(h)),n.releaseStatesOfGeometry(h),h.isInstancedBufferGeometry===!0&&delete h._maxInstanceCount,t.memory.geometries--}function o(f,h){return i[h.id]===!0||(h.addEventListener("dispose",a),i[h.id]=!0,t.memory.geometries++),h}function l(f){const h=f.attributes;for(const d in h)e.update(h[d],r.ARRAY_BUFFER)}function c(f){const h=[],d=f.index,_=f.attributes.position;let g=0;if(_===void 0)return;if(d!==null){const M=d.array;g=d.version;for(let S=0,y=M.length;S<y;S+=3){const A=M[S+0],w=M[S+1],C=M[S+2];h.push(A,w,w,C,C,A)}}else{const M=_.array;g=_.version;for(let S=0,y=M.length/3-1;S<y;S+=3){const A=S+0,w=S+1,C=S+2;h.push(A,w,w,C,C,A)}}const p=new(_.count>=65535?Kh:$h)(h,1);p.version=g;const m=s.get(f);m&&e.remove(m),s.set(f,p)}function u(f){const h=s.get(f);if(h){const d=f.index;d!==null&&h.version<d.version&&c(f)}else c(f);return s.get(f)}return{get:o,update:l,getWireframeAttribute:u}}function Gg(r,e,t){let n;function i(h){n=h}let s,a;function o(h){s=h.type,a=h.bytesPerElement}function l(h,d){r.drawElements(n,d,s,h*a),t.update(d,n,1)}function c(h,d,_){_!==0&&(r.drawElementsInstanced(n,d,s,h*a,_),t.update(d,n,_))}function u(h,d,_){if(_===0)return;e.get("WEBGL_multi_draw").multiDrawElementsWEBGL(n,d,0,s,h,0,_);let p=0;for(let m=0;m<_;m++)p+=d[m];t.update(p,n,1)}function f(h,d,_,g){if(_===0)return;const p=e.get("WEBGL_multi_draw");if(p===null)for(let m=0;m<h.length;m++)c(h[m]/a,d[m],g[m]);else{p.multiDrawElementsInstancedWEBGL(n,d,0,s,h,0,g,0,_);let m=0;for(let M=0;M<_;M++)m+=d[M]*g[M];t.update(m,n,1)}}this.setMode=i,this.setIndex=o,this.render=l,this.renderInstances=c,this.renderMultiDraw=u,this.renderMultiDrawInstances=f}function Hg(r){const e={geometries:0,textures:0},t={frame:0,calls:0,triangles:0,points:0,lines:0};function n(s,a,o){switch(t.calls++,a){case r.TRIANGLES:t.triangles+=o*(s/3);break;case r.LINES:t.lines+=o*(s/2);break;case r.LINE_STRIP:t.lines+=o*(s-1);break;case r.LINE_LOOP:t.lines+=o*s;break;case r.POINTS:t.points+=o*s;break;default:ut("WebGLInfo: Unknown draw mode:",a);break}}function i(){t.calls=0,t.triangles=0,t.points=0,t.lines=0}return{memory:e,render:t,programs:null,autoReset:!0,reset:i,update:n}}function Wg(r,e,t){const n=new WeakMap,i=new Pt;function s(a,o,l){const c=a.morphTargetInfluences,u=o.morphAttributes.position||o.morphAttributes.normal||o.morphAttributes.color,f=u!==void 0?u.length:0;let h=n.get(o);if(h===void 0||h.count!==f){let b=function(){C.dispose(),n.delete(o),o.removeEventListener("dispose",b)};h!==void 0&&h.texture.dispose();const d=o.morphAttributes.position!==void 0,_=o.morphAttributes.normal!==void 0,g=o.morphAttributes.color!==void 0,p=o.morphAttributes.position||[],m=o.morphAttributes.normal||[],M=o.morphAttributes.color||[];let S=0;d===!0&&(S=1),_===!0&&(S=2),g===!0&&(S=3);let y=o.attributes.position.count*S,A=1;y>e.maxTextureSize&&(A=Math.ceil(y/e.maxTextureSize),y=e.maxTextureSize);const w=new Float32Array(y*A*4*f),C=new qh(w,y,A,f);C.type=ni,C.needsUpdate=!0;const x=S*4;for(let W=0;W<f;W++){const L=p[W],G=m[W],z=M[W],q=y*A*4*W;for(let H=0;H<L.count;H++){const N=H*x;d===!0&&(i.fromBufferAttribute(L,H),w[q+N+0]=i.x,w[q+N+1]=i.y,w[q+N+2]=i.z,w[q+N+3]=0),_===!0&&(i.fromBufferAttribute(G,H),w[q+N+4]=i.x,w[q+N+5]=i.y,w[q+N+6]=i.z,w[q+N+7]=0),g===!0&&(i.fromBufferAttribute(z,H),w[q+N+8]=i.x,w[q+N+9]=i.y,w[q+N+10]=i.z,w[q+N+11]=z.itemSize===4?i.w:1)}}h={count:f,texture:C,size:new qe(y,A)},n.set(o,h),o.addEventListener("dispose",b)}if(a.isInstancedMesh===!0&&a.morphTexture!==null)l.getUniforms().setValue(r,"morphTexture",a.morphTexture,t);else{let d=0;for(let g=0;g<c.length;g++)d+=c[g];const _=o.morphTargetsRelative?1:1-d;l.getUniforms().setValue(r,"morphTargetBaseInfluence",_),l.getUniforms().setValue(r,"morphTargetInfluences",c)}l.getUniforms().setValue(r,"morphTargetsTexture",h.texture,t),l.getUniforms().setValue(r,"morphTargetsTextureSize",h.size)}return{update:s}}function Xg(r,e,t,n,i){let s=new WeakMap;function a(c){const u=i.render.frame,f=c.geometry,h=e.get(c,f);if(s.get(h)!==u&&(e.update(h),s.set(h,u)),c.isInstancedMesh&&(c.hasEventListener("dispose",l)===!1&&c.addEventListener("dispose",l),s.get(c)!==u&&(t.update(c.instanceMatrix,r.ARRAY_BUFFER),c.instanceColor!==null&&t.update(c.instanceColor,r.ARRAY_BUFFER),s.set(c,u))),c.isSkinnedMesh){const d=c.skeleton;s.get(d)!==u&&(d.update(),s.set(d,u))}return h}function o(){s=new WeakMap}function l(c){const u=c.target;u.removeEventListener("dispose",l),n.releaseStatesOfObject(u),t.remove(u.instanceMatrix),u.instanceColor!==null&&t.remove(u.instanceColor)}return{update:a,dispose:o}}const qg={[Ph]:"LINEAR_TONE_MAPPING",[Dh]:"REINHARD_TONE_MAPPING",[Lh]:"CINEON_TONE_MAPPING",[dc]:"ACES_FILMIC_TONE_MAPPING",[Uh]:"AGX_TONE_MAPPING",[Nh]:"NEUTRAL_TONE_MAPPING",[Ih]:"CUSTOM_TONE_MAPPING"};function Yg(r,e,t,n,i){const s=new ai(e,t,{type:r,depthBuffer:n,stencilBuffer:i}),a=new ai(e,t,{type:Ti,depthBuffer:!1,stencilBuffer:!1}),o=new ln;o.setAttribute("position",new Yt([-1,3,0,-1,-1,0,3,-1,0],3)),o.setAttribute("uv",new Yt([0,2,0,0,2,0],2));const l=new Np({uniforms:{tDiffuse:{value:null}},vertexShader:`
			precision highp float;

			uniform mat4 modelViewMatrix;
			uniform mat4 projectionMatrix;

			attribute vec3 position;
			attribute vec2 uv;

			varying vec2 vUv;

			void main() {
				vUv = uv;
				gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
			}`,fragmentShader:`
			precision highp float;

			uniform sampler2D tDiffuse;

			varying vec2 vUv;

			#include <tonemapping_pars_fragment>
			#include <colorspace_pars_fragment>

			void main() {
				gl_FragColor = texture2D( tDiffuse, vUv );

				#ifdef LINEAR_TONE_MAPPING
					gl_FragColor.rgb = LinearToneMapping( gl_FragColor.rgb );
				#elif defined( REINHARD_TONE_MAPPING )
					gl_FragColor.rgb = ReinhardToneMapping( gl_FragColor.rgb );
				#elif defined( CINEON_TONE_MAPPING )
					gl_FragColor.rgb = CineonToneMapping( gl_FragColor.rgb );
				#elif defined( ACES_FILMIC_TONE_MAPPING )
					gl_FragColor.rgb = ACESFilmicToneMapping( gl_FragColor.rgb );
				#elif defined( AGX_TONE_MAPPING )
					gl_FragColor.rgb = AgXToneMapping( gl_FragColor.rgb );
				#elif defined( NEUTRAL_TONE_MAPPING )
					gl_FragColor.rgb = NeutralToneMapping( gl_FragColor.rgb );
				#elif defined( CUSTOM_TONE_MAPPING )
					gl_FragColor.rgb = CustomToneMapping( gl_FragColor.rgb );
				#endif

				#ifdef SRGB_TRANSFER
					gl_FragColor = sRGBTransferOETF( gl_FragColor );
				#endif
			}`,depthTest:!1,depthWrite:!1}),c=new Xt(o,l),u=new Rc(-1,1,1,-1,0,1);let f=null,h=null,d=!1,_,g=null,p=[],m=!1;this.setSize=function(M,S){s.setSize(M,S),a.setSize(M,S);for(let y=0;y<p.length;y++){const A=p[y];A.setSize&&A.setSize(M,S)}},this.setEffects=function(M){p=M,m=p.length>0&&p[0].isRenderPass===!0;const S=s.width,y=s.height;for(let A=0;A<p.length;A++){const w=p[A];w.setSize&&w.setSize(S,y)}},this.begin=function(M,S){if(d||M.toneMapping===si&&p.length===0)return!1;if(g=S,S!==null){const y=S.width,A=S.height;(s.width!==y||s.height!==A)&&this.setSize(y,A)}return m===!1&&M.setRenderTarget(s),_=M.toneMapping,M.toneMapping=si,!0},this.hasRenderPass=function(){return m},this.end=function(M,S){M.toneMapping=_,d=!0;let y=s,A=a;for(let w=0;w<p.length;w++){const C=p[w];if(C.enabled!==!1&&(C.render(M,A,y,S),C.needsSwap!==!1)){const x=y;y=A,A=x}}if(f!==M.outputColorSpace||h!==M.toneMapping){f=M.outputColorSpace,h=M.toneMapping,l.defines={},ht.getTransfer(f)===_t&&(l.defines.SRGB_TRANSFER="");const w=qg[h];w&&(l.defines[w]=""),l.needsUpdate=!0}l.uniforms.tDiffuse.value=y.texture,M.setRenderTarget(g),M.render(c,u),g=null,d=!1},this.isCompositing=function(){return d},this.dispose=function(){s.dispose(),a.dispose(),o.dispose(),l.dispose()}}const sf=new on,Yl=new Bs(1,1),af=new qh,of=new hp,lf=new jh,zu=[],Vu=[],Gu=new Float32Array(16),Hu=new Float32Array(9),Wu=new Float32Array(4);function fs(r,e,t){const n=r[0];if(n<=0||n>0)return r;const i=e*t;let s=zu[i];if(s===void 0&&(s=new Float32Array(i),zu[i]=s),e!==0){n.toArray(s,0);for(let a=1,o=0;a!==e;++a)o+=t,r[a].toArray(s,o)}return s}function zt(r,e){if(r.length!==e.length)return!1;for(let t=0,n=r.length;t<n;t++)if(r[t]!==e[t])return!1;return!0}function Vt(r,e){for(let t=0,n=e.length;t<n;t++)r[t]=e[t]}function no(r,e){let t=Vu[e];t===void 0&&(t=new Int32Array(e),Vu[e]=t);for(let n=0;n!==e;++n)t[n]=r.allocateTextureUnit();return t}function $g(r,e){const t=this.cache;t[0]!==e&&(r.uniform1f(this.addr,e),t[0]=e)}function Kg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(r.uniform2f(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(zt(t,e))return;r.uniform2fv(this.addr,e),Vt(t,e)}}function Zg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(r.uniform3f(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else if(e.r!==void 0)(t[0]!==e.r||t[1]!==e.g||t[2]!==e.b)&&(r.uniform3f(this.addr,e.r,e.g,e.b),t[0]=e.r,t[1]=e.g,t[2]=e.b);else{if(zt(t,e))return;r.uniform3fv(this.addr,e),Vt(t,e)}}function jg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(r.uniform4f(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(zt(t,e))return;r.uniform4fv(this.addr,e),Vt(t,e)}}function Jg(r,e){const t=this.cache,n=e.elements;if(n===void 0){if(zt(t,e))return;r.uniformMatrix2fv(this.addr,!1,e),Vt(t,e)}else{if(zt(t,n))return;Wu.set(n),r.uniformMatrix2fv(this.addr,!1,Wu),Vt(t,n)}}function Qg(r,e){const t=this.cache,n=e.elements;if(n===void 0){if(zt(t,e))return;r.uniformMatrix3fv(this.addr,!1,e),Vt(t,e)}else{if(zt(t,n))return;Hu.set(n),r.uniformMatrix3fv(this.addr,!1,Hu),Vt(t,n)}}function e0(r,e){const t=this.cache,n=e.elements;if(n===void 0){if(zt(t,e))return;r.uniformMatrix4fv(this.addr,!1,e),Vt(t,e)}else{if(zt(t,n))return;Gu.set(n),r.uniformMatrix4fv(this.addr,!1,Gu),Vt(t,n)}}function t0(r,e){const t=this.cache;t[0]!==e&&(r.uniform1i(this.addr,e),t[0]=e)}function n0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(r.uniform2i(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(zt(t,e))return;r.uniform2iv(this.addr,e),Vt(t,e)}}function i0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(r.uniform3i(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else{if(zt(t,e))return;r.uniform3iv(this.addr,e),Vt(t,e)}}function r0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(r.uniform4i(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(zt(t,e))return;r.uniform4iv(this.addr,e),Vt(t,e)}}function s0(r,e){const t=this.cache;t[0]!==e&&(r.uniform1ui(this.addr,e),t[0]=e)}function a0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(r.uniform2ui(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(zt(t,e))return;r.uniform2uiv(this.addr,e),Vt(t,e)}}function o0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(r.uniform3ui(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else{if(zt(t,e))return;r.uniform3uiv(this.addr,e),Vt(t,e)}}function l0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(r.uniform4ui(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(zt(t,e))return;r.uniform4uiv(this.addr,e),Vt(t,e)}}function c0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i);let s;this.type===r.SAMPLER_2D_SHADOW?(Yl.compareFunction=t.isReversedDepthBuffer()?Sc:Mc,s=Yl):s=sf,t.setTexture2D(e||s,i)}function u0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i),t.setTexture3D(e||of,i)}function h0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i),t.setTextureCube(e||lf,i)}function f0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i),t.setTexture2DArray(e||af,i)}function d0(r){switch(r){case 5126:return $g;case 35664:return Kg;case 35665:return Zg;case 35666:return jg;case 35674:return Jg;case 35675:return Qg;case 35676:return e0;case 5124:case 35670:return t0;case 35667:case 35671:return n0;case 35668:case 35672:return i0;case 35669:case 35673:return r0;case 5125:return s0;case 36294:return a0;case 36295:return o0;case 36296:return l0;case 35678:case 36198:case 36298:case 36306:case 35682:return c0;case 35679:case 36299:case 36307:return u0;case 35680:case 36300:case 36308:case 36293:return h0;case 36289:case 36303:case 36311:case 36292:return f0}}function p0(r,e){r.uniform1fv(this.addr,e)}function m0(r,e){const t=fs(e,this.size,2);r.uniform2fv(this.addr,t)}function _0(r,e){const t=fs(e,this.size,3);r.uniform3fv(this.addr,t)}function g0(r,e){const t=fs(e,this.size,4);r.uniform4fv(this.addr,t)}function v0(r,e){const t=fs(e,this.size,4);r.uniformMatrix2fv(this.addr,!1,t)}function x0(r,e){const t=fs(e,this.size,9);r.uniformMatrix3fv(this.addr,!1,t)}function M0(r,e){const t=fs(e,this.size,16);r.uniformMatrix4fv(this.addr,!1,t)}function S0(r,e){r.uniform1iv(this.addr,e)}function y0(r,e){r.uniform2iv(this.addr,e)}function b0(r,e){r.uniform3iv(this.addr,e)}function E0(r,e){r.uniform4iv(this.addr,e)}function T0(r,e){r.uniform1uiv(this.addr,e)}function A0(r,e){r.uniform2uiv(this.addr,e)}function w0(r,e){r.uniform3uiv(this.addr,e)}function R0(r,e){r.uniform4uiv(this.addr,e)}function C0(r,e,t){const n=this.cache,i=e.length,s=no(t,i);zt(n,s)||(r.uniform1iv(this.addr,s),Vt(n,s));let a;this.type===r.SAMPLER_2D_SHADOW?a=Yl:a=sf;for(let o=0;o!==i;++o)t.setTexture2D(e[o]||a,s[o])}function P0(r,e,t){const n=this.cache,i=e.length,s=no(t,i);zt(n,s)||(r.uniform1iv(this.addr,s),Vt(n,s));for(let a=0;a!==i;++a)t.setTexture3D(e[a]||of,s[a])}function D0(r,e,t){const n=this.cache,i=e.length,s=no(t,i);zt(n,s)||(r.uniform1iv(this.addr,s),Vt(n,s));for(let a=0;a!==i;++a)t.setTextureCube(e[a]||lf,s[a])}function L0(r,e,t){const n=this.cache,i=e.length,s=no(t,i);zt(n,s)||(r.uniform1iv(this.addr,s),Vt(n,s));for(let a=0;a!==i;++a)t.setTexture2DArray(e[a]||af,s[a])}function I0(r){switch(r){case 5126:return p0;case 35664:return m0;case 35665:return _0;case 35666:return g0;case 35674:return v0;case 35675:return x0;case 35676:return M0;case 5124:case 35670:return S0;case 35667:case 35671:return y0;case 35668:case 35672:return b0;case 35669:case 35673:return E0;case 5125:return T0;case 36294:return A0;case 36295:return w0;case 36296:return R0;case 35678:case 36198:case 36298:case 36306:case 35682:return C0;case 35679:case 36299:case 36307:return P0;case 35680:case 36300:case 36308:case 36293:return D0;case 36289:case 36303:case 36311:case 36292:return L0}}class U0{constructor(e,t,n){this.id=e,this.addr=n,this.cache=[],this.type=t.type,this.setValue=d0(t.type)}}class N0{constructor(e,t,n){this.id=e,this.addr=n,this.cache=[],this.type=t.type,this.size=t.size,this.setValue=I0(t.type)}}class F0{constructor(e){this.id=e,this.seq=[],this.map={}}setValue(e,t,n){const i=this.seq;for(let s=0,a=i.length;s!==a;++s){const o=i[s];o.setValue(e,t[o.id],n)}}}const Wo=/(\w+)(\])?(\[|\.)?/g;function Xu(r,e){r.seq.push(e),r.map[e.id]=e}function O0(r,e,t){const n=r.name,i=n.length;for(Wo.lastIndex=0;;){const s=Wo.exec(n),a=Wo.lastIndex;let o=s[1];const l=s[2]==="]",c=s[3];if(l&&(o=o|0),c===void 0||c==="["&&a+2===i){Xu(t,c===void 0?new U0(o,r,e):new N0(o,r,e));break}else{let f=t.map[o];f===void 0&&(f=new F0(o),Xu(t,f)),t=f}}}class Oa{constructor(e,t){this.seq=[],this.map={};const n=e.getProgramParameter(t,e.ACTIVE_UNIFORMS);for(let a=0;a<n;++a){const o=e.getActiveUniform(t,a),l=e.getUniformLocation(t,o.name);O0(o,l,this)}const i=[],s=[];for(const a of this.seq)a.type===e.SAMPLER_2D_SHADOW||a.type===e.SAMPLER_CUBE_SHADOW||a.type===e.SAMPLER_2D_ARRAY_SHADOW?i.push(a):s.push(a);i.length>0&&(this.seq=i.concat(s))}setValue(e,t,n,i){const s=this.map[t];s!==void 0&&s.setValue(e,n,i)}setOptional(e,t,n){const i=t[n];i!==void 0&&this.setValue(e,n,i)}static upload(e,t,n,i){for(let s=0,a=t.length;s!==a;++s){const o=t[s],l=n[o.id];l.needsUpdate!==!1&&o.setValue(e,l.value,i)}}static seqWithValue(e,t){const n=[];for(let i=0,s=e.length;i!==s;++i){const a=e[i];a.id in t&&n.push(a)}return n}}function qu(r,e,t){const n=r.createShader(e);return r.shaderSource(n,t),r.compileShader(n),n}const B0=37297;let k0=0;function z0(r,e){const t=r.split(`
`),n=[],i=Math.max(e-6,0),s=Math.min(e+6,t.length);for(let a=i;a<s;a++){const o=a+1;n.push(`${o===e?">":" "} ${o}: ${t[a]}`)}return n.join(`
`)}const Yu=new Ke;function V0(r){ht._getMatrix(Yu,ht.workingColorSpace,r);const e=`mat3( ${Yu.elements.map(t=>t.toFixed(4))} )`;switch(ht.getTransfer(r)){case Va:return[e,"LinearTransferOETF"];case _t:return[e,"sRGBTransferOETF"];default:return He("WebGLProgram: Unsupported color space: ",r),[e,"LinearTransferOETF"]}}function $u(r,e,t){const n=r.getShaderParameter(e,r.COMPILE_STATUS),s=(r.getShaderInfoLog(e)||"").trim();if(n&&s==="")return"";const a=/ERROR: 0:(\d+)/.exec(s);if(a){const o=parseInt(a[1]);return t.toUpperCase()+`

`+s+`

`+z0(r.getShaderSource(e),o)}else return s}function G0(r,e){const t=V0(e);return[`vec4 ${r}( vec4 value ) {`,`	return ${t[1]}( vec4( value.rgb * ${t[0]}, value.a ) );`,"}"].join(`
`)}const H0={[Ph]:"Linear",[Dh]:"Reinhard",[Lh]:"Cineon",[dc]:"ACESFilmic",[Uh]:"AgX",[Nh]:"Neutral",[Ih]:"Custom"};function W0(r,e){const t=H0[e];return t===void 0?(He("WebGLProgram: Unsupported toneMapping:",e),"vec3 "+r+"( vec3 color ) { return LinearToneMapping( color ); }"):"vec3 "+r+"( vec3 color ) { return "+t+"ToneMapping( color ); }"}const Ra=new V;function X0(){ht.getLuminanceCoefficients(Ra);const r=Ra.x.toFixed(4),e=Ra.y.toFixed(4),t=Ra.z.toFixed(4);return["float luminance( const in vec3 rgb ) {",`	const vec3 weights = vec3( ${r}, ${e}, ${t} );`,"	return dot( weights, rgb );","}"].join(`
`)}function q0(r){return[r.extensionClipCullDistance?"#extension GL_ANGLE_clip_cull_distance : require":"",r.extensionMultiDraw?"#extension GL_ANGLE_multi_draw : require":""].filter(ws).join(`
`)}function Y0(r){const e=[];for(const t in r){const n=r[t];n!==!1&&e.push("#define "+t+" "+n)}return e.join(`
`)}function $0(r,e){const t={},n=r.getProgramParameter(e,r.ACTIVE_ATTRIBUTES);for(let i=0;i<n;i++){const s=r.getActiveAttrib(e,i),a=s.name;let o=1;s.type===r.FLOAT_MAT2&&(o=2),s.type===r.FLOAT_MAT3&&(o=3),s.type===r.FLOAT_MAT4&&(o=4),t[a]={type:s.type,location:r.getAttribLocation(e,a),locationSize:o}}return t}function ws(r){return r!==""}function Ku(r,e){const t=e.numSpotLightShadows+e.numSpotLightMaps-e.numSpotLightShadowsWithMaps;return r.replace(/NUM_DIR_LIGHTS/g,e.numDirLights).replace(/NUM_SPOT_LIGHTS/g,e.numSpotLights).replace(/NUM_SPOT_LIGHT_MAPS/g,e.numSpotLightMaps).replace(/NUM_SPOT_LIGHT_COORDS/g,t).replace(/NUM_RECT_AREA_LIGHTS/g,e.numRectAreaLights).replace(/NUM_POINT_LIGHTS/g,e.numPointLights).replace(/NUM_HEMI_LIGHTS/g,e.numHemiLights).replace(/NUM_DIR_LIGHT_SHADOWS/g,e.numDirLightShadows).replace(/NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS/g,e.numSpotLightShadowsWithMaps).replace(/NUM_SPOT_LIGHT_SHADOWS/g,e.numSpotLightShadows).replace(/NUM_POINT_LIGHT_SHADOWS/g,e.numPointLightShadows)}function Zu(r,e){return r.replace(/NUM_CLIPPING_PLANES/g,e.numClippingPlanes).replace(/UNION_CLIPPING_PLANES/g,e.numClippingPlanes-e.numClipIntersection)}const K0=/^[ \t]*#include +<([\w\d./]+)>/gm;function $l(r){return r.replace(K0,j0)}const Z0=new Map;function j0(r,e){let t=je[e];if(t===void 0){const n=Z0.get(e);if(n!==void 0)t=je[n],He('WebGLRenderer: Shader chunk "%s" has been deprecated. Use "%s" instead.',e,n);else throw new Error("Can not resolve #include <"+e+">")}return $l(t)}const J0=/#pragma unroll_loop_start\s+for\s*\(\s*int\s+i\s*=\s*(\d+)\s*;\s*i\s*<\s*(\d+)\s*;\s*i\s*\+\+\s*\)\s*{([\s\S]+?)}\s+#pragma unroll_loop_end/g;function ju(r){return r.replace(J0,Q0)}function Q0(r,e,t,n){let i="";for(let s=parseInt(e);s<parseInt(t);s++)i+=n.replace(/\[\s*i\s*\]/g,"[ "+s+" ]").replace(/UNROLLED_LOOP_INDEX/g,s);return i}function Ju(r){let e=`precision ${r.precision} float;
	precision ${r.precision} int;
	precision ${r.precision} sampler2D;
	precision ${r.precision} samplerCube;
	precision ${r.precision} sampler3D;
	precision ${r.precision} sampler2DArray;
	precision ${r.precision} sampler2DShadow;
	precision ${r.precision} samplerCubeShadow;
	precision ${r.precision} sampler2DArrayShadow;
	precision ${r.precision} isampler2D;
	precision ${r.precision} isampler3D;
	precision ${r.precision} isamplerCube;
	precision ${r.precision} isampler2DArray;
	precision ${r.precision} usampler2D;
	precision ${r.precision} usampler3D;
	precision ${r.precision} usamplerCube;
	precision ${r.precision} usampler2DArray;
	`;return r.precision==="highp"?e+=`
#define HIGH_PRECISION`:r.precision==="mediump"?e+=`
#define MEDIUM_PRECISION`:r.precision==="lowp"&&(e+=`
#define LOW_PRECISION`),e}const ev={[Da]:"SHADOWMAP_TYPE_PCF",[As]:"SHADOWMAP_TYPE_VSM"};function tv(r){return ev[r.shadowMapType]||"SHADOWMAP_TYPE_BASIC"}const nv={[yr]:"ENVMAP_TYPE_CUBE",[ts]:"ENVMAP_TYPE_CUBE",[eo]:"ENVMAP_TYPE_CUBE_UV"};function iv(r){return r.envMap===!1?"ENVMAP_TYPE_CUBE":nv[r.envMapMode]||"ENVMAP_TYPE_CUBE"}const rv={[ts]:"ENVMAP_MODE_REFRACTION"};function sv(r){return r.envMap===!1?"ENVMAP_MODE_REFLECTION":rv[r.envMapMode]||"ENVMAP_MODE_REFLECTION"}const av={[Ch]:"ENVMAP_BLENDING_MULTIPLY",[Hd]:"ENVMAP_BLENDING_MIX",[Wd]:"ENVMAP_BLENDING_ADD"};function ov(r){return r.envMap===!1?"ENVMAP_BLENDING_NONE":av[r.combine]||"ENVMAP_BLENDING_NONE"}function lv(r){const e=r.envMapCubeUVHeight;if(e===null)return null;const t=Math.log2(e)-2,n=1/e;return{texelWidth:1/(3*Math.max(Math.pow(2,t),7*16)),texelHeight:n,maxMip:t}}function cv(r,e,t,n){const i=r.getContext(),s=t.defines;let a=t.vertexShader,o=t.fragmentShader;const l=tv(t),c=iv(t),u=sv(t),f=ov(t),h=lv(t),d=q0(t),_=Y0(s),g=i.createProgram();let p,m,M=t.glslVersion?"#version "+t.glslVersion+`
`:"";t.isRawShaderMaterial?(p=["#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_].filter(ws).join(`
`),p.length>0&&(p+=`
`),m=["#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_].filter(ws).join(`
`),m.length>0&&(m+=`
`)):(p=[Ju(t),"#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_,t.extensionClipCullDistance?"#define USE_CLIP_DISTANCE":"",t.batching?"#define USE_BATCHING":"",t.batchingColor?"#define USE_BATCHING_COLOR":"",t.instancing?"#define USE_INSTANCING":"",t.instancingColor?"#define USE_INSTANCING_COLOR":"",t.instancingMorph?"#define USE_INSTANCING_MORPH":"",t.useFog&&t.fog?"#define USE_FOG":"",t.useFog&&t.fogExp2?"#define FOG_EXP2":"",t.map?"#define USE_MAP":"",t.envMap?"#define USE_ENVMAP":"",t.envMap?"#define "+u:"",t.lightMap?"#define USE_LIGHTMAP":"",t.aoMap?"#define USE_AOMAP":"",t.bumpMap?"#define USE_BUMPMAP":"",t.normalMap?"#define USE_NORMALMAP":"",t.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",t.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",t.displacementMap?"#define USE_DISPLACEMENTMAP":"",t.emissiveMap?"#define USE_EMISSIVEMAP":"",t.anisotropy?"#define USE_ANISOTROPY":"",t.anisotropyMap?"#define USE_ANISOTROPYMAP":"",t.clearcoatMap?"#define USE_CLEARCOATMAP":"",t.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",t.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",t.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",t.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",t.specularMap?"#define USE_SPECULARMAP":"",t.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",t.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",t.roughnessMap?"#define USE_ROUGHNESSMAP":"",t.metalnessMap?"#define USE_METALNESSMAP":"",t.alphaMap?"#define USE_ALPHAMAP":"",t.alphaHash?"#define USE_ALPHAHASH":"",t.transmission?"#define USE_TRANSMISSION":"",t.transmissionMap?"#define USE_TRANSMISSIONMAP":"",t.thicknessMap?"#define USE_THICKNESSMAP":"",t.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",t.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",t.mapUv?"#define MAP_UV "+t.mapUv:"",t.alphaMapUv?"#define ALPHAMAP_UV "+t.alphaMapUv:"",t.lightMapUv?"#define LIGHTMAP_UV "+t.lightMapUv:"",t.aoMapUv?"#define AOMAP_UV "+t.aoMapUv:"",t.emissiveMapUv?"#define EMISSIVEMAP_UV "+t.emissiveMapUv:"",t.bumpMapUv?"#define BUMPMAP_UV "+t.bumpMapUv:"",t.normalMapUv?"#define NORMALMAP_UV "+t.normalMapUv:"",t.displacementMapUv?"#define DISPLACEMENTMAP_UV "+t.displacementMapUv:"",t.metalnessMapUv?"#define METALNESSMAP_UV "+t.metalnessMapUv:"",t.roughnessMapUv?"#define ROUGHNESSMAP_UV "+t.roughnessMapUv:"",t.anisotropyMapUv?"#define ANISOTROPYMAP_UV "+t.anisotropyMapUv:"",t.clearcoatMapUv?"#define CLEARCOATMAP_UV "+t.clearcoatMapUv:"",t.clearcoatNormalMapUv?"#define CLEARCOAT_NORMALMAP_UV "+t.clearcoatNormalMapUv:"",t.clearcoatRoughnessMapUv?"#define CLEARCOAT_ROUGHNESSMAP_UV "+t.clearcoatRoughnessMapUv:"",t.iridescenceMapUv?"#define IRIDESCENCEMAP_UV "+t.iridescenceMapUv:"",t.iridescenceThicknessMapUv?"#define IRIDESCENCE_THICKNESSMAP_UV "+t.iridescenceThicknessMapUv:"",t.sheenColorMapUv?"#define SHEEN_COLORMAP_UV "+t.sheenColorMapUv:"",t.sheenRoughnessMapUv?"#define SHEEN_ROUGHNESSMAP_UV "+t.sheenRoughnessMapUv:"",t.specularMapUv?"#define SPECULARMAP_UV "+t.specularMapUv:"",t.specularColorMapUv?"#define SPECULAR_COLORMAP_UV "+t.specularColorMapUv:"",t.specularIntensityMapUv?"#define SPECULAR_INTENSITYMAP_UV "+t.specularIntensityMapUv:"",t.transmissionMapUv?"#define TRANSMISSIONMAP_UV "+t.transmissionMapUv:"",t.thicknessMapUv?"#define THICKNESSMAP_UV "+t.thicknessMapUv:"",t.vertexTangents&&t.flatShading===!1?"#define USE_TANGENT":"",t.vertexColors?"#define USE_COLOR":"",t.vertexAlphas?"#define USE_COLOR_ALPHA":"",t.vertexUv1s?"#define USE_UV1":"",t.vertexUv2s?"#define USE_UV2":"",t.vertexUv3s?"#define USE_UV3":"",t.pointsUvs?"#define USE_POINTS_UV":"",t.flatShading?"#define FLAT_SHADED":"",t.skinning?"#define USE_SKINNING":"",t.morphTargets?"#define USE_MORPHTARGETS":"",t.morphNormals&&t.flatShading===!1?"#define USE_MORPHNORMALS":"",t.morphColors?"#define USE_MORPHCOLORS":"",t.morphTargetsCount>0?"#define MORPHTARGETS_TEXTURE_STRIDE "+t.morphTextureStride:"",t.morphTargetsCount>0?"#define MORPHTARGETS_COUNT "+t.morphTargetsCount:"",t.doubleSided?"#define DOUBLE_SIDED":"",t.flipSided?"#define FLIP_SIDED":"",t.shadowMapEnabled?"#define USE_SHADOWMAP":"",t.shadowMapEnabled?"#define "+l:"",t.sizeAttenuation?"#define USE_SIZEATTENUATION":"",t.numLightProbes>0?"#define USE_LIGHT_PROBES":"",t.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",t.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 modelMatrix;","uniform mat4 modelViewMatrix;","uniform mat4 projectionMatrix;","uniform mat4 viewMatrix;","uniform mat3 normalMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;","#ifdef USE_INSTANCING","	attribute mat4 instanceMatrix;","#endif","#ifdef USE_INSTANCING_COLOR","	attribute vec3 instanceColor;","#endif","#ifdef USE_INSTANCING_MORPH","	uniform sampler2D morphTexture;","#endif","attribute vec3 position;","attribute vec3 normal;","attribute vec2 uv;","#ifdef USE_UV1","	attribute vec2 uv1;","#endif","#ifdef USE_UV2","	attribute vec2 uv2;","#endif","#ifdef USE_UV3","	attribute vec2 uv3;","#endif","#ifdef USE_TANGENT","	attribute vec4 tangent;","#endif","#if defined( USE_COLOR_ALPHA )","	attribute vec4 color;","#elif defined( USE_COLOR )","	attribute vec3 color;","#endif","#ifdef USE_SKINNING","	attribute vec4 skinIndex;","	attribute vec4 skinWeight;","#endif",`
`].filter(ws).join(`
`),m=[Ju(t),"#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_,t.useFog&&t.fog?"#define USE_FOG":"",t.useFog&&t.fogExp2?"#define FOG_EXP2":"",t.alphaToCoverage?"#define ALPHA_TO_COVERAGE":"",t.map?"#define USE_MAP":"",t.matcap?"#define USE_MATCAP":"",t.envMap?"#define USE_ENVMAP":"",t.envMap?"#define "+c:"",t.envMap?"#define "+u:"",t.envMap?"#define "+f:"",h?"#define CUBEUV_TEXEL_WIDTH "+h.texelWidth:"",h?"#define CUBEUV_TEXEL_HEIGHT "+h.texelHeight:"",h?"#define CUBEUV_MAX_MIP "+h.maxMip+".0":"",t.lightMap?"#define USE_LIGHTMAP":"",t.aoMap?"#define USE_AOMAP":"",t.bumpMap?"#define USE_BUMPMAP":"",t.normalMap?"#define USE_NORMALMAP":"",t.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",t.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",t.emissiveMap?"#define USE_EMISSIVEMAP":"",t.anisotropy?"#define USE_ANISOTROPY":"",t.anisotropyMap?"#define USE_ANISOTROPYMAP":"",t.clearcoat?"#define USE_CLEARCOAT":"",t.clearcoatMap?"#define USE_CLEARCOATMAP":"",t.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",t.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",t.dispersion?"#define USE_DISPERSION":"",t.iridescence?"#define USE_IRIDESCENCE":"",t.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",t.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",t.specularMap?"#define USE_SPECULARMAP":"",t.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",t.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",t.roughnessMap?"#define USE_ROUGHNESSMAP":"",t.metalnessMap?"#define USE_METALNESSMAP":"",t.alphaMap?"#define USE_ALPHAMAP":"",t.alphaTest?"#define USE_ALPHATEST":"",t.alphaHash?"#define USE_ALPHAHASH":"",t.sheen?"#define USE_SHEEN":"",t.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",t.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",t.transmission?"#define USE_TRANSMISSION":"",t.transmissionMap?"#define USE_TRANSMISSIONMAP":"",t.thicknessMap?"#define USE_THICKNESSMAP":"",t.vertexTangents&&t.flatShading===!1?"#define USE_TANGENT":"",t.vertexColors||t.instancingColor?"#define USE_COLOR":"",t.vertexAlphas||t.batchingColor?"#define USE_COLOR_ALPHA":"",t.vertexUv1s?"#define USE_UV1":"",t.vertexUv2s?"#define USE_UV2":"",t.vertexUv3s?"#define USE_UV3":"",t.pointsUvs?"#define USE_POINTS_UV":"",t.gradientMap?"#define USE_GRADIENTMAP":"",t.flatShading?"#define FLAT_SHADED":"",t.doubleSided?"#define DOUBLE_SIDED":"",t.flipSided?"#define FLIP_SIDED":"",t.shadowMapEnabled?"#define USE_SHADOWMAP":"",t.shadowMapEnabled?"#define "+l:"",t.premultipliedAlpha?"#define PREMULTIPLIED_ALPHA":"",t.numLightProbes>0?"#define USE_LIGHT_PROBES":"",t.decodeVideoTexture?"#define DECODE_VIDEO_TEXTURE":"",t.decodeVideoTextureEmissive?"#define DECODE_VIDEO_TEXTURE_EMISSIVE":"",t.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",t.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 viewMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;",t.toneMapping!==si?"#define TONE_MAPPING":"",t.toneMapping!==si?je.tonemapping_pars_fragment:"",t.toneMapping!==si?W0("toneMapping",t.toneMapping):"",t.dithering?"#define DITHERING":"",t.opaque?"#define OPAQUE":"",je.colorspace_pars_fragment,G0("linearToOutputTexel",t.outputColorSpace),X0(),t.useDepthPacking?"#define DEPTH_PACKING "+t.depthPacking:"",`
`].filter(ws).join(`
`)),a=$l(a),a=Ku(a,t),a=Zu(a,t),o=$l(o),o=Ku(o,t),o=Zu(o,t),a=ju(a),o=ju(o),t.isRawShaderMaterial!==!0&&(M=`#version 300 es
`,p=[d,"#define attribute in","#define varying out","#define texture2D texture"].join(`
`)+`
`+p,m=["#define varying in",t.glslVersion===tu?"":"layout(location = 0) out highp vec4 pc_fragColor;",t.glslVersion===tu?"":"#define gl_FragColor pc_fragColor","#define gl_FragDepthEXT gl_FragDepth","#define texture2D texture","#define textureCube texture","#define texture2DProj textureProj","#define texture2DLodEXT textureLod","#define texture2DProjLodEXT textureProjLod","#define textureCubeLodEXT textureLod","#define texture2DGradEXT textureGrad","#define texture2DProjGradEXT textureProjGrad","#define textureCubeGradEXT textureGrad"].join(`
`)+`
`+m);const S=M+p+a,y=M+m+o,A=qu(i,i.VERTEX_SHADER,S),w=qu(i,i.FRAGMENT_SHADER,y);i.attachShader(g,A),i.attachShader(g,w),t.index0AttributeName!==void 0?i.bindAttribLocation(g,0,t.index0AttributeName):t.morphTargets===!0&&i.bindAttribLocation(g,0,"position"),i.linkProgram(g);function C(L){if(r.debug.checkShaderErrors){const G=i.getProgramInfoLog(g)||"",z=i.getShaderInfoLog(A)||"",q=i.getShaderInfoLog(w)||"",H=G.trim(),N=z.trim(),F=q.trim();let ne=!0,ie=!0;if(i.getProgramParameter(g,i.LINK_STATUS)===!1)if(ne=!1,typeof r.debug.onShaderError=="function")r.debug.onShaderError(i,g,A,w);else{const me=$u(i,A,"vertex"),Te=$u(i,w,"fragment");ut("THREE.WebGLProgram: Shader Error "+i.getError()+" - VALIDATE_STATUS "+i.getProgramParameter(g,i.VALIDATE_STATUS)+`

Material Name: `+L.name+`
Material Type: `+L.type+`

Program Info Log: `+H+`
`+me+`
`+Te)}else H!==""?He("WebGLProgram: Program Info Log:",H):(N===""||F==="")&&(ie=!1);ie&&(L.diagnostics={runnable:ne,programLog:H,vertexShader:{log:N,prefix:p},fragmentShader:{log:F,prefix:m}})}i.deleteShader(A),i.deleteShader(w),x=new Oa(i,g),b=$0(i,g)}let x;this.getUniforms=function(){return x===void 0&&C(this),x};let b;this.getAttributes=function(){return b===void 0&&C(this),b};let W=t.rendererExtensionParallelShaderCompile===!1;return this.isReady=function(){return W===!1&&(W=i.getProgramParameter(g,B0)),W},this.destroy=function(){n.releaseStatesOfProgram(this),i.deleteProgram(g),this.program=void 0},this.type=t.shaderType,this.name=t.shaderName,this.id=k0++,this.cacheKey=e,this.usedTimes=1,this.program=g,this.vertexShader=A,this.fragmentShader=w,this}let uv=0;class hv{constructor(){this.shaderCache=new Map,this.materialCache=new Map}update(e){const t=e.vertexShader,n=e.fragmentShader,i=this._getShaderStage(t),s=this._getShaderStage(n),a=this._getShaderCacheForMaterial(e);return a.has(i)===!1&&(a.add(i),i.usedTimes++),a.has(s)===!1&&(a.add(s),s.usedTimes++),this}remove(e){const t=this.materialCache.get(e);for(const n of t)n.usedTimes--,n.usedTimes===0&&this.shaderCache.delete(n.code);return this.materialCache.delete(e),this}getVertexShaderID(e){return this._getShaderStage(e.vertexShader).id}getFragmentShaderID(e){return this._getShaderStage(e.fragmentShader).id}dispose(){this.shaderCache.clear(),this.materialCache.clear()}_getShaderCacheForMaterial(e){const t=this.materialCache;let n=t.get(e);return n===void 0&&(n=new Set,t.set(e,n)),n}_getShaderStage(e){const t=this.shaderCache;let n=t.get(e);return n===void 0&&(n=new fv(e),t.set(e,n)),n}}class fv{constructor(e){this.id=uv++,this.code=e,this.usedTimes=0}}function dv(r,e,t,n,i,s){const a=new bc,o=new hv,l=new Set,c=[],u=new Map,f=n.logarithmicDepthBuffer;let h=n.precision;const d={MeshDepthMaterial:"depth",MeshDistanceMaterial:"distance",MeshNormalMaterial:"normal",MeshBasicMaterial:"basic",MeshLambertMaterial:"lambert",MeshPhongMaterial:"phong",MeshToonMaterial:"toon",MeshStandardMaterial:"physical",MeshPhysicalMaterial:"physical",MeshMatcapMaterial:"matcap",LineBasicMaterial:"basic",LineDashedMaterial:"dashed",PointsMaterial:"points",ShadowMaterial:"shadow",SpriteMaterial:"sprite"};function _(x){return l.add(x),x===0?"uv":`uv${x}`}function g(x,b,W,L,G){const z=L.fog,q=G.geometry,H=x.isMeshStandardMaterial||x.isMeshLambertMaterial||x.isMeshPhongMaterial?L.environment:null,N=x.isMeshStandardMaterial||x.isMeshLambertMaterial&&!x.envMap||x.isMeshPhongMaterial&&!x.envMap,F=e.get(x.envMap||H,N),ne=F&&F.mapping===eo?F.image.height:null,ie=d[x.type];x.precision!==null&&(h=n.getMaxPrecision(x.precision),h!==x.precision&&He("WebGLProgram.getParameters:",x.precision,"not supported, using",h,"instead."));const me=q.morphAttributes.position||q.morphAttributes.normal||q.morphAttributes.color,Te=me!==void 0?me.length:0;let le=0;q.morphAttributes.position!==void 0&&(le=1),q.morphAttributes.normal!==void 0&&(le=2),q.morphAttributes.color!==void 0&&(le=3);let Oe,Je,$e,ee;if(ie){const tt=ei[ie];Oe=tt.vertexShader,Je=tt.fragmentShader}else Oe=x.vertexShader,Je=x.fragmentShader,o.update(x),$e=o.getVertexShaderID(x),ee=o.getFragmentShaderID(x);const de=r.getRenderTarget(),pe=r.state.buffers.depth.getReversed(),Ve=G.isInstancedMesh===!0,Ne=G.isBatchedMesh===!0,Fe=!!x.map,Tt=!!x.matcap,Qe=!!F,ot=!!x.aoMap,pt=!!x.lightMap,Ye=!!x.bumpMap,vt=!!x.normalMap,I=!!x.displacementMap,yt=!!x.emissiveMap,rt=!!x.metalnessMap,Q=!!x.roughnessMap,Z=x.anisotropy>0,T=x.clearcoat>0,v=x.dispersion>0,D=x.iridescence>0,B=x.sheen>0,j=x.transmission>0,$=Z&&!!x.anisotropyMap,ce=T&&!!x.clearcoatMap,se=T&&!!x.clearcoatNormalMap,Re=T&&!!x.clearcoatRoughnessMap,Ue=D&&!!x.iridescenceMap,re=D&&!!x.iridescenceThicknessMap,he=B&&!!x.sheenColorMap,Ce=B&&!!x.sheenRoughnessMap,be=!!x.specularMap,Ee=!!x.specularColorMap,Be=!!x.specularIntensityMap,U=j&&!!x.transmissionMap,fe=j&&!!x.thicknessMap,ue=!!x.gradientMap,_e=!!x.alphaMap,ae=x.alphaTest>0,J=!!x.alphaHash,we=!!x.extensions;let Le=si;x.toneMapped&&(de===null||de.isXRRenderTarget===!0)&&(Le=r.toneMapping);const ft={shaderID:ie,shaderType:x.type,shaderName:x.name,vertexShader:Oe,fragmentShader:Je,defines:x.defines,customVertexShaderID:$e,customFragmentShaderID:ee,isRawShaderMaterial:x.isRawShaderMaterial===!0,glslVersion:x.glslVersion,precision:h,batching:Ne,batchingColor:Ne&&G._colorsTexture!==null,instancing:Ve,instancingColor:Ve&&G.instanceColor!==null,instancingMorph:Ve&&G.morphTexture!==null,outputColorSpace:de===null?r.outputColorSpace:de.isXRRenderTarget===!0?de.texture.colorSpace:is,alphaToCoverage:!!x.alphaToCoverage,map:Fe,matcap:Tt,envMap:Qe,envMapMode:Qe&&F.mapping,envMapCubeUVHeight:ne,aoMap:ot,lightMap:pt,bumpMap:Ye,normalMap:vt,displacementMap:I,emissiveMap:yt,normalMapObjectSpace:vt&&x.normalMapType===Yd,normalMapTangentSpace:vt&&x.normalMapType===Wh,metalnessMap:rt,roughnessMap:Q,anisotropy:Z,anisotropyMap:$,clearcoat:T,clearcoatMap:ce,clearcoatNormalMap:se,clearcoatRoughnessMap:Re,dispersion:v,iridescence:D,iridescenceMap:Ue,iridescenceThicknessMap:re,sheen:B,sheenColorMap:he,sheenRoughnessMap:Ce,specularMap:be,specularColorMap:Ee,specularIntensityMap:Be,transmission:j,transmissionMap:U,thicknessMap:fe,gradientMap:ue,opaque:x.transparent===!1&&x.blending===Kr&&x.alphaToCoverage===!1,alphaMap:_e,alphaTest:ae,alphaHash:J,combine:x.combine,mapUv:Fe&&_(x.map.channel),aoMapUv:ot&&_(x.aoMap.channel),lightMapUv:pt&&_(x.lightMap.channel),bumpMapUv:Ye&&_(x.bumpMap.channel),normalMapUv:vt&&_(x.normalMap.channel),displacementMapUv:I&&_(x.displacementMap.channel),emissiveMapUv:yt&&_(x.emissiveMap.channel),metalnessMapUv:rt&&_(x.metalnessMap.channel),roughnessMapUv:Q&&_(x.roughnessMap.channel),anisotropyMapUv:$&&_(x.anisotropyMap.channel),clearcoatMapUv:ce&&_(x.clearcoatMap.channel),clearcoatNormalMapUv:se&&_(x.clearcoatNormalMap.channel),clearcoatRoughnessMapUv:Re&&_(x.clearcoatRoughnessMap.channel),iridescenceMapUv:Ue&&_(x.iridescenceMap.channel),iridescenceThicknessMapUv:re&&_(x.iridescenceThicknessMap.channel),sheenColorMapUv:he&&_(x.sheenColorMap.channel),sheenRoughnessMapUv:Ce&&_(x.sheenRoughnessMap.channel),specularMapUv:be&&_(x.specularMap.channel),specularColorMapUv:Ee&&_(x.specularColorMap.channel),specularIntensityMapUv:Be&&_(x.specularIntensityMap.channel),transmissionMapUv:U&&_(x.transmissionMap.channel),thicknessMapUv:fe&&_(x.thicknessMap.channel),alphaMapUv:_e&&_(x.alphaMap.channel),vertexTangents:!!q.attributes.tangent&&(vt||Z),vertexColors:x.vertexColors,vertexAlphas:x.vertexColors===!0&&!!q.attributes.color&&q.attributes.color.itemSize===4,pointsUvs:G.isPoints===!0&&!!q.attributes.uv&&(Fe||_e),fog:!!z,useFog:x.fog===!0,fogExp2:!!z&&z.isFogExp2,flatShading:x.wireframe===!1&&(x.flatShading===!0||q.attributes.normal===void 0&&vt===!1&&(x.isMeshLambertMaterial||x.isMeshPhongMaterial||x.isMeshStandardMaterial||x.isMeshPhysicalMaterial)),sizeAttenuation:x.sizeAttenuation===!0,logarithmicDepthBuffer:f,reversedDepthBuffer:pe,skinning:G.isSkinnedMesh===!0,morphTargets:q.morphAttributes.position!==void 0,morphNormals:q.morphAttributes.normal!==void 0,morphColors:q.morphAttributes.color!==void 0,morphTargetsCount:Te,morphTextureStride:le,numDirLights:b.directional.length,numPointLights:b.point.length,numSpotLights:b.spot.length,numSpotLightMaps:b.spotLightMap.length,numRectAreaLights:b.rectArea.length,numHemiLights:b.hemi.length,numDirLightShadows:b.directionalShadowMap.length,numPointLightShadows:b.pointShadowMap.length,numSpotLightShadows:b.spotShadowMap.length,numSpotLightShadowsWithMaps:b.numSpotLightShadowsWithMaps,numLightProbes:b.numLightProbes,numClippingPlanes:s.numPlanes,numClipIntersection:s.numIntersection,dithering:x.dithering,shadowMapEnabled:r.shadowMap.enabled&&W.length>0,shadowMapType:r.shadowMap.type,toneMapping:Le,decodeVideoTexture:Fe&&x.map.isVideoTexture===!0&&ht.getTransfer(x.map.colorSpace)===_t,decodeVideoTextureEmissive:yt&&x.emissiveMap.isVideoTexture===!0&&ht.getTransfer(x.emissiveMap.colorSpace)===_t,premultipliedAlpha:x.premultipliedAlpha,doubleSided:x.side===Xn,flipSided:x.side===mn,useDepthPacking:x.depthPacking>=0,depthPacking:x.depthPacking||0,index0AttributeName:x.index0AttributeName,extensionClipCullDistance:we&&x.extensions.clipCullDistance===!0&&t.has("WEBGL_clip_cull_distance"),extensionMultiDraw:(we&&x.extensions.multiDraw===!0||Ne)&&t.has("WEBGL_multi_draw"),rendererExtensionParallelShaderCompile:t.has("KHR_parallel_shader_compile"),customProgramCacheKey:x.customProgramCacheKey()};return ft.vertexUv1s=l.has(1),ft.vertexUv2s=l.has(2),ft.vertexUv3s=l.has(3),l.clear(),ft}function p(x){const b=[];if(x.shaderID?b.push(x.shaderID):(b.push(x.customVertexShaderID),b.push(x.customFragmentShaderID)),x.defines!==void 0)for(const W in x.defines)b.push(W),b.push(x.defines[W]);return x.isRawShaderMaterial===!1&&(m(b,x),M(b,x),b.push(r.outputColorSpace)),b.push(x.customProgramCacheKey),b.join()}function m(x,b){x.push(b.precision),x.push(b.outputColorSpace),x.push(b.envMapMode),x.push(b.envMapCubeUVHeight),x.push(b.mapUv),x.push(b.alphaMapUv),x.push(b.lightMapUv),x.push(b.aoMapUv),x.push(b.bumpMapUv),x.push(b.normalMapUv),x.push(b.displacementMapUv),x.push(b.emissiveMapUv),x.push(b.metalnessMapUv),x.push(b.roughnessMapUv),x.push(b.anisotropyMapUv),x.push(b.clearcoatMapUv),x.push(b.clearcoatNormalMapUv),x.push(b.clearcoatRoughnessMapUv),x.push(b.iridescenceMapUv),x.push(b.iridescenceThicknessMapUv),x.push(b.sheenColorMapUv),x.push(b.sheenRoughnessMapUv),x.push(b.specularMapUv),x.push(b.specularColorMapUv),x.push(b.specularIntensityMapUv),x.push(b.transmissionMapUv),x.push(b.thicknessMapUv),x.push(b.combine),x.push(b.fogExp2),x.push(b.sizeAttenuation),x.push(b.morphTargetsCount),x.push(b.morphAttributeCount),x.push(b.numDirLights),x.push(b.numPointLights),x.push(b.numSpotLights),x.push(b.numSpotLightMaps),x.push(b.numHemiLights),x.push(b.numRectAreaLights),x.push(b.numDirLightShadows),x.push(b.numPointLightShadows),x.push(b.numSpotLightShadows),x.push(b.numSpotLightShadowsWithMaps),x.push(b.numLightProbes),x.push(b.shadowMapType),x.push(b.toneMapping),x.push(b.numClippingPlanes),x.push(b.numClipIntersection),x.push(b.depthPacking)}function M(x,b){a.disableAll(),b.instancing&&a.enable(0),b.instancingColor&&a.enable(1),b.instancingMorph&&a.enable(2),b.matcap&&a.enable(3),b.envMap&&a.enable(4),b.normalMapObjectSpace&&a.enable(5),b.normalMapTangentSpace&&a.enable(6),b.clearcoat&&a.enable(7),b.iridescence&&a.enable(8),b.alphaTest&&a.enable(9),b.vertexColors&&a.enable(10),b.vertexAlphas&&a.enable(11),b.vertexUv1s&&a.enable(12),b.vertexUv2s&&a.enable(13),b.vertexUv3s&&a.enable(14),b.vertexTangents&&a.enable(15),b.anisotropy&&a.enable(16),b.alphaHash&&a.enable(17),b.batching&&a.enable(18),b.dispersion&&a.enable(19),b.batchingColor&&a.enable(20),b.gradientMap&&a.enable(21),x.push(a.mask),a.disableAll(),b.fog&&a.enable(0),b.useFog&&a.enable(1),b.flatShading&&a.enable(2),b.logarithmicDepthBuffer&&a.enable(3),b.reversedDepthBuffer&&a.enable(4),b.skinning&&a.enable(5),b.morphTargets&&a.enable(6),b.morphNormals&&a.enable(7),b.morphColors&&a.enable(8),b.premultipliedAlpha&&a.enable(9),b.shadowMapEnabled&&a.enable(10),b.doubleSided&&a.enable(11),b.flipSided&&a.enable(12),b.useDepthPacking&&a.enable(13),b.dithering&&a.enable(14),b.transmission&&a.enable(15),b.sheen&&a.enable(16),b.opaque&&a.enable(17),b.pointsUvs&&a.enable(18),b.decodeVideoTexture&&a.enable(19),b.decodeVideoTextureEmissive&&a.enable(20),b.alphaToCoverage&&a.enable(21),x.push(a.mask)}function S(x){const b=d[x.type];let W;if(b){const L=ei[b];W=Lp.clone(L.uniforms)}else W=x.uniforms;return W}function y(x,b){let W=u.get(b);return W!==void 0?++W.usedTimes:(W=new cv(r,b,x,i),c.push(W),u.set(b,W)),W}function A(x){if(--x.usedTimes===0){const b=c.indexOf(x);c[b]=c[c.length-1],c.pop(),u.delete(x.cacheKey),x.destroy()}}function w(x){o.remove(x)}function C(){o.dispose()}return{getParameters:g,getProgramCacheKey:p,getUniforms:S,acquireProgram:y,releaseProgram:A,releaseShaderCache:w,programs:c,dispose:C}}function pv(){let r=new WeakMap;function e(a){return r.has(a)}function t(a){let o=r.get(a);return o===void 0&&(o={},r.set(a,o)),o}function n(a){r.delete(a)}function i(a,o,l){r.get(a)[o]=l}function s(){r=new WeakMap}return{has:e,get:t,remove:n,update:i,dispose:s}}function mv(r,e){return r.groupOrder!==e.groupOrder?r.groupOrder-e.groupOrder:r.renderOrder!==e.renderOrder?r.renderOrder-e.renderOrder:r.material.id!==e.material.id?r.material.id-e.material.id:r.materialVariant!==e.materialVariant?r.materialVariant-e.materialVariant:r.z!==e.z?r.z-e.z:r.id-e.id}function Qu(r,e){return r.groupOrder!==e.groupOrder?r.groupOrder-e.groupOrder:r.renderOrder!==e.renderOrder?r.renderOrder-e.renderOrder:r.z!==e.z?e.z-r.z:r.id-e.id}function eh(){const r=[];let e=0;const t=[],n=[],i=[];function s(){e=0,t.length=0,n.length=0,i.length=0}function a(h){let d=0;return h.isInstancedMesh&&(d+=2),h.isSkinnedMesh&&(d+=1),d}function o(h,d,_,g,p,m){let M=r[e];return M===void 0?(M={id:h.id,object:h,geometry:d,material:_,materialVariant:a(h),groupOrder:g,renderOrder:h.renderOrder,z:p,group:m},r[e]=M):(M.id=h.id,M.object=h,M.geometry=d,M.material=_,M.materialVariant=a(h),M.groupOrder=g,M.renderOrder=h.renderOrder,M.z=p,M.group=m),e++,M}function l(h,d,_,g,p,m){const M=o(h,d,_,g,p,m);_.transmission>0?n.push(M):_.transparent===!0?i.push(M):t.push(M)}function c(h,d,_,g,p,m){const M=o(h,d,_,g,p,m);_.transmission>0?n.unshift(M):_.transparent===!0?i.unshift(M):t.unshift(M)}function u(h,d){t.length>1&&t.sort(h||mv),n.length>1&&n.sort(d||Qu),i.length>1&&i.sort(d||Qu)}function f(){for(let h=e,d=r.length;h<d;h++){const _=r[h];if(_.id===null)break;_.id=null,_.object=null,_.geometry=null,_.material=null,_.group=null}}return{opaque:t,transmissive:n,transparent:i,init:s,push:l,unshift:c,finish:f,sort:u}}function _v(){let r=new WeakMap;function e(n,i){const s=r.get(n);let a;return s===void 0?(a=new eh,r.set(n,[a])):i>=s.length?(a=new eh,s.push(a)):a=s[i],a}function t(){r=new WeakMap}return{get:e,dispose:t}}function gv(){const r={};return{get:function(e){if(r[e.id]!==void 0)return r[e.id];let t;switch(e.type){case"DirectionalLight":t={direction:new V,color:new it};break;case"SpotLight":t={position:new V,direction:new V,color:new it,distance:0,coneCos:0,penumbraCos:0,decay:0};break;case"PointLight":t={position:new V,color:new it,distance:0,decay:0};break;case"HemisphereLight":t={direction:new V,skyColor:new it,groundColor:new it};break;case"RectAreaLight":t={color:new it,position:new V,halfWidth:new V,halfHeight:new V};break}return r[e.id]=t,t}}}function vv(){const r={};return{get:function(e){if(r[e.id]!==void 0)return r[e.id];let t;switch(e.type){case"DirectionalLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new qe};break;case"SpotLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new qe};break;case"PointLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new qe,shadowCameraNear:1,shadowCameraFar:1e3};break}return r[e.id]=t,t}}}let xv=0;function Mv(r,e){return(e.castShadow?2:0)-(r.castShadow?2:0)+(e.map?1:0)-(r.map?1:0)}function Sv(r){const e=new gv,t=vv(),n={version:0,hash:{directionalLength:-1,pointLength:-1,spotLength:-1,rectAreaLength:-1,hemiLength:-1,numDirectionalShadows:-1,numPointShadows:-1,numSpotShadows:-1,numSpotMaps:-1,numLightProbes:-1},ambient:[0,0,0],probe:[],directional:[],directionalShadow:[],directionalShadowMap:[],directionalShadowMatrix:[],spot:[],spotLightMap:[],spotShadow:[],spotShadowMap:[],spotLightMatrix:[],rectArea:[],rectAreaLTC1:null,rectAreaLTC2:null,point:[],pointShadow:[],pointShadowMap:[],pointShadowMatrix:[],hemi:[],numSpotLightShadowsWithMaps:0,numLightProbes:0};for(let c=0;c<9;c++)n.probe.push(new V);const i=new V,s=new St,a=new St;function o(c){let u=0,f=0,h=0;for(let b=0;b<9;b++)n.probe[b].set(0,0,0);let d=0,_=0,g=0,p=0,m=0,M=0,S=0,y=0,A=0,w=0,C=0;c.sort(Mv);for(let b=0,W=c.length;b<W;b++){const L=c[b],G=L.color,z=L.intensity,q=L.distance;let H=null;if(L.shadow&&L.shadow.map&&(L.shadow.map.texture.format===ns?H=L.shadow.map.texture:H=L.shadow.map.depthTexture||L.shadow.map.texture),L.isAmbientLight)u+=G.r*z,f+=G.g*z,h+=G.b*z;else if(L.isLightProbe){for(let N=0;N<9;N++)n.probe[N].addScaledVector(L.sh.coefficients[N],z);C++}else if(L.isDirectionalLight){const N=e.get(L);if(N.color.copy(L.color).multiplyScalar(L.intensity),L.castShadow){const F=L.shadow,ne=t.get(L);ne.shadowIntensity=F.intensity,ne.shadowBias=F.bias,ne.shadowNormalBias=F.normalBias,ne.shadowRadius=F.radius,ne.shadowMapSize=F.mapSize,n.directionalShadow[d]=ne,n.directionalShadowMap[d]=H,n.directionalShadowMatrix[d]=L.shadow.matrix,M++}n.directional[d]=N,d++}else if(L.isSpotLight){const N=e.get(L);N.position.setFromMatrixPosition(L.matrixWorld),N.color.copy(G).multiplyScalar(z),N.distance=q,N.coneCos=Math.cos(L.angle),N.penumbraCos=Math.cos(L.angle*(1-L.penumbra)),N.decay=L.decay,n.spot[g]=N;const F=L.shadow;if(L.map&&(n.spotLightMap[A]=L.map,A++,F.updateMatrices(L),L.castShadow&&w++),n.spotLightMatrix[g]=F.matrix,L.castShadow){const ne=t.get(L);ne.shadowIntensity=F.intensity,ne.shadowBias=F.bias,ne.shadowNormalBias=F.normalBias,ne.shadowRadius=F.radius,ne.shadowMapSize=F.mapSize,n.spotShadow[g]=ne,n.spotShadowMap[g]=H,y++}g++}else if(L.isRectAreaLight){const N=e.get(L);N.color.copy(G).multiplyScalar(z),N.halfWidth.set(L.width*.5,0,0),N.halfHeight.set(0,L.height*.5,0),n.rectArea[p]=N,p++}else if(L.isPointLight){const N=e.get(L);if(N.color.copy(L.color).multiplyScalar(L.intensity),N.distance=L.distance,N.decay=L.decay,L.castShadow){const F=L.shadow,ne=t.get(L);ne.shadowIntensity=F.intensity,ne.shadowBias=F.bias,ne.shadowNormalBias=F.normalBias,ne.shadowRadius=F.radius,ne.shadowMapSize=F.mapSize,ne.shadowCameraNear=F.camera.near,ne.shadowCameraFar=F.camera.far,n.pointShadow[_]=ne,n.pointShadowMap[_]=H,n.pointShadowMatrix[_]=L.shadow.matrix,S++}n.point[_]=N,_++}else if(L.isHemisphereLight){const N=e.get(L);N.skyColor.copy(L.color).multiplyScalar(z),N.groundColor.copy(L.groundColor).multiplyScalar(z),n.hemi[m]=N,m++}}p>0&&(r.has("OES_texture_float_linear")===!0?(n.rectAreaLTC1=ye.LTC_FLOAT_1,n.rectAreaLTC2=ye.LTC_FLOAT_2):(n.rectAreaLTC1=ye.LTC_HALF_1,n.rectAreaLTC2=ye.LTC_HALF_2)),n.ambient[0]=u,n.ambient[1]=f,n.ambient[2]=h;const x=n.hash;(x.directionalLength!==d||x.pointLength!==_||x.spotLength!==g||x.rectAreaLength!==p||x.hemiLength!==m||x.numDirectionalShadows!==M||x.numPointShadows!==S||x.numSpotShadows!==y||x.numSpotMaps!==A||x.numLightProbes!==C)&&(n.directional.length=d,n.spot.length=g,n.rectArea.length=p,n.point.length=_,n.hemi.length=m,n.directionalShadow.length=M,n.directionalShadowMap.length=M,n.pointShadow.length=S,n.pointShadowMap.length=S,n.spotShadow.length=y,n.spotShadowMap.length=y,n.directionalShadowMatrix.length=M,n.pointShadowMatrix.length=S,n.spotLightMatrix.length=y+A-w,n.spotLightMap.length=A,n.numSpotLightShadowsWithMaps=w,n.numLightProbes=C,x.directionalLength=d,x.pointLength=_,x.spotLength=g,x.rectAreaLength=p,x.hemiLength=m,x.numDirectionalShadows=M,x.numPointShadows=S,x.numSpotShadows=y,x.numSpotMaps=A,x.numLightProbes=C,n.version=xv++)}function l(c,u){let f=0,h=0,d=0,_=0,g=0;const p=u.matrixWorldInverse;for(let m=0,M=c.length;m<M;m++){const S=c[m];if(S.isDirectionalLight){const y=n.directional[f];y.direction.setFromMatrixPosition(S.matrixWorld),i.setFromMatrixPosition(S.target.matrixWorld),y.direction.sub(i),y.direction.transformDirection(p),f++}else if(S.isSpotLight){const y=n.spot[d];y.position.setFromMatrixPosition(S.matrixWorld),y.position.applyMatrix4(p),y.direction.setFromMatrixPosition(S.matrixWorld),i.setFromMatrixPosition(S.target.matrixWorld),y.direction.sub(i),y.direction.transformDirection(p),d++}else if(S.isRectAreaLight){const y=n.rectArea[_];y.position.setFromMatrixPosition(S.matrixWorld),y.position.applyMatrix4(p),a.identity(),s.copy(S.matrixWorld),s.premultiply(p),a.extractRotation(s),y.halfWidth.set(S.width*.5,0,0),y.halfHeight.set(0,S.height*.5,0),y.halfWidth.applyMatrix4(a),y.halfHeight.applyMatrix4(a),_++}else if(S.isPointLight){const y=n.point[h];y.position.setFromMatrixPosition(S.matrixWorld),y.position.applyMatrix4(p),h++}else if(S.isHemisphereLight){const y=n.hemi[g];y.direction.setFromMatrixPosition(S.matrixWorld),y.direction.transformDirection(p),g++}}}return{setup:o,setupView:l,state:n}}function th(r){const e=new Sv(r),t=[],n=[];function i(u){c.camera=u,t.length=0,n.length=0}function s(u){t.push(u)}function a(u){n.push(u)}function o(){e.setup(t)}function l(u){e.setupView(t,u)}const c={lightsArray:t,shadowsArray:n,camera:null,lights:e,transmissionRenderTarget:{}};return{init:i,state:c,setupLights:o,setupLightsView:l,pushLight:s,pushShadow:a}}function yv(r){let e=new WeakMap;function t(i,s=0){const a=e.get(i);let o;return a===void 0?(o=new th(r),e.set(i,[o])):s>=a.length?(o=new th(r),a.push(o)):o=a[s],o}function n(){e=new WeakMap}return{get:t,dispose:n}}const bv=`void main() {
	gl_Position = vec4( position, 1.0 );
}`,Ev=`uniform sampler2D shadow_pass;
uniform vec2 resolution;
uniform float radius;
void main() {
	const float samples = float( VSM_SAMPLES );
	float mean = 0.0;
	float squared_mean = 0.0;
	float uvStride = samples <= 1.0 ? 0.0 : 2.0 / ( samples - 1.0 );
	float uvStart = samples <= 1.0 ? 0.0 : - 1.0;
	for ( float i = 0.0; i < samples; i ++ ) {
		float uvOffset = uvStart + i * uvStride;
		#ifdef HORIZONTAL_PASS
			vec2 distribution = texture2D( shadow_pass, ( gl_FragCoord.xy + vec2( uvOffset, 0.0 ) * radius ) / resolution ).rg;
			mean += distribution.x;
			squared_mean += distribution.y * distribution.y + distribution.x * distribution.x;
		#else
			float depth = texture2D( shadow_pass, ( gl_FragCoord.xy + vec2( 0.0, uvOffset ) * radius ) / resolution ).r;
			mean += depth;
			squared_mean += depth * depth;
		#endif
	}
	mean = mean / samples;
	squared_mean = squared_mean / samples;
	float std_dev = sqrt( max( 0.0, squared_mean - mean * mean ) );
	gl_FragColor = vec4( mean, std_dev, 0.0, 1.0 );
}`,Tv=[new V(1,0,0),new V(-1,0,0),new V(0,1,0),new V(0,-1,0),new V(0,0,1),new V(0,0,-1)],Av=[new V(0,-1,0),new V(0,-1,0),new V(0,0,1),new V(0,0,-1),new V(0,-1,0),new V(0,-1,0)],nh=new St,Es=new V,Xo=new V;function wv(r,e,t){let n=new Tc;const i=new qe,s=new qe,a=new Pt,o=new Fp,l=new Op,c={},u=t.maxTextureSize,f={[Zi]:mn,[mn]:Zi,[Xn]:Xn},h=new ci({defines:{VSM_SAMPLES:8},uniforms:{shadow_pass:{value:null},resolution:{value:new qe},radius:{value:4}},vertexShader:bv,fragmentShader:Ev}),d=h.clone();d.defines.HORIZONTAL_PASS=1;const _=new ln;_.setAttribute("position",new Yn(new Float32Array([-1,-1,.5,3,-1,.5,-1,3,.5]),3));const g=new Xt(_,h),p=this;this.enabled=!1,this.autoUpdate=!0,this.needsUpdate=!1,this.type=Da;let m=this.type;this.render=function(w,C,x){if(p.enabled===!1||p.autoUpdate===!1&&p.needsUpdate===!1||w.length===0)return;this.type===Ed&&(He("WebGLShadowMap: PCFSoftShadowMap has been deprecated. Using PCFShadowMap instead."),this.type=Da);const b=r.getRenderTarget(),W=r.getActiveCubeFace(),L=r.getActiveMipmapLevel(),G=r.state;G.setBlending(bi),G.buffers.depth.getReversed()===!0?G.buffers.color.setClear(0,0,0,0):G.buffers.color.setClear(1,1,1,1),G.buffers.depth.setTest(!0),G.setScissorTest(!1);const z=m!==this.type;z&&C.traverse(function(q){q.material&&(Array.isArray(q.material)?q.material.forEach(H=>H.needsUpdate=!0):q.material.needsUpdate=!0)});for(let q=0,H=w.length;q<H;q++){const N=w[q],F=N.shadow;if(F===void 0){He("WebGLShadowMap:",N,"has no shadow.");continue}if(F.autoUpdate===!1&&F.needsUpdate===!1)continue;i.copy(F.mapSize);const ne=F.getFrameExtents();i.multiply(ne),s.copy(F.mapSize),(i.x>u||i.y>u)&&(i.x>u&&(s.x=Math.floor(u/ne.x),i.x=s.x*ne.x,F.mapSize.x=s.x),i.y>u&&(s.y=Math.floor(u/ne.y),i.y=s.y*ne.y,F.mapSize.y=s.y));const ie=r.state.buffers.depth.getReversed();if(F.camera._reversedDepth=ie,F.map===null||z===!0){if(F.map!==null&&(F.map.depthTexture!==null&&(F.map.depthTexture.dispose(),F.map.depthTexture=null),F.map.dispose()),this.type===As){if(N.isPointLight){He("WebGLShadowMap: VSM shadow maps are not supported for PointLights. Use PCF or BasicShadowMap instead.");continue}F.map=new ai(i.x,i.y,{format:ns,type:Ti,minFilter:en,magFilter:en,generateMipmaps:!1}),F.map.texture.name=N.name+".shadowMap",F.map.depthTexture=new Bs(i.x,i.y,ni),F.map.depthTexture.name=N.name+".shadowMapDepth",F.map.depthTexture.format=Ai,F.map.depthTexture.compareFunction=null,F.map.depthTexture.minFilter=qt,F.map.depthTexture.magFilter=qt}else N.isPointLight?(F.map=new rf(i.x),F.map.depthTexture=new Pp(i.x,oi)):(F.map=new ai(i.x,i.y),F.map.depthTexture=new Bs(i.x,i.y,oi)),F.map.depthTexture.name=N.name+".shadowMap",F.map.depthTexture.format=Ai,this.type===Da?(F.map.depthTexture.compareFunction=ie?Sc:Mc,F.map.depthTexture.minFilter=en,F.map.depthTexture.magFilter=en):(F.map.depthTexture.compareFunction=null,F.map.depthTexture.minFilter=qt,F.map.depthTexture.magFilter=qt);F.camera.updateProjectionMatrix()}const me=F.map.isWebGLCubeRenderTarget?6:1;for(let Te=0;Te<me;Te++){if(F.map.isWebGLCubeRenderTarget)r.setRenderTarget(F.map,Te),r.clear();else{Te===0&&(r.setRenderTarget(F.map),r.clear());const le=F.getViewport(Te);a.set(s.x*le.x,s.y*le.y,s.x*le.z,s.y*le.w),G.viewport(a)}if(N.isPointLight){const le=F.camera,Oe=F.matrix,Je=N.distance||le.far;Je!==le.far&&(le.far=Je,le.updateProjectionMatrix()),Es.setFromMatrixPosition(N.matrixWorld),le.position.copy(Es),Xo.copy(le.position),Xo.add(Tv[Te]),le.up.copy(Av[Te]),le.lookAt(Xo),le.updateMatrixWorld(),Oe.makeTranslation(-Es.x,-Es.y,-Es.z),nh.multiplyMatrices(le.projectionMatrix,le.matrixWorldInverse),F._frustum.setFromProjectionMatrix(nh,le.coordinateSystem,le.reversedDepth)}else F.updateMatrices(N);n=F.getFrustum(),y(C,x,F.camera,N,this.type)}F.isPointLightShadow!==!0&&this.type===As&&M(F,x),F.needsUpdate=!1}m=this.type,p.needsUpdate=!1,r.setRenderTarget(b,W,L)};function M(w,C){const x=e.update(g);h.defines.VSM_SAMPLES!==w.blurSamples&&(h.defines.VSM_SAMPLES=w.blurSamples,d.defines.VSM_SAMPLES=w.blurSamples,h.needsUpdate=!0,d.needsUpdate=!0),w.mapPass===null&&(w.mapPass=new ai(i.x,i.y,{format:ns,type:Ti})),h.uniforms.shadow_pass.value=w.map.depthTexture,h.uniforms.resolution.value=w.mapSize,h.uniforms.radius.value=w.radius,r.setRenderTarget(w.mapPass),r.clear(),r.renderBufferDirect(C,null,x,h,g,null),d.uniforms.shadow_pass.value=w.mapPass.texture,d.uniforms.resolution.value=w.mapSize,d.uniforms.radius.value=w.radius,r.setRenderTarget(w.map),r.clear(),r.renderBufferDirect(C,null,x,d,g,null)}function S(w,C,x,b){let W=null;const L=x.isPointLight===!0?w.customDistanceMaterial:w.customDepthMaterial;if(L!==void 0)W=L;else if(W=x.isPointLight===!0?l:o,r.localClippingEnabled&&C.clipShadows===!0&&Array.isArray(C.clippingPlanes)&&C.clippingPlanes.length!==0||C.displacementMap&&C.displacementScale!==0||C.alphaMap&&C.alphaTest>0||C.map&&C.alphaTest>0||C.alphaToCoverage===!0){const G=W.uuid,z=C.uuid;let q=c[G];q===void 0&&(q={},c[G]=q);let H=q[z];H===void 0&&(H=W.clone(),q[z]=H,C.addEventListener("dispose",A)),W=H}if(W.visible=C.visible,W.wireframe=C.wireframe,b===As?W.side=C.shadowSide!==null?C.shadowSide:C.side:W.side=C.shadowSide!==null?C.shadowSide:f[C.side],W.alphaMap=C.alphaMap,W.alphaTest=C.alphaToCoverage===!0?.5:C.alphaTest,W.map=C.map,W.clipShadows=C.clipShadows,W.clippingPlanes=C.clippingPlanes,W.clipIntersection=C.clipIntersection,W.displacementMap=C.displacementMap,W.displacementScale=C.displacementScale,W.displacementBias=C.displacementBias,W.wireframeLinewidth=C.wireframeLinewidth,W.linewidth=C.linewidth,x.isPointLight===!0&&W.isMeshDistanceMaterial===!0){const G=r.properties.get(W);G.light=x}return W}function y(w,C,x,b,W){if(w.visible===!1)return;if(w.layers.test(C.layers)&&(w.isMesh||w.isLine||w.isPoints)&&(w.castShadow||w.receiveShadow&&W===As)&&(!w.frustumCulled||n.intersectsObject(w))){w.modelViewMatrix.multiplyMatrices(x.matrixWorldInverse,w.matrixWorld);const z=e.update(w),q=w.material;if(Array.isArray(q)){const H=z.groups;for(let N=0,F=H.length;N<F;N++){const ne=H[N],ie=q[ne.materialIndex];if(ie&&ie.visible){const me=S(w,ie,b,W);w.onBeforeShadow(r,w,C,x,z,me,ne),r.renderBufferDirect(x,null,z,me,w,ne),w.onAfterShadow(r,w,C,x,z,me,ne)}}}else if(q.visible){const H=S(w,q,b,W);w.onBeforeShadow(r,w,C,x,z,H,null),r.renderBufferDirect(x,null,z,H,w,null),w.onAfterShadow(r,w,C,x,z,H,null)}}const G=w.children;for(let z=0,q=G.length;z<q;z++)y(G[z],C,x,b,W)}function A(w){w.target.removeEventListener("dispose",A);for(const x in c){const b=c[x],W=w.target.uuid;W in b&&(b[W].dispose(),delete b[W])}}}function Rv(r,e){function t(){let U=!1;const fe=new Pt;let ue=null;const _e=new Pt(0,0,0,0);return{setMask:function(ae){ue!==ae&&!U&&(r.colorMask(ae,ae,ae,ae),ue=ae)},setLocked:function(ae){U=ae},setClear:function(ae,J,we,Le,ft){ft===!0&&(ae*=Le,J*=Le,we*=Le),fe.set(ae,J,we,Le),_e.equals(fe)===!1&&(r.clearColor(ae,J,we,Le),_e.copy(fe))},reset:function(){U=!1,ue=null,_e.set(-1,0,0,0)}}}function n(){let U=!1,fe=!1,ue=null,_e=null,ae=null;return{setReversed:function(J){if(fe!==J){const we=e.get("EXT_clip_control");J?we.clipControlEXT(we.LOWER_LEFT_EXT,we.ZERO_TO_ONE_EXT):we.clipControlEXT(we.LOWER_LEFT_EXT,we.NEGATIVE_ONE_TO_ONE_EXT),fe=J;const Le=ae;ae=null,this.setClear(Le)}},getReversed:function(){return fe},setTest:function(J){J?de(r.DEPTH_TEST):pe(r.DEPTH_TEST)},setMask:function(J){ue!==J&&!U&&(r.depthMask(J),ue=J)},setFunc:function(J){if(fe&&(J=ip[J]),_e!==J){switch(J){case il:r.depthFunc(r.NEVER);break;case rl:r.depthFunc(r.ALWAYS);break;case sl:r.depthFunc(r.LESS);break;case es:r.depthFunc(r.LEQUAL);break;case al:r.depthFunc(r.EQUAL);break;case ol:r.depthFunc(r.GEQUAL);break;case ll:r.depthFunc(r.GREATER);break;case cl:r.depthFunc(r.NOTEQUAL);break;default:r.depthFunc(r.LEQUAL)}_e=J}},setLocked:function(J){U=J},setClear:function(J){ae!==J&&(ae=J,fe&&(J=1-J),r.clearDepth(J))},reset:function(){U=!1,ue=null,_e=null,ae=null,fe=!1}}}function i(){let U=!1,fe=null,ue=null,_e=null,ae=null,J=null,we=null,Le=null,ft=null;return{setTest:function(tt){U||(tt?de(r.STENCIL_TEST):pe(r.STENCIL_TEST))},setMask:function(tt){fe!==tt&&!U&&(r.stencilMask(tt),fe=tt)},setFunc:function(tt,Ut,cn){(ue!==tt||_e!==Ut||ae!==cn)&&(r.stencilFunc(tt,Ut,cn),ue=tt,_e=Ut,ae=cn)},setOp:function(tt,Ut,cn){(J!==tt||we!==Ut||Le!==cn)&&(r.stencilOp(tt,Ut,cn),J=tt,we=Ut,Le=cn)},setLocked:function(tt){U=tt},setClear:function(tt){ft!==tt&&(r.clearStencil(tt),ft=tt)},reset:function(){U=!1,fe=null,ue=null,_e=null,ae=null,J=null,we=null,Le=null,ft=null}}}const s=new t,a=new n,o=new i,l=new WeakMap,c=new WeakMap;let u={},f={},h=new WeakMap,d=[],_=null,g=!1,p=null,m=null,M=null,S=null,y=null,A=null,w=null,C=new it(0,0,0),x=0,b=!1,W=null,L=null,G=null,z=null,q=null;const H=r.getParameter(r.MAX_COMBINED_TEXTURE_IMAGE_UNITS);let N=!1,F=0;const ne=r.getParameter(r.VERSION);ne.indexOf("WebGL")!==-1?(F=parseFloat(/^WebGL (\d)/.exec(ne)[1]),N=F>=1):ne.indexOf("OpenGL ES")!==-1&&(F=parseFloat(/^OpenGL ES (\d)/.exec(ne)[1]),N=F>=2);let ie=null,me={};const Te=r.getParameter(r.SCISSOR_BOX),le=r.getParameter(r.VIEWPORT),Oe=new Pt().fromArray(Te),Je=new Pt().fromArray(le);function $e(U,fe,ue,_e){const ae=new Uint8Array(4),J=r.createTexture();r.bindTexture(U,J),r.texParameteri(U,r.TEXTURE_MIN_FILTER,r.NEAREST),r.texParameteri(U,r.TEXTURE_MAG_FILTER,r.NEAREST);for(let we=0;we<ue;we++)U===r.TEXTURE_3D||U===r.TEXTURE_2D_ARRAY?r.texImage3D(fe,0,r.RGBA,1,1,_e,0,r.RGBA,r.UNSIGNED_BYTE,ae):r.texImage2D(fe+we,0,r.RGBA,1,1,0,r.RGBA,r.UNSIGNED_BYTE,ae);return J}const ee={};ee[r.TEXTURE_2D]=$e(r.TEXTURE_2D,r.TEXTURE_2D,1),ee[r.TEXTURE_CUBE_MAP]=$e(r.TEXTURE_CUBE_MAP,r.TEXTURE_CUBE_MAP_POSITIVE_X,6),ee[r.TEXTURE_2D_ARRAY]=$e(r.TEXTURE_2D_ARRAY,r.TEXTURE_2D_ARRAY,1,1),ee[r.TEXTURE_3D]=$e(r.TEXTURE_3D,r.TEXTURE_3D,1,1),s.setClear(0,0,0,1),a.setClear(1),o.setClear(0),de(r.DEPTH_TEST),a.setFunc(es),Ye(!1),vt(Kc),de(r.CULL_FACE),ot(bi);function de(U){u[U]!==!0&&(r.enable(U),u[U]=!0)}function pe(U){u[U]!==!1&&(r.disable(U),u[U]=!1)}function Ve(U,fe){return f[U]!==fe?(r.bindFramebuffer(U,fe),f[U]=fe,U===r.DRAW_FRAMEBUFFER&&(f[r.FRAMEBUFFER]=fe),U===r.FRAMEBUFFER&&(f[r.DRAW_FRAMEBUFFER]=fe),!0):!1}function Ne(U,fe){let ue=d,_e=!1;if(U){ue=h.get(fe),ue===void 0&&(ue=[],h.set(fe,ue));const ae=U.textures;if(ue.length!==ae.length||ue[0]!==r.COLOR_ATTACHMENT0){for(let J=0,we=ae.length;J<we;J++)ue[J]=r.COLOR_ATTACHMENT0+J;ue.length=ae.length,_e=!0}}else ue[0]!==r.BACK&&(ue[0]=r.BACK,_e=!0);_e&&r.drawBuffers(ue)}function Fe(U){return _!==U?(r.useProgram(U),_=U,!0):!1}const Tt={[dr]:r.FUNC_ADD,[Ad]:r.FUNC_SUBTRACT,[wd]:r.FUNC_REVERSE_SUBTRACT};Tt[Rd]=r.MIN,Tt[Cd]=r.MAX;const Qe={[Pd]:r.ZERO,[Dd]:r.ONE,[Ld]:r.SRC_COLOR,[tl]:r.SRC_ALPHA,[Bd]:r.SRC_ALPHA_SATURATE,[Fd]:r.DST_COLOR,[Ud]:r.DST_ALPHA,[Id]:r.ONE_MINUS_SRC_COLOR,[nl]:r.ONE_MINUS_SRC_ALPHA,[Od]:r.ONE_MINUS_DST_COLOR,[Nd]:r.ONE_MINUS_DST_ALPHA,[kd]:r.CONSTANT_COLOR,[zd]:r.ONE_MINUS_CONSTANT_COLOR,[Vd]:r.CONSTANT_ALPHA,[Gd]:r.ONE_MINUS_CONSTANT_ALPHA};function ot(U,fe,ue,_e,ae,J,we,Le,ft,tt){if(U===bi){g===!0&&(pe(r.BLEND),g=!1);return}if(g===!1&&(de(r.BLEND),g=!0),U!==Td){if(U!==p||tt!==b){if((m!==dr||y!==dr)&&(r.blendEquation(r.FUNC_ADD),m=dr,y=dr),tt)switch(U){case Kr:r.blendFuncSeparate(r.ONE,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case Zc:r.blendFunc(r.ONE,r.ONE);break;case jc:r.blendFuncSeparate(r.ZERO,r.ONE_MINUS_SRC_COLOR,r.ZERO,r.ONE);break;case Jc:r.blendFuncSeparate(r.DST_COLOR,r.ONE_MINUS_SRC_ALPHA,r.ZERO,r.ONE);break;default:ut("WebGLState: Invalid blending: ",U);break}else switch(U){case Kr:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case Zc:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE,r.ONE,r.ONE);break;case jc:ut("WebGLState: SubtractiveBlending requires material.premultipliedAlpha = true");break;case Jc:ut("WebGLState: MultiplyBlending requires material.premultipliedAlpha = true");break;default:ut("WebGLState: Invalid blending: ",U);break}M=null,S=null,A=null,w=null,C.set(0,0,0),x=0,p=U,b=tt}return}ae=ae||fe,J=J||ue,we=we||_e,(fe!==m||ae!==y)&&(r.blendEquationSeparate(Tt[fe],Tt[ae]),m=fe,y=ae),(ue!==M||_e!==S||J!==A||we!==w)&&(r.blendFuncSeparate(Qe[ue],Qe[_e],Qe[J],Qe[we]),M=ue,S=_e,A=J,w=we),(Le.equals(C)===!1||ft!==x)&&(r.blendColor(Le.r,Le.g,Le.b,ft),C.copy(Le),x=ft),p=U,b=!1}function pt(U,fe){U.side===Xn?pe(r.CULL_FACE):de(r.CULL_FACE);let ue=U.side===mn;fe&&(ue=!ue),Ye(ue),U.blending===Kr&&U.transparent===!1?ot(bi):ot(U.blending,U.blendEquation,U.blendSrc,U.blendDst,U.blendEquationAlpha,U.blendSrcAlpha,U.blendDstAlpha,U.blendColor,U.blendAlpha,U.premultipliedAlpha),a.setFunc(U.depthFunc),a.setTest(U.depthTest),a.setMask(U.depthWrite),s.setMask(U.colorWrite);const _e=U.stencilWrite;o.setTest(_e),_e&&(o.setMask(U.stencilWriteMask),o.setFunc(U.stencilFunc,U.stencilRef,U.stencilFuncMask),o.setOp(U.stencilFail,U.stencilZFail,U.stencilZPass)),yt(U.polygonOffset,U.polygonOffsetFactor,U.polygonOffsetUnits),U.alphaToCoverage===!0?de(r.SAMPLE_ALPHA_TO_COVERAGE):pe(r.SAMPLE_ALPHA_TO_COVERAGE)}function Ye(U){W!==U&&(U?r.frontFace(r.CW):r.frontFace(r.CCW),W=U)}function vt(U){U!==yd?(de(r.CULL_FACE),U!==L&&(U===Kc?r.cullFace(r.BACK):U===bd?r.cullFace(r.FRONT):r.cullFace(r.FRONT_AND_BACK))):pe(r.CULL_FACE),L=U}function I(U){U!==G&&(N&&r.lineWidth(U),G=U)}function yt(U,fe,ue){U?(de(r.POLYGON_OFFSET_FILL),(z!==fe||q!==ue)&&(z=fe,q=ue,a.getReversed()&&(fe=-fe),r.polygonOffset(fe,ue))):pe(r.POLYGON_OFFSET_FILL)}function rt(U){U?de(r.SCISSOR_TEST):pe(r.SCISSOR_TEST)}function Q(U){U===void 0&&(U=r.TEXTURE0+H-1),ie!==U&&(r.activeTexture(U),ie=U)}function Z(U,fe,ue){ue===void 0&&(ie===null?ue=r.TEXTURE0+H-1:ue=ie);let _e=me[ue];_e===void 0&&(_e={type:void 0,texture:void 0},me[ue]=_e),(_e.type!==U||_e.texture!==fe)&&(ie!==ue&&(r.activeTexture(ue),ie=ue),r.bindTexture(U,fe||ee[U]),_e.type=U,_e.texture=fe)}function T(){const U=me[ie];U!==void 0&&U.type!==void 0&&(r.bindTexture(U.type,null),U.type=void 0,U.texture=void 0)}function v(){try{r.compressedTexImage2D(...arguments)}catch(U){ut("WebGLState:",U)}}function D(){try{r.compressedTexImage3D(...arguments)}catch(U){ut("WebGLState:",U)}}function B(){try{r.texSubImage2D(...arguments)}catch(U){ut("WebGLState:",U)}}function j(){try{r.texSubImage3D(...arguments)}catch(U){ut("WebGLState:",U)}}function $(){try{r.compressedTexSubImage2D(...arguments)}catch(U){ut("WebGLState:",U)}}function ce(){try{r.compressedTexSubImage3D(...arguments)}catch(U){ut("WebGLState:",U)}}function se(){try{r.texStorage2D(...arguments)}catch(U){ut("WebGLState:",U)}}function Re(){try{r.texStorage3D(...arguments)}catch(U){ut("WebGLState:",U)}}function Ue(){try{r.texImage2D(...arguments)}catch(U){ut("WebGLState:",U)}}function re(){try{r.texImage3D(...arguments)}catch(U){ut("WebGLState:",U)}}function he(U){Oe.equals(U)===!1&&(r.scissor(U.x,U.y,U.z,U.w),Oe.copy(U))}function Ce(U){Je.equals(U)===!1&&(r.viewport(U.x,U.y,U.z,U.w),Je.copy(U))}function be(U,fe){let ue=c.get(fe);ue===void 0&&(ue=new WeakMap,c.set(fe,ue));let _e=ue.get(U);_e===void 0&&(_e=r.getUniformBlockIndex(fe,U.name),ue.set(U,_e))}function Ee(U,fe){const _e=c.get(fe).get(U);l.get(fe)!==_e&&(r.uniformBlockBinding(fe,_e,U.__bindingPointIndex),l.set(fe,_e))}function Be(){r.disable(r.BLEND),r.disable(r.CULL_FACE),r.disable(r.DEPTH_TEST),r.disable(r.POLYGON_OFFSET_FILL),r.disable(r.SCISSOR_TEST),r.disable(r.STENCIL_TEST),r.disable(r.SAMPLE_ALPHA_TO_COVERAGE),r.blendEquation(r.FUNC_ADD),r.blendFunc(r.ONE,r.ZERO),r.blendFuncSeparate(r.ONE,r.ZERO,r.ONE,r.ZERO),r.blendColor(0,0,0,0),r.colorMask(!0,!0,!0,!0),r.clearColor(0,0,0,0),r.depthMask(!0),r.depthFunc(r.LESS),a.setReversed(!1),r.clearDepth(1),r.stencilMask(4294967295),r.stencilFunc(r.ALWAYS,0,4294967295),r.stencilOp(r.KEEP,r.KEEP,r.KEEP),r.clearStencil(0),r.cullFace(r.BACK),r.frontFace(r.CCW),r.polygonOffset(0,0),r.activeTexture(r.TEXTURE0),r.bindFramebuffer(r.FRAMEBUFFER,null),r.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),r.bindFramebuffer(r.READ_FRAMEBUFFER,null),r.useProgram(null),r.lineWidth(1),r.scissor(0,0,r.canvas.width,r.canvas.height),r.viewport(0,0,r.canvas.width,r.canvas.height),u={},ie=null,me={},f={},h=new WeakMap,d=[],_=null,g=!1,p=null,m=null,M=null,S=null,y=null,A=null,w=null,C=new it(0,0,0),x=0,b=!1,W=null,L=null,G=null,z=null,q=null,Oe.set(0,0,r.canvas.width,r.canvas.height),Je.set(0,0,r.canvas.width,r.canvas.height),s.reset(),a.reset(),o.reset()}return{buffers:{color:s,depth:a,stencil:o},enable:de,disable:pe,bindFramebuffer:Ve,drawBuffers:Ne,useProgram:Fe,setBlending:ot,setMaterial:pt,setFlipSided:Ye,setCullFace:vt,setLineWidth:I,setPolygonOffset:yt,setScissorTest:rt,activeTexture:Q,bindTexture:Z,unbindTexture:T,compressedTexImage2D:v,compressedTexImage3D:D,texImage2D:Ue,texImage3D:re,updateUBOMapping:be,uniformBlockBinding:Ee,texStorage2D:se,texStorage3D:Re,texSubImage2D:B,texSubImage3D:j,compressedTexSubImage2D:$,compressedTexSubImage3D:ce,scissor:he,viewport:Ce,reset:Be}}function Cv(r,e,t,n,i,s,a){const o=e.has("WEBGL_multisampled_render_to_texture")?e.get("WEBGL_multisampled_render_to_texture"):null,l=typeof navigator>"u"?!1:/OculusBrowser/g.test(navigator.userAgent),c=new qe,u=new WeakMap;let f;const h=new WeakMap;let d=!1;try{d=typeof OffscreenCanvas<"u"&&new OffscreenCanvas(1,1).getContext("2d")!==null}catch{}function _(T,v){return d?new OffscreenCanvas(T,v):Ga("canvas")}function g(T,v,D){let B=1;const j=Z(T);if((j.width>D||j.height>D)&&(B=D/Math.max(j.width,j.height)),B<1)if(typeof HTMLImageElement<"u"&&T instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&T instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&T instanceof ImageBitmap||typeof VideoFrame<"u"&&T instanceof VideoFrame){const $=Math.floor(B*j.width),ce=Math.floor(B*j.height);f===void 0&&(f=_($,ce));const se=v?_($,ce):f;return se.width=$,se.height=ce,se.getContext("2d").drawImage(T,0,0,$,ce),He("WebGLRenderer: Texture has been resized from ("+j.width+"x"+j.height+") to ("+$+"x"+ce+")."),se}else return"data"in T&&He("WebGLRenderer: Image in DataTexture is too big ("+j.width+"x"+j.height+")."),T;return T}function p(T){return T.generateMipmaps}function m(T){r.generateMipmap(T)}function M(T){return T.isWebGLCubeRenderTarget?r.TEXTURE_CUBE_MAP:T.isWebGL3DRenderTarget?r.TEXTURE_3D:T.isWebGLArrayRenderTarget||T.isCompressedArrayTexture?r.TEXTURE_2D_ARRAY:r.TEXTURE_2D}function S(T,v,D,B,j=!1){if(T!==null){if(r[T]!==void 0)return r[T];He("WebGLRenderer: Attempt to use non-existing WebGL internal format '"+T+"'")}let $=v;if(v===r.RED&&(D===r.FLOAT&&($=r.R32F),D===r.HALF_FLOAT&&($=r.R16F),D===r.UNSIGNED_BYTE&&($=r.R8)),v===r.RED_INTEGER&&(D===r.UNSIGNED_BYTE&&($=r.R8UI),D===r.UNSIGNED_SHORT&&($=r.R16UI),D===r.UNSIGNED_INT&&($=r.R32UI),D===r.BYTE&&($=r.R8I),D===r.SHORT&&($=r.R16I),D===r.INT&&($=r.R32I)),v===r.RG&&(D===r.FLOAT&&($=r.RG32F),D===r.HALF_FLOAT&&($=r.RG16F),D===r.UNSIGNED_BYTE&&($=r.RG8)),v===r.RG_INTEGER&&(D===r.UNSIGNED_BYTE&&($=r.RG8UI),D===r.UNSIGNED_SHORT&&($=r.RG16UI),D===r.UNSIGNED_INT&&($=r.RG32UI),D===r.BYTE&&($=r.RG8I),D===r.SHORT&&($=r.RG16I),D===r.INT&&($=r.RG32I)),v===r.RGB_INTEGER&&(D===r.UNSIGNED_BYTE&&($=r.RGB8UI),D===r.UNSIGNED_SHORT&&($=r.RGB16UI),D===r.UNSIGNED_INT&&($=r.RGB32UI),D===r.BYTE&&($=r.RGB8I),D===r.SHORT&&($=r.RGB16I),D===r.INT&&($=r.RGB32I)),v===r.RGBA_INTEGER&&(D===r.UNSIGNED_BYTE&&($=r.RGBA8UI),D===r.UNSIGNED_SHORT&&($=r.RGBA16UI),D===r.UNSIGNED_INT&&($=r.RGBA32UI),D===r.BYTE&&($=r.RGBA8I),D===r.SHORT&&($=r.RGBA16I),D===r.INT&&($=r.RGBA32I)),v===r.RGB&&(D===r.UNSIGNED_INT_5_9_9_9_REV&&($=r.RGB9_E5),D===r.UNSIGNED_INT_10F_11F_11F_REV&&($=r.R11F_G11F_B10F)),v===r.RGBA){const ce=j?Va:ht.getTransfer(B);D===r.FLOAT&&($=r.RGBA32F),D===r.HALF_FLOAT&&($=r.RGBA16F),D===r.UNSIGNED_BYTE&&($=ce===_t?r.SRGB8_ALPHA8:r.RGBA8),D===r.UNSIGNED_SHORT_4_4_4_4&&($=r.RGBA4),D===r.UNSIGNED_SHORT_5_5_5_1&&($=r.RGB5_A1)}return($===r.R16F||$===r.R32F||$===r.RG16F||$===r.RG32F||$===r.RGBA16F||$===r.RGBA32F)&&e.get("EXT_color_buffer_float"),$}function y(T,v){let D;return T?v===null||v===oi||v===Fs?D=r.DEPTH24_STENCIL8:v===ni?D=r.DEPTH32F_STENCIL8:v===Ns&&(D=r.DEPTH24_STENCIL8,He("DepthTexture: 16 bit depth attachment is not supported with stencil. Using 24-bit attachment.")):v===null||v===oi||v===Fs?D=r.DEPTH_COMPONENT24:v===ni?D=r.DEPTH_COMPONENT32F:v===Ns&&(D=r.DEPTH_COMPONENT16),D}function A(T,v){return p(T)===!0||T.isFramebufferTexture&&T.minFilter!==qt&&T.minFilter!==en?Math.log2(Math.max(v.width,v.height))+1:T.mipmaps!==void 0&&T.mipmaps.length>0?T.mipmaps.length:T.isCompressedTexture&&Array.isArray(T.image)?v.mipmaps.length:1}function w(T){const v=T.target;v.removeEventListener("dispose",w),x(v),v.isVideoTexture&&u.delete(v)}function C(T){const v=T.target;v.removeEventListener("dispose",C),W(v)}function x(T){const v=n.get(T);if(v.__webglInit===void 0)return;const D=T.source,B=h.get(D);if(B){const j=B[v.__cacheKey];j.usedTimes--,j.usedTimes===0&&b(T),Object.keys(B).length===0&&h.delete(D)}n.remove(T)}function b(T){const v=n.get(T);r.deleteTexture(v.__webglTexture);const D=T.source,B=h.get(D);delete B[v.__cacheKey],a.memory.textures--}function W(T){const v=n.get(T);if(T.depthTexture&&(T.depthTexture.dispose(),n.remove(T.depthTexture)),T.isWebGLCubeRenderTarget)for(let B=0;B<6;B++){if(Array.isArray(v.__webglFramebuffer[B]))for(let j=0;j<v.__webglFramebuffer[B].length;j++)r.deleteFramebuffer(v.__webglFramebuffer[B][j]);else r.deleteFramebuffer(v.__webglFramebuffer[B]);v.__webglDepthbuffer&&r.deleteRenderbuffer(v.__webglDepthbuffer[B])}else{if(Array.isArray(v.__webglFramebuffer))for(let B=0;B<v.__webglFramebuffer.length;B++)r.deleteFramebuffer(v.__webglFramebuffer[B]);else r.deleteFramebuffer(v.__webglFramebuffer);if(v.__webglDepthbuffer&&r.deleteRenderbuffer(v.__webglDepthbuffer),v.__webglMultisampledFramebuffer&&r.deleteFramebuffer(v.__webglMultisampledFramebuffer),v.__webglColorRenderbuffer)for(let B=0;B<v.__webglColorRenderbuffer.length;B++)v.__webglColorRenderbuffer[B]&&r.deleteRenderbuffer(v.__webglColorRenderbuffer[B]);v.__webglDepthRenderbuffer&&r.deleteRenderbuffer(v.__webglDepthRenderbuffer)}const D=T.textures;for(let B=0,j=D.length;B<j;B++){const $=n.get(D[B]);$.__webglTexture&&(r.deleteTexture($.__webglTexture),a.memory.textures--),n.remove(D[B])}n.remove(T)}let L=0;function G(){L=0}function z(){const T=L;return T>=i.maxTextures&&He("WebGLTextures: Trying to use "+T+" texture units while this GPU supports only "+i.maxTextures),L+=1,T}function q(T){const v=[];return v.push(T.wrapS),v.push(T.wrapT),v.push(T.wrapR||0),v.push(T.magFilter),v.push(T.minFilter),v.push(T.anisotropy),v.push(T.internalFormat),v.push(T.format),v.push(T.type),v.push(T.generateMipmaps),v.push(T.premultiplyAlpha),v.push(T.flipY),v.push(T.unpackAlignment),v.push(T.colorSpace),v.join()}function H(T,v){const D=n.get(T);if(T.isVideoTexture&&rt(T),T.isRenderTargetTexture===!1&&T.isExternalTexture!==!0&&T.version>0&&D.__version!==T.version){const B=T.image;if(B===null)He("WebGLRenderer: Texture marked for update but no image data found.");else if(B.complete===!1)He("WebGLRenderer: Texture marked for update but image is incomplete");else{ee(D,T,v);return}}else T.isExternalTexture&&(D.__webglTexture=T.sourceTexture?T.sourceTexture:null);t.bindTexture(r.TEXTURE_2D,D.__webglTexture,r.TEXTURE0+v)}function N(T,v){const D=n.get(T);if(T.isRenderTargetTexture===!1&&T.version>0&&D.__version!==T.version){ee(D,T,v);return}else T.isExternalTexture&&(D.__webglTexture=T.sourceTexture?T.sourceTexture:null);t.bindTexture(r.TEXTURE_2D_ARRAY,D.__webglTexture,r.TEXTURE0+v)}function F(T,v){const D=n.get(T);if(T.isRenderTargetTexture===!1&&T.version>0&&D.__version!==T.version){ee(D,T,v);return}t.bindTexture(r.TEXTURE_3D,D.__webglTexture,r.TEXTURE0+v)}function ne(T,v){const D=n.get(T);if(T.isCubeDepthTexture!==!0&&T.version>0&&D.__version!==T.version){de(D,T,v);return}t.bindTexture(r.TEXTURE_CUBE_MAP,D.__webglTexture,r.TEXTURE0+v)}const ie={[ul]:r.REPEAT,[yi]:r.CLAMP_TO_EDGE,[hl]:r.MIRRORED_REPEAT},me={[qt]:r.NEAREST,[Xd]:r.NEAREST_MIPMAP_NEAREST,[ea]:r.NEAREST_MIPMAP_LINEAR,[en]:r.LINEAR,[ho]:r.LINEAR_MIPMAP_NEAREST,[mr]:r.LINEAR_MIPMAP_LINEAR},Te={[$d]:r.NEVER,[Qd]:r.ALWAYS,[Kd]:r.LESS,[Mc]:r.LEQUAL,[Zd]:r.EQUAL,[Sc]:r.GEQUAL,[jd]:r.GREATER,[Jd]:r.NOTEQUAL};function le(T,v){if(v.type===ni&&e.has("OES_texture_float_linear")===!1&&(v.magFilter===en||v.magFilter===ho||v.magFilter===ea||v.magFilter===mr||v.minFilter===en||v.minFilter===ho||v.minFilter===ea||v.minFilter===mr)&&He("WebGLRenderer: Unable to use linear filtering with floating point textures. OES_texture_float_linear not supported on this device."),r.texParameteri(T,r.TEXTURE_WRAP_S,ie[v.wrapS]),r.texParameteri(T,r.TEXTURE_WRAP_T,ie[v.wrapT]),(T===r.TEXTURE_3D||T===r.TEXTURE_2D_ARRAY)&&r.texParameteri(T,r.TEXTURE_WRAP_R,ie[v.wrapR]),r.texParameteri(T,r.TEXTURE_MAG_FILTER,me[v.magFilter]),r.texParameteri(T,r.TEXTURE_MIN_FILTER,me[v.minFilter]),v.compareFunction&&(r.texParameteri(T,r.TEXTURE_COMPARE_MODE,r.COMPARE_REF_TO_TEXTURE),r.texParameteri(T,r.TEXTURE_COMPARE_FUNC,Te[v.compareFunction])),e.has("EXT_texture_filter_anisotropic")===!0){if(v.magFilter===qt||v.minFilter!==ea&&v.minFilter!==mr||v.type===ni&&e.has("OES_texture_float_linear")===!1)return;if(v.anisotropy>1||n.get(v).__currentAnisotropy){const D=e.get("EXT_texture_filter_anisotropic");r.texParameterf(T,D.TEXTURE_MAX_ANISOTROPY_EXT,Math.min(v.anisotropy,i.getMaxAnisotropy())),n.get(v).__currentAnisotropy=v.anisotropy}}}function Oe(T,v){let D=!1;T.__webglInit===void 0&&(T.__webglInit=!0,v.addEventListener("dispose",w));const B=v.source;let j=h.get(B);j===void 0&&(j={},h.set(B,j));const $=q(v);if($!==T.__cacheKey){j[$]===void 0&&(j[$]={texture:r.createTexture(),usedTimes:0},a.memory.textures++,D=!0),j[$].usedTimes++;const ce=j[T.__cacheKey];ce!==void 0&&(j[T.__cacheKey].usedTimes--,ce.usedTimes===0&&b(v)),T.__cacheKey=$,T.__webglTexture=j[$].texture}return D}function Je(T,v,D){return Math.floor(Math.floor(T/D)/v)}function $e(T,v,D,B){const $=T.updateRanges;if($.length===0)t.texSubImage2D(r.TEXTURE_2D,0,0,0,v.width,v.height,D,B,v.data);else{$.sort((re,he)=>re.start-he.start);let ce=0;for(let re=1;re<$.length;re++){const he=$[ce],Ce=$[re],be=he.start+he.count,Ee=Je(Ce.start,v.width,4),Be=Je(he.start,v.width,4);Ce.start<=be+1&&Ee===Be&&Je(Ce.start+Ce.count-1,v.width,4)===Ee?he.count=Math.max(he.count,Ce.start+Ce.count-he.start):(++ce,$[ce]=Ce)}$.length=ce+1;const se=r.getParameter(r.UNPACK_ROW_LENGTH),Re=r.getParameter(r.UNPACK_SKIP_PIXELS),Ue=r.getParameter(r.UNPACK_SKIP_ROWS);r.pixelStorei(r.UNPACK_ROW_LENGTH,v.width);for(let re=0,he=$.length;re<he;re++){const Ce=$[re],be=Math.floor(Ce.start/4),Ee=Math.ceil(Ce.count/4),Be=be%v.width,U=Math.floor(be/v.width),fe=Ee,ue=1;r.pixelStorei(r.UNPACK_SKIP_PIXELS,Be),r.pixelStorei(r.UNPACK_SKIP_ROWS,U),t.texSubImage2D(r.TEXTURE_2D,0,Be,U,fe,ue,D,B,v.data)}T.clearUpdateRanges(),r.pixelStorei(r.UNPACK_ROW_LENGTH,se),r.pixelStorei(r.UNPACK_SKIP_PIXELS,Re),r.pixelStorei(r.UNPACK_SKIP_ROWS,Ue)}}function ee(T,v,D){let B=r.TEXTURE_2D;(v.isDataArrayTexture||v.isCompressedArrayTexture)&&(B=r.TEXTURE_2D_ARRAY),v.isData3DTexture&&(B=r.TEXTURE_3D);const j=Oe(T,v),$=v.source;t.bindTexture(B,T.__webglTexture,r.TEXTURE0+D);const ce=n.get($);if($.version!==ce.__version||j===!0){t.activeTexture(r.TEXTURE0+D);const se=ht.getPrimaries(ht.workingColorSpace),Re=v.colorSpace===Gi?null:ht.getPrimaries(v.colorSpace),Ue=v.colorSpace===Gi||se===Re?r.NONE:r.BROWSER_DEFAULT_WEBGL;r.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,v.flipY),r.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,v.premultiplyAlpha),r.pixelStorei(r.UNPACK_ALIGNMENT,v.unpackAlignment),r.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,Ue);let re=g(v.image,!1,i.maxTextureSize);re=Q(v,re);const he=s.convert(v.format,v.colorSpace),Ce=s.convert(v.type);let be=S(v.internalFormat,he,Ce,v.colorSpace,v.isVideoTexture);le(B,v);let Ee;const Be=v.mipmaps,U=v.isVideoTexture!==!0,fe=ce.__version===void 0||j===!0,ue=$.dataReady,_e=A(v,re);if(v.isDepthTexture)be=y(v.format===_r,v.type),fe&&(U?t.texStorage2D(r.TEXTURE_2D,1,be,re.width,re.height):t.texImage2D(r.TEXTURE_2D,0,be,re.width,re.height,0,he,Ce,null));else if(v.isDataTexture)if(Be.length>0){U&&fe&&t.texStorage2D(r.TEXTURE_2D,_e,be,Be[0].width,Be[0].height);for(let ae=0,J=Be.length;ae<J;ae++)Ee=Be[ae],U?ue&&t.texSubImage2D(r.TEXTURE_2D,ae,0,0,Ee.width,Ee.height,he,Ce,Ee.data):t.texImage2D(r.TEXTURE_2D,ae,be,Ee.width,Ee.height,0,he,Ce,Ee.data);v.generateMipmaps=!1}else U?(fe&&t.texStorage2D(r.TEXTURE_2D,_e,be,re.width,re.height),ue&&$e(v,re,he,Ce)):t.texImage2D(r.TEXTURE_2D,0,be,re.width,re.height,0,he,Ce,re.data);else if(v.isCompressedTexture)if(v.isCompressedArrayTexture){U&&fe&&t.texStorage3D(r.TEXTURE_2D_ARRAY,_e,be,Be[0].width,Be[0].height,re.depth);for(let ae=0,J=Be.length;ae<J;ae++)if(Ee=Be[ae],v.format!==qn)if(he!==null)if(U){if(ue)if(v.layerUpdates.size>0){const we=Iu(Ee.width,Ee.height,v.format,v.type);for(const Le of v.layerUpdates){const ft=Ee.data.subarray(Le*we/Ee.data.BYTES_PER_ELEMENT,(Le+1)*we/Ee.data.BYTES_PER_ELEMENT);t.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,ae,0,0,Le,Ee.width,Ee.height,1,he,ft)}v.clearLayerUpdates()}else t.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,ae,0,0,0,Ee.width,Ee.height,re.depth,he,Ee.data)}else t.compressedTexImage3D(r.TEXTURE_2D_ARRAY,ae,be,Ee.width,Ee.height,re.depth,0,Ee.data,0,0);else He("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()");else U?ue&&t.texSubImage3D(r.TEXTURE_2D_ARRAY,ae,0,0,0,Ee.width,Ee.height,re.depth,he,Ce,Ee.data):t.texImage3D(r.TEXTURE_2D_ARRAY,ae,be,Ee.width,Ee.height,re.depth,0,he,Ce,Ee.data)}else{U&&fe&&t.texStorage2D(r.TEXTURE_2D,_e,be,Be[0].width,Be[0].height);for(let ae=0,J=Be.length;ae<J;ae++)Ee=Be[ae],v.format!==qn?he!==null?U?ue&&t.compressedTexSubImage2D(r.TEXTURE_2D,ae,0,0,Ee.width,Ee.height,he,Ee.data):t.compressedTexImage2D(r.TEXTURE_2D,ae,be,Ee.width,Ee.height,0,Ee.data):He("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()"):U?ue&&t.texSubImage2D(r.TEXTURE_2D,ae,0,0,Ee.width,Ee.height,he,Ce,Ee.data):t.texImage2D(r.TEXTURE_2D,ae,be,Ee.width,Ee.height,0,he,Ce,Ee.data)}else if(v.isDataArrayTexture)if(U){if(fe&&t.texStorage3D(r.TEXTURE_2D_ARRAY,_e,be,re.width,re.height,re.depth),ue)if(v.layerUpdates.size>0){const ae=Iu(re.width,re.height,v.format,v.type);for(const J of v.layerUpdates){const we=re.data.subarray(J*ae/re.data.BYTES_PER_ELEMENT,(J+1)*ae/re.data.BYTES_PER_ELEMENT);t.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,J,re.width,re.height,1,he,Ce,we)}v.clearLayerUpdates()}else t.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,0,re.width,re.height,re.depth,he,Ce,re.data)}else t.texImage3D(r.TEXTURE_2D_ARRAY,0,be,re.width,re.height,re.depth,0,he,Ce,re.data);else if(v.isData3DTexture)U?(fe&&t.texStorage3D(r.TEXTURE_3D,_e,be,re.width,re.height,re.depth),ue&&t.texSubImage3D(r.TEXTURE_3D,0,0,0,0,re.width,re.height,re.depth,he,Ce,re.data)):t.texImage3D(r.TEXTURE_3D,0,be,re.width,re.height,re.depth,0,he,Ce,re.data);else if(v.isFramebufferTexture){if(fe)if(U)t.texStorage2D(r.TEXTURE_2D,_e,be,re.width,re.height);else{let ae=re.width,J=re.height;for(let we=0;we<_e;we++)t.texImage2D(r.TEXTURE_2D,we,be,ae,J,0,he,Ce,null),ae>>=1,J>>=1}}else if(Be.length>0){if(U&&fe){const ae=Z(Be[0]);t.texStorage2D(r.TEXTURE_2D,_e,be,ae.width,ae.height)}for(let ae=0,J=Be.length;ae<J;ae++)Ee=Be[ae],U?ue&&t.texSubImage2D(r.TEXTURE_2D,ae,0,0,he,Ce,Ee):t.texImage2D(r.TEXTURE_2D,ae,be,he,Ce,Ee);v.generateMipmaps=!1}else if(U){if(fe){const ae=Z(re);t.texStorage2D(r.TEXTURE_2D,_e,be,ae.width,ae.height)}ue&&t.texSubImage2D(r.TEXTURE_2D,0,0,0,he,Ce,re)}else t.texImage2D(r.TEXTURE_2D,0,be,he,Ce,re);p(v)&&m(B),ce.__version=$.version,v.onUpdate&&v.onUpdate(v)}T.__version=v.version}function de(T,v,D){if(v.image.length!==6)return;const B=Oe(T,v),j=v.source;t.bindTexture(r.TEXTURE_CUBE_MAP,T.__webglTexture,r.TEXTURE0+D);const $=n.get(j);if(j.version!==$.__version||B===!0){t.activeTexture(r.TEXTURE0+D);const ce=ht.getPrimaries(ht.workingColorSpace),se=v.colorSpace===Gi?null:ht.getPrimaries(v.colorSpace),Re=v.colorSpace===Gi||ce===se?r.NONE:r.BROWSER_DEFAULT_WEBGL;r.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,v.flipY),r.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,v.premultiplyAlpha),r.pixelStorei(r.UNPACK_ALIGNMENT,v.unpackAlignment),r.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,Re);const Ue=v.isCompressedTexture||v.image[0].isCompressedTexture,re=v.image[0]&&v.image[0].isDataTexture,he=[];for(let J=0;J<6;J++)!Ue&&!re?he[J]=g(v.image[J],!0,i.maxCubemapSize):he[J]=re?v.image[J].image:v.image[J],he[J]=Q(v,he[J]);const Ce=he[0],be=s.convert(v.format,v.colorSpace),Ee=s.convert(v.type),Be=S(v.internalFormat,be,Ee,v.colorSpace),U=v.isVideoTexture!==!0,fe=$.__version===void 0||B===!0,ue=j.dataReady;let _e=A(v,Ce);le(r.TEXTURE_CUBE_MAP,v);let ae;if(Ue){U&&fe&&t.texStorage2D(r.TEXTURE_CUBE_MAP,_e,Be,Ce.width,Ce.height);for(let J=0;J<6;J++){ae=he[J].mipmaps;for(let we=0;we<ae.length;we++){const Le=ae[we];v.format!==qn?be!==null?U?ue&&t.compressedTexSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we,0,0,Le.width,Le.height,be,Le.data):t.compressedTexImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we,Be,Le.width,Le.height,0,Le.data):He("WebGLRenderer: Attempt to load unsupported compressed texture format in .setTextureCube()"):U?ue&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we,0,0,Le.width,Le.height,be,Ee,Le.data):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we,Be,Le.width,Le.height,0,be,Ee,Le.data)}}}else{if(ae=v.mipmaps,U&&fe){ae.length>0&&_e++;const J=Z(he[0]);t.texStorage2D(r.TEXTURE_CUBE_MAP,_e,Be,J.width,J.height)}for(let J=0;J<6;J++)if(re){U?ue&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,0,0,0,he[J].width,he[J].height,be,Ee,he[J].data):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,0,Be,he[J].width,he[J].height,0,be,Ee,he[J].data);for(let we=0;we<ae.length;we++){const ft=ae[we].image[J].image;U?ue&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we+1,0,0,ft.width,ft.height,be,Ee,ft.data):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we+1,Be,ft.width,ft.height,0,be,Ee,ft.data)}}else{U?ue&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,0,0,0,be,Ee,he[J]):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,0,Be,be,Ee,he[J]);for(let we=0;we<ae.length;we++){const Le=ae[we];U?ue&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we+1,0,0,be,Ee,Le.image[J]):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+J,we+1,Be,be,Ee,Le.image[J])}}}p(v)&&m(r.TEXTURE_CUBE_MAP),$.__version=j.version,v.onUpdate&&v.onUpdate(v)}T.__version=v.version}function pe(T,v,D,B,j,$){const ce=s.convert(D.format,D.colorSpace),se=s.convert(D.type),Re=S(D.internalFormat,ce,se,D.colorSpace),Ue=n.get(v),re=n.get(D);if(re.__renderTarget=v,!Ue.__hasExternalTextures){const he=Math.max(1,v.width>>$),Ce=Math.max(1,v.height>>$);j===r.TEXTURE_3D||j===r.TEXTURE_2D_ARRAY?t.texImage3D(j,$,Re,he,Ce,v.depth,0,ce,se,null):t.texImage2D(j,$,Re,he,Ce,0,ce,se,null)}t.bindFramebuffer(r.FRAMEBUFFER,T),yt(v)?o.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,B,j,re.__webglTexture,0,I(v)):(j===r.TEXTURE_2D||j>=r.TEXTURE_CUBE_MAP_POSITIVE_X&&j<=r.TEXTURE_CUBE_MAP_NEGATIVE_Z)&&r.framebufferTexture2D(r.FRAMEBUFFER,B,j,re.__webglTexture,$),t.bindFramebuffer(r.FRAMEBUFFER,null)}function Ve(T,v,D){if(r.bindRenderbuffer(r.RENDERBUFFER,T),v.depthBuffer){const B=v.depthTexture,j=B&&B.isDepthTexture?B.type:null,$=y(v.stencilBuffer,j),ce=v.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;yt(v)?o.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,I(v),$,v.width,v.height):D?r.renderbufferStorageMultisample(r.RENDERBUFFER,I(v),$,v.width,v.height):r.renderbufferStorage(r.RENDERBUFFER,$,v.width,v.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,ce,r.RENDERBUFFER,T)}else{const B=v.textures;for(let j=0;j<B.length;j++){const $=B[j],ce=s.convert($.format,$.colorSpace),se=s.convert($.type),Re=S($.internalFormat,ce,se,$.colorSpace);yt(v)?o.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,I(v),Re,v.width,v.height):D?r.renderbufferStorageMultisample(r.RENDERBUFFER,I(v),Re,v.width,v.height):r.renderbufferStorage(r.RENDERBUFFER,Re,v.width,v.height)}}r.bindRenderbuffer(r.RENDERBUFFER,null)}function Ne(T,v,D){const B=v.isWebGLCubeRenderTarget===!0;if(t.bindFramebuffer(r.FRAMEBUFFER,T),!(v.depthTexture&&v.depthTexture.isDepthTexture))throw new Error("renderTarget.depthTexture must be an instance of THREE.DepthTexture");const j=n.get(v.depthTexture);if(j.__renderTarget=v,(!j.__webglTexture||v.depthTexture.image.width!==v.width||v.depthTexture.image.height!==v.height)&&(v.depthTexture.image.width=v.width,v.depthTexture.image.height=v.height,v.depthTexture.needsUpdate=!0),B){if(j.__webglInit===void 0&&(j.__webglInit=!0,v.depthTexture.addEventListener("dispose",w)),j.__webglTexture===void 0){j.__webglTexture=r.createTexture(),t.bindTexture(r.TEXTURE_CUBE_MAP,j.__webglTexture),le(r.TEXTURE_CUBE_MAP,v.depthTexture);const Ue=s.convert(v.depthTexture.format),re=s.convert(v.depthTexture.type);let he;v.depthTexture.format===Ai?he=r.DEPTH_COMPONENT24:v.depthTexture.format===_r&&(he=r.DEPTH24_STENCIL8);for(let Ce=0;Ce<6;Ce++)r.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Ce,0,he,v.width,v.height,0,Ue,re,null)}}else H(v.depthTexture,0);const $=j.__webglTexture,ce=I(v),se=B?r.TEXTURE_CUBE_MAP_POSITIVE_X+D:r.TEXTURE_2D,Re=v.depthTexture.format===_r?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;if(v.depthTexture.format===Ai)yt(v)?o.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,Re,se,$,0,ce):r.framebufferTexture2D(r.FRAMEBUFFER,Re,se,$,0);else if(v.depthTexture.format===_r)yt(v)?o.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,Re,se,$,0,ce):r.framebufferTexture2D(r.FRAMEBUFFER,Re,se,$,0);else throw new Error("Unknown depthTexture format")}function Fe(T){const v=n.get(T),D=T.isWebGLCubeRenderTarget===!0;if(v.__boundDepthTexture!==T.depthTexture){const B=T.depthTexture;if(v.__depthDisposeCallback&&v.__depthDisposeCallback(),B){const j=()=>{delete v.__boundDepthTexture,delete v.__depthDisposeCallback,B.removeEventListener("dispose",j)};B.addEventListener("dispose",j),v.__depthDisposeCallback=j}v.__boundDepthTexture=B}if(T.depthTexture&&!v.__autoAllocateDepthBuffer)if(D)for(let B=0;B<6;B++)Ne(v.__webglFramebuffer[B],T,B);else{const B=T.texture.mipmaps;B&&B.length>0?Ne(v.__webglFramebuffer[0],T,0):Ne(v.__webglFramebuffer,T,0)}else if(D){v.__webglDepthbuffer=[];for(let B=0;B<6;B++)if(t.bindFramebuffer(r.FRAMEBUFFER,v.__webglFramebuffer[B]),v.__webglDepthbuffer[B]===void 0)v.__webglDepthbuffer[B]=r.createRenderbuffer(),Ve(v.__webglDepthbuffer[B],T,!1);else{const j=T.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,$=v.__webglDepthbuffer[B];r.bindRenderbuffer(r.RENDERBUFFER,$),r.framebufferRenderbuffer(r.FRAMEBUFFER,j,r.RENDERBUFFER,$)}}else{const B=T.texture.mipmaps;if(B&&B.length>0?t.bindFramebuffer(r.FRAMEBUFFER,v.__webglFramebuffer[0]):t.bindFramebuffer(r.FRAMEBUFFER,v.__webglFramebuffer),v.__webglDepthbuffer===void 0)v.__webglDepthbuffer=r.createRenderbuffer(),Ve(v.__webglDepthbuffer,T,!1);else{const j=T.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,$=v.__webglDepthbuffer;r.bindRenderbuffer(r.RENDERBUFFER,$),r.framebufferRenderbuffer(r.FRAMEBUFFER,j,r.RENDERBUFFER,$)}}t.bindFramebuffer(r.FRAMEBUFFER,null)}function Tt(T,v,D){const B=n.get(T);v!==void 0&&pe(B.__webglFramebuffer,T,T.texture,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,0),D!==void 0&&Fe(T)}function Qe(T){const v=T.texture,D=n.get(T),B=n.get(v);T.addEventListener("dispose",C);const j=T.textures,$=T.isWebGLCubeRenderTarget===!0,ce=j.length>1;if(ce||(B.__webglTexture===void 0&&(B.__webglTexture=r.createTexture()),B.__version=v.version,a.memory.textures++),$){D.__webglFramebuffer=[];for(let se=0;se<6;se++)if(v.mipmaps&&v.mipmaps.length>0){D.__webglFramebuffer[se]=[];for(let Re=0;Re<v.mipmaps.length;Re++)D.__webglFramebuffer[se][Re]=r.createFramebuffer()}else D.__webglFramebuffer[se]=r.createFramebuffer()}else{if(v.mipmaps&&v.mipmaps.length>0){D.__webglFramebuffer=[];for(let se=0;se<v.mipmaps.length;se++)D.__webglFramebuffer[se]=r.createFramebuffer()}else D.__webglFramebuffer=r.createFramebuffer();if(ce)for(let se=0,Re=j.length;se<Re;se++){const Ue=n.get(j[se]);Ue.__webglTexture===void 0&&(Ue.__webglTexture=r.createTexture(),a.memory.textures++)}if(T.samples>0&&yt(T)===!1){D.__webglMultisampledFramebuffer=r.createFramebuffer(),D.__webglColorRenderbuffer=[],t.bindFramebuffer(r.FRAMEBUFFER,D.__webglMultisampledFramebuffer);for(let se=0;se<j.length;se++){const Re=j[se];D.__webglColorRenderbuffer[se]=r.createRenderbuffer(),r.bindRenderbuffer(r.RENDERBUFFER,D.__webglColorRenderbuffer[se]);const Ue=s.convert(Re.format,Re.colorSpace),re=s.convert(Re.type),he=S(Re.internalFormat,Ue,re,Re.colorSpace,T.isXRRenderTarget===!0),Ce=I(T);r.renderbufferStorageMultisample(r.RENDERBUFFER,Ce,he,T.width,T.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+se,r.RENDERBUFFER,D.__webglColorRenderbuffer[se])}r.bindRenderbuffer(r.RENDERBUFFER,null),T.depthBuffer&&(D.__webglDepthRenderbuffer=r.createRenderbuffer(),Ve(D.__webglDepthRenderbuffer,T,!0)),t.bindFramebuffer(r.FRAMEBUFFER,null)}}if($){t.bindTexture(r.TEXTURE_CUBE_MAP,B.__webglTexture),le(r.TEXTURE_CUBE_MAP,v);for(let se=0;se<6;se++)if(v.mipmaps&&v.mipmaps.length>0)for(let Re=0;Re<v.mipmaps.length;Re++)pe(D.__webglFramebuffer[se][Re],T,v,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+se,Re);else pe(D.__webglFramebuffer[se],T,v,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+se,0);p(v)&&m(r.TEXTURE_CUBE_MAP),t.unbindTexture()}else if(ce){for(let se=0,Re=j.length;se<Re;se++){const Ue=j[se],re=n.get(Ue);let he=r.TEXTURE_2D;(T.isWebGL3DRenderTarget||T.isWebGLArrayRenderTarget)&&(he=T.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),t.bindTexture(he,re.__webglTexture),le(he,Ue),pe(D.__webglFramebuffer,T,Ue,r.COLOR_ATTACHMENT0+se,he,0),p(Ue)&&m(he)}t.unbindTexture()}else{let se=r.TEXTURE_2D;if((T.isWebGL3DRenderTarget||T.isWebGLArrayRenderTarget)&&(se=T.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),t.bindTexture(se,B.__webglTexture),le(se,v),v.mipmaps&&v.mipmaps.length>0)for(let Re=0;Re<v.mipmaps.length;Re++)pe(D.__webglFramebuffer[Re],T,v,r.COLOR_ATTACHMENT0,se,Re);else pe(D.__webglFramebuffer,T,v,r.COLOR_ATTACHMENT0,se,0);p(v)&&m(se),t.unbindTexture()}T.depthBuffer&&Fe(T)}function ot(T){const v=T.textures;for(let D=0,B=v.length;D<B;D++){const j=v[D];if(p(j)){const $=M(T),ce=n.get(j).__webglTexture;t.bindTexture($,ce),m($),t.unbindTexture()}}}const pt=[],Ye=[];function vt(T){if(T.samples>0){if(yt(T)===!1){const v=T.textures,D=T.width,B=T.height;let j=r.COLOR_BUFFER_BIT;const $=T.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,ce=n.get(T),se=v.length>1;if(se)for(let Ue=0;Ue<v.length;Ue++)t.bindFramebuffer(r.FRAMEBUFFER,ce.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+Ue,r.RENDERBUFFER,null),t.bindFramebuffer(r.FRAMEBUFFER,ce.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+Ue,r.TEXTURE_2D,null,0);t.bindFramebuffer(r.READ_FRAMEBUFFER,ce.__webglMultisampledFramebuffer);const Re=T.texture.mipmaps;Re&&Re.length>0?t.bindFramebuffer(r.DRAW_FRAMEBUFFER,ce.__webglFramebuffer[0]):t.bindFramebuffer(r.DRAW_FRAMEBUFFER,ce.__webglFramebuffer);for(let Ue=0;Ue<v.length;Ue++){if(T.resolveDepthBuffer&&(T.depthBuffer&&(j|=r.DEPTH_BUFFER_BIT),T.stencilBuffer&&T.resolveStencilBuffer&&(j|=r.STENCIL_BUFFER_BIT)),se){r.framebufferRenderbuffer(r.READ_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.RENDERBUFFER,ce.__webglColorRenderbuffer[Ue]);const re=n.get(v[Ue]).__webglTexture;r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,re,0)}r.blitFramebuffer(0,0,D,B,0,0,D,B,j,r.NEAREST),l===!0&&(pt.length=0,Ye.length=0,pt.push(r.COLOR_ATTACHMENT0+Ue),T.depthBuffer&&T.resolveDepthBuffer===!1&&(pt.push($),Ye.push($),r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,Ye)),r.invalidateFramebuffer(r.READ_FRAMEBUFFER,pt))}if(t.bindFramebuffer(r.READ_FRAMEBUFFER,null),t.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),se)for(let Ue=0;Ue<v.length;Ue++){t.bindFramebuffer(r.FRAMEBUFFER,ce.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+Ue,r.RENDERBUFFER,ce.__webglColorRenderbuffer[Ue]);const re=n.get(v[Ue]).__webglTexture;t.bindFramebuffer(r.FRAMEBUFFER,ce.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+Ue,r.TEXTURE_2D,re,0)}t.bindFramebuffer(r.DRAW_FRAMEBUFFER,ce.__webglMultisampledFramebuffer)}else if(T.depthBuffer&&T.resolveDepthBuffer===!1&&l){const v=T.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,[v])}}}function I(T){return Math.min(i.maxSamples,T.samples)}function yt(T){const v=n.get(T);return T.samples>0&&e.has("WEBGL_multisampled_render_to_texture")===!0&&v.__useRenderToTexture!==!1}function rt(T){const v=a.render.frame;u.get(T)!==v&&(u.set(T,v),T.update())}function Q(T,v){const D=T.colorSpace,B=T.format,j=T.type;return T.isCompressedTexture===!0||T.isVideoTexture===!0||D!==is&&D!==Gi&&(ht.getTransfer(D)===_t?(B!==qn||j!==Rn)&&He("WebGLTextures: sRGB encoded textures have to use RGBAFormat and UnsignedByteType."):ut("WebGLTextures: Unsupported texture color space:",D)),v}function Z(T){return typeof HTMLImageElement<"u"&&T instanceof HTMLImageElement?(c.width=T.naturalWidth||T.width,c.height=T.naturalHeight||T.height):typeof VideoFrame<"u"&&T instanceof VideoFrame?(c.width=T.displayWidth,c.height=T.displayHeight):(c.width=T.width,c.height=T.height),c}this.allocateTextureUnit=z,this.resetTextureUnits=G,this.setTexture2D=H,this.setTexture2DArray=N,this.setTexture3D=F,this.setTextureCube=ne,this.rebindTextures=Tt,this.setupRenderTarget=Qe,this.updateRenderTargetMipmap=ot,this.updateMultisampleRenderTarget=vt,this.setupDepthRenderbuffer=Fe,this.setupFrameBufferTexture=pe,this.useMultisampledRTT=yt,this.isReversedDepthBuffer=function(){return t.buffers.depth.getReversed()}}function Pv(r,e){function t(n,i=Gi){let s;const a=ht.getTransfer(i);if(n===Rn)return r.UNSIGNED_BYTE;if(n===mc)return r.UNSIGNED_SHORT_4_4_4_4;if(n===_c)return r.UNSIGNED_SHORT_5_5_5_1;if(n===kh)return r.UNSIGNED_INT_5_9_9_9_REV;if(n===zh)return r.UNSIGNED_INT_10F_11F_11F_REV;if(n===Oh)return r.BYTE;if(n===Bh)return r.SHORT;if(n===Ns)return r.UNSIGNED_SHORT;if(n===pc)return r.INT;if(n===oi)return r.UNSIGNED_INT;if(n===ni)return r.FLOAT;if(n===Ti)return r.HALF_FLOAT;if(n===Vh)return r.ALPHA;if(n===Gh)return r.RGB;if(n===qn)return r.RGBA;if(n===Ai)return r.DEPTH_COMPONENT;if(n===_r)return r.DEPTH_STENCIL;if(n===Hh)return r.RED;if(n===gc)return r.RED_INTEGER;if(n===ns)return r.RG;if(n===vc)return r.RG_INTEGER;if(n===xc)return r.RGBA_INTEGER;if(n===La||n===Ia||n===Ua||n===Na)if(a===_t)if(s=e.get("WEBGL_compressed_texture_s3tc_srgb"),s!==null){if(n===La)return s.COMPRESSED_SRGB_S3TC_DXT1_EXT;if(n===Ia)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;if(n===Ua)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT;if(n===Na)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT}else return null;else if(s=e.get("WEBGL_compressed_texture_s3tc"),s!==null){if(n===La)return s.COMPRESSED_RGB_S3TC_DXT1_EXT;if(n===Ia)return s.COMPRESSED_RGBA_S3TC_DXT1_EXT;if(n===Ua)return s.COMPRESSED_RGBA_S3TC_DXT3_EXT;if(n===Na)return s.COMPRESSED_RGBA_S3TC_DXT5_EXT}else return null;if(n===fl||n===dl||n===pl||n===ml)if(s=e.get("WEBGL_compressed_texture_pvrtc"),s!==null){if(n===fl)return s.COMPRESSED_RGB_PVRTC_4BPPV1_IMG;if(n===dl)return s.COMPRESSED_RGB_PVRTC_2BPPV1_IMG;if(n===pl)return s.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;if(n===ml)return s.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG}else return null;if(n===_l||n===gl||n===vl||n===xl||n===Ml||n===Sl||n===yl)if(s=e.get("WEBGL_compressed_texture_etc"),s!==null){if(n===_l||n===gl)return a===_t?s.COMPRESSED_SRGB8_ETC2:s.COMPRESSED_RGB8_ETC2;if(n===vl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:s.COMPRESSED_RGBA8_ETC2_EAC;if(n===xl)return s.COMPRESSED_R11_EAC;if(n===Ml)return s.COMPRESSED_SIGNED_R11_EAC;if(n===Sl)return s.COMPRESSED_RG11_EAC;if(n===yl)return s.COMPRESSED_SIGNED_RG11_EAC}else return null;if(n===bl||n===El||n===Tl||n===Al||n===wl||n===Rl||n===Cl||n===Pl||n===Dl||n===Ll||n===Il||n===Ul||n===Nl||n===Fl)if(s=e.get("WEBGL_compressed_texture_astc"),s!==null){if(n===bl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR:s.COMPRESSED_RGBA_ASTC_4x4_KHR;if(n===El)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR:s.COMPRESSED_RGBA_ASTC_5x4_KHR;if(n===Tl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR:s.COMPRESSED_RGBA_ASTC_5x5_KHR;if(n===Al)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR:s.COMPRESSED_RGBA_ASTC_6x5_KHR;if(n===wl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR:s.COMPRESSED_RGBA_ASTC_6x6_KHR;if(n===Rl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR:s.COMPRESSED_RGBA_ASTC_8x5_KHR;if(n===Cl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR:s.COMPRESSED_RGBA_ASTC_8x6_KHR;if(n===Pl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR:s.COMPRESSED_RGBA_ASTC_8x8_KHR;if(n===Dl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR:s.COMPRESSED_RGBA_ASTC_10x5_KHR;if(n===Ll)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR:s.COMPRESSED_RGBA_ASTC_10x6_KHR;if(n===Il)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR:s.COMPRESSED_RGBA_ASTC_10x8_KHR;if(n===Ul)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR:s.COMPRESSED_RGBA_ASTC_10x10_KHR;if(n===Nl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR:s.COMPRESSED_RGBA_ASTC_12x10_KHR;if(n===Fl)return a===_t?s.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR:s.COMPRESSED_RGBA_ASTC_12x12_KHR}else return null;if(n===Ol||n===Bl||n===kl)if(s=e.get("EXT_texture_compression_bptc"),s!==null){if(n===Ol)return a===_t?s.COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT:s.COMPRESSED_RGBA_BPTC_UNORM_EXT;if(n===Bl)return s.COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT;if(n===kl)return s.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT}else return null;if(n===zl||n===Vl||n===Gl||n===Hl)if(s=e.get("EXT_texture_compression_rgtc"),s!==null){if(n===zl)return s.COMPRESSED_RED_RGTC1_EXT;if(n===Vl)return s.COMPRESSED_SIGNED_RED_RGTC1_EXT;if(n===Gl)return s.COMPRESSED_RED_GREEN_RGTC2_EXT;if(n===Hl)return s.COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT}else return null;return n===Fs?r.UNSIGNED_INT_24_8:r[n]!==void 0?r[n]:null}return{convert:t}}const Dv=`
void main() {

	gl_Position = vec4( position, 1.0 );

}`,Lv=`
uniform sampler2DArray depthColor;
uniform float depthWidth;
uniform float depthHeight;

void main() {

	vec2 coord = vec2( gl_FragCoord.x / depthWidth, gl_FragCoord.y / depthHeight );

	if ( coord.x >= 1.0 ) {

		gl_FragDepth = texture( depthColor, vec3( coord.x - 1.0, coord.y, 1 ) ).r;

	} else {

		gl_FragDepth = texture( depthColor, vec3( coord.x, coord.y, 0 ) ).r;

	}

}`;class Iv{constructor(){this.texture=null,this.mesh=null,this.depthNear=0,this.depthFar=0}init(e,t){if(this.texture===null){const n=new Jh(e.texture);(e.depthNear!==t.depthNear||e.depthFar!==t.depthFar)&&(this.depthNear=e.depthNear,this.depthFar=e.depthFar),this.texture=n}}getMesh(e){if(this.texture!==null&&this.mesh===null){const t=e.cameras[0].viewport,n=new ci({vertexShader:Dv,fragmentShader:Lv,uniforms:{depthColor:{value:this.texture},depthWidth:{value:t.z},depthHeight:{value:t.w}}});this.mesh=new Xt(new Ks(20,20),n)}return this.mesh}reset(){this.texture=null,this.mesh=null}getDepthTexture(){return this.texture}}class Uv extends br{constructor(e,t){super();const n=this;let i=null,s=1,a=null,o="local-floor",l=1,c=null,u=null,f=null,h=null,d=null,_=null;const g=typeof XRWebGLBinding<"u",p=new Iv,m={},M=t.getContextAttributes();let S=null,y=null;const A=[],w=[],C=new qe;let x=null;const b=new wn;b.viewport=new Pt;const W=new wn;W.viewport=new Pt;const L=[b,W],G=new Hp;let z=null,q=null;this.cameraAutoUpdate=!0,this.enabled=!1,this.isPresenting=!1,this.getController=function(ee){let de=A[ee];return de===void 0&&(de=new xo,A[ee]=de),de.getTargetRaySpace()},this.getControllerGrip=function(ee){let de=A[ee];return de===void 0&&(de=new xo,A[ee]=de),de.getGripSpace()},this.getHand=function(ee){let de=A[ee];return de===void 0&&(de=new xo,A[ee]=de),de.getHandSpace()};function H(ee){const de=w.indexOf(ee.inputSource);if(de===-1)return;const pe=A[de];pe!==void 0&&(pe.update(ee.inputSource,ee.frame,c||a),pe.dispatchEvent({type:ee.type,data:ee.inputSource}))}function N(){i.removeEventListener("select",H),i.removeEventListener("selectstart",H),i.removeEventListener("selectend",H),i.removeEventListener("squeeze",H),i.removeEventListener("squeezestart",H),i.removeEventListener("squeezeend",H),i.removeEventListener("end",N),i.removeEventListener("inputsourceschange",F);for(let ee=0;ee<A.length;ee++){const de=w[ee];de!==null&&(w[ee]=null,A[ee].disconnect(de))}z=null,q=null,p.reset();for(const ee in m)delete m[ee];e.setRenderTarget(S),d=null,h=null,f=null,i=null,y=null,$e.stop(),n.isPresenting=!1,e.setPixelRatio(x),e.setSize(C.width,C.height,!1),n.dispatchEvent({type:"sessionend"})}this.setFramebufferScaleFactor=function(ee){s=ee,n.isPresenting===!0&&He("WebXRManager: Cannot change framebuffer scale while presenting.")},this.setReferenceSpaceType=function(ee){o=ee,n.isPresenting===!0&&He("WebXRManager: Cannot change reference space type while presenting.")},this.getReferenceSpace=function(){return c||a},this.setReferenceSpace=function(ee){c=ee},this.getBaseLayer=function(){return h!==null?h:d},this.getBinding=function(){return f===null&&g&&(f=new XRWebGLBinding(i,t)),f},this.getFrame=function(){return _},this.getSession=function(){return i},this.setSession=async function(ee){if(i=ee,i!==null){if(S=e.getRenderTarget(),i.addEventListener("select",H),i.addEventListener("selectstart",H),i.addEventListener("selectend",H),i.addEventListener("squeeze",H),i.addEventListener("squeezestart",H),i.addEventListener("squeezeend",H),i.addEventListener("end",N),i.addEventListener("inputsourceschange",F),M.xrCompatible!==!0&&await t.makeXRCompatible(),x=e.getPixelRatio(),e.getSize(C),g&&"createProjectionLayer"in XRWebGLBinding.prototype){let pe=null,Ve=null,Ne=null;M.depth&&(Ne=M.stencil?t.DEPTH24_STENCIL8:t.DEPTH_COMPONENT24,pe=M.stencil?_r:Ai,Ve=M.stencil?Fs:oi);const Fe={colorFormat:t.RGBA8,depthFormat:Ne,scaleFactor:s};f=this.getBinding(),h=f.createProjectionLayer(Fe),i.updateRenderState({layers:[h]}),e.setPixelRatio(1),e.setSize(h.textureWidth,h.textureHeight,!1),y=new ai(h.textureWidth,h.textureHeight,{format:qn,type:Rn,depthTexture:new Bs(h.textureWidth,h.textureHeight,Ve,void 0,void 0,void 0,void 0,void 0,void 0,pe),stencilBuffer:M.stencil,colorSpace:e.outputColorSpace,samples:M.antialias?4:0,resolveDepthBuffer:h.ignoreDepthValues===!1,resolveStencilBuffer:h.ignoreDepthValues===!1})}else{const pe={antialias:M.antialias,alpha:!0,depth:M.depth,stencil:M.stencil,framebufferScaleFactor:s};d=new XRWebGLLayer(i,t,pe),i.updateRenderState({baseLayer:d}),e.setPixelRatio(1),e.setSize(d.framebufferWidth,d.framebufferHeight,!1),y=new ai(d.framebufferWidth,d.framebufferHeight,{format:qn,type:Rn,colorSpace:e.outputColorSpace,stencilBuffer:M.stencil,resolveDepthBuffer:d.ignoreDepthValues===!1,resolveStencilBuffer:d.ignoreDepthValues===!1})}y.isXRRenderTarget=!0,this.setFoveation(l),c=null,a=await i.requestReferenceSpace(o),$e.setContext(i),$e.start(),n.isPresenting=!0,n.dispatchEvent({type:"sessionstart"})}},this.getEnvironmentBlendMode=function(){if(i!==null)return i.environmentBlendMode},this.getDepthTexture=function(){return p.getDepthTexture()};function F(ee){for(let de=0;de<ee.removed.length;de++){const pe=ee.removed[de],Ve=w.indexOf(pe);Ve>=0&&(w[Ve]=null,A[Ve].disconnect(pe))}for(let de=0;de<ee.added.length;de++){const pe=ee.added[de];let Ve=w.indexOf(pe);if(Ve===-1){for(let Fe=0;Fe<A.length;Fe++)if(Fe>=w.length){w.push(pe),Ve=Fe;break}else if(w[Fe]===null){w[Fe]=pe,Ve=Fe;break}if(Ve===-1)break}const Ne=A[Ve];Ne&&Ne.connect(pe)}}const ne=new V,ie=new V;function me(ee,de,pe){ne.setFromMatrixPosition(de.matrixWorld),ie.setFromMatrixPosition(pe.matrixWorld);const Ve=ne.distanceTo(ie),Ne=de.projectionMatrix.elements,Fe=pe.projectionMatrix.elements,Tt=Ne[14]/(Ne[10]-1),Qe=Ne[14]/(Ne[10]+1),ot=(Ne[9]+1)/Ne[5],pt=(Ne[9]-1)/Ne[5],Ye=(Ne[8]-1)/Ne[0],vt=(Fe[8]+1)/Fe[0],I=Tt*Ye,yt=Tt*vt,rt=Ve/(-Ye+vt),Q=rt*-Ye;if(de.matrixWorld.decompose(ee.position,ee.quaternion,ee.scale),ee.translateX(Q),ee.translateZ(rt),ee.matrixWorld.compose(ee.position,ee.quaternion,ee.scale),ee.matrixWorldInverse.copy(ee.matrixWorld).invert(),Ne[10]===-1)ee.projectionMatrix.copy(de.projectionMatrix),ee.projectionMatrixInverse.copy(de.projectionMatrixInverse);else{const Z=Tt+rt,T=Qe+rt,v=I-Q,D=yt+(Ve-Q),B=ot*Qe/T*Z,j=pt*Qe/T*Z;ee.projectionMatrix.makePerspective(v,D,B,j,Z,T),ee.projectionMatrixInverse.copy(ee.projectionMatrix).invert()}}function Te(ee,de){de===null?ee.matrixWorld.copy(ee.matrix):ee.matrixWorld.multiplyMatrices(de.matrixWorld,ee.matrix),ee.matrixWorldInverse.copy(ee.matrixWorld).invert()}this.updateCamera=function(ee){if(i===null)return;let de=ee.near,pe=ee.far;p.texture!==null&&(p.depthNear>0&&(de=p.depthNear),p.depthFar>0&&(pe=p.depthFar)),G.near=W.near=b.near=de,G.far=W.far=b.far=pe,(z!==G.near||q!==G.far)&&(i.updateRenderState({depthNear:G.near,depthFar:G.far}),z=G.near,q=G.far),G.layers.mask=ee.layers.mask|6,b.layers.mask=G.layers.mask&-5,W.layers.mask=G.layers.mask&-3;const Ve=ee.parent,Ne=G.cameras;Te(G,Ve);for(let Fe=0;Fe<Ne.length;Fe++)Te(Ne[Fe],Ve);Ne.length===2?me(G,b,W):G.projectionMatrix.copy(b.projectionMatrix),le(ee,G,Ve)};function le(ee,de,pe){pe===null?ee.matrix.copy(de.matrixWorld):(ee.matrix.copy(pe.matrixWorld),ee.matrix.invert(),ee.matrix.multiply(de.matrixWorld)),ee.matrix.decompose(ee.position,ee.quaternion,ee.scale),ee.updateMatrixWorld(!0),ee.projectionMatrix.copy(de.projectionMatrix),ee.projectionMatrixInverse.copy(de.projectionMatrixInverse),ee.isPerspectiveCamera&&(ee.fov=Wl*2*Math.atan(1/ee.projectionMatrix.elements[5]),ee.zoom=1)}this.getCamera=function(){return G},this.getFoveation=function(){if(!(h===null&&d===null))return l},this.setFoveation=function(ee){l=ee,h!==null&&(h.fixedFoveation=ee),d!==null&&d.fixedFoveation!==void 0&&(d.fixedFoveation=ee)},this.hasDepthSensing=function(){return p.texture!==null},this.getDepthSensingMesh=function(){return p.getMesh(G)},this.getCameraTexture=function(ee){return m[ee]};let Oe=null;function Je(ee,de){if(u=de.getViewerPose(c||a),_=de,u!==null){const pe=u.views;d!==null&&(e.setRenderTargetFramebuffer(y,d.framebuffer),e.setRenderTarget(y));let Ve=!1;pe.length!==G.cameras.length&&(G.cameras.length=0,Ve=!0);for(let Qe=0;Qe<pe.length;Qe++){const ot=pe[Qe];let pt=null;if(d!==null)pt=d.getViewport(ot);else{const vt=f.getViewSubImage(h,ot);pt=vt.viewport,Qe===0&&(e.setRenderTargetTextures(y,vt.colorTexture,vt.depthStencilTexture),e.setRenderTarget(y))}let Ye=L[Qe];Ye===void 0&&(Ye=new wn,Ye.layers.enable(Qe),Ye.viewport=new Pt,L[Qe]=Ye),Ye.matrix.fromArray(ot.transform.matrix),Ye.matrix.decompose(Ye.position,Ye.quaternion,Ye.scale),Ye.projectionMatrix.fromArray(ot.projectionMatrix),Ye.projectionMatrixInverse.copy(Ye.projectionMatrix).invert(),Ye.viewport.set(pt.x,pt.y,pt.width,pt.height),Qe===0&&(G.matrix.copy(Ye.matrix),G.matrix.decompose(G.position,G.quaternion,G.scale)),Ve===!0&&G.cameras.push(Ye)}const Ne=i.enabledFeatures;if(Ne&&Ne.includes("depth-sensing")&&i.depthUsage=="gpu-optimized"&&g){f=n.getBinding();const Qe=f.getDepthInformation(pe[0]);Qe&&Qe.isValid&&Qe.texture&&p.init(Qe,i.renderState)}if(Ne&&Ne.includes("camera-access")&&g){e.state.unbindTexture(),f=n.getBinding();for(let Qe=0;Qe<pe.length;Qe++){const ot=pe[Qe].camera;if(ot){let pt=m[ot];pt||(pt=new Jh,m[ot]=pt);const Ye=f.getCameraImage(ot);pt.sourceTexture=Ye}}}}for(let pe=0;pe<A.length;pe++){const Ve=w[pe],Ne=A[pe];Ve!==null&&Ne!==void 0&&Ne.update(Ve,de,c||a)}Oe&&Oe(ee,de),de.detectedPlanes&&n.dispatchEvent({type:"planesdetected",data:de}),_=null}const $e=new nf;$e.setAnimationLoop(Je),this.setAnimationLoop=function(ee){Oe=ee},this.dispose=function(){}}}const cr=new li,Nv=new St;function Fv(r,e){function t(p,m){p.matrixAutoUpdate===!0&&p.updateMatrix(),m.value.copy(p.matrix)}function n(p,m){m.color.getRGB(p.fogColor.value,Qh(r)),m.isFog?(p.fogNear.value=m.near,p.fogFar.value=m.far):m.isFogExp2&&(p.fogDensity.value=m.density)}function i(p,m,M,S,y){m.isMeshBasicMaterial?s(p,m):m.isMeshLambertMaterial?(s(p,m),m.envMap&&(p.envMapIntensity.value=m.envMapIntensity)):m.isMeshToonMaterial?(s(p,m),f(p,m)):m.isMeshPhongMaterial?(s(p,m),u(p,m),m.envMap&&(p.envMapIntensity.value=m.envMapIntensity)):m.isMeshStandardMaterial?(s(p,m),h(p,m),m.isMeshPhysicalMaterial&&d(p,m,y)):m.isMeshMatcapMaterial?(s(p,m),_(p,m)):m.isMeshDepthMaterial?s(p,m):m.isMeshDistanceMaterial?(s(p,m),g(p,m)):m.isMeshNormalMaterial?s(p,m):m.isLineBasicMaterial?(a(p,m),m.isLineDashedMaterial&&o(p,m)):m.isPointsMaterial?l(p,m,M,S):m.isSpriteMaterial?c(p,m):m.isShadowMaterial?(p.color.value.copy(m.color),p.opacity.value=m.opacity):m.isShaderMaterial&&(m.uniformsNeedUpdate=!1)}function s(p,m){p.opacity.value=m.opacity,m.color&&p.diffuse.value.copy(m.color),m.emissive&&p.emissive.value.copy(m.emissive).multiplyScalar(m.emissiveIntensity),m.map&&(p.map.value=m.map,t(m.map,p.mapTransform)),m.alphaMap&&(p.alphaMap.value=m.alphaMap,t(m.alphaMap,p.alphaMapTransform)),m.bumpMap&&(p.bumpMap.value=m.bumpMap,t(m.bumpMap,p.bumpMapTransform),p.bumpScale.value=m.bumpScale,m.side===mn&&(p.bumpScale.value*=-1)),m.normalMap&&(p.normalMap.value=m.normalMap,t(m.normalMap,p.normalMapTransform),p.normalScale.value.copy(m.normalScale),m.side===mn&&p.normalScale.value.negate()),m.displacementMap&&(p.displacementMap.value=m.displacementMap,t(m.displacementMap,p.displacementMapTransform),p.displacementScale.value=m.displacementScale,p.displacementBias.value=m.displacementBias),m.emissiveMap&&(p.emissiveMap.value=m.emissiveMap,t(m.emissiveMap,p.emissiveMapTransform)),m.specularMap&&(p.specularMap.value=m.specularMap,t(m.specularMap,p.specularMapTransform)),m.alphaTest>0&&(p.alphaTest.value=m.alphaTest);const M=e.get(m),S=M.envMap,y=M.envMapRotation;S&&(p.envMap.value=S,cr.copy(y),cr.x*=-1,cr.y*=-1,cr.z*=-1,S.isCubeTexture&&S.isRenderTargetTexture===!1&&(cr.y*=-1,cr.z*=-1),p.envMapRotation.value.setFromMatrix4(Nv.makeRotationFromEuler(cr)),p.flipEnvMap.value=S.isCubeTexture&&S.isRenderTargetTexture===!1?-1:1,p.reflectivity.value=m.reflectivity,p.ior.value=m.ior,p.refractionRatio.value=m.refractionRatio),m.lightMap&&(p.lightMap.value=m.lightMap,p.lightMapIntensity.value=m.lightMapIntensity,t(m.lightMap,p.lightMapTransform)),m.aoMap&&(p.aoMap.value=m.aoMap,p.aoMapIntensity.value=m.aoMapIntensity,t(m.aoMap,p.aoMapTransform))}function a(p,m){p.diffuse.value.copy(m.color),p.opacity.value=m.opacity,m.map&&(p.map.value=m.map,t(m.map,p.mapTransform))}function o(p,m){p.dashSize.value=m.dashSize,p.totalSize.value=m.dashSize+m.gapSize,p.scale.value=m.scale}function l(p,m,M,S){p.diffuse.value.copy(m.color),p.opacity.value=m.opacity,p.size.value=m.size*M,p.scale.value=S*.5,m.map&&(p.map.value=m.map,t(m.map,p.uvTransform)),m.alphaMap&&(p.alphaMap.value=m.alphaMap,t(m.alphaMap,p.alphaMapTransform)),m.alphaTest>0&&(p.alphaTest.value=m.alphaTest)}function c(p,m){p.diffuse.value.copy(m.color),p.opacity.value=m.opacity,p.rotation.value=m.rotation,m.map&&(p.map.value=m.map,t(m.map,p.mapTransform)),m.alphaMap&&(p.alphaMap.value=m.alphaMap,t(m.alphaMap,p.alphaMapTransform)),m.alphaTest>0&&(p.alphaTest.value=m.alphaTest)}function u(p,m){p.specular.value.copy(m.specular),p.shininess.value=Math.max(m.shininess,1e-4)}function f(p,m){m.gradientMap&&(p.gradientMap.value=m.gradientMap)}function h(p,m){p.metalness.value=m.metalness,m.metalnessMap&&(p.metalnessMap.value=m.metalnessMap,t(m.metalnessMap,p.metalnessMapTransform)),p.roughness.value=m.roughness,m.roughnessMap&&(p.roughnessMap.value=m.roughnessMap,t(m.roughnessMap,p.roughnessMapTransform)),m.envMap&&(p.envMapIntensity.value=m.envMapIntensity)}function d(p,m,M){p.ior.value=m.ior,m.sheen>0&&(p.sheenColor.value.copy(m.sheenColor).multiplyScalar(m.sheen),p.sheenRoughness.value=m.sheenRoughness,m.sheenColorMap&&(p.sheenColorMap.value=m.sheenColorMap,t(m.sheenColorMap,p.sheenColorMapTransform)),m.sheenRoughnessMap&&(p.sheenRoughnessMap.value=m.sheenRoughnessMap,t(m.sheenRoughnessMap,p.sheenRoughnessMapTransform))),m.clearcoat>0&&(p.clearcoat.value=m.clearcoat,p.clearcoatRoughness.value=m.clearcoatRoughness,m.clearcoatMap&&(p.clearcoatMap.value=m.clearcoatMap,t(m.clearcoatMap,p.clearcoatMapTransform)),m.clearcoatRoughnessMap&&(p.clearcoatRoughnessMap.value=m.clearcoatRoughnessMap,t(m.clearcoatRoughnessMap,p.clearcoatRoughnessMapTransform)),m.clearcoatNormalMap&&(p.clearcoatNormalMap.value=m.clearcoatNormalMap,t(m.clearcoatNormalMap,p.clearcoatNormalMapTransform),p.clearcoatNormalScale.value.copy(m.clearcoatNormalScale),m.side===mn&&p.clearcoatNormalScale.value.negate())),m.dispersion>0&&(p.dispersion.value=m.dispersion),m.iridescence>0&&(p.iridescence.value=m.iridescence,p.iridescenceIOR.value=m.iridescenceIOR,p.iridescenceThicknessMinimum.value=m.iridescenceThicknessRange[0],p.iridescenceThicknessMaximum.value=m.iridescenceThicknessRange[1],m.iridescenceMap&&(p.iridescenceMap.value=m.iridescenceMap,t(m.iridescenceMap,p.iridescenceMapTransform)),m.iridescenceThicknessMap&&(p.iridescenceThicknessMap.value=m.iridescenceThicknessMap,t(m.iridescenceThicknessMap,p.iridescenceThicknessMapTransform))),m.transmission>0&&(p.transmission.value=m.transmission,p.transmissionSamplerMap.value=M.texture,p.transmissionSamplerSize.value.set(M.width,M.height),m.transmissionMap&&(p.transmissionMap.value=m.transmissionMap,t(m.transmissionMap,p.transmissionMapTransform)),p.thickness.value=m.thickness,m.thicknessMap&&(p.thicknessMap.value=m.thicknessMap,t(m.thicknessMap,p.thicknessMapTransform)),p.attenuationDistance.value=m.attenuationDistance,p.attenuationColor.value.copy(m.attenuationColor)),m.anisotropy>0&&(p.anisotropyVector.value.set(m.anisotropy*Math.cos(m.anisotropyRotation),m.anisotropy*Math.sin(m.anisotropyRotation)),m.anisotropyMap&&(p.anisotropyMap.value=m.anisotropyMap,t(m.anisotropyMap,p.anisotropyMapTransform))),p.specularIntensity.value=m.specularIntensity,p.specularColor.value.copy(m.specularColor),m.specularColorMap&&(p.specularColorMap.value=m.specularColorMap,t(m.specularColorMap,p.specularColorMapTransform)),m.specularIntensityMap&&(p.specularIntensityMap.value=m.specularIntensityMap,t(m.specularIntensityMap,p.specularIntensityMapTransform))}function _(p,m){m.matcap&&(p.matcap.value=m.matcap)}function g(p,m){const M=e.get(m).light;p.referencePosition.value.setFromMatrixPosition(M.matrixWorld),p.nearDistance.value=M.shadow.camera.near,p.farDistance.value=M.shadow.camera.far}return{refreshFogUniforms:n,refreshMaterialUniforms:i}}function Ov(r,e,t,n){let i={},s={},a=[];const o=r.getParameter(r.MAX_UNIFORM_BUFFER_BINDINGS);function l(M,S){const y=S.program;n.uniformBlockBinding(M,y)}function c(M,S){let y=i[M.id];y===void 0&&(_(M),y=u(M),i[M.id]=y,M.addEventListener("dispose",p));const A=S.program;n.updateUBOMapping(M,A);const w=e.render.frame;s[M.id]!==w&&(h(M),s[M.id]=w)}function u(M){const S=f();M.__bindingPointIndex=S;const y=r.createBuffer(),A=M.__size,w=M.usage;return r.bindBuffer(r.UNIFORM_BUFFER,y),r.bufferData(r.UNIFORM_BUFFER,A,w),r.bindBuffer(r.UNIFORM_BUFFER,null),r.bindBufferBase(r.UNIFORM_BUFFER,S,y),y}function f(){for(let M=0;M<o;M++)if(a.indexOf(M)===-1)return a.push(M),M;return ut("WebGLRenderer: Maximum number of simultaneously usable uniforms groups reached."),0}function h(M){const S=i[M.id],y=M.uniforms,A=M.__cache;r.bindBuffer(r.UNIFORM_BUFFER,S);for(let w=0,C=y.length;w<C;w++){const x=Array.isArray(y[w])?y[w]:[y[w]];for(let b=0,W=x.length;b<W;b++){const L=x[b];if(d(L,w,b,A)===!0){const G=L.__offset,z=Array.isArray(L.value)?L.value:[L.value];let q=0;for(let H=0;H<z.length;H++){const N=z[H],F=g(N);typeof N=="number"||typeof N=="boolean"?(L.__data[0]=N,r.bufferSubData(r.UNIFORM_BUFFER,G+q,L.__data)):N.isMatrix3?(L.__data[0]=N.elements[0],L.__data[1]=N.elements[1],L.__data[2]=N.elements[2],L.__data[3]=0,L.__data[4]=N.elements[3],L.__data[5]=N.elements[4],L.__data[6]=N.elements[5],L.__data[7]=0,L.__data[8]=N.elements[6],L.__data[9]=N.elements[7],L.__data[10]=N.elements[8],L.__data[11]=0):(N.toArray(L.__data,q),q+=F.storage/Float32Array.BYTES_PER_ELEMENT)}r.bufferSubData(r.UNIFORM_BUFFER,G,L.__data)}}}r.bindBuffer(r.UNIFORM_BUFFER,null)}function d(M,S,y,A){const w=M.value,C=S+"_"+y;if(A[C]===void 0)return typeof w=="number"||typeof w=="boolean"?A[C]=w:A[C]=w.clone(),!0;{const x=A[C];if(typeof w=="number"||typeof w=="boolean"){if(x!==w)return A[C]=w,!0}else if(x.equals(w)===!1)return x.copy(w),!0}return!1}function _(M){const S=M.uniforms;let y=0;const A=16;for(let C=0,x=S.length;C<x;C++){const b=Array.isArray(S[C])?S[C]:[S[C]];for(let W=0,L=b.length;W<L;W++){const G=b[W],z=Array.isArray(G.value)?G.value:[G.value];for(let q=0,H=z.length;q<H;q++){const N=z[q],F=g(N),ne=y%A,ie=ne%F.boundary,me=ne+ie;y+=ie,me!==0&&A-me<F.storage&&(y+=A-me),G.__data=new Float32Array(F.storage/Float32Array.BYTES_PER_ELEMENT),G.__offset=y,y+=F.storage}}}const w=y%A;return w>0&&(y+=A-w),M.__size=y,M.__cache={},this}function g(M){const S={boundary:0,storage:0};return typeof M=="number"||typeof M=="boolean"?(S.boundary=4,S.storage=4):M.isVector2?(S.boundary=8,S.storage=8):M.isVector3||M.isColor?(S.boundary=16,S.storage=12):M.isVector4?(S.boundary=16,S.storage=16):M.isMatrix3?(S.boundary=48,S.storage=48):M.isMatrix4?(S.boundary=64,S.storage=64):M.isTexture?He("WebGLRenderer: Texture samplers can not be part of an uniforms group."):He("WebGLRenderer: Unsupported uniform value type.",M),S}function p(M){const S=M.target;S.removeEventListener("dispose",p);const y=a.indexOf(S.__bindingPointIndex);a.splice(y,1),r.deleteBuffer(i[S.id]),delete i[S.id],delete s[S.id]}function m(){for(const M in i)r.deleteBuffer(i[M]);a=[],i={},s={}}return{bind:l,update:c,dispose:m}}const Bv=new Uint16Array([12469,15057,12620,14925,13266,14620,13807,14376,14323,13990,14545,13625,14713,13328,14840,12882,14931,12528,14996,12233,15039,11829,15066,11525,15080,11295,15085,10976,15082,10705,15073,10495,13880,14564,13898,14542,13977,14430,14158,14124,14393,13732,14556,13410,14702,12996,14814,12596,14891,12291,14937,11834,14957,11489,14958,11194,14943,10803,14921,10506,14893,10278,14858,9960,14484,14039,14487,14025,14499,13941,14524,13740,14574,13468,14654,13106,14743,12678,14818,12344,14867,11893,14889,11509,14893,11180,14881,10751,14852,10428,14812,10128,14765,9754,14712,9466,14764,13480,14764,13475,14766,13440,14766,13347,14769,13070,14786,12713,14816,12387,14844,11957,14860,11549,14868,11215,14855,10751,14825,10403,14782,10044,14729,9651,14666,9352,14599,9029,14967,12835,14966,12831,14963,12804,14954,12723,14936,12564,14917,12347,14900,11958,14886,11569,14878,11247,14859,10765,14828,10401,14784,10011,14727,9600,14660,9289,14586,8893,14508,8533,15111,12234,15110,12234,15104,12216,15092,12156,15067,12010,15028,11776,14981,11500,14942,11205,14902,10752,14861,10393,14812,9991,14752,9570,14682,9252,14603,8808,14519,8445,14431,8145,15209,11449,15208,11451,15202,11451,15190,11438,15163,11384,15117,11274,15055,10979,14994,10648,14932,10343,14871,9936,14803,9532,14729,9218,14645,8742,14556,8381,14461,8020,14365,7603,15273,10603,15272,10607,15267,10619,15256,10631,15231,10614,15182,10535,15118,10389,15042,10167,14963,9787,14883,9447,14800,9115,14710,8665,14615,8318,14514,7911,14411,7507,14279,7198,15314,9675,15313,9683,15309,9712,15298,9759,15277,9797,15229,9773,15166,9668,15084,9487,14995,9274,14898,8910,14800,8539,14697,8234,14590,7790,14479,7409,14367,7067,14178,6621,15337,8619,15337,8631,15333,8677,15325,8769,15305,8871,15264,8940,15202,8909,15119,8775,15022,8565,14916,8328,14804,8009,14688,7614,14569,7287,14448,6888,14321,6483,14088,6171,15350,7402,15350,7419,15347,7480,15340,7613,15322,7804,15287,7973,15229,8057,15148,8012,15046,7846,14933,7611,14810,7357,14682,7069,14552,6656,14421,6316,14251,5948,14007,5528,15356,5942,15356,5977,15353,6119,15348,6294,15332,6551,15302,6824,15249,7044,15171,7122,15070,7050,14949,6861,14818,6611,14679,6349,14538,6067,14398,5651,14189,5311,13935,4958,15359,4123,15359,4153,15356,4296,15353,4646,15338,5160,15311,5508,15263,5829,15188,6042,15088,6094,14966,6001,14826,5796,14678,5543,14527,5287,14377,4985,14133,4586,13869,4257,15360,1563,15360,1642,15358,2076,15354,2636,15341,3350,15317,4019,15273,4429,15203,4732,15105,4911,14981,4932,14836,4818,14679,4621,14517,4386,14359,4156,14083,3795,13808,3437,15360,122,15360,137,15358,285,15355,636,15344,1274,15322,2177,15281,2765,15215,3223,15120,3451,14995,3569,14846,3567,14681,3466,14511,3305,14344,3121,14037,2800,13753,2467,15360,0,15360,1,15359,21,15355,89,15346,253,15325,479,15287,796,15225,1148,15133,1492,15008,1749,14856,1882,14685,1886,14506,1783,14324,1608,13996,1398,13702,1183]);let Jn=null;function kv(){return Jn===null&&(Jn=new Ep(Bv,16,16,ns,Ti),Jn.name="DFG_LUT",Jn.minFilter=en,Jn.magFilter=en,Jn.wrapS=yi,Jn.wrapT=yi,Jn.generateMipmaps=!1,Jn.needsUpdate=!0),Jn}class zv{constructor(e={}){const{canvas:t=tp(),context:n=null,depth:i=!0,stencil:s=!1,alpha:a=!1,antialias:o=!1,premultipliedAlpha:l=!0,preserveDrawingBuffer:c=!1,powerPreference:u="default",failIfMajorPerformanceCaveat:f=!1,reversedDepthBuffer:h=!1,outputBufferType:d=Rn}=e;this.isWebGLRenderer=!0;let _;if(n!==null){if(typeof WebGLRenderingContext<"u"&&n instanceof WebGLRenderingContext)throw new Error("THREE.WebGLRenderer: WebGL 1 is not supported since r163.");_=n.getContextAttributes().alpha}else _=a;const g=d,p=new Set([xc,vc,gc]),m=new Set([Rn,oi,Ns,Fs,mc,_c]),M=new Uint32Array(4),S=new Int32Array(4);let y=null,A=null;const w=[],C=[];let x=null;this.domElement=t,this.debug={checkShaderErrors:!0,onShaderError:null},this.autoClear=!0,this.autoClearColor=!0,this.autoClearDepth=!0,this.autoClearStencil=!0,this.sortObjects=!0,this.clippingPlanes=[],this.localClippingEnabled=!1,this.toneMapping=si,this.toneMappingExposure=1,this.transmissionResolutionScale=1;const b=this;let W=!1;this._outputColorSpace=Tn;let L=0,G=0,z=null,q=-1,H=null;const N=new Pt,F=new Pt;let ne=null;const ie=new it(0);let me=0,Te=t.width,le=t.height,Oe=1,Je=null,$e=null;const ee=new Pt(0,0,Te,le),de=new Pt(0,0,Te,le);let pe=!1;const Ve=new Tc;let Ne=!1,Fe=!1;const Tt=new St,Qe=new V,ot=new Pt,pt={background:null,fog:null,environment:null,overrideMaterial:null,isScene:!0};let Ye=!1;function vt(){return z===null?Oe:1}let I=n;function yt(E,k){return t.getContext(E,k)}try{const E={alpha:!0,depth:i,stencil:s,antialias:o,premultipliedAlpha:l,preserveDrawingBuffer:c,powerPreference:u,failIfMajorPerformanceCaveat:f};if("setAttribute"in t&&t.setAttribute("data-engine",`three.js r${fc}`),t.addEventListener("webglcontextlost",we,!1),t.addEventListener("webglcontextrestored",Le,!1),t.addEventListener("webglcontextcreationerror",ft,!1),I===null){const k="webgl2";if(I=yt(k,E),I===null)throw yt(k)?new Error("Error creating WebGL context with your selected attributes."):new Error("Error creating WebGL context.")}}catch(E){throw ut("WebGLRenderer: "+E.message),E}let rt,Q,Z,T,v,D,B,j,$,ce,se,Re,Ue,re,he,Ce,be,Ee,Be,U,fe,ue,_e;function ae(){rt=new zg(I),rt.init(),fe=new Pv(I,rt),Q=new Lg(I,rt,e,fe),Z=new Rv(I,rt),Q.reversedDepthBuffer&&h&&Z.buffers.depth.setReversed(!0),T=new Hg(I),v=new pv,D=new Cv(I,rt,Z,v,Q,fe,T),B=new kg(b),j=new $p(I),ue=new Pg(I,j),$=new Vg(I,j,T,ue),ce=new Xg(I,$,j,ue,T),Ee=new Wg(I,Q,D),he=new Ig(v),se=new dv(b,B,rt,Q,ue,he),Re=new Fv(b,v),Ue=new _v,re=new yv(rt),be=new Cg(b,B,Z,ce,_,l),Ce=new wv(b,ce,Q),_e=new Ov(I,T,Q,Z),Be=new Dg(I,rt,T),U=new Gg(I,rt,T),T.programs=se.programs,b.capabilities=Q,b.extensions=rt,b.properties=v,b.renderLists=Ue,b.shadowMap=Ce,b.state=Z,b.info=T}ae(),g!==Rn&&(x=new Yg(g,t.width,t.height,i,s));const J=new Uv(b,I);this.xr=J,this.getContext=function(){return I},this.getContextAttributes=function(){return I.getContextAttributes()},this.forceContextLoss=function(){const E=rt.get("WEBGL_lose_context");E&&E.loseContext()},this.forceContextRestore=function(){const E=rt.get("WEBGL_lose_context");E&&E.restoreContext()},this.getPixelRatio=function(){return Oe},this.setPixelRatio=function(E){E!==void 0&&(Oe=E,this.setSize(Te,le,!1))},this.getSize=function(E){return E.set(Te,le)},this.setSize=function(E,k,K=!0){if(J.isPresenting){He("WebGLRenderer: Can't change size while VR device is presenting.");return}Te=E,le=k,t.width=Math.floor(E*Oe),t.height=Math.floor(k*Oe),K===!0&&(t.style.width=E+"px",t.style.height=k+"px"),x!==null&&x.setSize(t.width,t.height),this.setViewport(0,0,E,k)},this.getDrawingBufferSize=function(E){return E.set(Te*Oe,le*Oe).floor()},this.setDrawingBufferSize=function(E,k,K){Te=E,le=k,Oe=K,t.width=Math.floor(E*K),t.height=Math.floor(k*K),this.setViewport(0,0,E,k)},this.setEffects=function(E){if(g===Rn){console.error("THREE.WebGLRenderer: setEffects() requires outputBufferType set to HalfFloatType or FloatType.");return}if(E){for(let k=0;k<E.length;k++)if(E[k].isOutputPass===!0){console.warn("THREE.WebGLRenderer: OutputPass is not needed in setEffects(). Tone mapping and color space conversion are applied automatically.");break}}x.setEffects(E||[])},this.getCurrentViewport=function(E){return E.copy(N)},this.getViewport=function(E){return E.copy(ee)},this.setViewport=function(E,k,K,Y){E.isVector4?ee.set(E.x,E.y,E.z,E.w):ee.set(E,k,K,Y),Z.viewport(N.copy(ee).multiplyScalar(Oe).round())},this.getScissor=function(E){return E.copy(de)},this.setScissor=function(E,k,K,Y){E.isVector4?de.set(E.x,E.y,E.z,E.w):de.set(E,k,K,Y),Z.scissor(F.copy(de).multiplyScalar(Oe).round())},this.getScissorTest=function(){return pe},this.setScissorTest=function(E){Z.setScissorTest(pe=E)},this.setOpaqueSort=function(E){Je=E},this.setTransparentSort=function(E){$e=E},this.getClearColor=function(E){return E.copy(be.getClearColor())},this.setClearColor=function(){be.setClearColor(...arguments)},this.getClearAlpha=function(){return be.getClearAlpha()},this.setClearAlpha=function(){be.setClearAlpha(...arguments)},this.clear=function(E=!0,k=!0,K=!0){let Y=0;if(E){let X=!1;if(z!==null){const Se=z.texture.format;X=p.has(Se)}if(X){const Se=z.texture.type,Ae=m.has(Se),xe=be.getClearColor(),Pe=be.getClearAlpha(),De=xe.r,We=xe.g,P=xe.b;Ae?(M[0]=De,M[1]=We,M[2]=P,M[3]=Pe,I.clearBufferuiv(I.COLOR,0,M)):(S[0]=De,S[1]=We,S[2]=P,S[3]=Pe,I.clearBufferiv(I.COLOR,0,S))}else Y|=I.COLOR_BUFFER_BIT}k&&(Y|=I.DEPTH_BUFFER_BIT),K&&(Y|=I.STENCIL_BUFFER_BIT,this.state.buffers.stencil.setMask(4294967295)),Y!==0&&I.clear(Y)},this.clearColor=function(){this.clear(!0,!1,!1)},this.clearDepth=function(){this.clear(!1,!0,!1)},this.clearStencil=function(){this.clear(!1,!1,!0)},this.dispose=function(){t.removeEventListener("webglcontextlost",we,!1),t.removeEventListener("webglcontextrestored",Le,!1),t.removeEventListener("webglcontextcreationerror",ft,!1),be.dispose(),Ue.dispose(),re.dispose(),v.dispose(),B.dispose(),ce.dispose(),ue.dispose(),_e.dispose(),se.dispose(),J.dispose(),J.removeEventListener("sessionstart",$n),J.removeEventListener("sessionend",nr),Ot.stop()};function we(E){E.preventDefault(),iu("WebGLRenderer: Context Lost."),W=!0}function Le(){iu("WebGLRenderer: Context Restored."),W=!1;const E=T.autoReset,k=Ce.enabled,K=Ce.autoUpdate,Y=Ce.needsUpdate,X=Ce.type;ae(),T.autoReset=E,Ce.enabled=k,Ce.autoUpdate=K,Ce.needsUpdate=Y,Ce.type=X}function ft(E){ut("WebGLRenderer: A WebGL context could not be created. Reason: ",E.statusMessage)}function tt(E){const k=E.target;k.removeEventListener("dispose",tt),Ut(k)}function Ut(E){cn(E),v.remove(E)}function cn(E){const k=v.get(E).programs;k!==void 0&&(k.forEach(function(K){se.releaseProgram(K)}),E.isShaderMaterial&&se.releaseShaderCache(E))}this.renderBufferDirect=function(E,k,K,Y,X,Se){k===null&&(k=pt);const Ae=X.isMesh&&X.matrixWorld.determinant()<0,xe=ds(E,k,K,Y,X);Z.setMaterial(Y,Ae);let Pe=K.index,De=1;if(Y.wireframe===!0){if(Pe=$.getWireframeAttribute(K),Pe===void 0)return;De=2}const We=K.drawRange,P=K.attributes.position;let R=We.start*De,oe=(We.start+We.count)*De;Se!==null&&(R=Math.max(R,Se.start*De),oe=Math.min(oe,(Se.start+Se.count)*De)),Pe!==null?(R=Math.max(R,0),oe=Math.min(oe,Pe.count)):P!=null&&(R=Math.max(R,0),oe=Math.min(oe,P.count));const Me=oe-R;if(Me<0||Me===1/0)return;ue.setup(X,Y,xe,K,Pe);let Ie,Ze=Be;if(Pe!==null&&(Ie=j.get(Pe),Ze=U,Ze.setIndex(Ie)),X.isMesh)Y.wireframe===!0?(Z.setLineWidth(Y.wireframeLinewidth*vt()),Ze.setMode(I.LINES)):Ze.setMode(I.TRIANGLES);else if(X.isLine){let bt=Y.linewidth;bt===void 0&&(bt=1),Z.setLineWidth(bt*vt()),X.isLineSegments?Ze.setMode(I.LINES):X.isLineLoop?Ze.setMode(I.LINE_LOOP):Ze.setMode(I.LINE_STRIP)}else X.isPoints?Ze.setMode(I.POINTS):X.isSprite&&Ze.setMode(I.TRIANGLES);if(X.isBatchedMesh)if(X._multiDrawInstances!==null)Ha("WebGLRenderer: renderMultiDrawInstances has been deprecated and will be removed in r184. Append to renderMultiDraw arguments and use indirection."),Ze.renderMultiDrawInstances(X._multiDrawStarts,X._multiDrawCounts,X._multiDrawCount,X._multiDrawInstances);else if(rt.get("WEBGL_multi_draw"))Ze.renderMultiDraw(X._multiDrawStarts,X._multiDrawCounts,X._multiDrawCount);else{const bt=X._multiDrawStarts,ve=X._multiDrawCounts,Lt=X._multiDrawCount,ze=Pe?j.get(Pe).bytesPerElement:1,Gt=v.get(Y).currentProgram.getUniforms();for(let Zt=0;Zt<Lt;Zt++)Gt.setValue(I,"_gl_DrawID",Zt),Ze.render(bt[Zt]/ze,ve[Zt])}else if(X.isInstancedMesh)Ze.renderInstances(R,Me,X.count);else if(K.isInstancedBufferGeometry){const bt=K._maxInstanceCount!==void 0?K._maxInstanceCount:1/0,ve=Math.min(K.instanceCount,bt);Ze.renderInstances(R,Me,ve)}else Ze.render(R,Me)};function Sn(E,k,K){E.transparent===!0&&E.side===Xn&&E.forceSinglePass===!1?(E.side=mn,E.needsUpdate=!0,Zn(E,k,K),E.side=Zi,E.needsUpdate=!0,Zn(E,k,K),E.side=Xn):Zn(E,k,K)}this.compile=function(E,k,K=null){K===null&&(K=E),A=re.get(K),A.init(k),C.push(A),K.traverseVisible(function(X){X.isLight&&X.layers.test(k.layers)&&(A.pushLight(X),X.castShadow&&A.pushShadow(X))}),E!==K&&E.traverseVisible(function(X){X.isLight&&X.layers.test(k.layers)&&(A.pushLight(X),X.castShadow&&A.pushShadow(X))}),A.setupLights();const Y=new Set;return E.traverse(function(X){if(!(X.isMesh||X.isPoints||X.isLine||X.isSprite))return;const Se=X.material;if(Se)if(Array.isArray(Se))for(let Ae=0;Ae<Se.length;Ae++){const xe=Se[Ae];Sn(xe,K,X),Y.add(xe)}else Sn(Se,K,X),Y.add(Se)}),A=C.pop(),Y},this.compileAsync=function(E,k,K=null){const Y=this.compile(E,k,K);return new Promise(X=>{function Se(){if(Y.forEach(function(Ae){v.get(Ae).currentProgram.isReady()&&Y.delete(Ae)}),Y.size===0){X(E);return}setTimeout(Se,10)}rt.get("KHR_parallel_shader_compile")!==null?Se():setTimeout(Se,10)})};let Ci=null;function Ar(E){Ci&&Ci(E)}function $n(){Ot.stop()}function nr(){Ot.start()}const Ot=new nf;Ot.setAnimationLoop(Ar),typeof self<"u"&&Ot.setContext(self),this.setAnimationLoop=function(E){Ci=E,J.setAnimationLoop(E),E===null?Ot.stop():Ot.start()},J.addEventListener("sessionstart",$n),J.addEventListener("sessionend",nr),this.render=function(E,k){if(k!==void 0&&k.isCamera!==!0){ut("WebGLRenderer.render: camera is not an instance of THREE.Camera.");return}if(W===!0)return;const K=J.enabled===!0&&J.isPresenting===!0,Y=x!==null&&(z===null||K)&&x.begin(b,z);if(E.matrixWorldAutoUpdate===!0&&E.updateMatrixWorld(),k.parent===null&&k.matrixWorldAutoUpdate===!0&&k.updateMatrixWorld(),J.enabled===!0&&J.isPresenting===!0&&(x===null||x.isCompositing()===!1)&&(J.cameraAutoUpdate===!0&&J.updateCamera(k),k=J.getCamera()),E.isScene===!0&&E.onBeforeRender(b,E,k,z),A=re.get(E,C.length),A.init(k),C.push(A),Tt.multiplyMatrices(k.projectionMatrix,k.matrixWorldInverse),Ve.setFromProjectionMatrix(Tt,ii,k.reversedDepth),Fe=this.localClippingEnabled,Ne=he.init(this.clippingPlanes,Fe),y=Ue.get(E,w.length),y.init(),w.push(y),J.enabled===!0&&J.isPresenting===!0){const Ae=b.xr.getDepthSensingMesh();Ae!==null&&Kn(Ae,k,-1/0,b.sortObjects)}Kn(E,k,0,b.sortObjects),y.finish(),b.sortObjects===!0&&y.sort(Je,$e),Ye=J.enabled===!1||J.isPresenting===!1||J.hasDepthSensing()===!1,Ye&&be.addToRenderList(y,E),this.info.render.frame++,Ne===!0&&he.beginShadows();const X=A.state.shadowsArray;if(Ce.render(X,E,k),Ne===!0&&he.endShadows(),this.info.autoReset===!0&&this.info.reset(),(Y&&x.hasRenderPass())===!1){const Ae=y.opaque,xe=y.transmissive;if(A.setupLights(),k.isArrayCamera){const Pe=k.cameras;if(xe.length>0)for(let De=0,We=Pe.length;De<We;De++){const P=Pe[De];Pi(Ae,xe,E,P)}Ye&&be.render(E);for(let De=0,We=Pe.length;De<We;De++){const P=Pe[De];Kt(y,E,P,P.viewport)}}else xe.length>0&&Pi(Ae,xe,E,k),Ye&&be.render(E),Kt(y,E,k)}z!==null&&G===0&&(D.updateMultisampleRenderTarget(z),D.updateRenderTargetMipmap(z)),Y&&x.end(b),E.isScene===!0&&E.onAfterRender(b,E,k),ue.resetDefaultState(),q=-1,H=null,C.pop(),C.length>0?(A=C[C.length-1],Ne===!0&&he.setGlobalState(b.clippingPlanes,A.state.camera)):A=null,w.pop(),w.length>0?y=w[w.length-1]:y=null};function Kn(E,k,K,Y){if(E.visible===!1)return;if(E.layers.test(k.layers)){if(E.isGroup)K=E.renderOrder;else if(E.isLOD)E.autoUpdate===!0&&E.update(k);else if(E.isLight)A.pushLight(E),E.castShadow&&A.pushShadow(E);else if(E.isSprite){if(!E.frustumCulled||Ve.intersectsSprite(E)){Y&&ot.setFromMatrixPosition(E.matrixWorld).applyMatrix4(Tt);const Ae=ce.update(E),xe=E.material;xe.visible&&y.push(E,Ae,xe,K,ot.z,null)}}else if((E.isMesh||E.isLine||E.isPoints)&&(!E.frustumCulled||Ve.intersectsObject(E))){const Ae=ce.update(E),xe=E.material;if(Y&&(E.boundingSphere!==void 0?(E.boundingSphere===null&&E.computeBoundingSphere(),ot.copy(E.boundingSphere.center)):(Ae.boundingSphere===null&&Ae.computeBoundingSphere(),ot.copy(Ae.boundingSphere.center)),ot.applyMatrix4(E.matrixWorld).applyMatrix4(Tt)),Array.isArray(xe)){const Pe=Ae.groups;for(let De=0,We=Pe.length;De<We;De++){const P=Pe[De],R=xe[P.materialIndex];R&&R.visible&&y.push(E,Ae,R,K,ot.z,P)}}else xe.visible&&y.push(E,Ae,xe,K,ot.z,null)}}const Se=E.children;for(let Ae=0,xe=Se.length;Ae<xe;Ae++)Kn(Se[Ae],k,K,Y)}function Kt(E,k,K,Y){const{opaque:X,transmissive:Se,transparent:Ae}=E;A.setupLightsView(K),Ne===!0&&he.setGlobalState(b.clippingPlanes,K),Y&&Z.viewport(N.copy(Y)),X.length>0&&hi(X,k,K),Se.length>0&&hi(Se,k,K),Ae.length>0&&hi(Ae,k,K),Z.buffers.depth.setTest(!0),Z.buffers.depth.setMask(!0),Z.buffers.color.setMask(!0),Z.setPolygonOffset(!1)}function Pi(E,k,K,Y){if((K.isScene===!0?K.overrideMaterial:null)!==null)return;if(A.state.transmissionRenderTarget[Y.id]===void 0){const R=rt.has("EXT_color_buffer_half_float")||rt.has("EXT_color_buffer_float");A.state.transmissionRenderTarget[Y.id]=new ai(1,1,{generateMipmaps:!0,type:R?Ti:Rn,minFilter:mr,samples:Math.max(4,Q.samples),stencilBuffer:s,resolveDepthBuffer:!1,resolveStencilBuffer:!1,colorSpace:ht.workingColorSpace})}const Se=A.state.transmissionRenderTarget[Y.id],Ae=Y.viewport||N;Se.setSize(Ae.z*b.transmissionResolutionScale,Ae.w*b.transmissionResolutionScale);const xe=b.getRenderTarget(),Pe=b.getActiveCubeFace(),De=b.getActiveMipmapLevel();b.setRenderTarget(Se),b.getClearColor(ie),me=b.getClearAlpha(),me<1&&b.setClearColor(16777215,.5),b.clear(),Ye&&be.render(K);const We=b.toneMapping;b.toneMapping=si;const P=Y.viewport;if(Y.viewport!==void 0&&(Y.viewport=void 0),A.setupLightsView(Y),Ne===!0&&he.setGlobalState(b.clippingPlanes,Y),hi(E,K,Y),D.updateMultisampleRenderTarget(Se),D.updateRenderTargetMipmap(Se),rt.has("WEBGL_multisampled_render_to_texture")===!1){let R=!1;for(let oe=0,Me=k.length;oe<Me;oe++){const Ie=k[oe],{object:Ze,geometry:bt,material:ve,group:Lt}=Ie;if(ve.side===Xn&&Ze.layers.test(Y.layers)){const ze=ve.side;ve.side=mn,ve.needsUpdate=!0,ir(Ze,K,Y,bt,ve,Lt),ve.side=ze,ve.needsUpdate=!0,R=!0}}R===!0&&(D.updateMultisampleRenderTarget(Se),D.updateRenderTargetMipmap(Se))}b.setRenderTarget(xe,Pe,De),b.setClearColor(ie,me),P!==void 0&&(Y.viewport=P),b.toneMapping=We}function hi(E,k,K){const Y=k.isScene===!0?k.overrideMaterial:null;for(let X=0,Se=E.length;X<Se;X++){const Ae=E[X],{object:xe,geometry:Pe,group:De}=Ae;let We=Ae.material;We.allowOverride===!0&&Y!==null&&(We=Y),xe.layers.test(K.layers)&&ir(xe,k,K,Pe,We,De)}}function ir(E,k,K,Y,X,Se){E.onBeforeRender(b,k,K,Y,X,Se),E.modelViewMatrix.multiplyMatrices(K.matrixWorldInverse,E.matrixWorld),E.normalMatrix.getNormalMatrix(E.modelViewMatrix),X.onBeforeRender(b,k,K,Y,E,Se),X.transparent===!0&&X.side===Xn&&X.forceSinglePass===!1?(X.side=mn,X.needsUpdate=!0,b.renderBufferDirect(K,k,Y,X,E,Se),X.side=Zi,X.needsUpdate=!0,b.renderBufferDirect(K,k,Y,X,E,Se),X.side=Xn):b.renderBufferDirect(K,k,Y,X,E,Se),E.onAfterRender(b,k,K,Y,X,Se)}function Zn(E,k,K){k.isScene!==!0&&(k=pt);const Y=v.get(E),X=A.state.lights,Se=A.state.shadowsArray,Ae=X.state.version,xe=se.getParameters(E,X.state,Se,k,K),Pe=se.getProgramCacheKey(xe);let De=Y.programs;Y.environment=E.isMeshStandardMaterial||E.isMeshLambertMaterial||E.isMeshPhongMaterial?k.environment:null,Y.fog=k.fog;const We=E.isMeshStandardMaterial||E.isMeshLambertMaterial&&!E.envMap||E.isMeshPhongMaterial&&!E.envMap;Y.envMap=B.get(E.envMap||Y.environment,We),Y.envMapRotation=Y.environment!==null&&E.envMap===null?k.environmentRotation:E.envMapRotation,De===void 0&&(E.addEventListener("dispose",tt),De=new Map,Y.programs=De);let P=De.get(Pe);if(P!==void 0){if(Y.currentProgram===P&&Y.lightsStateVersion===Ae)return wr(E,xe),P}else xe.uniforms=se.getUniforms(E),E.onBeforeCompile(xe,b),P=se.acquireProgram(xe,Pe),De.set(Pe,P),Y.uniforms=xe.uniforms;const R=Y.uniforms;return(!E.isShaderMaterial&&!E.isRawShaderMaterial||E.clipping===!0)&&(R.clippingPlanes=he.uniform),wr(E,xe),Y.needsLights=oo(E),Y.lightsStateVersion=Ae,Y.needsLights&&(R.ambientLightColor.value=X.state.ambient,R.lightProbe.value=X.state.probe,R.directionalLights.value=X.state.directional,R.directionalLightShadows.value=X.state.directionalShadow,R.spotLights.value=X.state.spot,R.spotLightShadows.value=X.state.spotShadow,R.rectAreaLights.value=X.state.rectArea,R.ltc_1.value=X.state.rectAreaLTC1,R.ltc_2.value=X.state.rectAreaLTC2,R.pointLights.value=X.state.point,R.pointLightShadows.value=X.state.pointShadow,R.hemisphereLights.value=X.state.hemi,R.directionalShadowMatrix.value=X.state.directionalShadowMatrix,R.spotLightMatrix.value=X.state.spotLightMatrix,R.spotLightMap.value=X.state.spotLightMap,R.pointShadowMatrix.value=X.state.pointShadowMatrix),Y.currentProgram=P,Y.uniformsList=null,P}function Di(E){if(E.uniformsList===null){const k=E.currentProgram.getUniforms();E.uniformsList=Oa.seqWithValue(k.seq,E.uniforms)}return E.uniformsList}function wr(E,k){const K=v.get(E);K.outputColorSpace=k.outputColorSpace,K.batching=k.batching,K.batchingColor=k.batchingColor,K.instancing=k.instancing,K.instancingColor=k.instancingColor,K.instancingMorph=k.instancingMorph,K.skinning=k.skinning,K.morphTargets=k.morphTargets,K.morphNormals=k.morphNormals,K.morphColors=k.morphColors,K.morphTargetsCount=k.morphTargetsCount,K.numClippingPlanes=k.numClippingPlanes,K.numIntersection=k.numClipIntersection,K.vertexAlphas=k.vertexAlphas,K.vertexTangents=k.vertexTangents,K.toneMapping=k.toneMapping}function ds(E,k,K,Y,X){k.isScene!==!0&&(k=pt),D.resetTextureUnits();const Se=k.fog,Ae=Y.isMeshStandardMaterial||Y.isMeshLambertMaterial||Y.isMeshPhongMaterial?k.environment:null,xe=z===null?b.outputColorSpace:z.isXRRenderTarget===!0?z.texture.colorSpace:is,Pe=Y.isMeshStandardMaterial||Y.isMeshLambertMaterial&&!Y.envMap||Y.isMeshPhongMaterial&&!Y.envMap,De=B.get(Y.envMap||Ae,Pe),We=Y.vertexColors===!0&&!!K.attributes.color&&K.attributes.color.itemSize===4,P=!!K.attributes.tangent&&(!!Y.normalMap||Y.anisotropy>0),R=!!K.morphAttributes.position,oe=!!K.morphAttributes.normal,Me=!!K.morphAttributes.color;let Ie=si;Y.toneMapped&&(z===null||z.isXRRenderTarget===!0)&&(Ie=b.toneMapping);const Ze=K.morphAttributes.position||K.morphAttributes.normal||K.morphAttributes.color,bt=Ze!==void 0?Ze.length:0,ve=v.get(Y),Lt=A.state.lights;if(Ne===!0&&(Fe===!0||E!==H)){const lt=E===H&&Y.id===q;he.setState(Y,E,lt)}let ze=!1;Y.version===ve.__version?(ve.needsLights&&ve.lightsStateVersion!==Lt.state.version||ve.outputColorSpace!==xe||X.isBatchedMesh&&ve.batching===!1||!X.isBatchedMesh&&ve.batching===!0||X.isBatchedMesh&&ve.batchingColor===!0&&X.colorTexture===null||X.isBatchedMesh&&ve.batchingColor===!1&&X.colorTexture!==null||X.isInstancedMesh&&ve.instancing===!1||!X.isInstancedMesh&&ve.instancing===!0||X.isSkinnedMesh&&ve.skinning===!1||!X.isSkinnedMesh&&ve.skinning===!0||X.isInstancedMesh&&ve.instancingColor===!0&&X.instanceColor===null||X.isInstancedMesh&&ve.instancingColor===!1&&X.instanceColor!==null||X.isInstancedMesh&&ve.instancingMorph===!0&&X.morphTexture===null||X.isInstancedMesh&&ve.instancingMorph===!1&&X.morphTexture!==null||ve.envMap!==De||Y.fog===!0&&ve.fog!==Se||ve.numClippingPlanes!==void 0&&(ve.numClippingPlanes!==he.numPlanes||ve.numIntersection!==he.numIntersection)||ve.vertexAlphas!==We||ve.vertexTangents!==P||ve.morphTargets!==R||ve.morphNormals!==oe||ve.morphColors!==Me||ve.toneMapping!==Ie||ve.morphTargetsCount!==bt)&&(ze=!0):(ze=!0,ve.__version=Y.version);let Gt=ve.currentProgram;ze===!0&&(Gt=Zn(Y,k,X));let Zt=!1,Nn=!1,nn=!1;const dt=Gt.getUniforms(),Ct=ve.uniforms;if(Z.useProgram(Gt.program)&&(Zt=!0,Nn=!0,nn=!0),Y.id!==q&&(q=Y.id,Nn=!0),Zt||H!==E){Z.buffers.depth.getReversed()&&E.reversedDepth!==!0&&(E._reversedDepth=!0,E.updateProjectionMatrix()),dt.setValue(I,"projectionMatrix",E.projectionMatrix),dt.setValue(I,"viewMatrix",E.matrixWorldInverse);const rn=dt.map.cameraPosition;rn!==void 0&&rn.setValue(I,Qe.setFromMatrixPosition(E.matrixWorld)),Q.logarithmicDepthBuffer&&dt.setValue(I,"logDepthBufFC",2/(Math.log(E.far+1)/Math.LN2)),(Y.isMeshPhongMaterial||Y.isMeshToonMaterial||Y.isMeshLambertMaterial||Y.isMeshBasicMaterial||Y.isMeshStandardMaterial||Y.isShaderMaterial)&&dt.setValue(I,"isOrthographic",E.isOrthographicCamera===!0),H!==E&&(H=E,Nn=!0,nn=!0)}if(ve.needsLights&&(Lt.state.directionalShadowMap.length>0&&dt.setValue(I,"directionalShadowMap",Lt.state.directionalShadowMap,D),Lt.state.spotShadowMap.length>0&&dt.setValue(I,"spotShadowMap",Lt.state.spotShadowMap,D),Lt.state.pointShadowMap.length>0&&dt.setValue(I,"pointShadowMap",Lt.state.pointShadowMap,D)),X.isSkinnedMesh){dt.setOptional(I,X,"bindMatrix"),dt.setOptional(I,X,"bindMatrixInverse");const lt=X.skeleton;lt&&(lt.boneTexture===null&&lt.computeBoneTexture(),dt.setValue(I,"boneTexture",lt.boneTexture,D))}X.isBatchedMesh&&(dt.setOptional(I,X,"batchingTexture"),dt.setValue(I,"batchingTexture",X._matricesTexture,D),dt.setOptional(I,X,"batchingIdTexture"),dt.setValue(I,"batchingIdTexture",X._indirectTexture,D),dt.setOptional(I,X,"batchingColorTexture"),X._colorsTexture!==null&&dt.setValue(I,"batchingColorTexture",X._colorsTexture,D));const te=K.morphAttributes;if((te.position!==void 0||te.normal!==void 0||te.color!==void 0)&&Ee.update(X,K,Gt),(Nn||ve.receiveShadow!==X.receiveShadow)&&(ve.receiveShadow=X.receiveShadow,dt.setValue(I,"receiveShadow",X.receiveShadow)),(Y.isMeshStandardMaterial||Y.isMeshLambertMaterial||Y.isMeshPhongMaterial)&&Y.envMap===null&&k.environment!==null&&(Ct.envMapIntensity.value=k.environmentIntensity),Ct.dfgLUT!==void 0&&(Ct.dfgLUT.value=kv()),Nn&&(dt.setValue(I,"toneMappingExposure",b.toneMappingExposure),ve.needsLights&&ao(Ct,nn),Se&&Y.fog===!0&&Re.refreshFogUniforms(Ct,Se),Re.refreshMaterialUniforms(Ct,Y,Oe,le,A.state.transmissionRenderTarget[E.id]),Oa.upload(I,Di(ve),Ct,D)),Y.isShaderMaterial&&Y.uniformsNeedUpdate===!0&&(Oa.upload(I,Di(ve),Ct,D),Y.uniformsNeedUpdate=!1),Y.isSpriteMaterial&&dt.setValue(I,"center",X.center),dt.setValue(I,"modelViewMatrix",X.modelViewMatrix),dt.setValue(I,"normalMatrix",X.normalMatrix),dt.setValue(I,"modelMatrix",X.matrixWorld),Y.isShaderMaterial||Y.isRawShaderMaterial){const lt=Y.uniformsGroups;for(let rn=0,fi=lt.length;rn<fi;rn++){const ps=lt[rn];_e.update(ps,Gt),_e.bind(ps,Gt)}}return Gt}function ao(E,k){E.ambientLightColor.needsUpdate=k,E.lightProbe.needsUpdate=k,E.directionalLights.needsUpdate=k,E.directionalLightShadows.needsUpdate=k,E.pointLights.needsUpdate=k,E.pointLightShadows.needsUpdate=k,E.spotLights.needsUpdate=k,E.spotLightShadows.needsUpdate=k,E.rectAreaLights.needsUpdate=k,E.hemisphereLights.needsUpdate=k}function oo(E){return E.isMeshLambertMaterial||E.isMeshToonMaterial||E.isMeshPhongMaterial||E.isMeshStandardMaterial||E.isShadowMaterial||E.isShaderMaterial&&E.lights===!0}this.getActiveCubeFace=function(){return L},this.getActiveMipmapLevel=function(){return G},this.getRenderTarget=function(){return z},this.setRenderTargetTextures=function(E,k,K){const Y=v.get(E);Y.__autoAllocateDepthBuffer=E.resolveDepthBuffer===!1,Y.__autoAllocateDepthBuffer===!1&&(Y.__useRenderToTexture=!1),v.get(E.texture).__webglTexture=k,v.get(E.depthTexture).__webglTexture=Y.__autoAllocateDepthBuffer?void 0:K,Y.__hasExternalTextures=!0},this.setRenderTargetFramebuffer=function(E,k){const K=v.get(E);K.__webglFramebuffer=k,K.__useDefaultFramebuffer=k===void 0};const lo=I.createFramebuffer();this.setRenderTarget=function(E,k=0,K=0){z=E,L=k,G=K;let Y=null,X=!1,Se=!1;if(E){const xe=v.get(E);if(xe.__useDefaultFramebuffer!==void 0){Z.bindFramebuffer(I.FRAMEBUFFER,xe.__webglFramebuffer),N.copy(E.viewport),F.copy(E.scissor),ne=E.scissorTest,Z.viewport(N),Z.scissor(F),Z.setScissorTest(ne),q=-1;return}else if(xe.__webglFramebuffer===void 0)D.setupRenderTarget(E);else if(xe.__hasExternalTextures)D.rebindTextures(E,v.get(E.texture).__webglTexture,v.get(E.depthTexture).__webglTexture);else if(E.depthBuffer){const We=E.depthTexture;if(xe.__boundDepthTexture!==We){if(We!==null&&v.has(We)&&(E.width!==We.image.width||E.height!==We.image.height))throw new Error("WebGLRenderTarget: Attached DepthTexture is initialized to the incorrect size.");D.setupDepthRenderbuffer(E)}}const Pe=E.texture;(Pe.isData3DTexture||Pe.isDataArrayTexture||Pe.isCompressedArrayTexture)&&(Se=!0);const De=v.get(E).__webglFramebuffer;E.isWebGLCubeRenderTarget?(Array.isArray(De[k])?Y=De[k][K]:Y=De[k],X=!0):E.samples>0&&D.useMultisampledRTT(E)===!1?Y=v.get(E).__webglMultisampledFramebuffer:Array.isArray(De)?Y=De[K]:Y=De,N.copy(E.viewport),F.copy(E.scissor),ne=E.scissorTest}else N.copy(ee).multiplyScalar(Oe).floor(),F.copy(de).multiplyScalar(Oe).floor(),ne=pe;if(K!==0&&(Y=lo),Z.bindFramebuffer(I.FRAMEBUFFER,Y)&&Z.drawBuffers(E,Y),Z.viewport(N),Z.scissor(F),Z.setScissorTest(ne),X){const xe=v.get(E.texture);I.framebufferTexture2D(I.FRAMEBUFFER,I.COLOR_ATTACHMENT0,I.TEXTURE_CUBE_MAP_POSITIVE_X+k,xe.__webglTexture,K)}else if(Se){const xe=k;for(let Pe=0;Pe<E.textures.length;Pe++){const De=v.get(E.textures[Pe]);I.framebufferTextureLayer(I.FRAMEBUFFER,I.COLOR_ATTACHMENT0+Pe,De.__webglTexture,K,xe)}}else if(E!==null&&K!==0){const xe=v.get(E.texture);I.framebufferTexture2D(I.FRAMEBUFFER,I.COLOR_ATTACHMENT0,I.TEXTURE_2D,xe.__webglTexture,K)}q=-1},this.readRenderTargetPixels=function(E,k,K,Y,X,Se,Ae,xe=0){if(!(E&&E.isWebGLRenderTarget)){ut("WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");return}let Pe=v.get(E).__webglFramebuffer;if(E.isWebGLCubeRenderTarget&&Ae!==void 0&&(Pe=Pe[Ae]),Pe){Z.bindFramebuffer(I.FRAMEBUFFER,Pe);try{const De=E.textures[xe],We=De.format,P=De.type;if(E.textures.length>1&&I.readBuffer(I.COLOR_ATTACHMENT0+xe),!Q.textureFormatReadable(We)){ut("WebGLRenderer.readRenderTargetPixels: renderTarget is not in RGBA or implementation defined format.");return}if(!Q.textureTypeReadable(P)){ut("WebGLRenderer.readRenderTargetPixels: renderTarget is not in UnsignedByteType or implementation defined type.");return}k>=0&&k<=E.width-Y&&K>=0&&K<=E.height-X&&I.readPixels(k,K,Y,X,fe.convert(We),fe.convert(P),Se)}finally{const De=z!==null?v.get(z).__webglFramebuffer:null;Z.bindFramebuffer(I.FRAMEBUFFER,De)}}},this.readRenderTargetPixelsAsync=async function(E,k,K,Y,X,Se,Ae,xe=0){if(!(E&&E.isWebGLRenderTarget))throw new Error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");let Pe=v.get(E).__webglFramebuffer;if(E.isWebGLCubeRenderTarget&&Ae!==void 0&&(Pe=Pe[Ae]),Pe)if(k>=0&&k<=E.width-Y&&K>=0&&K<=E.height-X){Z.bindFramebuffer(I.FRAMEBUFFER,Pe);const De=E.textures[xe],We=De.format,P=De.type;if(E.textures.length>1&&I.readBuffer(I.COLOR_ATTACHMENT0+xe),!Q.textureFormatReadable(We))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in RGBA or implementation defined format.");if(!Q.textureTypeReadable(P))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in UnsignedByteType or implementation defined type.");const R=I.createBuffer();I.bindBuffer(I.PIXEL_PACK_BUFFER,R),I.bufferData(I.PIXEL_PACK_BUFFER,Se.byteLength,I.STREAM_READ),I.readPixels(k,K,Y,X,fe.convert(We),fe.convert(P),0);const oe=z!==null?v.get(z).__webglFramebuffer:null;Z.bindFramebuffer(I.FRAMEBUFFER,oe);const Me=I.fenceSync(I.SYNC_GPU_COMMANDS_COMPLETE,0);return I.flush(),await np(I,Me,4),I.bindBuffer(I.PIXEL_PACK_BUFFER,R),I.getBufferSubData(I.PIXEL_PACK_BUFFER,0,Se),I.deleteBuffer(R),I.deleteSync(Me),Se}else throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: requested read bounds are out of range.")},this.copyFramebufferToTexture=function(E,k=null,K=0){const Y=Math.pow(2,-K),X=Math.floor(E.image.width*Y),Se=Math.floor(E.image.height*Y),Ae=k!==null?k.x:0,xe=k!==null?k.y:0;D.setTexture2D(E,0),I.copyTexSubImage2D(I.TEXTURE_2D,K,0,0,Ae,xe,X,Se),Z.unbindTexture()};const js=I.createFramebuffer(),Js=I.createFramebuffer();this.copyTextureToTexture=function(E,k,K=null,Y=null,X=0,Se=0){let Ae,xe,Pe,De,We,P,R,oe,Me;const Ie=E.isCompressedTexture?E.mipmaps[Se]:E.image;if(K!==null)Ae=K.max.x-K.min.x,xe=K.max.y-K.min.y,Pe=K.isBox3?K.max.z-K.min.z:1,De=K.min.x,We=K.min.y,P=K.isBox3?K.min.z:0;else{const Ct=Math.pow(2,-X);Ae=Math.floor(Ie.width*Ct),xe=Math.floor(Ie.height*Ct),E.isDataArrayTexture?Pe=Ie.depth:E.isData3DTexture?Pe=Math.floor(Ie.depth*Ct):Pe=1,De=0,We=0,P=0}Y!==null?(R=Y.x,oe=Y.y,Me=Y.z):(R=0,oe=0,Me=0);const Ze=fe.convert(k.format),bt=fe.convert(k.type);let ve;k.isData3DTexture?(D.setTexture3D(k,0),ve=I.TEXTURE_3D):k.isDataArrayTexture||k.isCompressedArrayTexture?(D.setTexture2DArray(k,0),ve=I.TEXTURE_2D_ARRAY):(D.setTexture2D(k,0),ve=I.TEXTURE_2D),I.pixelStorei(I.UNPACK_FLIP_Y_WEBGL,k.flipY),I.pixelStorei(I.UNPACK_PREMULTIPLY_ALPHA_WEBGL,k.premultiplyAlpha),I.pixelStorei(I.UNPACK_ALIGNMENT,k.unpackAlignment);const Lt=I.getParameter(I.UNPACK_ROW_LENGTH),ze=I.getParameter(I.UNPACK_IMAGE_HEIGHT),Gt=I.getParameter(I.UNPACK_SKIP_PIXELS),Zt=I.getParameter(I.UNPACK_SKIP_ROWS),Nn=I.getParameter(I.UNPACK_SKIP_IMAGES);I.pixelStorei(I.UNPACK_ROW_LENGTH,Ie.width),I.pixelStorei(I.UNPACK_IMAGE_HEIGHT,Ie.height),I.pixelStorei(I.UNPACK_SKIP_PIXELS,De),I.pixelStorei(I.UNPACK_SKIP_ROWS,We),I.pixelStorei(I.UNPACK_SKIP_IMAGES,P);const nn=E.isDataArrayTexture||E.isData3DTexture,dt=k.isDataArrayTexture||k.isData3DTexture;if(E.isDepthTexture){const Ct=v.get(E),te=v.get(k),lt=v.get(Ct.__renderTarget),rn=v.get(te.__renderTarget);Z.bindFramebuffer(I.READ_FRAMEBUFFER,lt.__webglFramebuffer),Z.bindFramebuffer(I.DRAW_FRAMEBUFFER,rn.__webglFramebuffer);for(let fi=0;fi<Pe;fi++)nn&&(I.framebufferTextureLayer(I.READ_FRAMEBUFFER,I.COLOR_ATTACHMENT0,v.get(E).__webglTexture,X,P+fi),I.framebufferTextureLayer(I.DRAW_FRAMEBUFFER,I.COLOR_ATTACHMENT0,v.get(k).__webglTexture,Se,Me+fi)),I.blitFramebuffer(De,We,Ae,xe,R,oe,Ae,xe,I.DEPTH_BUFFER_BIT,I.NEAREST);Z.bindFramebuffer(I.READ_FRAMEBUFFER,null),Z.bindFramebuffer(I.DRAW_FRAMEBUFFER,null)}else if(X!==0||E.isRenderTargetTexture||v.has(E)){const Ct=v.get(E),te=v.get(k);Z.bindFramebuffer(I.READ_FRAMEBUFFER,js),Z.bindFramebuffer(I.DRAW_FRAMEBUFFER,Js);for(let lt=0;lt<Pe;lt++)nn?I.framebufferTextureLayer(I.READ_FRAMEBUFFER,I.COLOR_ATTACHMENT0,Ct.__webglTexture,X,P+lt):I.framebufferTexture2D(I.READ_FRAMEBUFFER,I.COLOR_ATTACHMENT0,I.TEXTURE_2D,Ct.__webglTexture,X),dt?I.framebufferTextureLayer(I.DRAW_FRAMEBUFFER,I.COLOR_ATTACHMENT0,te.__webglTexture,Se,Me+lt):I.framebufferTexture2D(I.DRAW_FRAMEBUFFER,I.COLOR_ATTACHMENT0,I.TEXTURE_2D,te.__webglTexture,Se),X!==0?I.blitFramebuffer(De,We,Ae,xe,R,oe,Ae,xe,I.COLOR_BUFFER_BIT,I.NEAREST):dt?I.copyTexSubImage3D(ve,Se,R,oe,Me+lt,De,We,Ae,xe):I.copyTexSubImage2D(ve,Se,R,oe,De,We,Ae,xe);Z.bindFramebuffer(I.READ_FRAMEBUFFER,null),Z.bindFramebuffer(I.DRAW_FRAMEBUFFER,null)}else dt?E.isDataTexture||E.isData3DTexture?I.texSubImage3D(ve,Se,R,oe,Me,Ae,xe,Pe,Ze,bt,Ie.data):k.isCompressedArrayTexture?I.compressedTexSubImage3D(ve,Se,R,oe,Me,Ae,xe,Pe,Ze,Ie.data):I.texSubImage3D(ve,Se,R,oe,Me,Ae,xe,Pe,Ze,bt,Ie):E.isDataTexture?I.texSubImage2D(I.TEXTURE_2D,Se,R,oe,Ae,xe,Ze,bt,Ie.data):E.isCompressedTexture?I.compressedTexSubImage2D(I.TEXTURE_2D,Se,R,oe,Ie.width,Ie.height,Ze,Ie.data):I.texSubImage2D(I.TEXTURE_2D,Se,R,oe,Ae,xe,Ze,bt,Ie);I.pixelStorei(I.UNPACK_ROW_LENGTH,Lt),I.pixelStorei(I.UNPACK_IMAGE_HEIGHT,ze),I.pixelStorei(I.UNPACK_SKIP_PIXELS,Gt),I.pixelStorei(I.UNPACK_SKIP_ROWS,Zt),I.pixelStorei(I.UNPACK_SKIP_IMAGES,Nn),Se===0&&k.generateMipmaps&&I.generateMipmap(ve),Z.unbindTexture()},this.initRenderTarget=function(E){v.get(E).__webglFramebuffer===void 0&&D.setupRenderTarget(E)},this.initTexture=function(E){E.isCubeTexture?D.setTextureCube(E,0):E.isData3DTexture?D.setTexture3D(E,0):E.isDataArrayTexture||E.isCompressedArrayTexture?D.setTexture2DArray(E,0):D.setTexture2D(E,0),Z.unbindTexture()},this.resetState=function(){L=0,G=0,z=null,Z.reset(),ue.reset()},typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}get coordinateSystem(){return ii}get outputColorSpace(){return this._outputColorSpace}set outputColorSpace(e){this._outputColorSpace=e;const t=this.getContext();t.drawingBufferColorSpace=ht._getDrawingBufferColorSpace(e),t.unpackColorSpace=ht._getUnpackColorSpace()}}const ih={type:"change"},Cc={type:"start"},cf={type:"end"},Ca=new $s,rh=new Vi,Vv=Math.cos(70*sp.DEG2RAD),Bt=new V,fn=2*Math.PI,gt={NONE:-1,ROTATE:0,DOLLY:1,PAN:2,TOUCH_ROTATE:3,TOUCH_PAN:4,TOUCH_DOLLY_PAN:5,TOUCH_DOLLY_ROTATE:6},qo=1e-6;class Gv extends qp{constructor(e,t=null){super(e,t),this.state=gt.NONE,this.target=new V,this.cursor=new V,this.minDistance=0,this.maxDistance=1/0,this.minZoom=0,this.maxZoom=1/0,this.minTargetRadius=0,this.maxTargetRadius=1/0,this.minPolarAngle=0,this.maxPolarAngle=Math.PI,this.minAzimuthAngle=-1/0,this.maxAzimuthAngle=1/0,this.enableDamping=!1,this.dampingFactor=.05,this.enableZoom=!0,this.zoomSpeed=1,this.enableRotate=!0,this.rotateSpeed=1,this.keyRotateSpeed=1,this.enablePan=!0,this.panSpeed=1,this.screenSpacePanning=!0,this.keyPanSpeed=7,this.zoomToCursor=!1,this.autoRotate=!1,this.autoRotateSpeed=2,this.keys={LEFT:"ArrowLeft",UP:"ArrowUp",RIGHT:"ArrowRight",BOTTOM:"ArrowDown"},this.mouseButtons={LEFT:$r.ROTATE,MIDDLE:$r.DOLLY,RIGHT:$r.PAN},this.touches={ONE:Xr.ROTATE,TWO:Xr.DOLLY_PAN},this.target0=this.target.clone(),this.position0=this.object.position.clone(),this.zoom0=this.object.zoom,this._cursorStyle="auto",this._domElementKeyEvents=null,this._lastPosition=new V,this._lastQuaternion=new ji,this._lastTargetPosition=new V,this._quat=new ji().setFromUnitVectors(e.up,new V(0,1,0)),this._quatInverse=this._quat.clone().invert(),this._spherical=new Lu,this._sphericalDelta=new Lu,this._scale=1,this._panOffset=new V,this._rotateStart=new qe,this._rotateEnd=new qe,this._rotateDelta=new qe,this._panStart=new qe,this._panEnd=new qe,this._panDelta=new qe,this._dollyStart=new qe,this._dollyEnd=new qe,this._dollyDelta=new qe,this._dollyDirection=new V,this._mouse=new qe,this._performCursorZoom=!1,this._pointers=[],this._pointerPositions={},this._controlActive=!1,this._onPointerMove=Wv.bind(this),this._onPointerDown=Hv.bind(this),this._onPointerUp=Xv.bind(this),this._onContextMenu=Jv.bind(this),this._onMouseWheel=$v.bind(this),this._onKeyDown=Kv.bind(this),this._onTouchStart=Zv.bind(this),this._onTouchMove=jv.bind(this),this._onMouseDown=qv.bind(this),this._onMouseMove=Yv.bind(this),this._interceptControlDown=Qv.bind(this),this._interceptControlUp=ex.bind(this),this.domElement!==null&&this.connect(this.domElement),this.update()}set cursorStyle(e){this._cursorStyle=e,e==="grab"?this.domElement.style.cursor="grab":this.domElement.style.cursor="auto"}get cursorStyle(){return this._cursorStyle}connect(e){super.connect(e),this.domElement.addEventListener("pointerdown",this._onPointerDown),this.domElement.addEventListener("pointercancel",this._onPointerUp),this.domElement.addEventListener("contextmenu",this._onContextMenu),this.domElement.addEventListener("wheel",this._onMouseWheel,{passive:!1}),this.domElement.getRootNode().addEventListener("keydown",this._interceptControlDown,{passive:!0,capture:!0}),this.domElement.style.touchAction="none"}disconnect(){this.domElement.removeEventListener("pointerdown",this._onPointerDown),this.domElement.ownerDocument.removeEventListener("pointermove",this._onPointerMove),this.domElement.ownerDocument.removeEventListener("pointerup",this._onPointerUp),this.domElement.removeEventListener("pointercancel",this._onPointerUp),this.domElement.removeEventListener("wheel",this._onMouseWheel),this.domElement.removeEventListener("contextmenu",this._onContextMenu),this.stopListenToKeyEvents(),this.domElement.getRootNode().removeEventListener("keydown",this._interceptControlDown,{capture:!0}),this.domElement.style.touchAction="auto"}dispose(){this.disconnect()}getPolarAngle(){return this._spherical.phi}getAzimuthalAngle(){return this._spherical.theta}getDistance(){return this.object.position.distanceTo(this.target)}listenToKeyEvents(e){e.addEventListener("keydown",this._onKeyDown),this._domElementKeyEvents=e}stopListenToKeyEvents(){this._domElementKeyEvents!==null&&(this._domElementKeyEvents.removeEventListener("keydown",this._onKeyDown),this._domElementKeyEvents=null)}saveState(){this.target0.copy(this.target),this.position0.copy(this.object.position),this.zoom0=this.object.zoom}reset(){this.target.copy(this.target0),this.object.position.copy(this.position0),this.object.zoom=this.zoom0,this.object.updateProjectionMatrix(),this.dispatchEvent(ih),this.update(),this.state=gt.NONE}pan(e,t){this._pan(e,t),this.update()}dollyIn(e){this._dollyIn(e),this.update()}dollyOut(e){this._dollyOut(e),this.update()}rotateLeft(e){this._rotateLeft(e),this.update()}rotateUp(e){this._rotateUp(e),this.update()}update(e=null){const t=this.object.position;Bt.copy(t).sub(this.target),Bt.applyQuaternion(this._quat),this._spherical.setFromVector3(Bt),this.autoRotate&&this.state===gt.NONE&&this._rotateLeft(this._getAutoRotationAngle(e)),this.enableDamping?(this._spherical.theta+=this._sphericalDelta.theta*this.dampingFactor,this._spherical.phi+=this._sphericalDelta.phi*this.dampingFactor):(this._spherical.theta+=this._sphericalDelta.theta,this._spherical.phi+=this._sphericalDelta.phi);let n=this.minAzimuthAngle,i=this.maxAzimuthAngle;isFinite(n)&&isFinite(i)&&(n<-Math.PI?n+=fn:n>Math.PI&&(n-=fn),i<-Math.PI?i+=fn:i>Math.PI&&(i-=fn),n<=i?this._spherical.theta=Math.max(n,Math.min(i,this._spherical.theta)):this._spherical.theta=this._spherical.theta>(n+i)/2?Math.max(n,this._spherical.theta):Math.min(i,this._spherical.theta)),this._spherical.phi=Math.max(this.minPolarAngle,Math.min(this.maxPolarAngle,this._spherical.phi)),this._spherical.makeSafe(),this.enableDamping===!0?this.target.addScaledVector(this._panOffset,this.dampingFactor):this.target.add(this._panOffset),this.target.sub(this.cursor),this.target.clampLength(this.minTargetRadius,this.maxTargetRadius),this.target.add(this.cursor);let s=!1;if(this.zoomToCursor&&this._performCursorZoom||this.object.isOrthographicCamera)this._spherical.radius=this._clampDistance(this._spherical.radius);else{const a=this._spherical.radius;this._spherical.radius=this._clampDistance(this._spherical.radius*this._scale),s=a!=this._spherical.radius}if(Bt.setFromSpherical(this._spherical),Bt.applyQuaternion(this._quatInverse),t.copy(this.target).add(Bt),this.object.lookAt(this.target),this.enableDamping===!0?(this._sphericalDelta.theta*=1-this.dampingFactor,this._sphericalDelta.phi*=1-this.dampingFactor,this._panOffset.multiplyScalar(1-this.dampingFactor)):(this._sphericalDelta.set(0,0,0),this._panOffset.set(0,0,0)),this.zoomToCursor&&this._performCursorZoom){let a=null;if(this.object.isPerspectiveCamera){const o=Bt.length();a=this._clampDistance(o*this._scale);const l=o-a;this.object.position.addScaledVector(this._dollyDirection,l),this.object.updateMatrixWorld(),s=!!l}else if(this.object.isOrthographicCamera){const o=new V(this._mouse.x,this._mouse.y,0);o.unproject(this.object);const l=this.object.zoom;this.object.zoom=Math.max(this.minZoom,Math.min(this.maxZoom,this.object.zoom/this._scale)),this.object.updateProjectionMatrix(),s=l!==this.object.zoom;const c=new V(this._mouse.x,this._mouse.y,0);c.unproject(this.object),this.object.position.sub(c).add(o),this.object.updateMatrixWorld(),a=Bt.length()}else console.warn("WARNING: OrbitControls.js encountered an unknown camera type - zoom to cursor disabled."),this.zoomToCursor=!1;a!==null&&(this.screenSpacePanning?this.target.set(0,0,-1).transformDirection(this.object.matrix).multiplyScalar(a).add(this.object.position):(Ca.origin.copy(this.object.position),Ca.direction.set(0,0,-1).transformDirection(this.object.matrix),Math.abs(this.object.up.dot(Ca.direction))<Vv?this.object.lookAt(this.target):(rh.setFromNormalAndCoplanarPoint(this.object.up,this.target),Ca.intersectPlane(rh,this.target))))}else if(this.object.isOrthographicCamera){const a=this.object.zoom;this.object.zoom=Math.max(this.minZoom,Math.min(this.maxZoom,this.object.zoom/this._scale)),a!==this.object.zoom&&(this.object.updateProjectionMatrix(),s=!0)}return this._scale=1,this._performCursorZoom=!1,s||this._lastPosition.distanceToSquared(this.object.position)>qo||8*(1-this._lastQuaternion.dot(this.object.quaternion))>qo||this._lastTargetPosition.distanceToSquared(this.target)>qo?(this.dispatchEvent(ih),this._lastPosition.copy(this.object.position),this._lastQuaternion.copy(this.object.quaternion),this._lastTargetPosition.copy(this.target),!0):!1}_getAutoRotationAngle(e){return e!==null?fn/60*this.autoRotateSpeed*e:fn/60/60*this.autoRotateSpeed}_getZoomScale(e){const t=Math.abs(e*.01);return Math.pow(.95,this.zoomSpeed*t)}_rotateLeft(e){this._sphericalDelta.theta-=e}_rotateUp(e){this._sphericalDelta.phi-=e}_panLeft(e,t){Bt.setFromMatrixColumn(t,0),Bt.multiplyScalar(-e),this._panOffset.add(Bt)}_panUp(e,t){this.screenSpacePanning===!0?Bt.setFromMatrixColumn(t,1):(Bt.setFromMatrixColumn(t,0),Bt.crossVectors(this.object.up,Bt)),Bt.multiplyScalar(e),this._panOffset.add(Bt)}_pan(e,t){const n=this.domElement;if(this.object.isPerspectiveCamera){const i=this.object.position;Bt.copy(i).sub(this.target);let s=Bt.length();s*=Math.tan(this.object.fov/2*Math.PI/180),this._panLeft(2*e*s/n.clientHeight,this.object.matrix),this._panUp(2*t*s/n.clientHeight,this.object.matrix)}else this.object.isOrthographicCamera?(this._panLeft(e*(this.object.right-this.object.left)/this.object.zoom/n.clientWidth,this.object.matrix),this._panUp(t*(this.object.top-this.object.bottom)/this.object.zoom/n.clientHeight,this.object.matrix)):(console.warn("WARNING: OrbitControls.js encountered an unknown camera type - pan disabled."),this.enablePan=!1)}_dollyOut(e){this.object.isPerspectiveCamera||this.object.isOrthographicCamera?this._scale/=e:(console.warn("WARNING: OrbitControls.js encountered an unknown camera type - dolly/zoom disabled."),this.enableZoom=!1)}_dollyIn(e){this.object.isPerspectiveCamera||this.object.isOrthographicCamera?this._scale*=e:(console.warn("WARNING: OrbitControls.js encountered an unknown camera type - dolly/zoom disabled."),this.enableZoom=!1)}_updateZoomParameters(e,t){if(!this.zoomToCursor)return;this._performCursorZoom=!0;const n=this.domElement.getBoundingClientRect(),i=e-n.left,s=t-n.top,a=n.width,o=n.height;this._mouse.x=i/a*2-1,this._mouse.y=-(s/o)*2+1,this._dollyDirection.set(this._mouse.x,this._mouse.y,1).unproject(this.object).sub(this.object.position).normalize()}_clampDistance(e){return Math.max(this.minDistance,Math.min(this.maxDistance,e))}_handleMouseDownRotate(e){this._rotateStart.set(e.clientX,e.clientY)}_handleMouseDownDolly(e){this._updateZoomParameters(e.clientX,e.clientX),this._dollyStart.set(e.clientX,e.clientY)}_handleMouseDownPan(e){this._panStart.set(e.clientX,e.clientY)}_handleMouseMoveRotate(e){this._rotateEnd.set(e.clientX,e.clientY),this._rotateDelta.subVectors(this._rotateEnd,this._rotateStart).multiplyScalar(this.rotateSpeed);const t=this.domElement;this._rotateLeft(fn*this._rotateDelta.x/t.clientHeight),this._rotateUp(fn*this._rotateDelta.y/t.clientHeight),this._rotateStart.copy(this._rotateEnd),this.update()}_handleMouseMoveDolly(e){this._dollyEnd.set(e.clientX,e.clientY),this._dollyDelta.subVectors(this._dollyEnd,this._dollyStart),this._dollyDelta.y>0?this._dollyOut(this._getZoomScale(this._dollyDelta.y)):this._dollyDelta.y<0&&this._dollyIn(this._getZoomScale(this._dollyDelta.y)),this._dollyStart.copy(this._dollyEnd),this.update()}_handleMouseMovePan(e){this._panEnd.set(e.clientX,e.clientY),this._panDelta.subVectors(this._panEnd,this._panStart).multiplyScalar(this.panSpeed),this._pan(this._panDelta.x,this._panDelta.y),this._panStart.copy(this._panEnd),this.update()}_handleMouseWheel(e){this._updateZoomParameters(e.clientX,e.clientY),e.deltaY<0?this._dollyIn(this._getZoomScale(e.deltaY)):e.deltaY>0&&this._dollyOut(this._getZoomScale(e.deltaY)),this.update()}_handleKeyDown(e){let t=!1;switch(e.code){case this.keys.UP:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateUp(fn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(0,this.keyPanSpeed),t=!0;break;case this.keys.BOTTOM:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateUp(-fn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(0,-this.keyPanSpeed),t=!0;break;case this.keys.LEFT:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateLeft(fn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(this.keyPanSpeed,0),t=!0;break;case this.keys.RIGHT:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateLeft(-fn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(-this.keyPanSpeed,0),t=!0;break}t&&(e.preventDefault(),this.update())}_handleTouchStartRotate(e){if(this._pointers.length===1)this._rotateStart.set(e.pageX,e.pageY);else{const t=this._getSecondPointerPosition(e),n=.5*(e.pageX+t.x),i=.5*(e.pageY+t.y);this._rotateStart.set(n,i)}}_handleTouchStartPan(e){if(this._pointers.length===1)this._panStart.set(e.pageX,e.pageY);else{const t=this._getSecondPointerPosition(e),n=.5*(e.pageX+t.x),i=.5*(e.pageY+t.y);this._panStart.set(n,i)}}_handleTouchStartDolly(e){const t=this._getSecondPointerPosition(e),n=e.pageX-t.x,i=e.pageY-t.y,s=Math.sqrt(n*n+i*i);this._dollyStart.set(0,s)}_handleTouchStartDollyPan(e){this.enableZoom&&this._handleTouchStartDolly(e),this.enablePan&&this._handleTouchStartPan(e)}_handleTouchStartDollyRotate(e){this.enableZoom&&this._handleTouchStartDolly(e),this.enableRotate&&this._handleTouchStartRotate(e)}_handleTouchMoveRotate(e){if(this._pointers.length==1)this._rotateEnd.set(e.pageX,e.pageY);else{const n=this._getSecondPointerPosition(e),i=.5*(e.pageX+n.x),s=.5*(e.pageY+n.y);this._rotateEnd.set(i,s)}this._rotateDelta.subVectors(this._rotateEnd,this._rotateStart).multiplyScalar(this.rotateSpeed);const t=this.domElement;this._rotateLeft(fn*this._rotateDelta.x/t.clientHeight),this._rotateUp(fn*this._rotateDelta.y/t.clientHeight),this._rotateStart.copy(this._rotateEnd)}_handleTouchMovePan(e){if(this._pointers.length===1)this._panEnd.set(e.pageX,e.pageY);else{const t=this._getSecondPointerPosition(e),n=.5*(e.pageX+t.x),i=.5*(e.pageY+t.y);this._panEnd.set(n,i)}this._panDelta.subVectors(this._panEnd,this._panStart).multiplyScalar(this.panSpeed),this._pan(this._panDelta.x,this._panDelta.y),this._panStart.copy(this._panEnd)}_handleTouchMoveDolly(e){const t=this._getSecondPointerPosition(e),n=e.pageX-t.x,i=e.pageY-t.y,s=Math.sqrt(n*n+i*i);this._dollyEnd.set(0,s),this._dollyDelta.set(0,Math.pow(this._dollyEnd.y/this._dollyStart.y,this.zoomSpeed)),this._dollyOut(this._dollyDelta.y),this._dollyStart.copy(this._dollyEnd);const a=(e.pageX+t.x)*.5,o=(e.pageY+t.y)*.5;this._updateZoomParameters(a,o)}_handleTouchMoveDollyPan(e){this.enableZoom&&this._handleTouchMoveDolly(e),this.enablePan&&this._handleTouchMovePan(e)}_handleTouchMoveDollyRotate(e){this.enableZoom&&this._handleTouchMoveDolly(e),this.enableRotate&&this._handleTouchMoveRotate(e)}_addPointer(e){this._pointers.push(e.pointerId)}_removePointer(e){delete this._pointerPositions[e.pointerId];for(let t=0;t<this._pointers.length;t++)if(this._pointers[t]==e.pointerId){this._pointers.splice(t,1);return}}_isTrackingPointer(e){for(let t=0;t<this._pointers.length;t++)if(this._pointers[t]==e.pointerId)return!0;return!1}_trackPointer(e){let t=this._pointerPositions[e.pointerId];t===void 0&&(t=new qe,this._pointerPositions[e.pointerId]=t),t.set(e.pageX,e.pageY)}_getSecondPointerPosition(e){const t=e.pointerId===this._pointers[0]?this._pointers[1]:this._pointers[0];return this._pointerPositions[t]}_customWheelEvent(e){const t=e.deltaMode,n={clientX:e.clientX,clientY:e.clientY,deltaY:e.deltaY};switch(t){case 1:n.deltaY*=16;break;case 2:n.deltaY*=100;break}return e.ctrlKey&&!this._controlActive&&(n.deltaY*=10),n}}function Hv(r){this.enabled!==!1&&(this._pointers.length===0&&(this.domElement.setPointerCapture(r.pointerId),this.domElement.ownerDocument.addEventListener("pointermove",this._onPointerMove),this.domElement.ownerDocument.addEventListener("pointerup",this._onPointerUp)),!this._isTrackingPointer(r)&&(this._addPointer(r),r.pointerType==="touch"?this._onTouchStart(r):this._onMouseDown(r),this._cursorStyle==="grab"&&(this.domElement.style.cursor="grabbing")))}function Wv(r){this.enabled!==!1&&(r.pointerType==="touch"?this._onTouchMove(r):this._onMouseMove(r))}function Xv(r){switch(this._removePointer(r),this._pointers.length){case 0:this.domElement.releasePointerCapture(r.pointerId),this.domElement.ownerDocument.removeEventListener("pointermove",this._onPointerMove),this.domElement.ownerDocument.removeEventListener("pointerup",this._onPointerUp),this.dispatchEvent(cf),this.state=gt.NONE,this._cursorStyle==="grab"&&(this.domElement.style.cursor="grab");break;case 1:const e=this._pointers[0],t=this._pointerPositions[e];this._onTouchStart({pointerId:e,pageX:t.x,pageY:t.y});break}}function qv(r){let e;switch(r.button){case 0:e=this.mouseButtons.LEFT;break;case 1:e=this.mouseButtons.MIDDLE;break;case 2:e=this.mouseButtons.RIGHT;break;default:e=-1}switch(e){case $r.DOLLY:if(this.enableZoom===!1)return;this._handleMouseDownDolly(r),this.state=gt.DOLLY;break;case $r.ROTATE:if(r.ctrlKey||r.metaKey||r.shiftKey){if(this.enablePan===!1)return;this._handleMouseDownPan(r),this.state=gt.PAN}else{if(this.enableRotate===!1)return;this._handleMouseDownRotate(r),this.state=gt.ROTATE}break;case $r.PAN:if(r.ctrlKey||r.metaKey||r.shiftKey){if(this.enableRotate===!1)return;this._handleMouseDownRotate(r),this.state=gt.ROTATE}else{if(this.enablePan===!1)return;this._handleMouseDownPan(r),this.state=gt.PAN}break;default:this.state=gt.NONE}this.state!==gt.NONE&&this.dispatchEvent(Cc)}function Yv(r){switch(this.state){case gt.ROTATE:if(this.enableRotate===!1)return;this._handleMouseMoveRotate(r);break;case gt.DOLLY:if(this.enableZoom===!1)return;this._handleMouseMoveDolly(r);break;case gt.PAN:if(this.enablePan===!1)return;this._handleMouseMovePan(r);break}}function $v(r){this.enabled===!1||this.enableZoom===!1||this.state!==gt.NONE||(r.preventDefault(),this.dispatchEvent(Cc),this._handleMouseWheel(this._customWheelEvent(r)),this.dispatchEvent(cf))}function Kv(r){this.enabled!==!1&&this._handleKeyDown(r)}function Zv(r){switch(this._trackPointer(r),this._pointers.length){case 1:switch(this.touches.ONE){case Xr.ROTATE:if(this.enableRotate===!1)return;this._handleTouchStartRotate(r),this.state=gt.TOUCH_ROTATE;break;case Xr.PAN:if(this.enablePan===!1)return;this._handleTouchStartPan(r),this.state=gt.TOUCH_PAN;break;default:this.state=gt.NONE}break;case 2:switch(this.touches.TWO){case Xr.DOLLY_PAN:if(this.enableZoom===!1&&this.enablePan===!1)return;this._handleTouchStartDollyPan(r),this.state=gt.TOUCH_DOLLY_PAN;break;case Xr.DOLLY_ROTATE:if(this.enableZoom===!1&&this.enableRotate===!1)return;this._handleTouchStartDollyRotate(r),this.state=gt.TOUCH_DOLLY_ROTATE;break;default:this.state=gt.NONE}break;default:this.state=gt.NONE}this.state!==gt.NONE&&this.dispatchEvent(Cc)}function jv(r){switch(this._trackPointer(r),this.state){case gt.TOUCH_ROTATE:if(this.enableRotate===!1)return;this._handleTouchMoveRotate(r),this.update();break;case gt.TOUCH_PAN:if(this.enablePan===!1)return;this._handleTouchMovePan(r),this.update();break;case gt.TOUCH_DOLLY_PAN:if(this.enableZoom===!1&&this.enablePan===!1)return;this._handleTouchMoveDollyPan(r),this.update();break;case gt.TOUCH_DOLLY_ROTATE:if(this.enableZoom===!1&&this.enableRotate===!1)return;this._handleTouchMoveDollyRotate(r),this.update();break;default:this.state=gt.NONE}}function Jv(r){this.enabled!==!1&&r.preventDefault()}function Qv(r){r.key==="Control"&&(this._controlActive=!0,this.domElement.getRootNode().addEventListener("keyup",this._interceptControlUp,{passive:!0,capture:!0}))}function ex(r){r.key==="Control"&&(this._controlActive=!1,this.domElement.getRootNode().removeEventListener("keyup",this._interceptControlUp,{passive:!0,capture:!0}))}class Gr extends kt{constructor(e=document.createElement("div")){super(),this.isCSS2DObject=!0,this.element=e,this.element.style.position="absolute",this.element.style.userSelect="none",this.element.setAttribute("draggable",!1),this.center=new qe(.5,.5),this.addEventListener("removed",function(){this.traverse(function(t){t.element&&t.element instanceof t.element.ownerDocument.defaultView.Element&&t.element.parentNode!==null&&t.element.remove()})})}copy(e,t){return super.copy(e,t),this.element=e.element.cloneNode(!0),this.center=e.center,this}}const Hr=new V,sh=new St,ah=new St,oh=new V,lh=new V;class tx{constructor(e={}){const t=this;let n,i,s,a;const o={objects:new WeakMap},l=e.element!==void 0?e.element:document.createElement("div");l.style.overflow="hidden",this.domElement=l,this.sortObjects=!0,this.getSize=function(){return{width:n,height:i}},this.render=function(_,g){_.matrixWorldAutoUpdate===!0&&_.updateMatrixWorld(),g.parent===null&&g.matrixWorldAutoUpdate===!0&&g.updateMatrixWorld(),sh.copy(g.matrixWorldInverse),ah.multiplyMatrices(g.projectionMatrix,sh),u(_,_,g),this.sortObjects&&d(_)},this.setSize=function(_,g){n=_,i=g,s=n/2,a=i/2,l.style.width=_+"px",l.style.height=g+"px"};function c(_){_.isCSS2DObject&&(_.element.style.display="none");for(let g=0,p=_.children.length;g<p;g++)c(_.children[g])}function u(_,g,p){if(_.visible===!1){c(_);return}if(_.isCSS2DObject){Hr.setFromMatrixPosition(_.matrixWorld),Hr.applyMatrix4(ah);const m=Hr.z>=-1&&Hr.z<=1&&_.layers.test(p.layers)===!0,M=_.element;M.style.display=m===!0?"":"none",m===!0&&(_.onBeforeRender(t,g,p),M.style.transform="translate("+-100*_.center.x+"%,"+-100*_.center.y+"%)translate("+(Hr.x*s+s)+"px,"+(-Hr.y*a+a)+"px)",M.parentNode!==l&&l.appendChild(M),_.onAfterRender(t,g,p));const S={distanceToCameraSquared:f(p,_)};o.objects.set(_,S)}for(let m=0,M=_.children.length;m<M;m++)u(_.children[m],g,p)}function f(_,g){return oh.setFromMatrixPosition(_.matrixWorld),lh.setFromMatrixPosition(g.matrixWorld),oh.distanceToSquared(lh)}function h(_){const g=[];return _.traverseVisible(function(p){p.isCSS2DObject&&g.push(p)}),g}function d(_){const g=h(_).sort(function(m,M){if(m.renderOrder!==M.renderOrder)return M.renderOrder-m.renderOrder;const S=o.objects.get(m).distanceToCameraSquared,y=o.objects.get(M).distanceToCameraSquared;return S-y}),p=g.length;for(let m=0,M=g.length;m<M;m++)g[m].element.style.zIndex=p-m}}}function xi(r){if(r===void 0)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return r}function uf(r,e){r.prototype=Object.create(e.prototype),r.prototype.constructor=r,r.__proto__=e}/*!
 * GSAP 3.14.2
 * https://gsap.com
 *
 * @license Copyright 2008-2025, GreenSock. All rights reserved.
 * Subject to the terms at https://gsap.com/standard-license
 * @author: Jack Doyle, jack@greensock.com
*/var Ln={autoSleep:120,force3D:"auto",nullTargetWarn:1,units:{lineHeight:""}},ss={duration:.5,overwrite:!1,delay:0},Pc,$t,Et,zn=1e8,Mt=1/zn,Kl=Math.PI*2,nx=Kl/4,ix=0,hf=Math.sqrt,rx=Math.cos,sx=Math.sin,Wt=function(e){return typeof e=="string"},Dt=function(e){return typeof e=="function"},wi=function(e){return typeof e=="number"},Dc=function(e){return typeof e>"u"},ui=function(e){return typeof e=="object"},_n=function(e){return e!==!1},Lc=function(){return typeof window<"u"},Pa=function(e){return Dt(e)||Wt(e)},ff=typeof ArrayBuffer=="function"&&ArrayBuffer.isView||function(){},tn=Array.isArray,ax=/random\([^)]+\)/g,ox=/,\s*/g,ch=/(?:-?\.?\d|\.)+/gi,df=/[-+=.]*\d+[.e\-+]*\d*[e\-+]*\d*/g,qr=/[-+=.]*\d+[.e-]*\d*[a-z%]*/g,Yo=/[-+=.]*\d+\.?\d*(?:e-|e\+)?\d*/gi,pf=/[+-]=-?[.\d]+/,lx=/[^,'"\[\]\s]+/gi,cx=/^[+\-=e\s\d]*\d+[.\d]*([a-z]*|%)\s*$/i,wt,Qn,Zl,Ic,In={},Ya={},mf,_f=function(e){return(Ya=as(e,In))&&Mn},Uc=function(e,t){return console.warn("Invalid property",e,"set to",t,"Missing plugin? gsap.registerPlugin()")},ks=function(e,t){return!t&&console.warn(e)},gf=function(e,t){return e&&(In[e]=t)&&Ya&&(Ya[e]=t)||In},zs=function(){return 0},ux={suppressEvents:!0,isStart:!0,kill:!1},Ba={suppressEvents:!0,kill:!1},hx={suppressEvents:!0},Nc={},$i=[],jl={},vf,An={},$o={},uh=30,ka=[],Fc="",Oc=function(e){var t=e[0],n,i;if(ui(t)||Dt(t)||(e=[e]),!(n=(t._gsap||{}).harness)){for(i=ka.length;i--&&!ka[i].targetTest(t););n=ka[i]}for(i=e.length;i--;)e[i]&&(e[i]._gsap||(e[i]._gsap=new Gf(e[i],n)))||e.splice(i,1);return e},vr=function(e){return e._gsap||Oc(Vn(e))[0]._gsap},xf=function(e,t,n){return(n=e[t])&&Dt(n)?e[t]():Dc(n)&&e.getAttribute&&e.getAttribute(t)||n},gn=function(e,t){return(e=e.split(",")).forEach(t)||e},It=function(e){return Math.round(e*1e5)/1e5||0},At=function(e){return Math.round(e*1e7)/1e7||0},jr=function(e,t){var n=t.charAt(0),i=parseFloat(t.substr(2));return e=parseFloat(e),n==="+"?e+i:n==="-"?e-i:n==="*"?e*i:e/i},fx=function(e,t){for(var n=t.length,i=0;e.indexOf(t[i])<0&&++i<n;);return i<n},$a=function(){var e=$i.length,t=$i.slice(0),n,i;for(jl={},$i.length=0,n=0;n<e;n++)i=t[n],i&&i._lazy&&(i.render(i._lazy[0],i._lazy[1],!0)._lazy=0)},Bc=function(e){return!!(e._initted||e._startAt||e.add)},Mf=function(e,t,n,i){$i.length&&!$t&&$a(),e.render(t,n,!!($t&&t<0&&Bc(e))),$i.length&&!$t&&$a()},Sf=function(e){var t=parseFloat(e);return(t||t===0)&&(e+"").match(lx).length<2?t:Wt(e)?e.trim():e},yf=function(e){return e},Un=function(e,t){for(var n in t)n in e||(e[n]=t[n]);return e},dx=function(e){return function(t,n){for(var i in n)i in t||i==="duration"&&e||i==="ease"||(t[i]=n[i])}},as=function(e,t){for(var n in t)e[n]=t[n];return e},hh=function r(e,t){for(var n in t)n!=="__proto__"&&n!=="constructor"&&n!=="prototype"&&(e[n]=ui(t[n])?r(e[n]||(e[n]={}),t[n]):t[n]);return e},Ka=function(e,t){var n={},i;for(i in e)i in t||(n[i]=e[i]);return n},Ls=function(e){var t=e.parent||wt,n=e.keyframes?dx(tn(e.keyframes)):Un;if(_n(e.inherit))for(;t;)n(e,t.vars.defaults),t=t.parent||t._dp;return e},px=function(e,t){for(var n=e.length,i=n===t.length;i&&n--&&e[n]===t[n];);return n<0},bf=function(e,t,n,i,s){var a=e[i],o;if(s)for(o=t[s];a&&a[s]>o;)a=a._prev;return a?(t._next=a._next,a._next=t):(t._next=e[n],e[n]=t),t._next?t._next._prev=t:e[i]=t,t._prev=a,t.parent=t._dp=e,t},io=function(e,t,n,i){n===void 0&&(n="_first"),i===void 0&&(i="_last");var s=t._prev,a=t._next;s?s._next=a:e[n]===t&&(e[n]=a),a?a._prev=s:e[i]===t&&(e[i]=s),t._next=t._prev=t.parent=null},Ji=function(e,t){e.parent&&(!t||e.parent.autoRemoveChildren)&&e.parent.remove&&e.parent.remove(e),e._act=0},xr=function(e,t){if(e&&(!t||t._end>e._dur||t._start<0))for(var n=e;n;)n._dirty=1,n=n.parent;return e},mx=function(e){for(var t=e.parent;t&&t.parent;)t._dirty=1,t.totalDuration(),t=t.parent;return e},Jl=function(e,t,n,i){return e._startAt&&($t?e._startAt.revert(Ba):e.vars.immediateRender&&!e.vars.autoRevert||e._startAt.render(t,!0,i))},_x=function r(e){return!e||e._ts&&r(e.parent)},fh=function(e){return e._repeat?os(e._tTime,e=e.duration()+e._rDelay)*e:0},os=function(e,t){var n=Math.floor(e=At(e/t));return e&&n===e?n-1:n},Za=function(e,t){return(e-t._start)*t._ts+(t._ts>=0?0:t._dirty?t.totalDuration():t._tDur)},ro=function(e){return e._end=At(e._start+(e._tDur/Math.abs(e._ts||e._rts||Mt)||0))},so=function(e,t){var n=e._dp;return n&&n.smoothChildTiming&&e._ts&&(e._start=At(n._time-(e._ts>0?t/e._ts:((e._dirty?e.totalDuration():e._tDur)-t)/-e._ts)),ro(e),n._dirty||xr(n,e)),e},Ef=function(e,t){var n;if((t._time||!t._dur&&t._initted||t._start<e._time&&(t._dur||!t.add))&&(n=Za(e.rawTime(),t),(!t._dur||Zs(0,t.totalDuration(),n)-t._tTime>Mt)&&t.render(n,!0)),xr(e,t)._dp&&e._initted&&e._time>=e._dur&&e._ts){if(e._dur<e.duration())for(n=e;n._dp;)n.rawTime()>=0&&n.totalTime(n._tTime),n=n._dp;e._zTime=-Mt}},ti=function(e,t,n,i){return t.parent&&Ji(t),t._start=At((wi(n)?n:n||e!==wt?Bn(e,n,t):e._time)+t._delay),t._end=At(t._start+(t.totalDuration()/Math.abs(t.timeScale())||0)),bf(e,t,"_first","_last",e._sort?"_start":0),Ql(t)||(e._recent=t),i||Ef(e,t),e._ts<0&&so(e,e._tTime),e},Tf=function(e,t){return(In.ScrollTrigger||Uc("scrollTrigger",t))&&In.ScrollTrigger.create(t,e)},Af=function(e,t,n,i,s){if(zc(e,t,s),!e._initted)return 1;if(!n&&e._pt&&!$t&&(e._dur&&e.vars.lazy!==!1||!e._dur&&e.vars.lazy)&&vf!==Cn.frame)return $i.push(e),e._lazy=[s,i],1},gx=function r(e){var t=e.parent;return t&&t._ts&&t._initted&&!t._lock&&(t.rawTime()<0||r(t))},Ql=function(e){var t=e.data;return t==="isFromStart"||t==="isStart"},vx=function(e,t,n,i){var s=e.ratio,a=t<0||!t&&(!e._start&&gx(e)&&!(!e._initted&&Ql(e))||(e._ts<0||e._dp._ts<0)&&!Ql(e))?0:1,o=e._rDelay,l=0,c,u,f;if(o&&e._repeat&&(l=Zs(0,e._tDur,t),u=os(l,o),e._yoyo&&u&1&&(a=1-a),u!==os(e._tTime,o)&&(s=1-a,e.vars.repeatRefresh&&e._initted&&e.invalidate())),a!==s||$t||i||e._zTime===Mt||!t&&e._zTime){if(!e._initted&&Af(e,t,i,n,l))return;for(f=e._zTime,e._zTime=t||(n?Mt:0),n||(n=t&&!f),e.ratio=a,e._from&&(a=1-a),e._time=0,e._tTime=l,c=e._pt;c;)c.r(a,c.d),c=c._next;t<0&&Jl(e,t,n,!0),e._onUpdate&&!n&&Pn(e,"onUpdate"),l&&e._repeat&&!n&&e.parent&&Pn(e,"onRepeat"),(t>=e._tDur||t<0)&&e.ratio===a&&(a&&Ji(e,1),!n&&!$t&&(Pn(e,a?"onComplete":"onReverseComplete",!0),e._prom&&e._prom()))}else e._zTime||(e._zTime=t)},xx=function(e,t,n){var i;if(n>t)for(i=e._first;i&&i._start<=n;){if(i.data==="isPause"&&i._start>t)return i;i=i._next}else for(i=e._last;i&&i._start>=n;){if(i.data==="isPause"&&i._start<t)return i;i=i._prev}},ls=function(e,t,n,i){var s=e._repeat,a=At(t)||0,o=e._tTime/e._tDur;return o&&!i&&(e._time*=a/e._dur),e._dur=a,e._tDur=s?s<0?1e10:At(a*(s+1)+e._rDelay*s):a,o>0&&!i&&so(e,e._tTime=e._tDur*o),e.parent&&ro(e),n||xr(e.parent,e),e},dh=function(e){return e instanceof an?xr(e):ls(e,e._dur)},Mx={_start:0,endTime:zs,totalDuration:zs},Bn=function r(e,t,n){var i=e.labels,s=e._recent||Mx,a=e.duration()>=zn?s.endTime(!1):e._dur,o,l,c;return Wt(t)&&(isNaN(t)||t in i)?(l=t.charAt(0),c=t.substr(-1)==="%",o=t.indexOf("="),l==="<"||l===">"?(o>=0&&(t=t.replace(/=/,"")),(l==="<"?s._start:s.endTime(s._repeat>=0))+(parseFloat(t.substr(1))||0)*(c?(o<0?s:n).totalDuration()/100:1)):o<0?(t in i||(i[t]=a),i[t]):(l=parseFloat(t.charAt(o-1)+t.substr(o+1)),c&&n&&(l=l/100*(tn(n)?n[0]:n).totalDuration()),o>1?r(e,t.substr(0,o-1),n)+l:a+l)):t==null?a:+t},Is=function(e,t,n){var i=wi(t[1]),s=(i?2:1)+(e<2?0:1),a=t[s],o,l;if(i&&(a.duration=t[1]),a.parent=n,e){for(o=a,l=n;l&&!("immediateRender"in o);)o=l.vars.defaults||{},l=_n(l.vars.inherit)&&l.parent;a.immediateRender=_n(o.immediateRender),e<2?a.runBackwards=1:a.startAt=t[s-1]}return new Ft(t[0],a,t[s+1])},tr=function(e,t){return e||e===0?t(e):t},Zs=function(e,t,n){return n<e?e:n>t?t:n},Qt=function(e,t){return!Wt(e)||!(t=cx.exec(e))?"":t[1]},Sx=function(e,t,n){return tr(n,function(i){return Zs(e,t,i)})},ec=[].slice,wf=function(e,t){return e&&ui(e)&&"length"in e&&(!t&&!e.length||e.length-1 in e&&ui(e[0]))&&!e.nodeType&&e!==Qn},yx=function(e,t,n){return n===void 0&&(n=[]),e.forEach(function(i){var s;return Wt(i)&&!t||wf(i,1)?(s=n).push.apply(s,Vn(i)):n.push(i)})||n},Vn=function(e,t,n){return Et&&!t&&Et.selector?Et.selector(e):Wt(e)&&!n&&(Zl||!cs())?ec.call((t||Ic).querySelectorAll(e),0):tn(e)?yx(e,n):wf(e)?ec.call(e,0):e?[e]:[]},tc=function(e){return e=Vn(e)[0]||ks("Invalid scope")||{},function(t){var n=e.current||e.nativeElement||e;return Vn(t,n.querySelectorAll?n:n===e?ks("Invalid scope")||Ic.createElement("div"):e)}},Rf=function(e){return e.sort(function(){return .5-Math.random()})},Cf=function(e){if(Dt(e))return e;var t=ui(e)?e:{each:e},n=Mr(t.ease),i=t.from||0,s=parseFloat(t.base)||0,a={},o=i>0&&i<1,l=isNaN(i)||o,c=t.axis,u=i,f=i;return Wt(i)?u=f={center:.5,edges:.5,end:1}[i]||0:!o&&l&&(u=i[0],f=i[1]),function(h,d,_){var g=(_||t).length,p=a[g],m,M,S,y,A,w,C,x,b;if(!p){if(b=t.grid==="auto"?0:(t.grid||[1,zn])[1],!b){for(C=-zn;C<(C=_[b++].getBoundingClientRect().left)&&b<g;);b<g&&b--}for(p=a[g]=[],m=l?Math.min(b,g)*u-.5:i%b,M=b===zn?0:l?g*f/b-.5:i/b|0,C=0,x=zn,w=0;w<g;w++)S=w%b-m,y=M-(w/b|0),p[w]=A=c?Math.abs(c==="y"?y:S):hf(S*S+y*y),A>C&&(C=A),A<x&&(x=A);i==="random"&&Rf(p),p.max=C-x,p.min=x,p.v=g=(parseFloat(t.amount)||parseFloat(t.each)*(b>g?g-1:c?c==="y"?g/b:b:Math.max(b,g/b))||0)*(i==="edges"?-1:1),p.b=g<0?s-g:s,p.u=Qt(t.amount||t.each)||0,n=n&&g<0?kf(n):n}return g=(p[h]-p.min)/p.max||0,At(p.b+(n?n(g):g)*p.v)+p.u}},nc=function(e){var t=Math.pow(10,((e+"").split(".")[1]||"").length);return function(n){var i=At(Math.round(parseFloat(n)/e)*e*t);return(i-i%1)/t+(wi(n)?0:Qt(n))}},Pf=function(e,t){var n=tn(e),i,s;return!n&&ui(e)&&(i=n=e.radius||zn,e.values?(e=Vn(e.values),(s=!wi(e[0]))&&(i*=i)):e=nc(e.increment)),tr(t,n?Dt(e)?function(a){return s=e(a),Math.abs(s-a)<=i?s:a}:function(a){for(var o=parseFloat(s?a.x:a),l=parseFloat(s?a.y:0),c=zn,u=0,f=e.length,h,d;f--;)s?(h=e[f].x-o,d=e[f].y-l,h=h*h+d*d):h=Math.abs(e[f]-o),h<c&&(c=h,u=f);return u=!i||c<=i?e[u]:a,s||u===a||wi(a)?u:u+Qt(a)}:nc(e))},Df=function(e,t,n,i){return tr(tn(e)?!t:n===!0?!!(n=0):!i,function(){return tn(e)?e[~~(Math.random()*e.length)]:(n=n||1e-5)&&(i=n<1?Math.pow(10,(n+"").length-2):1)&&Math.floor(Math.round((e-n/2+Math.random()*(t-e+n*.99))/n)*n*i)/i})},bx=function(){for(var e=arguments.length,t=new Array(e),n=0;n<e;n++)t[n]=arguments[n];return function(i){return t.reduce(function(s,a){return a(s)},i)}},Ex=function(e,t){return function(n){return e(parseFloat(n))+(t||Qt(n))}},Tx=function(e,t,n){return If(e,t,0,1,n)},Lf=function(e,t,n){return tr(n,function(i){return e[~~t(i)]})},Ax=function r(e,t,n){var i=t-e;return tn(e)?Lf(e,r(0,e.length),t):tr(n,function(s){return(i+(s-e)%i)%i+e})},wx=function r(e,t,n){var i=t-e,s=i*2;return tn(e)?Lf(e,r(0,e.length-1),t):tr(n,function(a){return a=(s+(a-e)%s)%s||0,e+(a>i?s-a:a)})},Vs=function(e){return e.replace(ax,function(t){var n=t.indexOf("[")+1,i=t.substring(n||7,n?t.indexOf("]"):t.length-1).split(ox);return Df(n?i:+i[0],n?0:+i[1],+i[2]||1e-5)})},If=function(e,t,n,i,s){var a=t-e,o=i-n;return tr(s,function(l){return n+((l-e)/a*o||0)})},Rx=function r(e,t,n,i){var s=isNaN(e+t)?0:function(d){return(1-d)*e+d*t};if(!s){var a=Wt(e),o={},l,c,u,f,h;if(n===!0&&(i=1)&&(n=null),a)e={p:e},t={p:t};else if(tn(e)&&!tn(t)){for(u=[],f=e.length,h=f-2,c=1;c<f;c++)u.push(r(e[c-1],e[c]));f--,s=function(_){_*=f;var g=Math.min(h,~~_);return u[g](_-g)},n=t}else i||(e=as(tn(e)?[]:{},e));if(!u){for(l in t)kc.call(o,e,l,"get",t[l]);s=function(_){return Hc(_,o)||(a?e.p:e)}}}return tr(n,s)},ph=function(e,t,n){var i=e.labels,s=zn,a,o,l;for(a in i)o=i[a]-t,o<0==!!n&&o&&s>(o=Math.abs(o))&&(l=a,s=o);return l},Pn=function(e,t,n){var i=e.vars,s=i[t],a=Et,o=e._ctx,l,c,u;if(s)return l=i[t+"Params"],c=i.callbackScope||e,n&&$i.length&&$a(),o&&(Et=o),u=l?s.apply(c,l):s.call(c),Et=a,u},Rs=function(e){return Ji(e),e.scrollTrigger&&e.scrollTrigger.kill(!!$t),e.progress()<1&&Pn(e,"onInterrupt"),e},Yr,Uf=[],Nf=function(e){if(e)if(e=!e.name&&e.default||e,Lc()||e.headless){var t=e.name,n=Dt(e),i=t&&!n&&e.init?function(){this._props=[]}:e,s={init:zs,render:Hc,add:kc,kill:Wx,modifier:Hx,rawVars:0},a={targetTest:0,get:0,getSetter:Gc,aliases:{},register:0};if(cs(),e!==i){if(An[t])return;Un(i,Un(Ka(e,s),a)),as(i.prototype,as(s,Ka(e,a))),An[i.prop=t]=i,e.targetTest&&(ka.push(i),Nc[t]=1),t=(t==="css"?"CSS":t.charAt(0).toUpperCase()+t.substr(1))+"Plugin"}gf(t,i),e.register&&e.register(Mn,i,vn)}else Uf.push(e)},xt=255,Cs={aqua:[0,xt,xt],lime:[0,xt,0],silver:[192,192,192],black:[0,0,0],maroon:[128,0,0],teal:[0,128,128],blue:[0,0,xt],navy:[0,0,128],white:[xt,xt,xt],olive:[128,128,0],yellow:[xt,xt,0],orange:[xt,165,0],gray:[128,128,128],purple:[128,0,128],green:[0,128,0],red:[xt,0,0],pink:[xt,192,203],cyan:[0,xt,xt],transparent:[xt,xt,xt,0]},Ko=function(e,t,n){return e+=e<0?1:e>1?-1:0,(e*6<1?t+(n-t)*e*6:e<.5?n:e*3<2?t+(n-t)*(2/3-e)*6:t)*xt+.5|0},Ff=function(e,t,n){var i=e?wi(e)?[e>>16,e>>8&xt,e&xt]:0:Cs.black,s,a,o,l,c,u,f,h,d,_;if(!i){if(e.substr(-1)===","&&(e=e.substr(0,e.length-1)),Cs[e])i=Cs[e];else if(e.charAt(0)==="#"){if(e.length<6&&(s=e.charAt(1),a=e.charAt(2),o=e.charAt(3),e="#"+s+s+a+a+o+o+(e.length===5?e.charAt(4)+e.charAt(4):"")),e.length===9)return i=parseInt(e.substr(1,6),16),[i>>16,i>>8&xt,i&xt,parseInt(e.substr(7),16)/255];e=parseInt(e.substr(1),16),i=[e>>16,e>>8&xt,e&xt]}else if(e.substr(0,3)==="hsl"){if(i=_=e.match(ch),!t)l=+i[0]%360/360,c=+i[1]/100,u=+i[2]/100,a=u<=.5?u*(c+1):u+c-u*c,s=u*2-a,i.length>3&&(i[3]*=1),i[0]=Ko(l+1/3,s,a),i[1]=Ko(l,s,a),i[2]=Ko(l-1/3,s,a);else if(~e.indexOf("="))return i=e.match(df),n&&i.length<4&&(i[3]=1),i}else i=e.match(ch)||Cs.transparent;i=i.map(Number)}return t&&!_&&(s=i[0]/xt,a=i[1]/xt,o=i[2]/xt,f=Math.max(s,a,o),h=Math.min(s,a,o),u=(f+h)/2,f===h?l=c=0:(d=f-h,c=u>.5?d/(2-f-h):d/(f+h),l=f===s?(a-o)/d+(a<o?6:0):f===a?(o-s)/d+2:(s-a)/d+4,l*=60),i[0]=~~(l+.5),i[1]=~~(c*100+.5),i[2]=~~(u*100+.5)),n&&i.length<4&&(i[3]=1),i},Of=function(e){var t=[],n=[],i=-1;return e.split(Ki).forEach(function(s){var a=s.match(qr)||[];t.push.apply(t,a),n.push(i+=a.length+1)}),t.c=n,t},mh=function(e,t,n){var i="",s=(e+i).match(Ki),a=t?"hsla(":"rgba(",o=0,l,c,u,f;if(!s)return e;if(s=s.map(function(h){return(h=Ff(h,t,1))&&a+(t?h[0]+","+h[1]+"%,"+h[2]+"%,"+h[3]:h.join(","))+")"}),n&&(u=Of(e),l=n.c,l.join(i)!==u.c.join(i)))for(c=e.replace(Ki,"1").split(qr),f=c.length-1;o<f;o++)i+=c[o]+(~l.indexOf(o)?s.shift()||a+"0,0,0,0)":(u.length?u:s.length?s:n).shift());if(!c)for(c=e.split(Ki),f=c.length-1;o<f;o++)i+=c[o]+s[o];return i+c[f]},Ki=function(){var r="(?:\\b(?:(?:rgb|rgba|hsl|hsla)\\(.+?\\))|\\B#(?:[0-9a-f]{3,4}){1,2}\\b",e;for(e in Cs)r+="|"+e+"\\b";return new RegExp(r+")","gi")}(),Cx=/hsl[a]?\(/,Bf=function(e){var t=e.join(" "),n;if(Ki.lastIndex=0,Ki.test(t))return n=Cx.test(t),e[1]=mh(e[1],n),e[0]=mh(e[0],n,Of(e[1])),!0},Gs,Cn=function(){var r=Date.now,e=500,t=33,n=r(),i=n,s=1e3/240,a=s,o=[],l,c,u,f,h,d,_=function g(p){var m=r()-i,M=p===!0,S,y,A,w;if((m>e||m<0)&&(n+=m-t),i+=m,A=i-n,S=A-a,(S>0||M)&&(w=++f.frame,h=A-f.time*1e3,f.time=A=A/1e3,a+=S+(S>=s?4:s-S),y=1),M||(l=c(g)),y)for(d=0;d<o.length;d++)o[d](A,h,w,p)};return f={time:0,frame:0,tick:function(){_(!0)},deltaRatio:function(p){return h/(1e3/(p||60))},wake:function(){mf&&(!Zl&&Lc()&&(Qn=Zl=window,Ic=Qn.document||{},In.gsap=Mn,(Qn.gsapVersions||(Qn.gsapVersions=[])).push(Mn.version),_f(Ya||Qn.GreenSockGlobals||!Qn.gsap&&Qn||{}),Uf.forEach(Nf)),u=typeof requestAnimationFrame<"u"&&requestAnimationFrame,l&&f.sleep(),c=u||function(p){return setTimeout(p,a-f.time*1e3+1|0)},Gs=1,_(2))},sleep:function(){(u?cancelAnimationFrame:clearTimeout)(l),Gs=0,c=zs},lagSmoothing:function(p,m){e=p||1/0,t=Math.min(m||33,e)},fps:function(p){s=1e3/(p||240),a=f.time*1e3+s},add:function(p,m,M){var S=m?function(y,A,w,C){p(y,A,w,C),f.remove(S)}:p;return f.remove(p),o[M?"unshift":"push"](S),cs(),S},remove:function(p,m){~(m=o.indexOf(p))&&o.splice(m,1)&&d>=m&&d--},_listeners:o},f}(),cs=function(){return!Gs&&Cn.wake()},at={},Px=/^[\d.\-M][\d.\-,\s]/,Dx=/["']/g,Lx=function(e){for(var t={},n=e.substr(1,e.length-3).split(":"),i=n[0],s=1,a=n.length,o,l,c;s<a;s++)l=n[s],o=s!==a-1?l.lastIndexOf(","):l.length,c=l.substr(0,o),t[i]=isNaN(c)?c.replace(Dx,"").trim():+c,i=l.substr(o+1).trim();return t},Ix=function(e){var t=e.indexOf("(")+1,n=e.indexOf(")"),i=e.indexOf("(",t);return e.substring(t,~i&&i<n?e.indexOf(")",n+1):n)},Ux=function(e){var t=(e+"").split("("),n=at[t[0]];return n&&t.length>1&&n.config?n.config.apply(null,~e.indexOf("{")?[Lx(t[1])]:Ix(e).split(",").map(Sf)):at._CE&&Px.test(e)?at._CE("",e):n},kf=function(e){return function(t){return 1-e(1-t)}},zf=function r(e,t){for(var n=e._first,i;n;)n instanceof an?r(n,t):n.vars.yoyoEase&&(!n._yoyo||!n._repeat)&&n._yoyo!==t&&(n.timeline?r(n.timeline,t):(i=n._ease,n._ease=n._yEase,n._yEase=i,n._yoyo=t)),n=n._next},Mr=function(e,t){return e&&(Dt(e)?e:at[e]||Ux(e))||t},Tr=function(e,t,n,i){n===void 0&&(n=function(l){return 1-t(1-l)}),i===void 0&&(i=function(l){return l<.5?t(l*2)/2:1-t((1-l)*2)/2});var s={easeIn:t,easeOut:n,easeInOut:i},a;return gn(e,function(o){at[o]=In[o]=s,at[a=o.toLowerCase()]=n;for(var l in s)at[a+(l==="easeIn"?".in":l==="easeOut"?".out":".inOut")]=at[o+"."+l]=s[l]}),s},Vf=function(e){return function(t){return t<.5?(1-e(1-t*2))/2:.5+e((t-.5)*2)/2}},Zo=function r(e,t,n){var i=t>=1?t:1,s=(n||(e?.3:.45))/(t<1?t:1),a=s/Kl*(Math.asin(1/i)||0),o=function(u){return u===1?1:i*Math.pow(2,-10*u)*sx((u-a)*s)+1},l=e==="out"?o:e==="in"?function(c){return 1-o(1-c)}:Vf(o);return s=Kl/s,l.config=function(c,u){return r(e,c,u)},l},jo=function r(e,t){t===void 0&&(t=1.70158);var n=function(a){return a?--a*a*((t+1)*a+t)+1:0},i=e==="out"?n:e==="in"?function(s){return 1-n(1-s)}:Vf(n);return i.config=function(s){return r(e,s)},i};gn("Linear,Quad,Cubic,Quart,Quint,Strong",function(r,e){var t=e<5?e+1:e;Tr(r+",Power"+(t-1),e?function(n){return Math.pow(n,t)}:function(n){return n},function(n){return 1-Math.pow(1-n,t)},function(n){return n<.5?Math.pow(n*2,t)/2:1-Math.pow((1-n)*2,t)/2})});at.Linear.easeNone=at.none=at.Linear.easeIn;Tr("Elastic",Zo("in"),Zo("out"),Zo());(function(r,e){var t=1/e,n=2*t,i=2.5*t,s=function(o){return o<t?r*o*o:o<n?r*Math.pow(o-1.5/e,2)+.75:o<i?r*(o-=2.25/e)*o+.9375:r*Math.pow(o-2.625/e,2)+.984375};Tr("Bounce",function(a){return 1-s(1-a)},s)})(7.5625,2.75);Tr("Expo",function(r){return Math.pow(2,10*(r-1))*r+r*r*r*r*r*r*(1-r)});Tr("Circ",function(r){return-(hf(1-r*r)-1)});Tr("Sine",function(r){return r===1?1:-rx(r*nx)+1});Tr("Back",jo("in"),jo("out"),jo());at.SteppedEase=at.steps=In.SteppedEase={config:function(e,t){e===void 0&&(e=1);var n=1/e,i=e+(t?0:1),s=t?1:0,a=1-Mt;return function(o){return((i*Zs(0,a,o)|0)+s)*n}}};ss.ease=at["quad.out"];gn("onComplete,onUpdate,onStart,onRepeat,onReverseComplete,onInterrupt",function(r){return Fc+=r+","+r+"Params,"});var Gf=function(e,t){this.id=ix++,e._gsap=this,this.target=e,this.harness=t,this.get=t?t.get:xf,this.set=t?t.getSetter:Gc},Hs=function(){function r(t){this.vars=t,this._delay=+t.delay||0,(this._repeat=t.repeat===1/0?-2:t.repeat||0)&&(this._rDelay=t.repeatDelay||0,this._yoyo=!!t.yoyo||!!t.yoyoEase),this._ts=1,ls(this,+t.duration,1,1),this.data=t.data,Et&&(this._ctx=Et,Et.data.push(this)),Gs||Cn.wake()}var e=r.prototype;return e.delay=function(n){return n||n===0?(this.parent&&this.parent.smoothChildTiming&&this.startTime(this._start+n-this._delay),this._delay=n,this):this._delay},e.duration=function(n){return arguments.length?this.totalDuration(this._repeat>0?n+(n+this._rDelay)*this._repeat:n):this.totalDuration()&&this._dur},e.totalDuration=function(n){return arguments.length?(this._dirty=0,ls(this,this._repeat<0?n:(n-this._repeat*this._rDelay)/(this._repeat+1))):this._tDur},e.totalTime=function(n,i){if(cs(),!arguments.length)return this._tTime;var s=this._dp;if(s&&s.smoothChildTiming&&this._ts){for(so(this,n),!s._dp||s.parent||Ef(s,this);s&&s.parent;)s.parent._time!==s._start+(s._ts>=0?s._tTime/s._ts:(s.totalDuration()-s._tTime)/-s._ts)&&s.totalTime(s._tTime,!0),s=s.parent;!this.parent&&this._dp.autoRemoveChildren&&(this._ts>0&&n<this._tDur||this._ts<0&&n>0||!this._tDur&&!n)&&ti(this._dp,this,this._start-this._delay)}return(this._tTime!==n||!this._dur&&!i||this._initted&&Math.abs(this._zTime)===Mt||!this._initted&&this._dur&&n||!n&&!this._initted&&(this.add||this._ptLookup))&&(this._ts||(this._pTime=n),Mf(this,n,i)),this},e.time=function(n,i){return arguments.length?this.totalTime(Math.min(this.totalDuration(),n+fh(this))%(this._dur+this._rDelay)||(n?this._dur:0),i):this._time},e.totalProgress=function(n,i){return arguments.length?this.totalTime(this.totalDuration()*n,i):this.totalDuration()?Math.min(1,this._tTime/this._tDur):this.rawTime()>=0&&this._initted?1:0},e.progress=function(n,i){return arguments.length?this.totalTime(this.duration()*(this._yoyo&&!(this.iteration()&1)?1-n:n)+fh(this),i):this.duration()?Math.min(1,this._time/this._dur):this.rawTime()>0?1:0},e.iteration=function(n,i){var s=this.duration()+this._rDelay;return arguments.length?this.totalTime(this._time+(n-1)*s,i):this._repeat?os(this._tTime,s)+1:1},e.timeScale=function(n,i){if(!arguments.length)return this._rts===-Mt?0:this._rts;if(this._rts===n)return this;var s=this.parent&&this._ts?Za(this.parent._time,this):this._tTime;return this._rts=+n||0,this._ts=this._ps||n===-Mt?0:this._rts,this.totalTime(Zs(-Math.abs(this._delay),this.totalDuration(),s),i!==!1),ro(this),mx(this)},e.paused=function(n){return arguments.length?(this._ps!==n&&(this._ps=n,n?(this._pTime=this._tTime||Math.max(-this._delay,this.rawTime()),this._ts=this._act=0):(cs(),this._ts=this._rts,this.totalTime(this.parent&&!this.parent.smoothChildTiming?this.rawTime():this._tTime||this._pTime,this.progress()===1&&Math.abs(this._zTime)!==Mt&&(this._tTime-=Mt)))),this):this._ps},e.startTime=function(n){if(arguments.length){this._start=At(n);var i=this.parent||this._dp;return i&&(i._sort||!this.parent)&&ti(i,this,this._start-this._delay),this}return this._start},e.endTime=function(n){return this._start+(_n(n)?this.totalDuration():this.duration())/Math.abs(this._ts||1)},e.rawTime=function(n){var i=this.parent||this._dp;return i?n&&(!this._ts||this._repeat&&this._time&&this.totalProgress()<1)?this._tTime%(this._dur+this._rDelay):this._ts?Za(i.rawTime(n),this):this._tTime:this._tTime},e.revert=function(n){n===void 0&&(n=hx);var i=$t;return $t=n,Bc(this)&&(this.timeline&&this.timeline.revert(n),this.totalTime(-.01,n.suppressEvents)),this.data!=="nested"&&n.kill!==!1&&this.kill(),$t=i,this},e.globalTime=function(n){for(var i=this,s=arguments.length?n:i.rawTime();i;)s=i._start+s/(Math.abs(i._ts)||1),i=i._dp;return!this.parent&&this._sat?this._sat.globalTime(n):s},e.repeat=function(n){return arguments.length?(this._repeat=n===1/0?-2:n,dh(this)):this._repeat===-2?1/0:this._repeat},e.repeatDelay=function(n){if(arguments.length){var i=this._time;return this._rDelay=n,dh(this),i?this.time(i):this}return this._rDelay},e.yoyo=function(n){return arguments.length?(this._yoyo=n,this):this._yoyo},e.seek=function(n,i){return this.totalTime(Bn(this,n),_n(i))},e.restart=function(n,i){return this.play().totalTime(n?-this._delay:0,_n(i)),this._dur||(this._zTime=-Mt),this},e.play=function(n,i){return n!=null&&this.seek(n,i),this.reversed(!1).paused(!1)},e.reverse=function(n,i){return n!=null&&this.seek(n||this.totalDuration(),i),this.reversed(!0).paused(!1)},e.pause=function(n,i){return n!=null&&this.seek(n,i),this.paused(!0)},e.resume=function(){return this.paused(!1)},e.reversed=function(n){return arguments.length?(!!n!==this.reversed()&&this.timeScale(-this._rts||(n?-Mt:0)),this):this._rts<0},e.invalidate=function(){return this._initted=this._act=0,this._zTime=-Mt,this},e.isActive=function(){var n=this.parent||this._dp,i=this._start,s;return!!(!n||this._ts&&this._initted&&n.isActive()&&(s=n.rawTime(!0))>=i&&s<this.endTime(!0)-Mt)},e.eventCallback=function(n,i,s){var a=this.vars;return arguments.length>1?(i?(a[n]=i,s&&(a[n+"Params"]=s),n==="onUpdate"&&(this._onUpdate=i)):delete a[n],this):a[n]},e.then=function(n){var i=this,s=i._prom;return new Promise(function(a){var o=Dt(n)?n:yf,l=function(){var u=i.then;i.then=null,s&&s(),Dt(o)&&(o=o(i))&&(o.then||o===i)&&(i.then=u),a(o),i.then=u};i._initted&&i.totalProgress()===1&&i._ts>=0||!i._tTime&&i._ts<0?l():i._prom=l})},e.kill=function(){Rs(this)},r}();Un(Hs.prototype,{_time:0,_start:0,_end:0,_tTime:0,_tDur:0,_dirty:0,_repeat:0,_yoyo:!1,parent:null,_initted:!1,_rDelay:0,_ts:1,_dp:0,ratio:0,_zTime:-Mt,_prom:0,_ps:!1,_rts:1});var an=function(r){uf(e,r);function e(n,i){var s;return n===void 0&&(n={}),s=r.call(this,n)||this,s.labels={},s.smoothChildTiming=!!n.smoothChildTiming,s.autoRemoveChildren=!!n.autoRemoveChildren,s._sort=_n(n.sortChildren),wt&&ti(n.parent||wt,xi(s),i),n.reversed&&s.reverse(),n.paused&&s.paused(!0),n.scrollTrigger&&Tf(xi(s),n.scrollTrigger),s}var t=e.prototype;return t.to=function(i,s,a){return Is(0,arguments,this),this},t.from=function(i,s,a){return Is(1,arguments,this),this},t.fromTo=function(i,s,a,o){return Is(2,arguments,this),this},t.set=function(i,s,a){return s.duration=0,s.parent=this,Ls(s).repeatDelay||(s.repeat=0),s.immediateRender=!!s.immediateRender,new Ft(i,s,Bn(this,a),1),this},t.call=function(i,s,a){return ti(this,Ft.delayedCall(0,i,s),a)},t.staggerTo=function(i,s,a,o,l,c,u){return a.duration=s,a.stagger=a.stagger||o,a.onComplete=c,a.onCompleteParams=u,a.parent=this,new Ft(i,a,Bn(this,l)),this},t.staggerFrom=function(i,s,a,o,l,c,u){return a.runBackwards=1,Ls(a).immediateRender=_n(a.immediateRender),this.staggerTo(i,s,a,o,l,c,u)},t.staggerFromTo=function(i,s,a,o,l,c,u,f){return o.startAt=a,Ls(o).immediateRender=_n(o.immediateRender),this.staggerTo(i,s,o,l,c,u,f)},t.render=function(i,s,a){var o=this._time,l=this._dirty?this.totalDuration():this._tDur,c=this._dur,u=i<=0?0:At(i),f=this._zTime<0!=i<0&&(this._initted||!c),h,d,_,g,p,m,M,S,y,A,w,C;if(this!==wt&&u>l&&i>=0&&(u=l),u!==this._tTime||a||f){if(o!==this._time&&c&&(u+=this._time-o,i+=this._time-o),h=u,y=this._start,S=this._ts,m=!S,f&&(c||(o=this._zTime),(i||!s)&&(this._zTime=i)),this._repeat){if(w=this._yoyo,p=c+this._rDelay,this._repeat<-1&&i<0)return this.totalTime(p*100+i,s,a);if(h=At(u%p),u===l?(g=this._repeat,h=c):(A=At(u/p),g=~~A,g&&g===A&&(h=c,g--),h>c&&(h=c)),A=os(this._tTime,p),!o&&this._tTime&&A!==g&&this._tTime-A*p-this._dur<=0&&(A=g),w&&g&1&&(h=c-h,C=1),g!==A&&!this._lock){var x=w&&A&1,b=x===(w&&g&1);if(g<A&&(x=!x),o=x?0:u%c?c:u,this._lock=1,this.render(o||(C?0:At(g*p)),s,!c)._lock=0,this._tTime=u,!s&&this.parent&&Pn(this,"onRepeat"),this.vars.repeatRefresh&&!C&&(this.invalidate()._lock=1,A=g),o&&o!==this._time||m!==!this._ts||this.vars.onRepeat&&!this.parent&&!this._act)return this;if(c=this._dur,l=this._tDur,b&&(this._lock=2,o=x?c:-1e-4,this.render(o,!0),this.vars.repeatRefresh&&!C&&this.invalidate()),this._lock=0,!this._ts&&!m)return this;zf(this,C)}}if(this._hasPause&&!this._forcing&&this._lock<2&&(M=xx(this,At(o),At(h)),M&&(u-=h-(h=M._start))),this._tTime=u,this._time=h,this._act=!S,this._initted||(this._onUpdate=this.vars.onUpdate,this._initted=1,this._zTime=i,o=0),!o&&u&&c&&!s&&!A&&(Pn(this,"onStart"),this._tTime!==u))return this;if(h>=o&&i>=0)for(d=this._first;d;){if(_=d._next,(d._act||h>=d._start)&&d._ts&&M!==d){if(d.parent!==this)return this.render(i,s,a);if(d.render(d._ts>0?(h-d._start)*d._ts:(d._dirty?d.totalDuration():d._tDur)+(h-d._start)*d._ts,s,a),h!==this._time||!this._ts&&!m){M=0,_&&(u+=this._zTime=-Mt);break}}d=_}else{d=this._last;for(var W=i<0?i:h;d;){if(_=d._prev,(d._act||W<=d._end)&&d._ts&&M!==d){if(d.parent!==this)return this.render(i,s,a);if(d.render(d._ts>0?(W-d._start)*d._ts:(d._dirty?d.totalDuration():d._tDur)+(W-d._start)*d._ts,s,a||$t&&Bc(d)),h!==this._time||!this._ts&&!m){M=0,_&&(u+=this._zTime=W?-Mt:Mt);break}}d=_}}if(M&&!s&&(this.pause(),M.render(h>=o?0:-Mt)._zTime=h>=o?1:-1,this._ts))return this._start=y,ro(this),this.render(i,s,a);this._onUpdate&&!s&&Pn(this,"onUpdate",!0),(u===l&&this._tTime>=this.totalDuration()||!u&&o)&&(y===this._start||Math.abs(S)!==Math.abs(this._ts))&&(this._lock||((i||!c)&&(u===l&&this._ts>0||!u&&this._ts<0)&&Ji(this,1),!s&&!(i<0&&!o)&&(u||o||!l)&&(Pn(this,u===l&&i>=0?"onComplete":"onReverseComplete",!0),this._prom&&!(u<l&&this.timeScale()>0)&&this._prom())))}return this},t.add=function(i,s){var a=this;if(wi(s)||(s=Bn(this,s,i)),!(i instanceof Hs)){if(tn(i))return i.forEach(function(o){return a.add(o,s)}),this;if(Wt(i))return this.addLabel(i,s);if(Dt(i))i=Ft.delayedCall(0,i);else return this}return this!==i?ti(this,i,s):this},t.getChildren=function(i,s,a,o){i===void 0&&(i=!0),s===void 0&&(s=!0),a===void 0&&(a=!0),o===void 0&&(o=-zn);for(var l=[],c=this._first;c;)c._start>=o&&(c instanceof Ft?s&&l.push(c):(a&&l.push(c),i&&l.push.apply(l,c.getChildren(!0,s,a)))),c=c._next;return l},t.getById=function(i){for(var s=this.getChildren(1,1,1),a=s.length;a--;)if(s[a].vars.id===i)return s[a]},t.remove=function(i){return Wt(i)?this.removeLabel(i):Dt(i)?this.killTweensOf(i):(i.parent===this&&io(this,i),i===this._recent&&(this._recent=this._last),xr(this))},t.totalTime=function(i,s){return arguments.length?(this._forcing=1,!this._dp&&this._ts&&(this._start=At(Cn.time-(this._ts>0?i/this._ts:(this.totalDuration()-i)/-this._ts))),r.prototype.totalTime.call(this,i,s),this._forcing=0,this):this._tTime},t.addLabel=function(i,s){return this.labels[i]=Bn(this,s),this},t.removeLabel=function(i){return delete this.labels[i],this},t.addPause=function(i,s,a){var o=Ft.delayedCall(0,s||zs,a);return o.data="isPause",this._hasPause=1,ti(this,o,Bn(this,i))},t.removePause=function(i){var s=this._first;for(i=Bn(this,i);s;)s._start===i&&s.data==="isPause"&&Ji(s),s=s._next},t.killTweensOf=function(i,s,a){for(var o=this.getTweensOf(i,a),l=o.length;l--;)Xi!==o[l]&&o[l].kill(i,s);return this},t.getTweensOf=function(i,s){for(var a=[],o=Vn(i),l=this._first,c=wi(s),u;l;)l instanceof Ft?fx(l._targets,o)&&(c?(!Xi||l._initted&&l._ts)&&l.globalTime(0)<=s&&l.globalTime(l.totalDuration())>s:!s||l.isActive())&&a.push(l):(u=l.getTweensOf(o,s)).length&&a.push.apply(a,u),l=l._next;return a},t.tweenTo=function(i,s){s=s||{};var a=this,o=Bn(a,i),l=s,c=l.startAt,u=l.onStart,f=l.onStartParams,h=l.immediateRender,d,_=Ft.to(a,Un({ease:s.ease||"none",lazy:!1,immediateRender:!1,time:o,overwrite:"auto",duration:s.duration||Math.abs((o-(c&&"time"in c?c.time:a._time))/a.timeScale())||Mt,onStart:function(){if(a.pause(),!d){var p=s.duration||Math.abs((o-(c&&"time"in c?c.time:a._time))/a.timeScale());_._dur!==p&&ls(_,p,0,1).render(_._time,!0,!0),d=1}u&&u.apply(_,f||[])}},s));return h?_.render(0):_},t.tweenFromTo=function(i,s,a){return this.tweenTo(s,Un({startAt:{time:Bn(this,i)}},a))},t.recent=function(){return this._recent},t.nextLabel=function(i){return i===void 0&&(i=this._time),ph(this,Bn(this,i))},t.previousLabel=function(i){return i===void 0&&(i=this._time),ph(this,Bn(this,i),1)},t.currentLabel=function(i){return arguments.length?this.seek(i,!0):this.previousLabel(this._time+Mt)},t.shiftChildren=function(i,s,a){a===void 0&&(a=0);var o=this._first,l=this.labels,c;for(i=At(i);o;)o._start>=a&&(o._start+=i,o._end+=i),o=o._next;if(s)for(c in l)l[c]>=a&&(l[c]+=i);return xr(this)},t.invalidate=function(i){var s=this._first;for(this._lock=0;s;)s.invalidate(i),s=s._next;return r.prototype.invalidate.call(this,i)},t.clear=function(i){i===void 0&&(i=!0);for(var s=this._first,a;s;)a=s._next,this.remove(s),s=a;return this._dp&&(this._time=this._tTime=this._pTime=0),i&&(this.labels={}),xr(this)},t.totalDuration=function(i){var s=0,a=this,o=a._last,l=zn,c,u,f;if(arguments.length)return a.timeScale((a._repeat<0?a.duration():a.totalDuration())/(a.reversed()?-i:i));if(a._dirty){for(f=a.parent;o;)c=o._prev,o._dirty&&o.totalDuration(),u=o._start,u>l&&a._sort&&o._ts&&!a._lock?(a._lock=1,ti(a,o,u-o._delay,1)._lock=0):l=u,u<0&&o._ts&&(s-=u,(!f&&!a._dp||f&&f.smoothChildTiming)&&(a._start+=At(u/a._ts),a._time-=u,a._tTime-=u),a.shiftChildren(-u,!1,-1/0),l=0),o._end>s&&o._ts&&(s=o._end),o=c;ls(a,a===wt&&a._time>s?a._time:s,1,1),a._dirty=0}return a._tDur},e.updateRoot=function(i){if(wt._ts&&(Mf(wt,Za(i,wt)),vf=Cn.frame),Cn.frame>=uh){uh+=Ln.autoSleep||120;var s=wt._first;if((!s||!s._ts)&&Ln.autoSleep&&Cn._listeners.length<2){for(;s&&!s._ts;)s=s._next;s||Cn.sleep()}}},e}(Hs);Un(an.prototype,{_lock:0,_hasPause:0,_forcing:0});var Nx=function(e,t,n,i,s,a,o){var l=new vn(this._pt,e,t,0,1,$f,null,s),c=0,u=0,f,h,d,_,g,p,m,M;for(l.b=n,l.e=i,n+="",i+="",(m=~i.indexOf("random("))&&(i=Vs(i)),a&&(M=[n,i],a(M,e,t),n=M[0],i=M[1]),h=n.match(Yo)||[];f=Yo.exec(i);)_=f[0],g=i.substring(c,f.index),d?d=(d+1)%5:g.substr(-5)==="rgba("&&(d=1),_!==h[u++]&&(p=parseFloat(h[u-1])||0,l._pt={_next:l._pt,p:g||u===1?g:",",s:p,c:_.charAt(1)==="="?jr(p,_)-p:parseFloat(_)-p,m:d&&d<4?Math.round:0},c=Yo.lastIndex);return l.c=c<i.length?i.substring(c,i.length):"",l.fp=o,(pf.test(i)||m)&&(l.e=0),this._pt=l,l},kc=function(e,t,n,i,s,a,o,l,c,u){Dt(i)&&(i=i(s||0,e,a));var f=e[t],h=n!=="get"?n:Dt(f)?c?e[t.indexOf("set")||!Dt(e["get"+t.substr(3)])?t:"get"+t.substr(3)](c):e[t]():f,d=Dt(f)?c?zx:qf:Vc,_;if(Wt(i)&&(~i.indexOf("random(")&&(i=Vs(i)),i.charAt(1)==="="&&(_=jr(h,i)+(Qt(h)||0),(_||_===0)&&(i=_))),!u||h!==i||ic)return!isNaN(h*i)&&i!==""?(_=new vn(this._pt,e,t,+h||0,i-(h||0),typeof f=="boolean"?Gx:Yf,0,d),c&&(_.fp=c),o&&_.modifier(o,this,e),this._pt=_):(!f&&!(t in e)&&Uc(t,i),Nx.call(this,e,t,h,i,d,l||Ln.stringFilter,c))},Fx=function(e,t,n,i,s){if(Dt(e)&&(e=Us(e,s,t,n,i)),!ui(e)||e.style&&e.nodeType||tn(e)||ff(e))return Wt(e)?Us(e,s,t,n,i):e;var a={},o;for(o in e)a[o]=Us(e[o],s,t,n,i);return a},Hf=function(e,t,n,i,s,a){var o,l,c,u;if(An[e]&&(o=new An[e]).init(s,o.rawVars?t[e]:Fx(t[e],i,s,a,n),n,i,a)!==!1&&(n._pt=l=new vn(n._pt,s,e,0,1,o.render,o,0,o.priority),n!==Yr))for(c=n._ptLookup[n._targets.indexOf(s)],u=o._props.length;u--;)c[o._props[u]]=l;return o},Xi,ic,zc=function r(e,t,n){var i=e.vars,s=i.ease,a=i.startAt,o=i.immediateRender,l=i.lazy,c=i.onUpdate,u=i.runBackwards,f=i.yoyoEase,h=i.keyframes,d=i.autoRevert,_=e._dur,g=e._startAt,p=e._targets,m=e.parent,M=m&&m.data==="nested"?m.vars.targets:p,S=e._overwrite==="auto"&&!Pc,y=e.timeline,A,w,C,x,b,W,L,G,z,q,H,N,F;if(y&&(!h||!s)&&(s="none"),e._ease=Mr(s,ss.ease),e._yEase=f?kf(Mr(f===!0?s:f,ss.ease)):0,f&&e._yoyo&&!e._repeat&&(f=e._yEase,e._yEase=e._ease,e._ease=f),e._from=!y&&!!i.runBackwards,!y||h&&!i.stagger){if(G=p[0]?vr(p[0]).harness:0,N=G&&i[G.prop],A=Ka(i,Nc),g&&(g._zTime<0&&g.progress(1),t<0&&u&&o&&!d?g.render(-1,!0):g.revert(u&&_?Ba:ux),g._lazy=0),a){if(Ji(e._startAt=Ft.set(p,Un({data:"isStart",overwrite:!1,parent:m,immediateRender:!0,lazy:!g&&_n(l),startAt:null,delay:0,onUpdate:c&&function(){return Pn(e,"onUpdate")},stagger:0},a))),e._startAt._dp=0,e._startAt._sat=e,t<0&&($t||!o&&!d)&&e._startAt.revert(Ba),o&&_&&t<=0&&n<=0){t&&(e._zTime=t);return}}else if(u&&_&&!g){if(t&&(o=!1),C=Un({overwrite:!1,data:"isFromStart",lazy:o&&!g&&_n(l),immediateRender:o,stagger:0,parent:m},A),N&&(C[G.prop]=N),Ji(e._startAt=Ft.set(p,C)),e._startAt._dp=0,e._startAt._sat=e,t<0&&($t?e._startAt.revert(Ba):e._startAt.render(-1,!0)),e._zTime=t,!o)r(e._startAt,Mt,Mt);else if(!t)return}for(e._pt=e._ptCache=0,l=_&&_n(l)||l&&!_,w=0;w<p.length;w++){if(b=p[w],L=b._gsap||Oc(p)[w]._gsap,e._ptLookup[w]=q={},jl[L.id]&&$i.length&&$a(),H=M===p?w:M.indexOf(b),G&&(z=new G).init(b,N||A,e,H,M)!==!1&&(e._pt=x=new vn(e._pt,b,z.name,0,1,z.render,z,0,z.priority),z._props.forEach(function(ne){q[ne]=x}),z.priority&&(W=1)),!G||N)for(C in A)An[C]&&(z=Hf(C,A,e,H,b,M))?z.priority&&(W=1):q[C]=x=kc.call(e,b,C,"get",A[C],H,M,0,i.stringFilter);e._op&&e._op[w]&&e.kill(b,e._op[w]),S&&e._pt&&(Xi=e,wt.killTweensOf(b,q,e.globalTime(t)),F=!e.parent,Xi=0),e._pt&&l&&(jl[L.id]=1)}W&&Kf(e),e._onInit&&e._onInit(e)}e._onUpdate=c,e._initted=(!e._op||e._pt)&&!F,h&&t<=0&&y.render(zn,!0,!0)},Ox=function(e,t,n,i,s,a,o,l){var c=(e._pt&&e._ptCache||(e._ptCache={}))[t],u,f,h,d;if(!c)for(c=e._ptCache[t]=[],h=e._ptLookup,d=e._targets.length;d--;){if(u=h[d][t],u&&u.d&&u.d._pt)for(u=u.d._pt;u&&u.p!==t&&u.fp!==t;)u=u._next;if(!u)return ic=1,e.vars[t]="+=0",zc(e,o),ic=0,l?ks(t+" not eligible for reset"):1;c.push(u)}for(d=c.length;d--;)f=c[d],u=f._pt||f,u.s=(i||i===0)&&!s?i:u.s+(i||0)+a*u.c,u.c=n-u.s,f.e&&(f.e=It(n)+Qt(f.e)),f.b&&(f.b=u.s+Qt(f.b))},Bx=function(e,t){var n=e[0]?vr(e[0]).harness:0,i=n&&n.aliases,s,a,o,l;if(!i)return t;s=as({},t);for(a in i)if(a in s)for(l=i[a].split(","),o=l.length;o--;)s[l[o]]=s[a];return s},kx=function(e,t,n,i){var s=t.ease||i||"power1.inOut",a,o;if(tn(t))o=n[e]||(n[e]=[]),t.forEach(function(l,c){return o.push({t:c/(t.length-1)*100,v:l,e:s})});else for(a in t)o=n[a]||(n[a]=[]),a==="ease"||o.push({t:parseFloat(e),v:t[a],e:s})},Us=function(e,t,n,i,s){return Dt(e)?e.call(t,n,i,s):Wt(e)&&~e.indexOf("random(")?Vs(e):e},Wf=Fc+"repeat,repeatDelay,yoyo,repeatRefresh,yoyoEase,autoRevert",Xf={};gn(Wf+",id,stagger,delay,duration,paused,scrollTrigger",function(r){return Xf[r]=1});var Ft=function(r){uf(e,r);function e(n,i,s,a){var o;typeof i=="number"&&(s.duration=i,i=s,s=null),o=r.call(this,a?i:Ls(i))||this;var l=o.vars,c=l.duration,u=l.delay,f=l.immediateRender,h=l.stagger,d=l.overwrite,_=l.keyframes,g=l.defaults,p=l.scrollTrigger,m=l.yoyoEase,M=i.parent||wt,S=(tn(n)||ff(n)?wi(n[0]):"length"in i)?[n]:Vn(n),y,A,w,C,x,b,W,L;if(o._targets=S.length?Oc(S):ks("GSAP target "+n+" not found. https://gsap.com",!Ln.nullTargetWarn)||[],o._ptLookup=[],o._overwrite=d,_||h||Pa(c)||Pa(u)){if(i=o.vars,y=o.timeline=new an({data:"nested",defaults:g||{},targets:M&&M.data==="nested"?M.vars.targets:S}),y.kill(),y.parent=y._dp=xi(o),y._start=0,h||Pa(c)||Pa(u)){if(C=S.length,W=h&&Cf(h),ui(h))for(x in h)~Wf.indexOf(x)&&(L||(L={}),L[x]=h[x]);for(A=0;A<C;A++)w=Ka(i,Xf),w.stagger=0,m&&(w.yoyoEase=m),L&&as(w,L),b=S[A],w.duration=+Us(c,xi(o),A,b,S),w.delay=(+Us(u,xi(o),A,b,S)||0)-o._delay,!h&&C===1&&w.delay&&(o._delay=u=w.delay,o._start+=u,w.delay=0),y.to(b,w,W?W(A,b,S):0),y._ease=at.none;y.duration()?c=u=0:o.timeline=0}else if(_){Ls(Un(y.vars.defaults,{ease:"none"})),y._ease=Mr(_.ease||i.ease||"none");var G=0,z,q,H;if(tn(_))_.forEach(function(N){return y.to(S,N,">")}),y.duration();else{w={};for(x in _)x==="ease"||x==="easeEach"||kx(x,_[x],w,_.easeEach);for(x in w)for(z=w[x].sort(function(N,F){return N.t-F.t}),G=0,A=0;A<z.length;A++)q=z[A],H={ease:q.e,duration:(q.t-(A?z[A-1].t:0))/100*c},H[x]=q.v,y.to(S,H,G),G+=H.duration;y.duration()<c&&y.to({},{duration:c-y.duration()})}}c||o.duration(c=y.duration())}else o.timeline=0;return d===!0&&!Pc&&(Xi=xi(o),wt.killTweensOf(S),Xi=0),ti(M,xi(o),s),i.reversed&&o.reverse(),i.paused&&o.paused(!0),(f||!c&&!_&&o._start===At(M._time)&&_n(f)&&_x(xi(o))&&M.data!=="nested")&&(o._tTime=-Mt,o.render(Math.max(0,-u)||0)),p&&Tf(xi(o),p),o}var t=e.prototype;return t.render=function(i,s,a){var o=this._time,l=this._tDur,c=this._dur,u=i<0,f=i>l-Mt&&!u?l:i<Mt?0:i,h,d,_,g,p,m,M,S,y;if(!c)vx(this,i,s,a);else if(f!==this._tTime||!i||a||!this._initted&&this._tTime||this._startAt&&this._zTime<0!==u||this._lazy){if(h=f,S=this.timeline,this._repeat){if(g=c+this._rDelay,this._repeat<-1&&u)return this.totalTime(g*100+i,s,a);if(h=At(f%g),f===l?(_=this._repeat,h=c):(p=At(f/g),_=~~p,_&&_===p?(h=c,_--):h>c&&(h=c)),m=this._yoyo&&_&1,m&&(y=this._yEase,h=c-h),p=os(this._tTime,g),h===o&&!a&&this._initted&&_===p)return this._tTime=f,this;_!==p&&(S&&this._yEase&&zf(S,m),this.vars.repeatRefresh&&!m&&!this._lock&&h!==g&&this._initted&&(this._lock=a=1,this.render(At(g*_),!0).invalidate()._lock=0))}if(!this._initted){if(Af(this,u?i:h,a,s,f))return this._tTime=0,this;if(o!==this._time&&!(a&&this.vars.repeatRefresh&&_!==p))return this;if(c!==this._dur)return this.render(i,s,a)}if(this._tTime=f,this._time=h,!this._act&&this._ts&&(this._act=1,this._lazy=0),this.ratio=M=(y||this._ease)(h/c),this._from&&(this.ratio=M=1-M),!o&&f&&!s&&!p&&(Pn(this,"onStart"),this._tTime!==f))return this;for(d=this._pt;d;)d.r(M,d.d),d=d._next;S&&S.render(i<0?i:S._dur*S._ease(h/this._dur),s,a)||this._startAt&&(this._zTime=i),this._onUpdate&&!s&&(u&&Jl(this,i,s,a),Pn(this,"onUpdate")),this._repeat&&_!==p&&this.vars.onRepeat&&!s&&this.parent&&Pn(this,"onRepeat"),(f===this._tDur||!f)&&this._tTime===f&&(u&&!this._onUpdate&&Jl(this,i,!0,!0),(i||!c)&&(f===this._tDur&&this._ts>0||!f&&this._ts<0)&&Ji(this,1),!s&&!(u&&!o)&&(f||o||m)&&(Pn(this,f===l?"onComplete":"onReverseComplete",!0),this._prom&&!(f<l&&this.timeScale()>0)&&this._prom()))}return this},t.targets=function(){return this._targets},t.invalidate=function(i){return(!i||!this.vars.runBackwards)&&(this._startAt=0),this._pt=this._op=this._onUpdate=this._lazy=this.ratio=0,this._ptLookup=[],this.timeline&&this.timeline.invalidate(i),r.prototype.invalidate.call(this,i)},t.resetTo=function(i,s,a,o,l){Gs||Cn.wake(),this._ts||this.play();var c=Math.min(this._dur,(this._dp._time-this._start)*this._ts),u;return this._initted||zc(this,c),u=this._ease(c/this._dur),Ox(this,i,s,a,o,u,c,l)?this.resetTo(i,s,a,o,1):(so(this,0),this.parent||bf(this._dp,this,"_first","_last",this._dp._sort?"_start":0),this.render(0))},t.kill=function(i,s){if(s===void 0&&(s="all"),!i&&(!s||s==="all"))return this._lazy=this._pt=0,this.parent?Rs(this):this.scrollTrigger&&this.scrollTrigger.kill(!!$t),this;if(this.timeline){var a=this.timeline.totalDuration();return this.timeline.killTweensOf(i,s,Xi&&Xi.vars.overwrite!==!0)._first||Rs(this),this.parent&&a!==this.timeline.totalDuration()&&ls(this,this._dur*this.timeline._tDur/a,0,1),this}var o=this._targets,l=i?Vn(i):o,c=this._ptLookup,u=this._pt,f,h,d,_,g,p,m;if((!s||s==="all")&&px(o,l))return s==="all"&&(this._pt=0),Rs(this);for(f=this._op=this._op||[],s!=="all"&&(Wt(s)&&(g={},gn(s,function(M){return g[M]=1}),s=g),s=Bx(o,s)),m=o.length;m--;)if(~l.indexOf(o[m])){h=c[m],s==="all"?(f[m]=s,_=h,d={}):(d=f[m]=f[m]||{},_=s);for(g in _)p=h&&h[g],p&&((!("kill"in p.d)||p.d.kill(g)===!0)&&io(this,p,"_pt"),delete h[g]),d!=="all"&&(d[g]=1)}return this._initted&&!this._pt&&u&&Rs(this),this},e.to=function(i,s){return new e(i,s,arguments[2])},e.from=function(i,s){return Is(1,arguments)},e.delayedCall=function(i,s,a,o){return new e(s,0,{immediateRender:!1,lazy:!1,overwrite:!1,delay:i,onComplete:s,onReverseComplete:s,onCompleteParams:a,onReverseCompleteParams:a,callbackScope:o})},e.fromTo=function(i,s,a){return Is(2,arguments)},e.set=function(i,s){return s.duration=0,s.repeatDelay||(s.repeat=0),new e(i,s)},e.killTweensOf=function(i,s,a){return wt.killTweensOf(i,s,a)},e}(Hs);Un(Ft.prototype,{_targets:[],_lazy:0,_startAt:0,_op:0,_onInit:0});gn("staggerTo,staggerFrom,staggerFromTo",function(r){Ft[r]=function(){var e=new an,t=ec.call(arguments,0);return t.splice(r==="staggerFromTo"?5:4,0,0),e[r].apply(e,t)}});var Vc=function(e,t,n){return e[t]=n},qf=function(e,t,n){return e[t](n)},zx=function(e,t,n,i){return e[t](i.fp,n)},Vx=function(e,t,n){return e.setAttribute(t,n)},Gc=function(e,t){return Dt(e[t])?qf:Dc(e[t])&&e.setAttribute?Vx:Vc},Yf=function(e,t){return t.set(t.t,t.p,Math.round((t.s+t.c*e)*1e6)/1e6,t)},Gx=function(e,t){return t.set(t.t,t.p,!!(t.s+t.c*e),t)},$f=function(e,t){var n=t._pt,i="";if(!e&&t.b)i=t.b;else if(e===1&&t.e)i=t.e;else{for(;n;)i=n.p+(n.m?n.m(n.s+n.c*e):Math.round((n.s+n.c*e)*1e4)/1e4)+i,n=n._next;i+=t.c}t.set(t.t,t.p,i,t)},Hc=function(e,t){for(var n=t._pt;n;)n.r(e,n.d),n=n._next},Hx=function(e,t,n,i){for(var s=this._pt,a;s;)a=s._next,s.p===i&&s.modifier(e,t,n),s=a},Wx=function(e){for(var t=this._pt,n,i;t;)i=t._next,t.p===e&&!t.op||t.op===e?io(this,t,"_pt"):t.dep||(n=1),t=i;return!n},Xx=function(e,t,n,i){i.mSet(e,t,i.m.call(i.tween,n,i.mt),i)},Kf=function(e){for(var t=e._pt,n,i,s,a;t;){for(n=t._next,i=s;i&&i.pr>t.pr;)i=i._next;(t._prev=i?i._prev:a)?t._prev._next=t:s=t,(t._next=i)?i._prev=t:a=t,t=n}e._pt=s},vn=function(){function r(t,n,i,s,a,o,l,c,u){this.t=n,this.s=s,this.c=a,this.p=i,this.r=o||Yf,this.d=l||this,this.set=c||Vc,this.pr=u||0,this._next=t,t&&(t._prev=this)}var e=r.prototype;return e.modifier=function(n,i,s){this.mSet=this.mSet||this.set,this.set=Xx,this.m=n,this.mt=s,this.tween=i},r}();gn(Fc+"parent,duration,ease,delay,overwrite,runBackwards,startAt,yoyo,immediateRender,repeat,repeatDelay,data,paused,reversed,lazy,callbackScope,stringFilter,id,yoyoEase,stagger,inherit,repeatRefresh,keyframes,autoRevert,scrollTrigger",function(r){return Nc[r]=1});In.TweenMax=In.TweenLite=Ft;In.TimelineLite=In.TimelineMax=an;wt=new an({sortChildren:!1,defaults:ss,autoRemoveChildren:!0,id:"root",smoothChildTiming:!0});Ln.stringFilter=Bf;var Sr=[],za={},qx=[],_h=0,Yx=0,Jo=function(e){return(za[e]||qx).map(function(t){return t()})},rc=function(){var e=Date.now(),t=[];e-_h>2&&(Jo("matchMediaInit"),Sr.forEach(function(n){var i=n.queries,s=n.conditions,a,o,l,c;for(o in i)a=Qn.matchMedia(i[o]).matches,a&&(l=1),a!==s[o]&&(s[o]=a,c=1);c&&(n.revert(),l&&t.push(n))}),Jo("matchMediaRevert"),t.forEach(function(n){return n.onMatch(n,function(i){return n.add(null,i)})}),_h=e,Jo("matchMedia"))},Zf=function(){function r(t,n){this.selector=n&&tc(n),this.data=[],this._r=[],this.isReverted=!1,this.id=Yx++,t&&this.add(t)}var e=r.prototype;return e.add=function(n,i,s){Dt(n)&&(s=i,i=n,n=Dt);var a=this,o=function(){var c=Et,u=a.selector,f;return c&&c!==a&&c.data.push(a),s&&(a.selector=tc(s)),Et=a,f=i.apply(a,arguments),Dt(f)&&a._r.push(f),Et=c,a.selector=u,a.isReverted=!1,f};return a.last=o,n===Dt?o(a,function(l){return a.add(null,l)}):n?a[n]=o:o},e.ignore=function(n){var i=Et;Et=null,n(this),Et=i},e.getTweens=function(){var n=[];return this.data.forEach(function(i){return i instanceof r?n.push.apply(n,i.getTweens()):i instanceof Ft&&!(i.parent&&i.parent.data==="nested")&&n.push(i)}),n},e.clear=function(){this._r.length=this.data.length=0},e.kill=function(n,i){var s=this;if(n?function(){for(var o=s.getTweens(),l=s.data.length,c;l--;)c=s.data[l],c.data==="isFlip"&&(c.revert(),c.getChildren(!0,!0,!1).forEach(function(u){return o.splice(o.indexOf(u),1)}));for(o.map(function(u){return{g:u._dur||u._delay||u._sat&&!u._sat.vars.immediateRender?u.globalTime(0):-1/0,t:u}}).sort(function(u,f){return f.g-u.g||-1/0}).forEach(function(u){return u.t.revert(n)}),l=s.data.length;l--;)c=s.data[l],c instanceof an?c.data!=="nested"&&(c.scrollTrigger&&c.scrollTrigger.revert(),c.kill()):!(c instanceof Ft)&&c.revert&&c.revert(n);s._r.forEach(function(u){return u(n,s)}),s.isReverted=!0}():this.data.forEach(function(o){return o.kill&&o.kill()}),this.clear(),i)for(var a=Sr.length;a--;)Sr[a].id===this.id&&Sr.splice(a,1)},e.revert=function(n){this.kill(n||{})},r}(),$x=function(){function r(t){this.contexts=[],this.scope=t,Et&&Et.data.push(this)}var e=r.prototype;return e.add=function(n,i,s){ui(n)||(n={matches:n});var a=new Zf(0,s||this.scope),o=a.conditions={},l,c,u;Et&&!a.selector&&(a.selector=Et.selector),this.contexts.push(a),i=a.add("onMatch",i),a.queries=n;for(c in n)c==="all"?u=1:(l=Qn.matchMedia(n[c]),l&&(Sr.indexOf(a)<0&&Sr.push(a),(o[c]=l.matches)&&(u=1),l.addListener?l.addListener(rc):l.addEventListener("change",rc)));return u&&i(a,function(f){return a.add(null,f)}),this},e.revert=function(n){this.kill(n||{})},e.kill=function(n){this.contexts.forEach(function(i){return i.kill(n,!0)})},r}(),ja={registerPlugin:function(){for(var e=arguments.length,t=new Array(e),n=0;n<e;n++)t[n]=arguments[n];t.forEach(function(i){return Nf(i)})},timeline:function(e){return new an(e)},getTweensOf:function(e,t){return wt.getTweensOf(e,t)},getProperty:function(e,t,n,i){Wt(e)&&(e=Vn(e)[0]);var s=vr(e||{}).get,a=n?yf:Sf;return n==="native"&&(n=""),e&&(t?a((An[t]&&An[t].get||s)(e,t,n,i)):function(o,l,c){return a((An[o]&&An[o].get||s)(e,o,l,c))})},quickSetter:function(e,t,n){if(e=Vn(e),e.length>1){var i=e.map(function(u){return Mn.quickSetter(u,t,n)}),s=i.length;return function(u){for(var f=s;f--;)i[f](u)}}e=e[0]||{};var a=An[t],o=vr(e),l=o.harness&&(o.harness.aliases||{})[t]||t,c=a?function(u){var f=new a;Yr._pt=0,f.init(e,n?u+n:u,Yr,0,[e]),f.render(1,f),Yr._pt&&Hc(1,Yr)}:o.set(e,l);return a?c:function(u){return c(e,l,n?u+n:u,o,1)}},quickTo:function(e,t,n){var i,s=Mn.to(e,Un((i={},i[t]="+=0.1",i.paused=!0,i.stagger=0,i),n||{})),a=function(l,c,u){return s.resetTo(t,l,c,u)};return a.tween=s,a},isTweening:function(e){return wt.getTweensOf(e,!0).length>0},defaults:function(e){return e&&e.ease&&(e.ease=Mr(e.ease,ss.ease)),hh(ss,e||{})},config:function(e){return hh(Ln,e||{})},registerEffect:function(e){var t=e.name,n=e.effect,i=e.plugins,s=e.defaults,a=e.extendTimeline;(i||"").split(",").forEach(function(o){return o&&!An[o]&&!In[o]&&ks(t+" effect requires "+o+" plugin.")}),$o[t]=function(o,l,c){return n(Vn(o),Un(l||{},s),c)},a&&(an.prototype[t]=function(o,l,c){return this.add($o[t](o,ui(l)?l:(c=l)&&{},this),c)})},registerEase:function(e,t){at[e]=Mr(t)},parseEase:function(e,t){return arguments.length?Mr(e,t):at},getById:function(e){return wt.getById(e)},exportRoot:function(e,t){e===void 0&&(e={});var n=new an(e),i,s;for(n.smoothChildTiming=_n(e.smoothChildTiming),wt.remove(n),n._dp=0,n._time=n._tTime=wt._time,i=wt._first;i;)s=i._next,(t||!(!i._dur&&i instanceof Ft&&i.vars.onComplete===i._targets[0]))&&ti(n,i,i._start-i._delay),i=s;return ti(wt,n,0),n},context:function(e,t){return e?new Zf(e,t):Et},matchMedia:function(e){return new $x(e)},matchMediaRefresh:function(){return Sr.forEach(function(e){var t=e.conditions,n,i;for(i in t)t[i]&&(t[i]=!1,n=1);n&&e.revert()})||rc()},addEventListener:function(e,t){var n=za[e]||(za[e]=[]);~n.indexOf(t)||n.push(t)},removeEventListener:function(e,t){var n=za[e],i=n&&n.indexOf(t);i>=0&&n.splice(i,1)},utils:{wrap:Ax,wrapYoyo:wx,distribute:Cf,random:Df,snap:Pf,normalize:Tx,getUnit:Qt,clamp:Sx,splitColor:Ff,toArray:Vn,selector:tc,mapRange:If,pipe:bx,unitize:Ex,interpolate:Rx,shuffle:Rf},install:_f,effects:$o,ticker:Cn,updateRoot:an.updateRoot,plugins:An,globalTimeline:wt,core:{PropTween:vn,globals:gf,Tween:Ft,Timeline:an,Animation:Hs,getCache:vr,_removeLinkedListItem:io,reverting:function(){return $t},context:function(e){return e&&Et&&(Et.data.push(e),e._ctx=Et),Et},suppressOverwrites:function(e){return Pc=e}}};gn("to,from,fromTo,delayedCall,set,killTweensOf",function(r){return ja[r]=Ft[r]});Cn.add(an.updateRoot);Yr=ja.to({},{duration:0});var Kx=function(e,t){for(var n=e._pt;n&&n.p!==t&&n.op!==t&&n.fp!==t;)n=n._next;return n},Zx=function(e,t){var n=e._targets,i,s,a;for(i in t)for(s=n.length;s--;)a=e._ptLookup[s][i],a&&(a=a.d)&&(a._pt&&(a=Kx(a,i)),a&&a.modifier&&a.modifier(t[i],e,n[s],i))},Qo=function(e,t){return{name:e,headless:1,rawVars:1,init:function(i,s,a){a._onInit=function(o){var l,c;if(Wt(s)&&(l={},gn(s,function(u){return l[u]=1}),s=l),t){l={};for(c in s)l[c]=t(s[c]);s=l}Zx(o,s)}}}},Mn=ja.registerPlugin({name:"attr",init:function(e,t,n,i,s){var a,o,l;this.tween=n;for(a in t)l=e.getAttribute(a)||"",o=this.add(e,"setAttribute",(l||0)+"",t[a],i,s,0,0,a),o.op=a,o.b=l,this._props.push(a)},render:function(e,t){for(var n=t._pt;n;)$t?n.set(n.t,n.p,n.b,n):n.r(e,n.d),n=n._next}},{name:"endArray",headless:1,init:function(e,t){for(var n=t.length;n--;)this.add(e,n,e[n]||0,t[n],0,0,0,0,0,1)}},Qo("roundProps",nc),Qo("modifiers"),Qo("snap",Pf))||ja;Ft.version=an.version=Mn.version="3.14.2";mf=1;Lc()&&cs();at.Power0;at.Power1;at.Power2;at.Power3;at.Power4;at.Linear;at.Quad;at.Cubic;at.Quart;at.Quint;at.Strong;at.Elastic;at.Back;at.SteppedEase;at.Bounce;at.Sine;at.Expo;at.Circ;/*!
 * CSSPlugin 3.14.2
 * https://gsap.com
 *
 * Copyright 2008-2025, GreenSock. All rights reserved.
 * Subject to the terms at https://gsap.com/standard-license
 * @author: Jack Doyle, jack@greensock.com
*/var gh,qi,Jr,Wc,gr,vh,Xc,jx=function(){return typeof window<"u"},Ri={},fr=180/Math.PI,Qr=Math.PI/180,Wr=Math.atan2,xh=1e8,qc=/([A-Z])/g,Jx=/(left|right|width|margin|padding|x)/i,Qx=/[\s,\(]\S/,ri={autoAlpha:"opacity,visibility",scale:"scaleX,scaleY",alpha:"opacity"},sc=function(e,t){return t.set(t.t,t.p,Math.round((t.s+t.c*e)*1e4)/1e4+t.u,t)},eM=function(e,t){return t.set(t.t,t.p,e===1?t.e:Math.round((t.s+t.c*e)*1e4)/1e4+t.u,t)},tM=function(e,t){return t.set(t.t,t.p,e?Math.round((t.s+t.c*e)*1e4)/1e4+t.u:t.b,t)},nM=function(e,t){return t.set(t.t,t.p,e===1?t.e:e?Math.round((t.s+t.c*e)*1e4)/1e4+t.u:t.b,t)},iM=function(e,t){var n=t.s+t.c*e;t.set(t.t,t.p,~~(n+(n<0?-.5:.5))+t.u,t)},jf=function(e,t){return t.set(t.t,t.p,e?t.e:t.b,t)},Jf=function(e,t){return t.set(t.t,t.p,e!==1?t.b:t.e,t)},rM=function(e,t,n){return e.style[t]=n},sM=function(e,t,n){return e.style.setProperty(t,n)},aM=function(e,t,n){return e._gsap[t]=n},oM=function(e,t,n){return e._gsap.scaleX=e._gsap.scaleY=n},lM=function(e,t,n,i,s){var a=e._gsap;a.scaleX=a.scaleY=n,a.renderTransform(s,a)},cM=function(e,t,n,i,s){var a=e._gsap;a[t]=n,a.renderTransform(s,a)},Rt="transform",xn=Rt+"Origin",uM=function r(e,t){var n=this,i=this.target,s=i.style,a=i._gsap;if(e in Ri&&s){if(this.tfm=this.tfm||{},e!=="transform")e=ri[e]||e,~e.indexOf(",")?e.split(",").forEach(function(o){return n.tfm[o]=Mi(i,o)}):this.tfm[e]=a.x?a[e]:Mi(i,e),e===xn&&(this.tfm.zOrigin=a.zOrigin);else return ri.transform.split(",").forEach(function(o){return r.call(n,o,t)});if(this.props.indexOf(Rt)>=0)return;a.svg&&(this.svgo=i.getAttribute("data-svg-origin"),this.props.push(xn,t,"")),e=Rt}(s||t)&&this.props.push(e,t,s[e])},Qf=function(e){e.translate&&(e.removeProperty("translate"),e.removeProperty("scale"),e.removeProperty("rotate"))},hM=function(){var e=this.props,t=this.target,n=t.style,i=t._gsap,s,a;for(s=0;s<e.length;s+=3)e[s+1]?e[s+1]===2?t[e[s]](e[s+2]):t[e[s]]=e[s+2]:e[s+2]?n[e[s]]=e[s+2]:n.removeProperty(e[s].substr(0,2)==="--"?e[s]:e[s].replace(qc,"-$1").toLowerCase());if(this.tfm){for(a in this.tfm)i[a]=this.tfm[a];i.svg&&(i.renderTransform(),t.setAttribute("data-svg-origin",this.svgo||"")),s=Xc(),(!s||!s.isStart)&&!n[Rt]&&(Qf(n),i.zOrigin&&n[xn]&&(n[xn]+=" "+i.zOrigin+"px",i.zOrigin=0,i.renderTransform()),i.uncache=1)}},ed=function(e,t){var n={target:e,props:[],revert:hM,save:uM};return e._gsap||Mn.core.getCache(e),t&&e.style&&e.nodeType&&t.split(",").forEach(function(i){return n.save(i)}),n},td,ac=function(e,t){var n=qi.createElementNS?qi.createElementNS((t||"http://www.w3.org/1999/xhtml").replace(/^https/,"http"),e):qi.createElement(e);return n&&n.style?n:qi.createElement(e)},Dn=function r(e,t,n){var i=getComputedStyle(e);return i[t]||i.getPropertyValue(t.replace(qc,"-$1").toLowerCase())||i.getPropertyValue(t)||!n&&r(e,us(t)||t,1)||""},Mh="O,Moz,ms,Ms,Webkit".split(","),us=function(e,t,n){var i=t||gr,s=i.style,a=5;if(e in s&&!n)return e;for(e=e.charAt(0).toUpperCase()+e.substr(1);a--&&!(Mh[a]+e in s););return a<0?null:(a===3?"ms":a>=0?Mh[a]:"")+e},oc=function(){jx()&&window.document&&(gh=window,qi=gh.document,Jr=qi.documentElement,gr=ac("div")||{style:{}},ac("div"),Rt=us(Rt),xn=Rt+"Origin",gr.style.cssText="border-width:0;line-height:0;position:absolute;padding:0",td=!!us("perspective"),Xc=Mn.core.reverting,Wc=1)},Sh=function(e){var t=e.ownerSVGElement,n=ac("svg",t&&t.getAttribute("xmlns")||"http://www.w3.org/2000/svg"),i=e.cloneNode(!0),s;i.style.display="block",n.appendChild(i),Jr.appendChild(n);try{s=i.getBBox()}catch{}return n.removeChild(i),Jr.removeChild(n),s},yh=function(e,t){for(var n=t.length;n--;)if(e.hasAttribute(t[n]))return e.getAttribute(t[n])},nd=function(e){var t,n;try{t=e.getBBox()}catch{t=Sh(e),n=1}return t&&(t.width||t.height)||n||(t=Sh(e)),t&&!t.width&&!t.x&&!t.y?{x:+yh(e,["x","cx","x1"])||0,y:+yh(e,["y","cy","y1"])||0,width:0,height:0}:t},id=function(e){return!!(e.getCTM&&(!e.parentNode||e.ownerSVGElement)&&nd(e))},Qi=function(e,t){if(t){var n=e.style,i;t in Ri&&t!==xn&&(t=Rt),n.removeProperty?(i=t.substr(0,2),(i==="ms"||t.substr(0,6)==="webkit")&&(t="-"+t),n.removeProperty(i==="--"?t:t.replace(qc,"-$1").toLowerCase())):n.removeAttribute(t)}},Yi=function(e,t,n,i,s,a){var o=new vn(e._pt,t,n,0,1,a?Jf:jf);return e._pt=o,o.b=i,o.e=s,e._props.push(n),o},bh={deg:1,rad:1,turn:1},fM={grid:1,flex:1},er=function r(e,t,n,i){var s=parseFloat(n)||0,a=(n+"").trim().substr((s+"").length)||"px",o=gr.style,l=Jx.test(t),c=e.tagName.toLowerCase()==="svg",u=(c?"client":"offset")+(l?"Width":"Height"),f=100,h=i==="px",d=i==="%",_,g,p,m;if(i===a||!s||bh[i]||bh[a])return s;if(a!=="px"&&!h&&(s=r(e,t,n,"px")),m=e.getCTM&&id(e),(d||a==="%")&&(Ri[t]||~t.indexOf("adius")))return _=m?e.getBBox()[l?"width":"height"]:e[u],It(d?s/_*f:s/100*_);if(o[l?"width":"height"]=f+(h?a:i),g=i!=="rem"&&~t.indexOf("adius")||i==="em"&&e.appendChild&&!c?e:e.parentNode,m&&(g=(e.ownerSVGElement||{}).parentNode),(!g||g===qi||!g.appendChild)&&(g=qi.body),p=g._gsap,p&&d&&p.width&&l&&p.time===Cn.time&&!p.uncache)return It(s/p.width*f);if(d&&(t==="height"||t==="width")){var M=e.style[t];e.style[t]=f+i,_=e[u],M?e.style[t]=M:Qi(e,t)}else(d||a==="%")&&!fM[Dn(g,"display")]&&(o.position=Dn(e,"position")),g===e&&(o.position="static"),g.appendChild(gr),_=gr[u],g.removeChild(gr),o.position="absolute";return l&&d&&(p=vr(g),p.time=Cn.time,p.width=g[u]),It(h?_*s/f:_&&s?f/_*s:0)},Mi=function(e,t,n,i){var s;return Wc||oc(),t in ri&&t!=="transform"&&(t=ri[t],~t.indexOf(",")&&(t=t.split(",")[0])),Ri[t]&&t!=="transform"?(s=Xs(e,i),s=t!=="transformOrigin"?s[t]:s.svg?s.origin:Qa(Dn(e,xn))+" "+s.zOrigin+"px"):(s=e.style[t],(!s||s==="auto"||i||~(s+"").indexOf("calc("))&&(s=Ja[t]&&Ja[t](e,t,n)||Dn(e,t)||xf(e,t)||(t==="opacity"?1:0))),n&&!~(s+"").trim().indexOf(" ")?er(e,t,s,n)+n:s},dM=function(e,t,n,i){if(!n||n==="none"){var s=us(t,e,1),a=s&&Dn(e,s,1);a&&a!==n?(t=s,n=a):t==="borderColor"&&(n=Dn(e,"borderTopColor"))}var o=new vn(this._pt,e.style,t,0,1,$f),l=0,c=0,u,f,h,d,_,g,p,m,M,S,y,A;if(o.b=n,o.e=i,n+="",i+="",i.substring(0,6)==="var(--"&&(i=Dn(e,i.substring(4,i.indexOf(")")))),i==="auto"&&(g=e.style[t],e.style[t]=i,i=Dn(e,t)||i,g?e.style[t]=g:Qi(e,t)),u=[n,i],Bf(u),n=u[0],i=u[1],h=n.match(qr)||[],A=i.match(qr)||[],A.length){for(;f=qr.exec(i);)p=f[0],M=i.substring(l,f.index),_?_=(_+1)%5:(M.substr(-5)==="rgba("||M.substr(-5)==="hsla(")&&(_=1),p!==(g=h[c++]||"")&&(d=parseFloat(g)||0,y=g.substr((d+"").length),p.charAt(1)==="="&&(p=jr(d,p)+y),m=parseFloat(p),S=p.substr((m+"").length),l=qr.lastIndex-S.length,S||(S=S||Ln.units[t]||y,l===i.length&&(i+=S,o.e+=S)),y!==S&&(d=er(e,t,g,S)||0),o._pt={_next:o._pt,p:M||c===1?M:",",s:d,c:m-d,m:_&&_<4||t==="zIndex"?Math.round:0});o.c=l<i.length?i.substring(l,i.length):""}else o.r=t==="display"&&i==="none"?Jf:jf;return pf.test(i)&&(o.e=0),this._pt=o,o},Eh={top:"0%",bottom:"100%",left:"0%",right:"100%",center:"50%"},pM=function(e){var t=e.split(" "),n=t[0],i=t[1]||"50%";return(n==="top"||n==="bottom"||i==="left"||i==="right")&&(e=n,n=i,i=e),t[0]=Eh[n]||n,t[1]=Eh[i]||i,t.join(" ")},mM=function(e,t){if(t.tween&&t.tween._time===t.tween._dur){var n=t.t,i=n.style,s=t.u,a=n._gsap,o,l,c;if(s==="all"||s===!0)i.cssText="",l=1;else for(s=s.split(","),c=s.length;--c>-1;)o=s[c],Ri[o]&&(l=1,o=o==="transformOrigin"?xn:Rt),Qi(n,o);l&&(Qi(n,Rt),a&&(a.svg&&n.removeAttribute("transform"),i.scale=i.rotate=i.translate="none",Xs(n,1),a.uncache=1,Qf(i)))}},Ja={clearProps:function(e,t,n,i,s){if(s.data!=="isFromStart"){var a=e._pt=new vn(e._pt,t,n,0,0,mM);return a.u=i,a.pr=-10,a.tween=s,e._props.push(n),1}}},Ws=[1,0,0,1,0,0],rd={},sd=function(e){return e==="matrix(1, 0, 0, 1, 0, 0)"||e==="none"||!e},Th=function(e){var t=Dn(e,Rt);return sd(t)?Ws:t.substr(7).match(df).map(It)},Yc=function(e,t){var n=e._gsap||vr(e),i=e.style,s=Th(e),a,o,l,c;return n.svg&&e.getAttribute("transform")?(l=e.transform.baseVal.consolidate().matrix,s=[l.a,l.b,l.c,l.d,l.e,l.f],s.join(",")==="1,0,0,1,0,0"?Ws:s):(s===Ws&&!e.offsetParent&&e!==Jr&&!n.svg&&(l=i.display,i.display="block",a=e.parentNode,(!a||!e.offsetParent&&!e.getBoundingClientRect().width)&&(c=1,o=e.nextElementSibling,Jr.appendChild(e)),s=Th(e),l?i.display=l:Qi(e,"display"),c&&(o?a.insertBefore(e,o):a?a.appendChild(e):Jr.removeChild(e))),t&&s.length>6?[s[0],s[1],s[4],s[5],s[12],s[13]]:s)},lc=function(e,t,n,i,s,a){var o=e._gsap,l=s||Yc(e,!0),c=o.xOrigin||0,u=o.yOrigin||0,f=o.xOffset||0,h=o.yOffset||0,d=l[0],_=l[1],g=l[2],p=l[3],m=l[4],M=l[5],S=t.split(" "),y=parseFloat(S[0])||0,A=parseFloat(S[1])||0,w,C,x,b;n?l!==Ws&&(C=d*p-_*g)&&(x=y*(p/C)+A*(-g/C)+(g*M-p*m)/C,b=y*(-_/C)+A*(d/C)-(d*M-_*m)/C,y=x,A=b):(w=nd(e),y=w.x+(~S[0].indexOf("%")?y/100*w.width:y),A=w.y+(~(S[1]||S[0]).indexOf("%")?A/100*w.height:A)),i||i!==!1&&o.smooth?(m=y-c,M=A-u,o.xOffset=f+(m*d+M*g)-m,o.yOffset=h+(m*_+M*p)-M):o.xOffset=o.yOffset=0,o.xOrigin=y,o.yOrigin=A,o.smooth=!!i,o.origin=t,o.originIsAbsolute=!!n,e.style[xn]="0px 0px",a&&(Yi(a,o,"xOrigin",c,y),Yi(a,o,"yOrigin",u,A),Yi(a,o,"xOffset",f,o.xOffset),Yi(a,o,"yOffset",h,o.yOffset)),e.setAttribute("data-svg-origin",y+" "+A)},Xs=function(e,t){var n=e._gsap||new Gf(e);if("x"in n&&!t&&!n.uncache)return n;var i=e.style,s=n.scaleX<0,a="px",o="deg",l=getComputedStyle(e),c=Dn(e,xn)||"0",u,f,h,d,_,g,p,m,M,S,y,A,w,C,x,b,W,L,G,z,q,H,N,F,ne,ie,me,Te,le,Oe,Je,$e;return u=f=h=g=p=m=M=S=y=0,d=_=1,n.svg=!!(e.getCTM&&id(e)),l.translate&&((l.translate!=="none"||l.scale!=="none"||l.rotate!=="none")&&(i[Rt]=(l.translate!=="none"?"translate3d("+(l.translate+" 0 0").split(" ").slice(0,3).join(", ")+") ":"")+(l.rotate!=="none"?"rotate("+l.rotate+") ":"")+(l.scale!=="none"?"scale("+l.scale.split(" ").join(",")+") ":"")+(l[Rt]!=="none"?l[Rt]:"")),i.scale=i.rotate=i.translate="none"),C=Yc(e,n.svg),n.svg&&(n.uncache?(ne=e.getBBox(),c=n.xOrigin-ne.x+"px "+(n.yOrigin-ne.y)+"px",F=""):F=!t&&e.getAttribute("data-svg-origin"),lc(e,F||c,!!F||n.originIsAbsolute,n.smooth!==!1,C)),A=n.xOrigin||0,w=n.yOrigin||0,C!==Ws&&(L=C[0],G=C[1],z=C[2],q=C[3],u=H=C[4],f=N=C[5],C.length===6?(d=Math.sqrt(L*L+G*G),_=Math.sqrt(q*q+z*z),g=L||G?Wr(G,L)*fr:0,M=z||q?Wr(z,q)*fr+g:0,M&&(_*=Math.abs(Math.cos(M*Qr))),n.svg&&(u-=A-(A*L+w*z),f-=w-(A*G+w*q))):($e=C[6],Oe=C[7],me=C[8],Te=C[9],le=C[10],Je=C[11],u=C[12],f=C[13],h=C[14],x=Wr($e,le),p=x*fr,x&&(b=Math.cos(-x),W=Math.sin(-x),F=H*b+me*W,ne=N*b+Te*W,ie=$e*b+le*W,me=H*-W+me*b,Te=N*-W+Te*b,le=$e*-W+le*b,Je=Oe*-W+Je*b,H=F,N=ne,$e=ie),x=Wr(-z,le),m=x*fr,x&&(b=Math.cos(-x),W=Math.sin(-x),F=L*b-me*W,ne=G*b-Te*W,ie=z*b-le*W,Je=q*W+Je*b,L=F,G=ne,z=ie),x=Wr(G,L),g=x*fr,x&&(b=Math.cos(x),W=Math.sin(x),F=L*b+G*W,ne=H*b+N*W,G=G*b-L*W,N=N*b-H*W,L=F,H=ne),p&&Math.abs(p)+Math.abs(g)>359.9&&(p=g=0,m=180-m),d=It(Math.sqrt(L*L+G*G+z*z)),_=It(Math.sqrt(N*N+$e*$e)),x=Wr(H,N),M=Math.abs(x)>2e-4?x*fr:0,y=Je?1/(Je<0?-Je:Je):0),n.svg&&(F=e.getAttribute("transform"),n.forceCSS=e.setAttribute("transform","")||!sd(Dn(e,Rt)),F&&e.setAttribute("transform",F))),Math.abs(M)>90&&Math.abs(M)<270&&(s?(d*=-1,M+=g<=0?180:-180,g+=g<=0?180:-180):(_*=-1,M+=M<=0?180:-180)),t=t||n.uncache,n.x=u-((n.xPercent=u&&(!t&&n.xPercent||(Math.round(e.offsetWidth/2)===Math.round(-u)?-50:0)))?e.offsetWidth*n.xPercent/100:0)+a,n.y=f-((n.yPercent=f&&(!t&&n.yPercent||(Math.round(e.offsetHeight/2)===Math.round(-f)?-50:0)))?e.offsetHeight*n.yPercent/100:0)+a,n.z=h+a,n.scaleX=It(d),n.scaleY=It(_),n.rotation=It(g)+o,n.rotationX=It(p)+o,n.rotationY=It(m)+o,n.skewX=M+o,n.skewY=S+o,n.transformPerspective=y+a,(n.zOrigin=parseFloat(c.split(" ")[2])||!t&&n.zOrigin||0)&&(i[xn]=Qa(c)),n.xOffset=n.yOffset=0,n.force3D=Ln.force3D,n.renderTransform=n.svg?gM:td?ad:_M,n.uncache=0,n},Qa=function(e){return(e=e.split(" "))[0]+" "+e[1]},el=function(e,t,n){var i=Qt(t);return It(parseFloat(t)+parseFloat(er(e,"x",n+"px",i)))+i},_M=function(e,t){t.z="0px",t.rotationY=t.rotationX="0deg",t.force3D=0,ad(e,t)},ur="0deg",Ts="0px",hr=") ",ad=function(e,t){var n=t||this,i=n.xPercent,s=n.yPercent,a=n.x,o=n.y,l=n.z,c=n.rotation,u=n.rotationY,f=n.rotationX,h=n.skewX,d=n.skewY,_=n.scaleX,g=n.scaleY,p=n.transformPerspective,m=n.force3D,M=n.target,S=n.zOrigin,y="",A=m==="auto"&&e&&e!==1||m===!0;if(S&&(f!==ur||u!==ur)){var w=parseFloat(u)*Qr,C=Math.sin(w),x=Math.cos(w),b;w=parseFloat(f)*Qr,b=Math.cos(w),a=el(M,a,C*b*-S),o=el(M,o,-Math.sin(w)*-S),l=el(M,l,x*b*-S+S)}p!==Ts&&(y+="perspective("+p+hr),(i||s)&&(y+="translate("+i+"%, "+s+"%) "),(A||a!==Ts||o!==Ts||l!==Ts)&&(y+=l!==Ts||A?"translate3d("+a+", "+o+", "+l+") ":"translate("+a+", "+o+hr),c!==ur&&(y+="rotate("+c+hr),u!==ur&&(y+="rotateY("+u+hr),f!==ur&&(y+="rotateX("+f+hr),(h!==ur||d!==ur)&&(y+="skew("+h+", "+d+hr),(_!==1||g!==1)&&(y+="scale("+_+", "+g+hr),M.style[Rt]=y||"translate(0, 0)"},gM=function(e,t){var n=t||this,i=n.xPercent,s=n.yPercent,a=n.x,o=n.y,l=n.rotation,c=n.skewX,u=n.skewY,f=n.scaleX,h=n.scaleY,d=n.target,_=n.xOrigin,g=n.yOrigin,p=n.xOffset,m=n.yOffset,M=n.forceCSS,S=parseFloat(a),y=parseFloat(o),A,w,C,x,b;l=parseFloat(l),c=parseFloat(c),u=parseFloat(u),u&&(u=parseFloat(u),c+=u,l+=u),l||c?(l*=Qr,c*=Qr,A=Math.cos(l)*f,w=Math.sin(l)*f,C=Math.sin(l-c)*-h,x=Math.cos(l-c)*h,c&&(u*=Qr,b=Math.tan(c-u),b=Math.sqrt(1+b*b),C*=b,x*=b,u&&(b=Math.tan(u),b=Math.sqrt(1+b*b),A*=b,w*=b)),A=It(A),w=It(w),C=It(C),x=It(x)):(A=f,x=h,w=C=0),(S&&!~(a+"").indexOf("px")||y&&!~(o+"").indexOf("px"))&&(S=er(d,"x",a,"px"),y=er(d,"y",o,"px")),(_||g||p||m)&&(S=It(S+_-(_*A+g*C)+p),y=It(y+g-(_*w+g*x)+m)),(i||s)&&(b=d.getBBox(),S=It(S+i/100*b.width),y=It(y+s/100*b.height)),b="matrix("+A+","+w+","+C+","+x+","+S+","+y+")",d.setAttribute("transform",b),M&&(d.style[Rt]=b)},vM=function(e,t,n,i,s){var a=360,o=Wt(s),l=parseFloat(s)*(o&&~s.indexOf("rad")?fr:1),c=l-i,u=i+c+"deg",f,h;return o&&(f=s.split("_")[1],f==="short"&&(c%=a,c!==c%(a/2)&&(c+=c<0?a:-a)),f==="cw"&&c<0?c=(c+a*xh)%a-~~(c/a)*a:f==="ccw"&&c>0&&(c=(c-a*xh)%a-~~(c/a)*a)),e._pt=h=new vn(e._pt,t,n,i,c,eM),h.e=u,h.u="deg",e._props.push(n),h},Ah=function(e,t){for(var n in t)e[n]=t[n];return e},xM=function(e,t,n){var i=Ah({},n._gsap),s="perspective,force3D,transformOrigin,svgOrigin",a=n.style,o,l,c,u,f,h,d,_;i.svg?(c=n.getAttribute("transform"),n.setAttribute("transform",""),a[Rt]=t,o=Xs(n,1),Qi(n,Rt),n.setAttribute("transform",c)):(c=getComputedStyle(n)[Rt],a[Rt]=t,o=Xs(n,1),a[Rt]=c);for(l in Ri)c=i[l],u=o[l],c!==u&&s.indexOf(l)<0&&(d=Qt(c),_=Qt(u),f=d!==_?er(n,l,c,_):parseFloat(c),h=parseFloat(u),e._pt=new vn(e._pt,o,l,f,h-f,sc),e._pt.u=_||0,e._props.push(l));Ah(o,i)};gn("padding,margin,Width,Radius",function(r,e){var t="Top",n="Right",i="Bottom",s="Left",a=(e<3?[t,n,i,s]:[t+s,t+n,i+n,i+s]).map(function(o){return e<2?r+o:"border"+o+r});Ja[e>1?"border"+r:r]=function(o,l,c,u,f){var h,d;if(arguments.length<4)return h=a.map(function(_){return Mi(o,_,c)}),d=h.join(" "),d.split(h[0]).length===5?h[0]:d;h=(u+"").split(" "),d={},a.forEach(function(_,g){return d[_]=h[g]=h[g]||h[(g-1)/2|0]}),o.init(l,d,f)}});var od={name:"css",register:oc,targetTest:function(e){return e.style&&e.nodeType},init:function(e,t,n,i,s){var a=this._props,o=e.style,l=n.vars.startAt,c,u,f,h,d,_,g,p,m,M,S,y,A,w,C,x,b;Wc||oc(),this.styles=this.styles||ed(e),x=this.styles.props,this.tween=n;for(g in t)if(g!=="autoRound"&&(u=t[g],!(An[g]&&Hf(g,t,n,i,e,s)))){if(d=typeof u,_=Ja[g],d==="function"&&(u=u.call(n,i,e,s),d=typeof u),d==="string"&&~u.indexOf("random(")&&(u=Vs(u)),_)_(this,e,g,u,n)&&(C=1);else if(g.substr(0,2)==="--")c=(getComputedStyle(e).getPropertyValue(g)+"").trim(),u+="",Ki.lastIndex=0,Ki.test(c)||(p=Qt(c),m=Qt(u),m?p!==m&&(c=er(e,g,c,m)+m):p&&(u+=p)),this.add(o,"setProperty",c,u,i,s,0,0,g),a.push(g),x.push(g,0,o[g]);else if(d!=="undefined"){if(l&&g in l?(c=typeof l[g]=="function"?l[g].call(n,i,e,s):l[g],Wt(c)&&~c.indexOf("random(")&&(c=Vs(c)),Qt(c+"")||c==="auto"||(c+=Ln.units[g]||Qt(Mi(e,g))||""),(c+"").charAt(1)==="="&&(c=Mi(e,g))):c=Mi(e,g),h=parseFloat(c),M=d==="string"&&u.charAt(1)==="="&&u.substr(0,2),M&&(u=u.substr(2)),f=parseFloat(u),g in ri&&(g==="autoAlpha"&&(h===1&&Mi(e,"visibility")==="hidden"&&f&&(h=0),x.push("visibility",0,o.visibility),Yi(this,o,"visibility",h?"inherit":"hidden",f?"inherit":"hidden",!f)),g!=="scale"&&g!=="transform"&&(g=ri[g],~g.indexOf(",")&&(g=g.split(",")[0]))),S=g in Ri,S){if(this.styles.save(g),b=u,d==="string"&&u.substring(0,6)==="var(--"){if(u=Dn(e,u.substring(4,u.indexOf(")"))),u.substring(0,5)==="calc("){var W=e.style.perspective;e.style.perspective=u,u=Dn(e,"perspective"),W?e.style.perspective=W:Qi(e,"perspective")}f=parseFloat(u)}if(y||(A=e._gsap,A.renderTransform&&!t.parseTransform||Xs(e,t.parseTransform),w=t.smoothOrigin!==!1&&A.smooth,y=this._pt=new vn(this._pt,o,Rt,0,1,A.renderTransform,A,0,-1),y.dep=1),g==="scale")this._pt=new vn(this._pt,A,"scaleY",A.scaleY,(M?jr(A.scaleY,M+f):f)-A.scaleY||0,sc),this._pt.u=0,a.push("scaleY",g),g+="X";else if(g==="transformOrigin"){x.push(xn,0,o[xn]),u=pM(u),A.svg?lc(e,u,0,w,0,this):(m=parseFloat(u.split(" ")[2])||0,m!==A.zOrigin&&Yi(this,A,"zOrigin",A.zOrigin,m),Yi(this,o,g,Qa(c),Qa(u)));continue}else if(g==="svgOrigin"){lc(e,u,1,w,0,this);continue}else if(g in rd){vM(this,A,g,h,M?jr(h,M+u):u);continue}else if(g==="smoothOrigin"){Yi(this,A,"smooth",A.smooth,u);continue}else if(g==="force3D"){A[g]=u;continue}else if(g==="transform"){xM(this,u,e);continue}}else g in o||(g=us(g)||g);if(S||(f||f===0)&&(h||h===0)&&!Qx.test(u)&&g in o)p=(c+"").substr((h+"").length),f||(f=0),m=Qt(u)||(g in Ln.units?Ln.units[g]:p),p!==m&&(h=er(e,g,c,m)),this._pt=new vn(this._pt,S?A:o,g,h,(M?jr(h,M+f):f)-h,!S&&(m==="px"||g==="zIndex")&&t.autoRound!==!1?iM:sc),this._pt.u=m||0,S&&b!==u?(this._pt.b=c,this._pt.e=b,this._pt.r=nM):p!==m&&m!=="%"&&(this._pt.b=c,this._pt.r=tM);else if(g in o)dM.call(this,e,g,c,M?M+u:u);else if(g in e)this.add(e,g,c||e[g],M?M+u:u,i,s);else if(g!=="parseTransform"){Uc(g,u);continue}S||(g in o?x.push(g,0,o[g]):typeof e[g]=="function"?x.push(g,2,e[g]()):x.push(g,1,c||e[g])),a.push(g)}}C&&Kf(this)},render:function(e,t){if(t.tween._time||!Xc())for(var n=t._pt;n;)n.r(e,n.d),n=n._next;else t.styles.revert()},get:Mi,aliases:ri,getSetter:function(e,t,n){var i=ri[t];return i&&i.indexOf(",")<0&&(t=i),t in Ri&&t!==xn&&(e._gsap.x||Mi(e,"x"))?n&&vh===n?t==="scale"?oM:aM:(vh=n||{})&&(t==="scale"?lM:cM):e.style&&!Dc(e.style[t])?rM:~t.indexOf("-")?sM:Gc(e,t)},core:{_removeProperty:Qi,_getMatrix:Yc}};Mn.utils.checkPrefix=us;Mn.core.getStyleSaver=ed;(function(r,e,t,n){var i=gn(r+","+e+","+t,function(s){Ri[s]=1});gn(e,function(s){Ln.units[s]="deg",rd[s]=1}),ri[i[13]]=r+","+e,gn(n,function(s){var a=s.split(":");ri[a[1]]=i[a[0]]})})("x,y,z,scale,scaleX,scaleY,xPercent,yPercent","rotation,rotationX,rotationY,skewX,skewY","transform,transformOrigin,svgOrigin,force3D,smoothOrigin,transformPerspective","0:translateX,1:translateY,2:translateZ,8:rotate,8:rotationZ,8:rotateZ,9:rotateX,10:rotateY");gn("x,y,z,top,right,bottom,left,width,height,fontSize,padding,margin,perspective",function(r){Ln.units[r]="px"});Mn.registerPlugin(od);var zi=Mn.registerPlugin(od)||Mn;zi.core.Tween;const MM={class:"scene-toolbar"},SM={class:"toolbar-left"},yM={class:"toolbar-center"},bM={class:"scene-hud"},EM={class:"hud-panel"},TM={class:"hud-panel"},AM={class:"hud-panel"},wM={class:"tt-header"},RM=["innerHTML"],CM=cc({__name:"Panorama3D",props:{buildings:{},floors:{},roomLookup:{}},emits:["click-room","click-bed"],setup(r,{emit:e}){const t=r,n=e,i=ct(null),s=ct(null),a=ct("PARK"),o=ct(""),l=ct(""),c=ct(""),u=ct(!0),f=ct(!1),h=ct("园区总览"),d=ct("等待交互"),_=ct({visible:!1,x:0,y:0,title:"",content:""}),g=Ge(()=>{let Q="园区全景";return o.value&&(Q+=` / ${o.value}`),l.value&&(Q+=` / ${l.value}`),c.value&&(Q+=` / 房间 ${c.value}`),Q}),p=Ge(()=>a.value==="PARK"?"园区":a.value==="BUILDING"?"楼栋":a.value==="FLOOR"?"楼层":"房间"),m=ms(new pu),M=ms(null),S=ms(null),y=ms(null),A=ms(null),w=new Map,C=[],x=[];let b=null,W=0,L=0,G=0;const z=ct(!0),q=new qe,H=new Wp;let N=null;const F={building:new hn({color:5756927,transparent:!0,opacity:.1,roughness:.15,metalness:.86,side:Xn}),floor:new hn({color:530735,transparent:!0,opacity:.95,roughness:.42,metalness:.42}),room:new hn({color:1456217,transparent:!0,opacity:.7,roughness:.55,metalness:.2}),roomHover:new hn({color:2871295,emissive:2871295,emissiveIntensity:.4,transparent:!0,opacity:.85}),hover:new hn({color:6546431,emissive:2871295,emissiveIntensity:.6,transparent:!0,opacity:.85}),bedNormal:new hn({color:7506595,emissive:7506595,emissiveIntensity:.22,metalness:.42,roughness:.34}),bedOccupied:new hn({color:3790079,emissive:3790079,emissiveIntensity:.44,metalness:.38,roughness:.28}),bedSleep:new hn({color:4550143,emissive:4550143,emissiveIntensity:.4,metalness:.36,roughness:.3}),bedAi:new hn({color:10649087,emissive:10649087,emissiveIntensity:.42,metalness:.34,roughness:.32}),bedWarning:new hn({color:16757588,emissive:16757588,emissiveIntensity:.48,metalness:.34,roughness:.3}),bedAlert:new hn({color:16735612,emissive:16735612,emissiveIntensity:.76,metalness:.26,roughness:.28}),bedOffline:new hn({color:7571616,emissive:7571616,emissiveIntensity:.18,metalness:.2,roughness:.5})};function ne(){if(!s.value)return;const Q=s.value.clientWidth,Z=s.value.clientHeight;m.value=new pu,m.value.background=new it(199446),m.value.fog=new Ec(133137,.008),M.value=new wn(42,Q/Z,.1,1600),M.value.position.set(42,38,64),S.value=new zv({antialias:!0,alpha:!0}),S.value.setSize(Q,Z),S.value.setPixelRatio(Math.min(window.devicePixelRatio,2)),S.value.setClearColor(133137,1),S.value.outputColorSpace=Tn,S.value.toneMapping=dc,S.value.toneMappingExposure=1.16,s.value.appendChild(S.value.domElement),y.value=new tx,y.value.setSize(Q,Z),y.value.domElement.style.position="absolute",y.value.domElement.style.top="0",y.value.domElement.style.pointerEvents="none",s.value.appendChild(y.value.domElement),A.value=new Gv(M.value,S.value.domElement),A.value.enableDamping=!0,A.value.dampingFactor=.05,A.value.minDistance=8,A.value.maxDistance=180,A.value.maxPolarAngle=Math.PI/2-.04,A.value.autoRotate=!0,A.value.autoRotateSpeed=.12;const T=new Vp(14217215,.62);m.value.add(T);const v=new Cu(15793151,1.45);v.position.set(36,80,40),m.value.add(v);const D=new Cu(4571135,.65);D.position.set(-40,24,-36),m.value.add(D);const B=new kp(9401599,1.2,260,2);B.position.set(0,20,-24),m.value.add(B),ie(),S.value.domElement.addEventListener("pointermove",Ve),S.value.domElement.addEventListener("click",Ne),window.addEventListener("resize",Ye),$e(),rt()}function ie(){if(!m.value)return;const Q=new Xp(280,42,1456217,660774);Q.position.y=-.12,Q.material.transparent=!0,Q.material.opacity=.38,Q.userData.isGenerated=!0,m.value.add(Q);const Z=new Ac(32,34,120),T=new Wa({color:2871295,transparent:!0,opacity:.08,side:Xn}),v=new Xt(Z,T);v.rotation.x=-Math.PI/2,v.position.y=-.08,v.userData.isGenerated=!0,m.value.add(v);const D=new ln,B=320,j=new Float32Array(B*3);for(let ce=0;ce<B;ce+=1)j[ce*3]=(Math.random()-.5)*180,j[ce*3+1]=Math.random()*28+4,j[ce*3+2]=(Math.random()-.5)*180;D.setAttribute("position",new Yn(j,3));const $=new Zh({size:.7,color:5756927,transparent:!0,opacity:.4});b=new Cp(D,$),b.userData.isGenerated=!0,m.value.add(b)}function me(Q,Z){const T=document.createElement("div");return T.className=`scene-label ${Z}`,T.textContent=Q,new Gr(T)}function Te(Q){const Z=Number(Q.abnormalVital24hCount||0);return Q.status===0||Q.occupancySource==="FROZEN"?"offline":Q.riskLevel==="HIGH"||Z>0?"alert":Q.status===3||Q.occupancySource==="MAINTENANCE"||Q.occupancySource==="CLEANING"?"warning":Q.riskLevel==="MEDIUM"||Q.riskSource?"ai":Q.riskLevel==="LOW"&&Q.elderId?"sleep":Q.elderId?"occupied":"normal"}function le(Q){return Q==="occupied"?F.bedOccupied.clone():Q==="sleep"?F.bedSleep.clone():Q==="ai"?F.bedAi.clone():Q==="warning"?F.bedWarning.clone():Q==="alert"?F.bedAlert.clone():Q==="offline"?F.bedOffline.clone():F.bedNormal.clone()}function Oe(Q,Z,T){const v=Te(Q),D=new Si,B=.22,j=new Xt(new pn(Z,B,T),new hn({color:1058109,metalness:.5,roughness:.42}));j.position.y=B/2,D.add(j);const $=le(v),ce=new Xt(new pn(Z*.82,.14,T*.74),$);ce.position.y=B+.08,ce.userData={type:"bed",bed:Q,originalMat:$,state:v},D.add(ce),C.push(ce);const se=new Xt(new pn(Z*.84,.34,.08),new hn({color:2048871,metalness:.42,roughness:.26}));se.position.set(0,.34,-T*.36),D.add(se);const Re=new Xt(new Ks(Z*.96,T*.88),new Wa({color:$.color,transparent:!0,opacity:v==="alert"?.24:.12}));return Re.rotation.x=-Math.PI/2,Re.position.y=.02,D.add(Re),v==="alert"?x.push({mesh:ce,type:"pulse",baseIntensity:.76}):v==="ai"?x.push({mesh:ce,type:"ai",baseIntensity:.42}):v==="warning"?x.push({mesh:ce,type:"blink",baseIntensity:.48}):v==="sleep"&&x.push({mesh:ce,type:"sleep",baseIntensity:.44}),D}function Je(){C.length=0,x.length=0,w.clear(),N=null,_.value.visible=!1,m.value.children.filter(Z=>Z.userData.isGenerated).forEach(Z=>{Z.traverse(T=>{var B,j,$;const v=T;v.geometry&&((j=(B=v.geometry).dispose)==null||j.call(B));const D=v.material;Array.isArray(D)?D.forEach(ce=>{var se;return(se=ce.dispose)==null?void 0:se.call(ce)}):($=D==null?void 0:D.dispose)==null||$.call(D)}),m.value.remove(Z)}),ie()}function $e(){if(Je(),!t.buildings.length)return;const Q=new Si;Q.userData.isGenerated=!0,m.value.add(Q);const Z=Math.ceil(Math.sqrt(t.buildings.length)),T=24;t.buildings.forEach((v,D)=>{const B=new Si;B.userData={type:"building",name:v};const j=Math.floor(D/Z),$=D%Z;B.position.set(($-(Z-1)/2)*T,0,(j-(Math.ceil(t.buildings.length/Z)-1)/2)*T);const ce=t.floors.filter(be=>t.roomLookup.has(`${v}@@${be}`)),se=4,Re=ce.length||1;ce.forEach((be,Ee)=>{const Be=new Si,U=Re-1-Ee;Be.position.y=U*se,Be.userData={type:"floor",building:v,name:be,floorYIndex:U};const fe=t.roomLookup.get(`${v}@@${be}`)||[],ue=Math.ceil(Math.sqrt(Math.max(fe.length,1))),_e=4.4,ae=.8,J=ue*(_e+ae)+1.2,we=Math.ceil(Math.max(fe.length,1)/ue)*(_e+ae)+1.2,Le=new Xt(new pn(J,.42,we),F.floor.clone());Le.position.y=.18,Le.userData={type:"floorSlab",building:v,floor:be,originalMat:F.floor},Be.add(Le),C.push(Le),w.set(`floor_${v}_${be}`,Be);const ft=new Fa(new Bo(new pn(J,.42,we)),new Ds({color:2871295,transparent:!0,opacity:.28}));ft.position.y=.18,Be.add(ft),fe.forEach((Ut,cn)=>{const Sn=new Si;Sn.userData={type:"room",building:v,floor:be,room:Ut};const Ci=Math.floor(cn/ue),Ar=cn%ue;Sn.position.set((Ar-(ue-1)/2)*(_e+ae),.42,(Ci-(Math.ceil(Math.max(fe.length,1)/ue)-1)/2)*(_e+ae));const $n=new Xt(new pn(_e,.22,_e),F.room.clone());$n.position.y=.11,$n.userData={type:"roomTile",ref:Sn,data:Ut,floorName:be,originalMat:F.room},Sn.add($n),C.push($n);const nr=new Fa(new Bo(new pn(_e,.22,_e)),new Ds({color:2871295,transparent:!0,opacity:.26}));nr.position.y=.11,Sn.add(nr);const Ot=Ut.beds.length;if(Ot){const Kt=Math.ceil(Math.sqrt(Ot)),Pi=(_e-.8)/Kt,hi=Math.min(Pi*1.5,(_e-.8)/Math.ceil(Ot/Kt));Ut.beds.forEach((ir,Zn)=>{const Di=Oe(ir,Pi*.88,hi*.82),wr=Math.floor(Zn/Kt),ds=Zn%Kt;Di.position.set((ds-(Kt-1)/2)*Pi,.26,(wr-(Math.ceil(Ot/Kt)-1)/2)*hi),Sn.add(Di)})}const Kn=me(`${Ut.roomNo}  ${Ut.occupiedBeds}/${Ut.totalBeds}`,"room-label");Kn.position.set(0,1.9,0),Kn.visible=!1,Sn.add(Kn),Be.add(Sn)});const tt=me(be,"floor-label");tt.position.set(J/2+1.6,.2,0),tt.visible=!1,Be.add(tt),B.add(Be)});const Ue=Math.max(Re*se,se),re=new Xt(new pn(12,Ue,12),F.building.clone());re.position.y=Ue/2,re.userData={type:"buildingHitbox",name:v,ref:B,totalFloors:Re,totalRooms:ce.reduce((be,Ee)=>{var Be;return be+(((Be=t.roomLookup.get(`${v}@@${Ee}`))==null?void 0:Be.length)||0)},0)},B.add(re),C.push(re);const he=new Fa(new Bo(new pn(12,Ue,12)),new Ds({color:5756927,transparent:!0,opacity:.58}));he.position.y=Ue/2,B.add(he);const Ce=me(v,"building-label");Ce.position.set(0,Ue+2.2,0),B.add(Ce),Q.add(B),w.set(`building_${v}`,B)}),ee()}function ee(){A.value.autoRotate=a.value==="PARK"&&u.value,w.forEach((Q,Z)=>{if(!Z.startsWith("building_"))return;const T=Q.userData.name,v=Q.children.find(B=>B.userData.type==="buildingHitbox"),D=Q.children.find(B=>B instanceof Gr&&B.element.className.includes("building-label"));if(a.value==="PARK"){Q.visible=!0,v&&(v.visible=!0),D&&(D.visible=!0),Q.children.forEach(B=>{B.userData.type==="floor"&&(B.visible=!1,zi.to(B.position,{y:B.userData.originalY??B.position.y,duration:.5}))});return}if(T!==o.value){Q.visible=!1;return}Q.visible=!0,v&&(v.visible=!1),D&&(D.visible=!1),Q.children.forEach(B=>{if(B.userData.type==="floor"){if(B.userData.originalY=B.userData.originalY??B.position.y,a.value==="BUILDING"){B.visible=!0,zi.to(B.position,{y:B.userData.floorYIndex*8.5,duration:.85,ease:"power2.out"}),B.children.forEach(j=>{if(j.userData.type==="room"){j.visible=!0;const $=j.children.find(ce=>ce instanceof Gr&&ce.element.className.includes("room-label"));$&&($.visible=!0)}j instanceof Gr&&j.element.className.includes("floor-label")&&(j.visible=!1)});return}B.userData.name===l.value?(B.visible=!0,zi.to(B.position,{y:0,duration:.85,ease:"power2.out"}),B.children.forEach(j=>{if(j.userData.type==="room"){const $=j.userData.room.roomNo===c.value;j.visible=a.value!=="ROOM"||$;const ce=j.children.find(se=>se instanceof Gr&&se.element.className.includes("room-label"));ce&&(ce.visible=a.value==="FLOOR")}j instanceof Gr&&j.element.className.includes("floor-label")&&(j.visible=!1)})):B.visible=!1}})})}function de(Q,Z){if(_.value.visible=!0,_.value.x=Q.clientX+18,_.value.y=Q.clientY+18,Z.userData.type==="buildingHitbox"){_.value.title=Z.userData.name,_.value.content=`<div class="tt-row"><span>楼层数</span><span class="tt-val cyan">${Z.userData.totalFloors} 层</span></div>
      <div class="tt-row"><span>总房间</span><span class="tt-val">${Z.userData.totalRooms} 间</span></div>
      <div class="tt-tip">点击展开楼栋内部结构</div>`,h.value=Z.userData.name,d.value="楼栋级观察";return}if(Z.userData.type==="floorSlab"){_.value.title=`${Z.userData.building} / ${Z.userData.floor}`,_.value.content=`<div class="tt-row"><span>透视模式</span><span class="tt-val cyan">楼层平铺</span></div>
      <div class="tt-tip">点击进入楼层鸟瞰视图</div>`,h.value=`${Z.userData.building} ${Z.userData.floor}`,d.value="楼层级观察";return}if(Z.userData.type==="roomTile"){const T=Z.userData.data;_.value.title=`房间 ${T.roomNo||"-"}`,_.value.content=`<div class="tt-row"><span>容量</span><span class="tt-val">${T.totalBeds} 床</span></div>
      <div class="tt-row"><span>入住</span><span class="tt-val ${T.occupiedBeds>=T.totalBeds?"red":"green"}">${T.occupiedBeds}/${T.totalBeds}</span></div>
      <div class="tt-tip">${a.value==="ROOM"?"再次点击打开房间详情":"点击进入房间视角"}</div>`,h.value=`房间 ${T.roomNo||"-"}`,d.value=`${T.occupiedBeds}/${T.totalBeds} 床位占用`;return}if(Z.userData.type==="bed"){const T=Z.userData.bed,v=T.elderId?`${T.elderName||"在住长者"}`:"空床待命";_.value.title=`床位 ${T.bedNo||"-"}`,_.value.content=`<div class="tt-row"><span>状态</span><span class="tt-val">${v}</span></div>
      <div class="tt-row"><span>风险</span><span class="tt-val ${T.riskLevel==="HIGH"?"red":T.riskLevel==="MEDIUM"?"purple":T.riskLevel==="LOW"?"blue":"cyan"}">${T.riskLabel||"守护稳定"}</span></div>
      <div class="tt-row"><span>24h异常</span><span class="tt-val">${T.abnormalVital24hCount||0} 次</span></div>
      <div class="tt-tip">点击查看业务详情</div>`,h.value=`${T.roomNo||"-"} / ${T.bedNo||"-"}`,d.value=T.riskLabel||(T.elderId?"长者守护中":"空床待命");return}_.value.visible=!1}function pe(Q){(Q.userData.type==="buildingHitbox"||Q.userData.type==="floorSlab")&&(Q.material=Q.userData.originalMat||F.building),Q.userData.type==="roomTile"&&(Q.material=Q.userData.originalMat||F.room),Q.userData.type==="bed"&&(Q.material=Q.userData.originalMat,Q.scale.set(1,1,1))}function Ve(Q){var B;if(!s.value||!M.value)return;const Z=performance.now();if(Z-G<32)return;G=Z;const T=s.value.getBoundingClientRect();q.x=(Q.clientX-T.left)/T.width*2-1,q.y=-((Q.clientY-T.top)/T.height)*2+1,H.setFromCamera(q,M.value);const v=H.intersectObjects(C,!1).filter(j=>j.object.visible);if(N&&N!==((B=v[0])==null?void 0:B.object)&&(pe(N),N=null),!v.length){_.value.visible=!1,S.value&&(S.value.domElement.style.cursor="default");return}const D=v[0].object;de(Q,D),D!==N&&((D.userData.type==="buildingHitbox"||D.userData.type==="floorSlab")&&(D.userData.originalMat=D.material,D.material=F.hover),D.userData.type==="roomTile"&&(D.material=F.roomHover),D.userData.type==="bed"&&(D.scale.set(1.14,1.14,1.14),x.some(j=>j.mesh===D)||(D.material=F.hover)),N=D),S.value.domElement.style.cursor="pointer"}function Ne(){if(!N){Tt();return}if(N.userData.type==="buildingHitbox"){o.value=N.userData.name,l.value="",c.value="",a.value="BUILDING",ee(),Fe(N.userData.ref);return}if(N.userData.type==="floorSlab"){l.value=N.userData.floor,c.value="",a.value="FLOOR",ee();const Q=w.get(`floor_${o.value}_${l.value}`);Q&&Fe(Q,!0);return}if(N.userData.type==="roomTile"){if(a.value==="ROOM"){n("click-room",N.userData.data);return}c.value=N.userData.ref.userData.room.roomNo,a.value="ROOM",ee(),Fe(N.userData.ref,!1,18);return}N.userData.type==="bed"&&n("click-bed",N.userData.bed)}function Fe(Q,Z=!1,T){if(!M.value||!A.value)return;const v=new hs().setFromObject(Q),D=v.getCenter(new V),B=v.getSize(new V),j=Math.max(B.x,B.y,B.z),$=M.value.fov*(Math.PI/180),ce=T||Math.abs(j/2/Math.tan($/2))*1.8,se=Z?0:ce*.42,Re=Z?0:ce;zi.to(M.value.position,{x:D.x+se,y:Z?D.y+ce:D.y+B.y/2+10,z:D.z+Re,duration:1.24,ease:"power3.inOut"}),zi.to(A.value.target,{x:D.x,y:D.y,z:D.z,duration:1.24,ease:"power3.inOut"})}function Tt(){if(a.value==="ROOM"){a.value="FLOOR",c.value="",ee();const Q=w.get(`floor_${o.value}_${l.value}`);Q&&Fe(Q,!0);return}if(a.value==="FLOOR"){a.value="BUILDING",l.value="",ee();const Q=w.get(`building_${o.value}`);Q&&Fe(Q);return}a.value==="BUILDING"&&(a.value="PARK",o.value="",l.value="",ee(),Qe())}function Qe(){!M.value||!A.value||(a.value="PARK",o.value="",l.value="",c.value="",u.value=!0,h.value="园区总览",d.value="等待交互",ee(),zi.to(M.value.position,{x:42,y:38,z:64,duration:1.1,ease:"power3.out"}),zi.to(A.value.target,{x:0,y:0,z:0,duration:1.1,ease:"power3.out"}))}function ot(){u.value=!u.value,ee()}async function pt(){var Q,Z,T;if(i.value){if(!document.fullscreenElement){await((Z=(Q=i.value).requestFullscreen)==null?void 0:Z.call(Q).catch(()=>{})),f.value=!!document.fullscreenElement;return}await((T=document.exitFullscreen)==null?void 0:T.call(document).catch(()=>{})),f.value=!!document.fullscreenElement}}function Ye(){if(!s.value||!M.value||!S.value||!y.value)return;const Q=s.value.clientWidth,Z=s.value.clientHeight;M.value.aspect=Q/Z,M.value.updateProjectionMatrix(),S.value.setSize(Q,Z),y.value.setSize(Q,Z)}function vt(){L&&window.clearTimeout(L),L=window.setTimeout(()=>{L=0,$e()},80)}function I(){z.value=!document.hidden,z.value&&!W&&rt()}function yt(){f.value=!!document.fullscreenElement}function rt(){var Z,T,v;if(W=requestAnimationFrame(rt),!z.value)return;const Q=Date.now()*.0014;(Z=A.value)==null||Z.update(),x.forEach(D=>{const B=D.mesh.material;D.type==="pulse"?B.emissiveIntensity=D.baseIntensity+(Math.sin(Q*5)+1)*.3:D.type==="ai"?B.emissiveIntensity=D.baseIntensity+(Math.sin(Q*3.4)+1)*.18:D.type==="blink"?B.emissiveIntensity=Math.sin(Q*7)>0?D.baseIntensity+.28:D.baseIntensity*.36:B.emissiveIntensity=D.baseIntensity+(Math.sin(Q*2.8)+1)*.12}),b&&(b.rotation.y+=8e-4,b.position.y=Math.sin(Q*.8)*.6),(T=S.value)==null||T.render(m.value,M.value),(v=y.value)==null||v.render(m.value,M.value)}return Hi(()=>t.buildings,vt,{deep:!0}),Hi(()=>t.floors,vt,{deep:!0}),Hi(()=>t.roomLookup,vt,{deep:!0}),wh(()=>{z.value=!document.hidden,ne(),document.addEventListener("visibilitychange",I),document.addEventListener("fullscreenchange",yt)}),uc(()=>{var Q,Z,T,v;cancelAnimationFrame(W),W=0,L&&(window.clearTimeout(L),L=0),(Q=S.value)==null||Q.domElement.removeEventListener("pointermove",Ve),(Z=S.value)==null||Z.domElement.removeEventListener("click",Ne),window.removeEventListener("resize",Ye),document.removeEventListener("visibilitychange",I),document.removeEventListener("fullscreenchange",yt),(T=A.value)==null||T.dispose(),(v=S.value)==null||v.dispose()}),(Q,Z)=>{const T=dn("a-button");return et(),mt("div",{class:"panorama-container",ref_key:"containerRef",ref:i},[Z[7]||(Z[7]=O("div",{class:"scene-atmosphere"},null,-1)),Z[8]||(Z[8]=O("div",{class:"scene-scanlines"},null,-1)),O("div",MM,[O("div",SM,[a.value!=="PARK"?(et(),Bi(T,{key:0,class:"tech-btn",onClick:Tt},{default:Xe(()=>[...Z[0]||(Z[0]=[st("返回上级",-1)])]),_:1})):ki("",!0),ke(T,{class:"tech-btn",onClick:ot},{default:Xe(()=>[st(ge(u.value?"暂停巡航":"自动巡航"),1)]),_:1}),ke(T,{class:"tech-btn",onClick:Qe},{default:Xe(()=>[...Z[1]||(Z[1]=[st("重置视角",-1)])]),_:1}),ke(T,{class:"tech-btn",onClick:pt},{default:Xe(()=>[st(ge(f.value?"退出全屏":"全屏展示"),1)]),_:1})]),O("div",yM,[Z[2]||(Z[2]=O("span",{class:"toolbar-eyebrow"},"Current Scope",-1)),O("strong",null,ge(g.value),1)]),Z[3]||(Z[3]=ld('<div class="toolbar-right" data-v-ffb3085a><div class="legend-chip status-occupied" data-v-ffb3085a>正常守护</div><div class="legend-chip status-empty" data-v-ffb3085a>空床待命</div><div class="legend-chip status-warning" data-v-ffb3085a>离床观察</div><div class="legend-chip status-sleep" data-v-ffb3085a>睡眠稳定</div><div class="legend-chip status-ai" data-v-ffb3085a>AI关注</div><div class="legend-chip status-alert" data-v-ffb3085a>异常提醒</div></div>',1))]),O("div",bM,[O("div",EM,[Z[4]||(Z[4]=O("span",{class:"hud-label"},"当前层级",-1)),O("strong",null,ge(p.value),1)]),O("div",TM,[Z[5]||(Z[5]=O("span",{class:"hud-label"},"聚焦对象",-1)),O("strong",null,ge(h.value),1)]),O("div",AM,[Z[6]||(Z[6]=O("span",{class:"hud-label"},"运行状态",-1)),O("strong",null,ge(d.value),1)])]),cd(O("div",{class:"tech-tooltip",style:Rh({left:`${_.value.x}px`,top:`${_.value.y}px`})},[O("div",wM,ge(_.value.title),1),O("div",{class:"tt-body",innerHTML:_.value.content},null,8,RM)],4),[[ud,_.value.visible]]),O("div",{ref_key:"canvasRef",ref:s,class:"canvas-wrapper"},null,512)],512)}}}),PM=hc(CM,[["__scopeId","data-v-ffb3085a"]]),DM={class:"immersive-stage-shell"},LM={class:"hud hud-topbar"},IM={class:"hud-topbar__brand"},UM={class:"brand-copy"},NM={class:"hud-topbar__metrics"},FM={class:"hud-topbar__ops"},OM={class:"status-stack"},BM={class:"clock-stack"},kM={class:"operator-stack"},zM={class:"hud hud-left"},VM={class:"hud-panel"},GM={class:"hud-panel__head"},HM={class:"overview-list"},WM={class:"status-bars"},XM={class:"status-bar__top"},qM={class:"distribution-track"},YM={class:"hud-panel hud-panel--compact"},$M={class:"field-block"},KM={class:"field-block"},ZM={class:"field-block"},jM={class:"field-inline"},JM={class:"field-block"},QM={class:"scope-chip-list"},eS=["onClick"],tS={key:0,class:"field-block"},nS={class:"scope-chip-list compact"},iS=["onClick"],rS={class:"scene-actions"},sS={class:"hud hud-center"},aS={class:"hud-panel hud-panel--spotlight"},oS={class:"hud-panel__head"},lS={class:"spotlight-meta"},cS={class:"spotlight-block"},uS={class:"spotlight-block"},hS={class:"overlay-progress"},fS={class:"spotlight-actions"},dS=["onClick"],pS={class:"hud hud-right"},mS={key:0,class:"hud-panel hud-panel--detail"},_S={class:"hud-panel__head"},gS={class:"detail-tags"},vS={class:"overlay-chip"},xS={key:0,class:"overlay-chip"},MS={class:"overlay-chip"},SS={class:"detail-grid"},yS={key:0,class:"blocker-list"},bS={class:"action-grid"},ES=["onClick"],TS={class:"hud-panel hud-panel--compact"},AS={class:"hud-panel__head"},wS={class:"event-list"},RS={class:"event-top"},CS={class:"event-type"},PS={class:"event-time"},DS={class:"hud-panel hud-panel--compact"},LS={class:"timeline-list"},IS={class:"hud-panel hud-panel--compact"},US={class:"focus-list"},NS=["onClick"],FS={class:"focus-status"},OS={class:"hud hud-bottom"},BS={class:"trend-panel"},kS={class:"trend-card"},zS={class:"trend-card"},VS={class:"trend-card"},GS={class:"trend-card"},HS=cc({__name:"BedPanorama",setup(r){const e=fd(),t=hd(),n=vd(),i=ct([]),s=ct({}),a=ct({}),o=ct(""),l=ct(null),c=ct(!1),u=ct(!1),f=ct(null),h=ct([]),d=ct(!1),_=ct("ALL"),g=ct(!1),p=ct("ALL"),m=ct(!1),M=ct(!1),S=ct(""),y=ct(""),A=ct(Qs()),w=[{label:"全部",value:"ALL"},{label:"仅空床",value:"IDLE"},{label:"仅入住",value:"OCCUPIED"}],C=[{label:"全部风险",value:"ALL"},{label:"高风险",value:"HIGH"},{label:"中风险",value:"MEDIUM"},{label:"低风险",value:"LOW"}],x=["合同签署入院","入住选床完成","在住照护执行","风险干预闭环"],b=["bedBuilding","bedFloor","bedKeyword","bedQuick","bedRiskEnabled","bedRiskLevel"],W=ct(""),L=ct(!1);let G=null;const z=Ge(()=>{const P=String(t.query.source||"").trim().toLowerCase(),R=String(t.query.scene||"").trim().toLowerCase(),oe=P==="lifecycle"||R==="status-change";return{active:oe,message:oe?"当前来自入住状态变更联动，可在床态视图快速确认清洁/维修与空床调配。":""}}),q=Ge(()=>i.value.filter(P=>{if(o.value){const R=`${P.roomNo||""} ${P.elderName||""}`.toLowerCase(),oe=o.value.toLowerCase();if(!R.includes(oe))return!1}return!0})),H=Ge(()=>{const P=q.value.filter(R=>_.value==="IDLE"?Kt(R):_.value==="OCCUPIED"?!!R.elderId:!0);return!g.value||!M.value?P:P.filter(R=>R.riskLevel?p.value==="ALL"?!0:R.riskLevel===p.value:!1)}),N=Ge(()=>H.value.filter(P=>!(S.value&&String(P.building||"")!==S.value||y.value&&String(P.floorNo||"")!==y.value))),F=Ge(()=>{const P=new Set;return N.value.forEach(R=>P.add((R.building||"未分配楼栋").trim()||"未分配楼栋")),Array.from(P).sort((R,oe)=>R.localeCompare(oe,"zh-CN"))}),ne=Ge(()=>F.value),ie=Ge(()=>{const P=new Set;return N.value.forEach(R=>P.add((R.floorNo||"未知楼层").trim()||"未知楼层")),Array.from(P).sort((R,oe)=>{const Me=Ar(oe)-Ar(R);return Me!==0?Me:String(R).localeCompare(String(oe),"zh-CN")})}),me=Ge(()=>ie.value),Te=Ge(()=>Array.from(Fe.value.values()).reduce((P,R)=>P+R.length,0)),le=Ge(()=>{const P={idle:0,reserved:0,occupied:0,maintenance:0,cleaning:0,locked:0};return i.value.forEach(R=>{const oe=Ot(R);oe==="空闲"&&(P.idle+=1),oe==="预定"&&(P.reserved+=1),oe==="在住"&&(P.occupied+=1),oe==="维修"&&(P.maintenance+=1),oe==="清洁中"&&(P.cleaning+=1),oe==="锁定"&&(P.locked+=1)}),P}),Oe=Ge(()=>[...H.value].filter(P=>Pi(P)).sort((P,R)=>+(R.riskLevel==="HIGH")-+(P.riskLevel==="HIGH")||Number(R.abnormalVital24hCount||0)-Number(P.abnormalVital24hCount||0))),Je=Ge(()=>Oe.value.filter(P=>P.riskLevel==="HIGH"||P.status===0).length),$e=Ge(()=>Oe.value.length),ee=Ge(()=>i.value.length?Math.round(le.value.occupied/i.value.length*100):0),de=Ge(()=>i.value.length?Math.max(0,Math.round((i.value.length-le.value.locked-le.value.maintenance)/i.value.length*100)):100),pe=Ge(()=>i.value.filter(P=>P.elderId&&P.riskLevel!=="HIGH"&&Number(P.abnormalVital24hCount||0)===0).length),Ve=Ge(()=>i.value.filter(P=>P.riskLevel==="MEDIUM"||P.occupancySource==="CLEANING").length),Ne=Ge(()=>i.value.filter(P=>P.riskLevel==="MEDIUM"||P.riskSource).length),Fe=Ge(()=>{const P=new Map,R=new Map;return N.value.forEach(oe=>{const Me=(oe.building||"未分配楼栋").trim()||"未分配楼栋",Ie=(oe.floorNo||"未知楼层").trim()||"未知楼层",Ze=(oe.roomNo||`房间-${oe.roomId||"-"}`).trim()||`房间-${oe.roomId||"-"}`,bt=`${Me}@@${Ie}@@${Ze}`;R.has(bt)||R.set(bt,[]),R.get(bt).push(oe)}),R.forEach((oe,Me)=>{var te,lt;const[Ie,Ze,bt]=Me.split("@@"),ve=`${Ie}@@${Ze}`;P.has(ve)||P.set(ve,[]);const Lt=String(((te=oe[0])==null?void 0:te.roomId)||""),ze=[...oe].sort((rn,fi)=>{const ps=Kt(rn)?0:1,$c=Kt(fi)?0:1;return ps!==$c?ps-$c:String(rn.bedNo||"").localeCompare(String(fi.bedNo||""))}),Gt=oe.length,Zt=oe.filter(rn=>!!rn.elderId).length,Nn=oe.filter(rn=>!!rn.elderName).length,nn=Gt-Zt,dt=a.value[Lt]||Gt||1,Ct=dt>=3?2:1;P.get(ve).push({key:Me,roomNo:bt,roomType:Kn(s.value[Lt]||"标准间"),capacity:dt,autoSpan:Ct,beds:ze,totalBeds:Gt,occupiedBeds:Zt,elderCount:Nn,emptyBeds:nn,remark:(lt=oe[0])==null?void 0:lt.roomRemark})}),P.forEach(oe=>{oe.sort((Me,Ie)=>Me.capacity!==Ie.capacity?Ie.capacity-Me.capacity:Me.emptyBeds!==Ie.emptyBeds?Ie.emptyBeds-Me.emptyBeds:String(Me.roomNo).localeCompare(String(Ie.roomNo)))}),P}),Tt=Ge(()=>A.value.format("YYYY年MM月DD日 dddd")),Qe=Ge(()=>A.value.format("HH:mm:ss")),ot=Ge(()=>{var oe,Me;const P=String(((oe=n.staffInfo)==null?void 0:oe.realName)||"").trim();if(P)return P;const R=String(((Me=n.staffInfo)==null?void 0:Me.username)||"").trim();return R||"值班护理长"}),pt=Ge(()=>String((n.roles||[])[0]||"").trim()||"智慧床态调度员"),Ye=Ge(()=>ot.value.slice(0,1)||"护"),vt=Ge(()=>z.value.active?"入住状态联动中":"全院床态守护中"),I=Ge(()=>[{label:"总床位数",numericValue:i.value.length,meta:`${ne.value.length} 栋 ${me.value.length} 层`,tone:"tone-cyan"},{label:"在住长者",numericValue:le.value.occupied,meta:`在住率 ${ee.value}%`,tone:"tone-green"},{label:"空床位",numericValue:le.value.idle,meta:`可立即调配 ${Math.max(0,le.value.idle-le.value.cleaning)} 张`,tone:"tone-gray"},{label:"今日需关注",numericValue:$e.value,meta:`${Je.value} 条高优先提醒`,tone:"tone-red"},{label:"守护设备在线率",numericValue:de.value,suffix:"%",meta:`在线设备 ${Math.max(0,i.value.length-le.value.maintenance-le.value.locked)}`,tone:"tone-blue"}]),yt=Ge(()=>[{label:"空床位",value:le.value.idle,meta:"待迎接入住",tone:"tone-gray"},{label:"在住长者",value:le.value.occupied,meta:"持续守护中",tone:"tone-cyan"},{label:"离床观察",value:Ve.value,meta:"夜巡重点",tone:"tone-orange"},{label:"需关注长者",value:$e.value,meta:"风险与异常联动",tone:"tone-red"},{label:"维修设备",value:le.value.maintenance,meta:"待恢复上线",tone:"tone-orange"},{label:"睡眠稳定",value:pe.value,meta:"夜间状态平稳",tone:"tone-deep-blue"}]),rt=Ge(()=>ne.value.slice(0,6)),Q=Ge(()=>S.value?ie.value.slice(0,8):me.value.slice(0,8)),Z=Ge(()=>{const P=Math.max(1,i.value.length);return[{label:"空床位",value:le.value.idle,percent:Math.round(le.value.idle/P*100),tone:"fill-gray"},{label:"在住长者",value:le.value.occupied,percent:Math.round(le.value.occupied/P*100),tone:"fill-cyan"},{label:"AI关注",value:Ne.value,percent:Math.round(Ne.value/P*100),tone:"fill-purple"},{label:"离床观察",value:Ve.value,percent:Math.round(Ve.value/P*100),tone:"fill-orange"},{label:"实时风险提醒",value:$e.value,percent:Math.round($e.value/P*100),tone:"fill-red"}]}),T=Ge(()=>(Oe.value.length?Oe.value:H.value.filter(R=>R.elderId)).slice(0,4)),v=Ge(()=>Oe.value.slice(0,6).map((P,R)=>({key:`alert-${P.id}`,type:P.riskLevel==="HIGH"?"高优先提醒":"实时提醒",time:Qs().subtract(R*4,"minute").format("HH:mm"),title:`${P.roomNo||"-"} / ${P.bedNo||"-"} ${P.elderName||"空床"}`,description:`${P.riskLabel||"体征波动"}，24h异常 ${P.abnormalVital24hCount||0} 次，当前床态 ${Ot(P)}`,tone:P.riskLevel==="HIGH"?"tone-red":"tone-orange"}))),D=Ge(()=>(Oe.value.length?Oe.value.slice(0,3):H.value.filter(R=>R.elderId).slice(0,3)).map((R,oe)=>({key:`ai-${R.id||oe}`,title:`${R.elderName||"待关怀长者"} · ${R.roomNo||"-"} ${R.bedNo||"-"}`,description:R.riskLevel==="HIGH"?"建议立即通知当班护理员复核生命体征，并发起 15 分钟内回访。":R.riskLevel==="MEDIUM"?"建议加入夜巡优先名单，关注离床频次、翻身节奏与睡眠连续性。":"建议保持当前照护节奏，并在下一班交接时同步睡眠稳定观察结果。",tone:R.riskLevel==="HIGH"?"dot-red":R.riskLevel==="MEDIUM"?"dot-purple":"dot-cyan"}))),B=Ge(()=>l.value?Kt(l.value)?1:l.value.riskLevel==="HIGH"?3:2:2),j=Ge(()=>l.value?Kt(l.value)?"空床待分配":l.value.riskLevel==="HIGH"?"高风险干预中":"照护执行中":"在院运行中"),$=Ge(()=>l.value?`床位 ${l.value.bedNo||"-"} / 房间 ${l.value.roomNo||"-"} / 长者 ${l.value.elderName||"空床"}`:`当前范围：${S.value||"全部楼栋"} ${y.value||"全部楼层"}`),ce=Ge(()=>{const P=[];return l.value?(Kt(l.value)&&P.push({code:"B201",text:"当前为空床，需通过入住办理分配",actionLabel:"去入住办理",actionKey:"go-admission"}),l.value.status===2&&P.push({code:"B301",text:"床位处于维修状态"}),l.value.riskLevel==="HIGH"&&P.push({code:"B401",text:"高风险长者需优先处置并建任务",actionLabel:"生成提醒",actionKey:"create-alert"}),P):(_.value==="IDLE"&&le.value.idle===0&&P.push({code:"B202",text:"暂无可分配空床"}),_.value==="OCCUPIED"&&le.value.occupied===0&&P.push({code:"B203",text:"当前无在住床位"}),P)}),se=Ge(()=>l.value?{title:`${l.value.roomNo||"-"} / ${l.value.bedNo||"-"}`,meta:`${l.value.elderName||"空床"} · ${Ot(l.value)} · ${l.value.riskLabel||"实时监测中"}`}:f.value?{title:`${f.value.roomNo||"-"} 房间`,meta:`${f.value.roomType} · 容量 ${f.value.capacity||0} 床 · 空床 ${f.value.emptyBeds||0}`}:{title:S.value||y.value?`${S.value||"全部楼栋"} ${y.value||""}`.trim():"园区总览",meta:S.value||y.value?`当前范围 ${Te.value} 间房 / ${H.value.length} 张床位`:"点击楼栋、楼层、房间或床位，快速进入对应视角"}),Re=Ge(()=>[{key:"reset-filters",label:"重置筛选",description:"回到总览范围",tone:"tone-cyan"},{key:"open-map",label:"房态视图",description:"切到平面房态图",tone:"tone-blue"},{key:"open-manage",label:"床位管理",description:"执行基础维护",tone:"tone-green"}]),Ue=Ge(()=>[{key:"open-profile",label:"长者档案",description:"查看在住档案与照护信息",tone:"tone-blue"},{key:"allocate-bed",label:"床位分配",description:"为空床发起入住分配",tone:"tone-cyan"},{key:"open-assessment",label:"评估档案",description:"进入能力评估归档",tone:"tone-purple"},{key:"open-contracts",label:"合同票据",description:"处理合同与账单联动",tone:"tone-green"},{key:"open-status-center",label:"状态中心",description:"发起状态变更闭环",tone:"tone-orange"},{key:"create-alert",label:"生成提醒",description:"同步推送提醒与任务",tone:"tone-red"}]),re=Ge(()=>Array.from({length:7},(P,R)=>Qs().subtract(6-R,"day").format("MM/DD")));function he(P,R){return R.map((oe,Me)=>{const Ie=Math.sin((Me+1)*.9)*Math.max(1,P*.05);return Math.max(0,Math.round(P*oe+Ie))})}const Ce=Ge(()=>U({labels:re.value,data:he(le.value.occupied,[.84,.88,.9,.94,.95,.97,1]),color:"#52f3c4",areaColor:"rgba(62, 232, 181, 0.22)"})),be=Ge(()=>U({labels:re.value,data:he(Math.max(1,Ve.value),[.72,.76,.8,.84,.9,.96,1]),color:"#ffbf74",areaColor:"rgba(255, 191, 116, 0.22)"})),Ee=Ge(()=>U({labels:re.value,data:he(Math.max(1,$e.value),[1.34,1.2,1.12,.92,.98,1.05,1]),color:"#ff6d89",areaColor:"rgba(255, 93, 124, 0.2)"})),Be=Ge(()=>U({labels:re.value,data:he(de.value,[.91,.92,.94,.95,.97,.98,1]),color:"#57d7ff",areaColor:"rgba(87, 215, 255, 0.2)",formatter:"{value}%"}));function U(P){return{animationDuration:900,animationEasing:"cubicOut",grid:{left:14,right:18,top:24,bottom:22,containLabel:!0},tooltip:{trigger:"axis",backgroundColor:"rgba(6, 18, 34, 0.92)",borderColor:"rgba(87, 215, 255, 0.3)",textStyle:{color:"#eaf7ff"}},xAxis:{type:"category",boundaryGap:!1,data:P.labels,axisLine:{lineStyle:{color:"rgba(120, 164, 201, 0.25)"}},axisLabel:{color:"#7fa2bf",fontSize:11}},yAxis:{type:"value",axisLine:{show:!1},splitLine:{lineStyle:{color:"rgba(120, 164, 201, 0.12)"}},axisLabel:{color:"#7fa2bf",fontSize:11,formatter:P.formatter||"{value}"}},series:[{type:"line",smooth:!0,symbol:"circle",symbolSize:7,data:P.data,lineStyle:{width:2,color:P.color,shadowBlur:14,shadowColor:P.color},itemStyle:{color:P.color,borderColor:"#ffffff",borderWidth:1},areaStyle:{color:P.areaColor}}]}}function fe(P){if(P.actionKey==="go-admission"){Y();return}P.actionKey==="create-alert"&&X()}function ue(P){if(P==="reset-filters"){we();return}if(P==="open-map"){Ae();return}if(P==="open-manage"){xe();return}if(P==="open-profile"){K();return}if(P==="allocate-bed"){Y();return}if(P==="open-assessment"){Js();return}if(P==="open-contracts"){E();return}if(P==="open-status-center"){k();return}P==="create-alert"&&X()}function _e(P){S.value=P,y.value=""}function ae(P){y.value=P}function J(){S.value="",y.value=""}function we(){o.value="",_.value="ALL",g.value=!1,p.value="ALL",J()}function Le(P){return Array.isArray(P)?Le(P[0]):P==null?"":String(P).trim()}function ft(P){return Object.entries(P||{}).reduce((R,[oe,Me])=>{const Ie=Le(Me);return Ie&&(R[oe]=Ie),R},{})}function tt(P){return[...b].sort().map(R=>`${R}:${Le(P[R])}`).join("|")}function Ut(){o.value=Le(t.query.bedKeyword);const P=Le(t.query.bedQuick).toUpperCase();_.value=P==="IDLE"||P==="OCCUPIED"?P:"ALL",g.value=Le(t.query.bedRiskEnabled)==="1";const R=Le(t.query.bedRiskLevel).toUpperCase();p.value=R==="HIGH"||R==="MEDIUM"||R==="LOW"?R:"ALL",S.value=Le(t.query.bedBuilding),y.value=Le(t.query.bedFloor)}function cn(){const P={};return Object.entries(ft(t.query)).forEach(([R,oe])=>{b.includes(R)||(P[R]=oe)}),o.value.trim()&&(P.bedKeyword=o.value.trim()),P.bedQuick=_.value,g.value&&(P.bedRiskEnabled="1"),g.value&&p.value!=="ALL"&&(P.bedRiskLevel=p.value),S.value&&(P.bedBuilding=S.value),y.value&&(P.bedFloor=y.value),P}function Sn(P){const R=ft(t.query),oe=Object.keys(R),Me=Object.keys(P);return oe.length!==Me.length?!1:Me.every(Ie=>R[Ie]===P[Ie])}async function Ci(){const P=cn();Sn(P)||(L.value=!0,W.value=tt(P),await e.replace({path:t.path,query:P}))}function Ar(P){const R=String(P||"").trim();if(!R)return-999;const oe=R.replace(/\s+/g,"").toUpperCase();if(/^(ROOF|RF|屋顶|天台)$/.test(oe))return 999;const Me=oe.match(/(?:地下|负|B)([0-9]+|[一二三四五六七八九十百两]+)/);if(Me)return-$n(Me[1]);const Ie=oe.match(/([0-9]+|[一二三四五六七八九十百两]+)(?:F|楼|层)?/);return Ie?$n(Ie[1]):-999}function $n(P){if(/^\d+$/.test(P))return Number(P);const R=nr(P);return R>0?R:0}function nr(P){const R=P.split(""),oe={零:0,一:1,二:2,两:2,三:3,四:4,五:5,六:6,七:7,八:8,九:9};if(!R.length)return 0;if(R.length===1)return oe[R[0]]??0;let Me=0,Ie=0;return R.forEach(Ze=>{if(oe[Ze]!==void 0){Ie=oe[Ze];return}if(Ze==="十"){Me+=(Ie||1)*10,Ie=0;return}Ze==="百"&&(Me+=(Ie||1)*100,Ie=0)}),Me+Ie}function Ot(P){return P.elderId?"在住":P.status===0?"锁定":P.occupancySource==="CLEANING"?"清洁中":P.status===3||P.occupancySource==="MAINTENANCE"?"维修":P.occupancySource==="RESERVATION"?"预定":"空闲"}function Kn(P){const R=String(P||"").trim();if(!R)return"-";const oe=R.toUpperCase();return{1:"单人间",2:"双人间",3:"三人间",SINGLE:"单人间",DOUBLE:"双人间",TRIPLE:"三人间",ONE:"单人间",TWO:"双人间",THREE:"三人间",STANDARD:"标准间",STANDARD_ROOM:"标准间",DELUXE:"豪华间",VIP:"VIP房",SUITE:"套间"}[oe]||R}function Kt(P){return!P.elderId&&Ot(P)==="空闲"}function Pi(P){return P.riskLevel==="HIGH"||P.status===0||Number(P.abnormalVital24hCount||0)>0}function hi(P){return P.riskLabel||""}function ir(P){const R=P.latestAssessmentLevel||"-",oe=P.latestAssessmentDate||"-";return R==="-"&&oe==="-"?"-":`${R} / ${oe}`}function Zn(P){l.value=P,f.value=null,c.value=!0}function Di(P){if(!P)return[];try{const R=JSON.parse(P);return(Array.isArray(R==null?void 0:R.slots)?R.slots:[R==null?void 0:R.remark1,R==null?void 0:R.remark2,R==null?void 0:R.remark3]).map((Me,Ie)=>Me?typeof Me=="string"?{text:Me,visible:!0,index:Ie}:{text:String(Me.text||Me.value||"").trim(),visible:Me.visible!==!1,index:Ie}:null).filter(Me=>Me&&Me.text)}catch{return[{text:P,visible:!0,index:0}]}}function wr(P){return Di(P).filter(R=>R.visible).map(R=>R.text).join("；")}function ds(P){return Di(P).map(R=>R.text).join("；")}async function ao(P){f.value=P,l.value=null,u.value=!0;const R=Array.from(new Set(P.beds.map(oe=>oe.elderId).filter(Boolean)));if(!R.length){h.value=[];return}d.value=!0;try{const oe=await Promise.allSettled(R.map(Me=>_d(Me)));h.value=oe.filter(Me=>Me.status==="fulfilled").map(Me=>Me.value)}finally{d.value=!1}}function oo(P){u.value=!1,e.push(`/elder/detail/${P}`)}function lo(P){e.push(`/finance/bills/in-resident?elderId=${P}`)}function js(){var R;const P=String(((R=l.value)==null?void 0:R.elderId)||"").trim();return P||(_s.warning("当前床位未关联长者"),"")}function Js(){const P=js();P&&(c.value=!1,e.push(`/elder/assessment/ability/archive?elderId=${encodeURIComponent(P)}`))}function E(){const P=js();P&&(c.value=!1,e.push(`/elder/contracts-invoices?elderId=${encodeURIComponent(P)}`))}function k(){var R;c.value=!1;const P=String(((R=l.value)==null?void 0:R.elderId)||"").trim();e.push({path:"/elder/status-change",query:P?{residentId:P}:void 0})}function K(){var P;if(!((P=l.value)!=null&&P.elderId)){_s.warning("当前是空床位，请先分配床位");return}c.value=!1,e.push(`/elder/detail/${l.value.elderId}`)}function Y(){if(!l.value){_s.warning("请先选择床位");return}c.value=!1,e.push(`/elder/admission-processing?bedId=${l.value.id}`)}function X(){if(!l.value){_s.warning("请先选择床位");return}c.value=!1,_s.success("已生成提醒并推送到提醒中心/任务中心"),e.push("/oa/work-execution/task?category=alert")}function Se(){var P;c.value=!1,e.push(`/care/workbench/qr?bedId=${((P=l.value)==null?void 0:P.id)||""}`)}function Ae(){e.push("/logistics/assets/room-state-map")}function xe(){e.push("/logistics/assets/bed-management")}async function Pe(P=!1){i.value=await pd({params:{includeRisk:P}}),P?M.value=!0:M.value=!1}async function De(){if(!(M.value||m.value)){m.value=!0;try{await Pe(!0)}finally{m.value=!1}}}async function We(){const P=await md(),R={},oe={};P.forEach(Me=>{const Ie=String(Me.id);R[Ie]=Me.roomType||"标准间",oe[Ie]=Number(Me.capacity||1)}),s.value=R,a.value=oe}return gd({topics:["elder","bed","lifecycle","finance","care","dining"],refresh:async()=>{await Promise.all([Pe(!1),We()]),g.value&&await De()}}),wh(async()=>{Ut(),W.value=tt(t.query),G=window.setInterval(()=>{A.value=Qs()},1e3),await Promise.all([Pe(!1),We()]),g.value&&await De(),await Ci().catch(()=>{})}),uc(()=>{G&&window.clearInterval(G)}),Hi(g,P=>{P&&De().catch(()=>{})}),Hi(H,()=>{S.value&&!H.value.some(P=>String(P.building||"")===S.value)&&(S.value=""),y.value&&!H.value.some(P=>String(P.floorNo||"")===y.value)&&(y.value="")}),Hi([o,_,g,p,S,y],()=>{Ci().catch(()=>{})}),Hi(()=>t.query,P=>{const R=tt(P);if(L.value){L.value=!1,W.value=R;return}R!==W.value&&(W.value=R,Ut())},{deep:!0}),(P,R)=>{const oe=dn("a-empty"),Me=dn("a-avatar"),Ie=dn("a-input-search"),Ze=dn("a-segmented"),bt=dn("a-switch"),ve=dn("a-button"),Lt=dn("v-chart"),ze=dn("a-descriptions-item"),Gt=dn("a-descriptions"),Zt=dn("a-space"),Nn=dn("a-modal"),nn=dn("a-table-column"),dt=dn("a-table");return et(),Bi(dd,{class:"bed-immersive-page",title:"3D床态全景",subTitle:"智慧养老数字孪生床态指挥舱"},{default:Xe(()=>{var Ct;return[O("div",DM,[ne.value.length&&me.value.length?(et(),Bi(PM,{key:0,buildings:ne.value,floors:me.value,"room-lookup":Fe.value,onClickRoom:ao,onClickBed:Zn},null,8,["buildings","floors","room-lookup"])):(et(),Bi(oe,{key:1,class:"stage-empty",description:"暂无床位数据"})),R[36]||(R[36]=O("div",{class:"scene-vignette"},null,-1)),R[37]||(R[37]=O("div",{class:"scene-glow scene-glow-left"},null,-1)),R[38]||(R[38]=O("div",{class:"scene-glow scene-glow-right"},null,-1)),O("header",LM,[O("div",IM,[R[8]||(R[8]=O("div",{class:"brand-mark"},[O("span")],-1)),O("div",UM,[R[6]||(R[6]=O("div",{class:"brand-kicker"},"Smart Senior Care Digital Twin",-1)),R[7]||(R[7]=O("strong",null,"智慧养老数字孪生床态指挥舱",-1)),O("small",null,ge(z.value.active?"入住联动模式已开启":"长者床态、风险提醒与护理节奏实时联动中"),1)])]),O("div",NM,[(et(!0),mt(yn,null,Fn(I.value,te=>(et(),mt("button",{key:te.label,class:di(["metric-pill",te.tone])},[O("span",null,ge(te.label),1),O("strong",null,[ke(Sd,{value:te.numericValue,suffix:te.suffix||""},null,8,["value","suffix"])]),O("small",null,ge(te.meta),1)],2))),128))]),O("div",FM,[O("div",OM,[R[10]||(R[10]=O("span",{class:"status-dot"},null,-1)),O("div",null,[R[9]||(R[9]=O("strong",null,"系统平稳运行",-1)),O("small",null,ge(vt.value),1)])]),O("div",BM,[O("span",null,ge(Tt.value),1),O("strong",null,ge(Qe.value),1)]),O("div",kM,[ke(Me,{class:"operator-avatar"},{default:Xe(()=>[st(ge(Ye.value),1)]),_:1}),O("div",null,[O("strong",null,ge(ot.value),1),O("small",null,ge(pt.value),1)])])])]),O("aside",zM,[O("section",VM,[O("div",GM,[R[11]||(R[11]=O("div",null,[O("span",{class:"panel-kicker"},"运行总览"),O("strong",null,"床态守护概况")],-1)),O("small",null,ge(Te.value)+" 间房 / "+ge(H.value.length)+" 张床位",1)]),O("div",HM,[(et(!0),mt(yn,null,Fn(yt.value,te=>(et(),mt("div",{key:te.label,class:"overview-row"},[O("span",null,ge(te.label),1),O("strong",null,ge(te.value),1),O("small",null,ge(te.meta),1)]))),128))]),O("div",WM,[(et(!0),mt(yn,null,Fn(Z.value,te=>(et(),mt("div",{key:te.label,class:"status-bar"},[O("div",XM,[O("span",null,ge(te.label),1),O("strong",null,ge(te.value),1)]),O("div",qM,[O("span",{class:di(["distribution-fill",te.tone]),style:Rh({width:`${te.percent}%`})},null,6)])]))),128))])]),O("section",YM,[R[20]||(R[20]=O("div",{class:"hud-panel__head"},[O("div",null,[O("span",{class:"panel-kicker"},"场景控制"),O("strong",null,"楼栋与床态筛选")])],-1)),O("label",$M,[R[12]||(R[12]=O("span",{class:"field-label"},"搜索目标",-1)),ke(Ie,{value:o.value,"onUpdate:value":R[0]||(R[0]=te=>o.value=te),placeholder:"搜索房间号 / 长者名","allow-clear":""},null,8,["value"])]),O("div",KM,[R[13]||(R[13]=O("span",{class:"field-label"},"床位状态筛选",-1)),ke(Ze,{value:_.value,"onUpdate:value":R[1]||(R[1]=te=>_.value=te),options:w,block:""},null,8,["value"])]),O("div",ZM,[O("div",jM,[R[14]||(R[14]=O("span",{class:"field-label"},"风险筛选",-1)),ke(bt,{checked:g.value,"onUpdate:checked":R[2]||(R[2]=te=>g.value=te)},null,8,["checked"])]),g.value?(et(),Bi(Ze,{key:0,value:p.value,"onUpdate:value":R[3]||(R[3]=te=>p.value=te),options:C,block:""},null,8,["value"])):ki("",!0)]),O("div",JM,[R[15]||(R[15]=O("span",{class:"field-label"},"楼栋切换",-1)),O("div",QM,[(et(!0),mt(yn,null,Fn(rt.value,te=>(et(),mt("button",{key:te,class:di(["scope-chip",{active:te===S.value}]),onClick:lt=>_e(te)},ge(te),11,eS))),128))])]),Q.value.length?(et(),mt("div",tS,[R[16]||(R[16]=O("span",{class:"field-label"},"楼层切换",-1)),O("div",nS,[(et(!0),mt(yn,null,Fn(Q.value,te=>(et(),mt("button",{key:te,class:di(["scope-chip",{active:te===y.value}]),onClick:lt=>ae(te)},ge(te),11,iS))),128))])])):ki("",!0),O("div",rS,[ke(ve,{onClick:Ae},{default:Xe(()=>[...R[17]||(R[17]=[st("房态视图",-1)])]),_:1}),ke(ve,{onClick:xe},{default:Xe(()=>[...R[18]||(R[18]=[st("床位管理",-1)])]),_:1}),ke(ve,{type:"primary",onClick:we},{default:Xe(()=>[...R[19]||(R[19]=[st("重置场景",-1)])]),_:1})])])]),O("div",sS,[O("section",aS,[O("div",oS,[O("div",null,[R[21]||(R[21]=O("span",{class:"panel-kicker"},"当前聚焦",-1)),O("strong",null,ge(se.value.title),1)]),O("small",null,ge(se.value.meta),1)]),O("div",lS,[O("div",cS,[R[22]||(R[22]=O("span",null,"当前范围",-1)),O("strong",null,ge($.value),1)]),O("div",uS,[R[23]||(R[23]=O("span",null,"护理阶段",-1)),O("strong",null,ge(j.value),1)])]),O("div",hS,[(et(),mt(yn,null,Fn(x,(te,lt)=>O("div",{key:te,class:di(["overlay-step",{active:lt<=B.value,current:lt===B.value}])},[O("span",null,ge(lt+1),1),O("small",null,ge(te),1)],2)),64))]),O("div",fS,[(et(!0),mt(yn,null,Fn(Re.value,te=>(et(),mt("button",{key:te.key,class:di(["stage-action-pill",te.tone]),onClick:lt=>ue(te.key)},[O("strong",null,ge(te.label),1),O("span",null,ge(te.description),1)],10,dS))),128))])])]),O("aside",pS,[l.value?(et(),mt("section",mS,[O("div",_S,[O("div",null,[R[24]||(R[24]=O("span",{class:"panel-kicker"},"床位详情",-1)),O("strong",null,ge(l.value.elderName||"空床待命"),1)]),O("small",null,ge(l.value.roomNo||"-")+" / "+ge(l.value.bedNo||"-"),1)]),O("div",gS,[O("span",vS,ge(Ot(l.value)),1),l.value.riskLabel?(et(),mt("span",xS,ge(l.value.riskLabel),1)):ki("",!0),O("span",MS,ge(l.value.careLevel||"未配置护理等级"),1)]),O("div",SS,[O("div",null,[R[25]||(R[25]=O("span",null,"楼栋楼层",-1)),O("strong",null,ge(l.value.building||"-")+" / "+ge(l.value.floorNo||"-"),1)]),O("div",null,[R[26]||(R[26]=O("span",null,"24h异常",-1)),O("strong",null,ge(l.value.abnormalVital24hCount||0)+" 次",1)]),O("div",null,[R[27]||(R[27]=O("span",null,"风险来源",-1)),O("strong",null,ge(l.value.riskSource||"守护稳定"),1)]),O("div",null,[R[28]||(R[28]=O("span",null,"最近评估",-1)),O("strong",null,ge(ir(l.value)),1)])]),ce.value.length?(et(),mt("div",yS,[(et(!0),mt(yn,null,Fn(ce.value,te=>(et(),mt("div",{key:te.code,class:"blocker-card"},[O("div",null,[O("strong",null,ge(te.code),1),O("p",null,ge(te.text),1)]),te.actionKey&&te.actionLabel?(et(),Bi(ve,{key:0,size:"small",type:"link",onClick:lt=>fe(te)},{default:Xe(()=>[st(ge(te.actionLabel),1)]),_:2},1032,["onClick"])):ki("",!0)]))),128))])):ki("",!0),O("div",bS,[(et(!0),mt(yn,null,Fn(Ue.value,te=>(et(),mt("button",{key:te.key,class:di(["action-card",te.tone]),onClick:lt=>ue(te.key)},[O("strong",null,ge(te.label),1),O("span",null,ge(te.description),1)],10,ES))),128))])])):(et(),mt(yn,{key:1},[O("section",TS,[O("div",AS,[R[29]||(R[29]=O("div",null,[O("span",{class:"panel-kicker"},"实时风险提醒"),O("strong",null,"值守事件流")],-1)),O("small",null,ge($e.value)+" 条提醒",1)]),O("div",wS,[(et(!0),mt(yn,null,Fn(v.value.slice(0,4),te=>(et(),mt("div",{key:te.key,class:di(["event-card",te.tone])},[O("div",RS,[O("span",CS,ge(te.type),1),O("span",PS,ge(te.time),1)]),O("strong",null,ge(te.title),1),O("p",null,ge(te.description),1)],2))),128))])]),O("section",DS,[R[30]||(R[30]=O("div",{class:"hud-panel__head"},[O("div",null,[O("span",{class:"panel-kicker"},"AI护理建议"),O("strong",null,"值守提示")])],-1)),O("div",LS,[(et(!0),mt(yn,null,Fn(D.value,te=>(et(),mt("div",{key:te.key,class:"timeline-item"},[O("span",{class:di(["timeline-dot",te.tone])},null,2),O("div",null,[O("strong",null,ge(te.title),1),O("p",null,ge(te.description),1)])]))),128))])]),O("section",IS,[R[31]||(R[31]=O("div",{class:"hud-panel__head"},[O("div",null,[O("span",{class:"panel-kicker"},"今日重点关注"),O("strong",null,"长者守护名单")])],-1)),O("div",US,[(et(!0),mt(yn,null,Fn(T.value,te=>(et(),mt("button",{key:te.id,class:"focus-row",onClick:lt=>Zn(te)},[O("div",null,[O("strong",null,ge(te.elderName||"空床待命"),1),O("small",null,ge(te.roomNo||"-")+" / "+ge(te.bedNo||"-"),1)]),O("span",FS,ge(te.riskLabel||Ot(te)),1)],8,NS))),128))])])],64))]),O("footer",OS,[O("section",BS,[O("div",kS,[R[32]||(R[32]=O("div",{class:"trend-head"},[O("span",null,"24小时床态趋势"),O("strong",null,"在住与空床节奏")],-1)),ke(Lt,{class:"chart-view",option:Ce.value,autoresize:""},null,8,["option"])]),O("div",zS,[R[33]||(R[33]=O("div",{class:"trend-head"},[O("span",null,"夜间离床统计"),O("strong",null,"夜巡观察参考")],-1)),ke(Lt,{class:"chart-view",option:be.value,autoresize:""},null,8,["option"])]),O("div",VS,[R[34]||(R[34]=O("div",{class:"trend-head"},[O("span",null,"护理响应效率"),O("strong",null,"风险任务闭环")],-1)),ke(Lt,{class:"chart-view",option:Ee.value,autoresize:""},null,8,["option"])]),O("div",GS,[R[35]||(R[35]=O("div",{class:"trend-head"},[O("span",null,"风险热力趋势"),O("strong",null,"守护在线状态")],-1)),ke(Lt,{class:"chart-view",option:Be.value,autoresize:""},null,8,["option"])])])])]),ke(Nn,{open:c.value,"onUpdate:open":R[4]||(R[4]=te=>c.value=te),title:"床位详情",width:"560px",footer:null,"destroy-on-close":""},{default:Xe(()=>[l.value?(et(),Bi(Gt,{key:0,column:1,size:"small",bordered:""},{default:Xe(()=>[ke(ze,{label:"床位"},{default:Xe(()=>[st(ge(l.value.bedNo||"-"),1)]),_:1}),ke(ze,{label:"楼栋/楼层/房间"},{default:Xe(()=>[st(ge(l.value.building||"-")+" / "+ge(l.value.floorNo||"-")+" / "+ge(l.value.roomNo||"-"),1)]),_:1}),ke(ze,{label:"在住长者"},{default:Xe(()=>[st(ge(l.value.elderName||"-"),1)]),_:1}),ke(ze,{label:"护理等级"},{default:Xe(()=>[st(ge(l.value.careLevel||"-"),1)]),_:1}),ke(ze,{label:"风险级别"},{default:Xe(()=>[st(ge(hi(l.value)||"无"),1)]),_:1}),ke(ze,{label:"风险来源"},{default:Xe(()=>[st(ge(l.value.riskSource||"-"),1)]),_:1}),ke(ze,{label:"最近评估"},{default:Xe(()=>[st(ge(ir(l.value)),1)]),_:1}),ke(ze,{label:"24h异常体征"},{default:Xe(()=>[st(ge(l.value.abnormalVital24hCount||0)+" 次",1)]),_:1}),ke(ze,{label:"今日任务"},{default:Xe(()=>[...R[39]||(R[39]=[st("巡视2次、翻身3次、测量生命体征1次",-1)])]),_:1}),ke(ze,{label:"余额/欠费"},{default:Xe(()=>[...R[40]||(R[40]=[st("余额 1500，欠费 0",-1)])]),_:1})]),_:1})):ki("",!0),ke(Zt,{direction:"vertical",style:{"margin-top":"12px",width:"100%"}},{default:Xe(()=>[ke(ve,{block:"",type:"primary",onClick:K},{default:Xe(()=>[...R[41]||(R[41]=[st("打开长者档案",-1)])]),_:1}),ke(ve,{block:"",onClick:Y},{default:Xe(()=>[...R[42]||(R[42]=[st("一键分配床位",-1)])]),_:1}),ke(ve,{block:"",onClick:Js},{default:Xe(()=>[...R[43]||(R[43]=[st("查看评估档案",-1)])]),_:1}),ke(ve,{block:"",onClick:E},{default:Xe(()=>[...R[44]||(R[44]=[st("合同与票据",-1)])]),_:1}),ke(ve,{block:"",onClick:k},{default:Xe(()=>[...R[45]||(R[45]=[st("状态变更中心",-1)])]),_:1}),ke(ve,{block:"",danger:"",onClick:X},{default:Xe(()=>[...R[46]||(R[46]=[st("生成提醒并进入任务中心",-1)])]),_:1}),ke(ve,{block:"",onClick:Se},{default:Xe(()=>[...R[47]||(R[47]=[st("扫码执行（定位今日任务）",-1)])]),_:1})]),_:1})]),_:1},8,["open"]),ke(Nn,{open:u.value,"onUpdate:open":R[5]||(R[5]=te=>u.value=te),title:`房间详情 · ${((Ct=f.value)==null?void 0:Ct.roomNo)||"-"}`,width:"760px",footer:null,"destroy-on-close":""},{default:Xe(()=>[ke(Gt,{bordered:"",size:"small",column:2,style:{"margin-bottom":"12px"}},{default:Xe(()=>[ke(ze,{label:"房型"},{default:Xe(()=>{var te;return[st(ge(Kn((te=f.value)==null?void 0:te.roomType)),1)]}),_:1}),ke(ze,{label:"容量"},{default:Xe(()=>{var te;return[st(ge(((te=f.value)==null?void 0:te.capacity)||0)+" 床",1)]}),_:1}),ke(ze,{label:"在住人数"},{default:Xe(()=>{var te;return[st(ge(((te=f.value)==null?void 0:te.elderCount)||0)+" 人",1)]}),_:1}),ke(ze,{label:"空床"},{default:Xe(()=>{var te;return[st(ge(((te=f.value)==null?void 0:te.emptyBeds)||0)+" 床",1)]}),_:1}),ke(ze,{label:"公开备注",span:2},{default:Xe(()=>{var te;return[st(ge(wr((te=f.value)==null?void 0:te.remark)||"-"),1)]}),_:1}),ke(ze,{label:"全部备注",span:2},{default:Xe(()=>{var te;return[st(ge(ds((te=f.value)==null?void 0:te.remark)||"-"),1)]}),_:1})]),_:1}),ke(dt,{loading:d.value,"data-source":h.value,pagination:!1,"row-key":te=>te.id,size:"small"},{default:Xe(()=>[ke(nn,{title:"头像",key:"avatar",width:"70"},{default:Xe(({record:te})=>[ke(Me,{style:{"background-color":"#1677ff"}},{default:Xe(()=>[st(ge(String((te==null?void 0:te.fullName)||"?").slice(-1)),1)]),_:2},1024)]),_:1}),ke(nn,{title:"姓名","data-index":"fullName",key:"fullName",width:"110"}),ke(nn,{title:"生日","data-index":"birthDate",key:"birthDate",width:"120"}),ke(nn,{title:"家庭住址","data-index":"homeAddress",key:"homeAddress"}),ke(nn,{title:"备注","data-index":"remark",key:"remark"}),ke(nn,{title:"操作",key:"action",width:"180"},{default:Xe(({record:te})=>[ke(Zt,null,{default:Xe(()=>[ke(ve,{type:"link",size:"small",onClick:lt=>oo(te.id)},{default:Xe(()=>[...R[48]||(R[48]=[st("详情",-1)])]),_:1},8,["onClick"]),ke(ve,{type:"link",size:"small",onClick:lt=>lo(te.id)},{default:Xe(()=>[...R[49]||(R[49]=[st("账单",-1)])]),_:1},8,["onClick"])]),_:2},1024)]),_:1})]),_:1},8,["loading","data-source","row-key"]),!d.value&&!h.value.length?(et(),Bi(oe,{key:0,description:"当前房间暂无入住长者"})):ki("",!0)]),_:1},8,["open","title"])]}),_:1})}}}),ey=hc(HS,[["__scopeId","data-v-bbb4f47a"]]);export{ey as default};
