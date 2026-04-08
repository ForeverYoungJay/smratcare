import{d as bh,O as vs,x as _r,o as Eh,y as Th,a as ft,c as qt,b as q,h as fn,w as Ve,f as et,i as Tn,e as Be,t as Ie,P as od,Q as ld,R as cd,B as Ah,k as pt,j as dt,p as en,m as ta,g as Dr,F as Lr,v as ud,l as xs,q as hd,n as Ms}from"./index-ook8yukD.js";import{P as fd}from"./PageContainer-BREfdBMY.js";import{B as dd}from"./BedInfoCard-BzvNj22f.js";import{_ as wh}from"./_plugin-vue_export-helper-DlAUqK2U.js";import{g as pd,a as md}from"./bed-DaxzaRFz.js";import{g as _d}from"./elder-CbU7YfI7.js";import{u as gd}from"./useLiveSyncRefresh-BTSVWFkn.js";import"./request-Du0AqNUg.js";/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */const lc="183",Qr={ROTATE:0,DOLLY:1,PAN:2},Zr={ROTATE:0,PAN:1,DOLLY_PAN:2,DOLLY_ROTATE:3},vd=0,Xc=1,xd=2,Ia=1,Md=2,Ps=3,Zi=0,pn=1,$n=2,Ti=0,es=1,qc=2,Yc=3,$c=4,Sd=5,pr=100,yd=101,bd=102,Ed=103,Td=104,Ad=200,wd=201,Rd=202,Cd=203,el=204,tl=205,Pd=206,Dd=207,Ld=208,Id=209,Ud=210,Nd=211,Fd=212,Od=213,Bd=214,nl=0,il=1,rl=2,ss=3,sl=4,al=5,ol=6,ll=7,Rh=0,kd=1,zd=2,ci=0,Ch=1,Ph=2,Dh=3,cc=4,Lh=5,Ih=6,Uh=7,Nh=300,Er=301,as=302,lo=303,co=304,no=306,cl=1e3,Ei=1001,ul=1002,$t=1003,Vd=1004,na=1005,nn=1006,uo=1007,gr=1008,Cn=1009,Fh=1010,Oh=1011,ks=1012,uc=1013,hi=1014,ai=1015,wi=1016,hc=1017,fc=1018,zs=1020,Bh=35902,kh=35899,zh=1021,Vh=1022,Kn=1023,Ri=1026,vr=1027,Gh=1028,dc=1029,os=1030,pc=1031,mc=1033,Ua=33776,Na=33777,Fa=33778,Oa=33779,hl=35840,fl=35841,dl=35842,pl=35843,ml=36196,_l=37492,gl=37496,vl=37488,xl=37489,Ml=37490,Sl=37491,yl=37808,bl=37809,El=37810,Tl=37811,Al=37812,wl=37813,Rl=37814,Cl=37815,Pl=37816,Dl=37817,Ll=37818,Il=37819,Ul=37820,Nl=37821,Fl=36492,Ol=36494,Bl=36495,kl=36283,zl=36284,Vl=36285,Gl=36286,Gd=3200,Hh=0,Hd=1,Hi="",An="srgb",ls="srgb-linear",Ha="linear",mt="srgb",Ir=7680,Kc=519,Wd=512,Xd=513,qd=514,_c=515,Yd=516,$d=517,gc=518,Kd=519,Zc=35044,jc="300 es",oi=2e3,Vs=2001;function Zd(r){for(let e=r.length-1;e>=0;--e)if(r[e]>=65535)return!0;return!1}function Wa(r){return document.createElementNS("http://www.w3.org/1999/xhtml",r)}function jd(){const r=Wa("canvas");return r.style.display="block",r}const Jc={};function Qc(...r){const e="THREE."+r.shift();console.log(e,...r)}function Wh(r){const e=r[0];if(typeof e=="string"&&e.startsWith("TSL:")){const t=r[1];t&&t.isStackTrace?r[0]+=" "+t.getLocation():r[1]='Stack trace not available. Enable "THREE.Node.captureStackTrace" to capture stack traces.'}return r}function Xe(...r){r=Wh(r);const e="THREE."+r.shift();{const t=r[0];t&&t.isStackTrace?console.warn(t.getError(e)):console.warn(e,...r)}}function lt(...r){r=Wh(r);const e="THREE."+r.shift();{const t=r[0];t&&t.isStackTrace?console.error(t.getError(e)):console.error(e,...r)}}function Xa(...r){const e=r.join(" ");e in Jc||(Jc[e]=!0,Xe(...r))}function Jd(r,e,t){return new Promise(function(n,i){function s(){switch(r.clientWaitSync(e,r.SYNC_FLUSH_COMMANDS_BIT,0)){case r.WAIT_FAILED:i();break;case r.TIMEOUT_EXPIRED:setTimeout(s,t);break;default:n()}}setTimeout(s,t)})}const Qd={[nl]:il,[rl]:ol,[sl]:ll,[ss]:al,[il]:nl,[ol]:rl,[ll]:sl,[al]:ss};class Tr{addEventListener(e,t){this._listeners===void 0&&(this._listeners={});const n=this._listeners;n[e]===void 0&&(n[e]=[]),n[e].indexOf(t)===-1&&n[e].push(t)}hasEventListener(e,t){const n=this._listeners;return n===void 0?!1:n[e]!==void 0&&n[e].indexOf(t)!==-1}removeEventListener(e,t){const n=this._listeners;if(n===void 0)return;const i=n[e];if(i!==void 0){const s=i.indexOf(t);s!==-1&&i.splice(s,1)}}dispatchEvent(e){const t=this._listeners;if(t===void 0)return;const n=t[e.type];if(n!==void 0){e.target=this;const i=n.slice(0);for(let s=0,a=i.length;s<a;s++)i[s].call(this,e);e.target=null}}}const Jt=["00","01","02","03","04","05","06","07","08","09","0a","0b","0c","0d","0e","0f","10","11","12","13","14","15","16","17","18","19","1a","1b","1c","1d","1e","1f","20","21","22","23","24","25","26","27","28","29","2a","2b","2c","2d","2e","2f","30","31","32","33","34","35","36","37","38","39","3a","3b","3c","3d","3e","3f","40","41","42","43","44","45","46","47","48","49","4a","4b","4c","4d","4e","4f","50","51","52","53","54","55","56","57","58","59","5a","5b","5c","5d","5e","5f","60","61","62","63","64","65","66","67","68","69","6a","6b","6c","6d","6e","6f","70","71","72","73","74","75","76","77","78","79","7a","7b","7c","7d","7e","7f","80","81","82","83","84","85","86","87","88","89","8a","8b","8c","8d","8e","8f","90","91","92","93","94","95","96","97","98","99","9a","9b","9c","9d","9e","9f","a0","a1","a2","a3","a4","a5","a6","a7","a8","a9","aa","ab","ac","ad","ae","af","b0","b1","b2","b3","b4","b5","b6","b7","b8","b9","ba","bb","bc","bd","be","bf","c0","c1","c2","c3","c4","c5","c6","c7","c8","c9","ca","cb","cc","cd","ce","cf","d0","d1","d2","d3","d4","d5","d6","d7","d8","d9","da","db","dc","dd","de","df","e0","e1","e2","e3","e4","e5","e6","e7","e8","e9","ea","eb","ec","ed","ee","ef","f0","f1","f2","f3","f4","f5","f6","f7","f8","f9","fa","fb","fc","fd","fe","ff"],Us=Math.PI/180,Hl=180/Math.PI;function Zs(){const r=Math.random()*4294967295|0,e=Math.random()*4294967295|0,t=Math.random()*4294967295|0,n=Math.random()*4294967295|0;return(Jt[r&255]+Jt[r>>8&255]+Jt[r>>16&255]+Jt[r>>24&255]+"-"+Jt[e&255]+Jt[e>>8&255]+"-"+Jt[e>>16&15|64]+Jt[e>>24&255]+"-"+Jt[t&63|128]+Jt[t>>8&255]+"-"+Jt[t>>16&255]+Jt[t>>24&255]+Jt[n&255]+Jt[n>>8&255]+Jt[n>>16&255]+Jt[n>>24&255]).toLowerCase()}function it(r,e,t){return Math.max(e,Math.min(t,r))}function ep(r,e){return(r%e+e)%e}function ho(r,e,t){return(1-t)*r+t*e}function Ss(r,e){switch(e.constructor){case Float32Array:return r;case Uint32Array:return r/4294967295;case Uint16Array:return r/65535;case Uint8Array:return r/255;case Int32Array:return Math.max(r/2147483647,-1);case Int16Array:return Math.max(r/32767,-1);case Int8Array:return Math.max(r/127,-1);default:throw new Error("Invalid component type.")}}function un(r,e){switch(e.constructor){case Float32Array:return r;case Uint32Array:return Math.round(r*4294967295);case Uint16Array:return Math.round(r*65535);case Uint8Array:return Math.round(r*255);case Int32Array:return Math.round(r*2147483647);case Int16Array:return Math.round(r*32767);case Int8Array:return Math.round(r*127);default:throw new Error("Invalid component type.")}}const tp={DEG2RAD:Us};class $e{constructor(e=0,t=0){$e.prototype.isVector2=!0,this.x=e,this.y=t}get width(){return this.x}set width(e){this.x=e}get height(){return this.y}set height(e){this.y=e}set(e,t){return this.x=e,this.y=t,this}setScalar(e){return this.x=e,this.y=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y)}copy(e){return this.x=e.x,this.y=e.y,this}add(e){return this.x+=e.x,this.y+=e.y,this}addScalar(e){return this.x+=e,this.y+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this}subScalar(e){return this.x-=e,this.y-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this}multiply(e){return this.x*=e.x,this.y*=e.y,this}multiplyScalar(e){return this.x*=e,this.y*=e,this}divide(e){return this.x/=e.x,this.y/=e.y,this}divideScalar(e){return this.multiplyScalar(1/e)}applyMatrix3(e){const t=this.x,n=this.y,i=e.elements;return this.x=i[0]*t+i[3]*n+i[6],this.y=i[1]*t+i[4]*n+i[7],this}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this}clamp(e,t){return this.x=it(this.x,e.x,t.x),this.y=it(this.y,e.y,t.y),this}clampScalar(e,t){return this.x=it(this.x,e,t),this.y=it(this.y,e,t),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(it(n,e,t))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this}negate(){return this.x=-this.x,this.y=-this.y,this}dot(e){return this.x*e.x+this.y*e.y}cross(e){return this.x*e.y-this.y*e.x}lengthSq(){return this.x*this.x+this.y*this.y}length(){return Math.sqrt(this.x*this.x+this.y*this.y)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)}normalize(){return this.divideScalar(this.length()||1)}angle(){return Math.atan2(-this.y,-this.x)+Math.PI}angleTo(e){const t=Math.sqrt(this.lengthSq()*e.lengthSq());if(t===0)return Math.PI/2;const n=this.dot(e)/t;return Math.acos(it(n,-1,1))}distanceTo(e){return Math.sqrt(this.distanceToSquared(e))}distanceToSquared(e){const t=this.x-e.x,n=this.y-e.y;return t*t+n*n}manhattanDistanceTo(e){return Math.abs(this.x-e.x)+Math.abs(this.y-e.y)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this}equals(e){return e.x===this.x&&e.y===this.y}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this}rotateAround(e,t){const n=Math.cos(t),i=Math.sin(t),s=this.x-e.x,a=this.y-e.y;return this.x=s*n-a*i+e.x,this.y=s*i+a*n+e.y,this}random(){return this.x=Math.random(),this.y=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y}}class ji{constructor(e=0,t=0,n=0,i=1){this.isQuaternion=!0,this._x=e,this._y=t,this._z=n,this._w=i}static slerpFlat(e,t,n,i,s,a,o){let c=n[i+0],l=n[i+1],u=n[i+2],f=n[i+3],h=s[a+0],m=s[a+1],_=s[a+2],g=s[a+3];if(f!==g||c!==h||l!==m||u!==_){let d=c*h+l*m+u*_+f*g;d<0&&(h=-h,m=-m,_=-_,g=-g,d=-d);let p=1-o;if(d<.9995){const M=Math.acos(d),y=Math.sin(M);p=Math.sin(p*M)/y,o=Math.sin(o*M)/y,c=c*p+h*o,l=l*p+m*o,u=u*p+_*o,f=f*p+g*o}else{c=c*p+h*o,l=l*p+m*o,u=u*p+_*o,f=f*p+g*o;const M=1/Math.sqrt(c*c+l*l+u*u+f*f);c*=M,l*=M,u*=M,f*=M}}e[t]=c,e[t+1]=l,e[t+2]=u,e[t+3]=f}static multiplyQuaternionsFlat(e,t,n,i,s,a){const o=n[i],c=n[i+1],l=n[i+2],u=n[i+3],f=s[a],h=s[a+1],m=s[a+2],_=s[a+3];return e[t]=o*_+u*f+c*m-l*h,e[t+1]=c*_+u*h+l*f-o*m,e[t+2]=l*_+u*m+o*h-c*f,e[t+3]=u*_-o*f-c*h-l*m,e}get x(){return this._x}set x(e){this._x=e,this._onChangeCallback()}get y(){return this._y}set y(e){this._y=e,this._onChangeCallback()}get z(){return this._z}set z(e){this._z=e,this._onChangeCallback()}get w(){return this._w}set w(e){this._w=e,this._onChangeCallback()}set(e,t,n,i){return this._x=e,this._y=t,this._z=n,this._w=i,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._w)}copy(e){return this._x=e.x,this._y=e.y,this._z=e.z,this._w=e.w,this._onChangeCallback(),this}setFromEuler(e,t=!0){const n=e._x,i=e._y,s=e._z,a=e._order,o=Math.cos,c=Math.sin,l=o(n/2),u=o(i/2),f=o(s/2),h=c(n/2),m=c(i/2),_=c(s/2);switch(a){case"XYZ":this._x=h*u*f+l*m*_,this._y=l*m*f-h*u*_,this._z=l*u*_+h*m*f,this._w=l*u*f-h*m*_;break;case"YXZ":this._x=h*u*f+l*m*_,this._y=l*m*f-h*u*_,this._z=l*u*_-h*m*f,this._w=l*u*f+h*m*_;break;case"ZXY":this._x=h*u*f-l*m*_,this._y=l*m*f+h*u*_,this._z=l*u*_+h*m*f,this._w=l*u*f-h*m*_;break;case"ZYX":this._x=h*u*f-l*m*_,this._y=l*m*f+h*u*_,this._z=l*u*_-h*m*f,this._w=l*u*f+h*m*_;break;case"YZX":this._x=h*u*f+l*m*_,this._y=l*m*f+h*u*_,this._z=l*u*_-h*m*f,this._w=l*u*f-h*m*_;break;case"XZY":this._x=h*u*f-l*m*_,this._y=l*m*f-h*u*_,this._z=l*u*_+h*m*f,this._w=l*u*f+h*m*_;break;default:Xe("Quaternion: .setFromEuler() encountered an unknown order: "+a)}return t===!0&&this._onChangeCallback(),this}setFromAxisAngle(e,t){const n=t/2,i=Math.sin(n);return this._x=e.x*i,this._y=e.y*i,this._z=e.z*i,this._w=Math.cos(n),this._onChangeCallback(),this}setFromRotationMatrix(e){const t=e.elements,n=t[0],i=t[4],s=t[8],a=t[1],o=t[5],c=t[9],l=t[2],u=t[6],f=t[10],h=n+o+f;if(h>0){const m=.5/Math.sqrt(h+1);this._w=.25/m,this._x=(u-c)*m,this._y=(s-l)*m,this._z=(a-i)*m}else if(n>o&&n>f){const m=2*Math.sqrt(1+n-o-f);this._w=(u-c)/m,this._x=.25*m,this._y=(i+a)/m,this._z=(s+l)/m}else if(o>f){const m=2*Math.sqrt(1+o-n-f);this._w=(s-l)/m,this._x=(i+a)/m,this._y=.25*m,this._z=(c+u)/m}else{const m=2*Math.sqrt(1+f-n-o);this._w=(a-i)/m,this._x=(s+l)/m,this._y=(c+u)/m,this._z=.25*m}return this._onChangeCallback(),this}setFromUnitVectors(e,t){let n=e.dot(t)+1;return n<1e-8?(n=0,Math.abs(e.x)>Math.abs(e.z)?(this._x=-e.y,this._y=e.x,this._z=0,this._w=n):(this._x=0,this._y=-e.z,this._z=e.y,this._w=n)):(this._x=e.y*t.z-e.z*t.y,this._y=e.z*t.x-e.x*t.z,this._z=e.x*t.y-e.y*t.x,this._w=n),this.normalize()}angleTo(e){return 2*Math.acos(Math.abs(it(this.dot(e),-1,1)))}rotateTowards(e,t){const n=this.angleTo(e);if(n===0)return this;const i=Math.min(1,t/n);return this.slerp(e,i),this}identity(){return this.set(0,0,0,1)}invert(){return this.conjugate()}conjugate(){return this._x*=-1,this._y*=-1,this._z*=-1,this._onChangeCallback(),this}dot(e){return this._x*e._x+this._y*e._y+this._z*e._z+this._w*e._w}lengthSq(){return this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w}length(){return Math.sqrt(this._x*this._x+this._y*this._y+this._z*this._z+this._w*this._w)}normalize(){let e=this.length();return e===0?(this._x=0,this._y=0,this._z=0,this._w=1):(e=1/e,this._x=this._x*e,this._y=this._y*e,this._z=this._z*e,this._w=this._w*e),this._onChangeCallback(),this}multiply(e){return this.multiplyQuaternions(this,e)}premultiply(e){return this.multiplyQuaternions(e,this)}multiplyQuaternions(e,t){const n=e._x,i=e._y,s=e._z,a=e._w,o=t._x,c=t._y,l=t._z,u=t._w;return this._x=n*u+a*o+i*l-s*c,this._y=i*u+a*c+s*o-n*l,this._z=s*u+a*l+n*c-i*o,this._w=a*u-n*o-i*c-s*l,this._onChangeCallback(),this}slerp(e,t){let n=e._x,i=e._y,s=e._z,a=e._w,o=this.dot(e);o<0&&(n=-n,i=-i,s=-s,a=-a,o=-o);let c=1-t;if(o<.9995){const l=Math.acos(o),u=Math.sin(l);c=Math.sin(c*l)/u,t=Math.sin(t*l)/u,this._x=this._x*c+n*t,this._y=this._y*c+i*t,this._z=this._z*c+s*t,this._w=this._w*c+a*t,this._onChangeCallback()}else this._x=this._x*c+n*t,this._y=this._y*c+i*t,this._z=this._z*c+s*t,this._w=this._w*c+a*t,this.normalize();return this}slerpQuaternions(e,t,n){return this.copy(e).slerp(t,n)}random(){const e=2*Math.PI*Math.random(),t=2*Math.PI*Math.random(),n=Math.random(),i=Math.sqrt(1-n),s=Math.sqrt(n);return this.set(i*Math.sin(e),i*Math.cos(e),s*Math.sin(t),s*Math.cos(t))}equals(e){return e._x===this._x&&e._y===this._y&&e._z===this._z&&e._w===this._w}fromArray(e,t=0){return this._x=e[t],this._y=e[t+1],this._z=e[t+2],this._w=e[t+3],this._onChangeCallback(),this}toArray(e=[],t=0){return e[t]=this._x,e[t+1]=this._y,e[t+2]=this._z,e[t+3]=this._w,e}fromBufferAttribute(e,t){return this._x=e.getX(t),this._y=e.getY(t),this._z=e.getZ(t),this._w=e.getW(t),this._onChangeCallback(),this}toJSON(){return this.toArray()}_onChange(e){return this._onChangeCallback=e,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._w}}class k{constructor(e=0,t=0,n=0){k.prototype.isVector3=!0,this.x=e,this.y=t,this.z=n}set(e,t,n){return n===void 0&&(n=this.z),this.x=e,this.y=t,this.z=n,this}setScalar(e){return this.x=e,this.y=e,this.z=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setZ(e){return this.z=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;case 2:this.z=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;case 2:return this.z;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y,this.z)}copy(e){return this.x=e.x,this.y=e.y,this.z=e.z,this}add(e){return this.x+=e.x,this.y+=e.y,this.z+=e.z,this}addScalar(e){return this.x+=e,this.y+=e,this.z+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this.z=e.z+t.z,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this.z+=e.z*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this.z-=e.z,this}subScalar(e){return this.x-=e,this.y-=e,this.z-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this.z=e.z-t.z,this}multiply(e){return this.x*=e.x,this.y*=e.y,this.z*=e.z,this}multiplyScalar(e){return this.x*=e,this.y*=e,this.z*=e,this}multiplyVectors(e,t){return this.x=e.x*t.x,this.y=e.y*t.y,this.z=e.z*t.z,this}applyEuler(e){return this.applyQuaternion(eu.setFromEuler(e))}applyAxisAngle(e,t){return this.applyQuaternion(eu.setFromAxisAngle(e,t))}applyMatrix3(e){const t=this.x,n=this.y,i=this.z,s=e.elements;return this.x=s[0]*t+s[3]*n+s[6]*i,this.y=s[1]*t+s[4]*n+s[7]*i,this.z=s[2]*t+s[5]*n+s[8]*i,this}applyNormalMatrix(e){return this.applyMatrix3(e).normalize()}applyMatrix4(e){const t=this.x,n=this.y,i=this.z,s=e.elements,a=1/(s[3]*t+s[7]*n+s[11]*i+s[15]);return this.x=(s[0]*t+s[4]*n+s[8]*i+s[12])*a,this.y=(s[1]*t+s[5]*n+s[9]*i+s[13])*a,this.z=(s[2]*t+s[6]*n+s[10]*i+s[14])*a,this}applyQuaternion(e){const t=this.x,n=this.y,i=this.z,s=e.x,a=e.y,o=e.z,c=e.w,l=2*(a*i-o*n),u=2*(o*t-s*i),f=2*(s*n-a*t);return this.x=t+c*l+a*f-o*u,this.y=n+c*u+o*l-s*f,this.z=i+c*f+s*u-a*l,this}project(e){return this.applyMatrix4(e.matrixWorldInverse).applyMatrix4(e.projectionMatrix)}unproject(e){return this.applyMatrix4(e.projectionMatrixInverse).applyMatrix4(e.matrixWorld)}transformDirection(e){const t=this.x,n=this.y,i=this.z,s=e.elements;return this.x=s[0]*t+s[4]*n+s[8]*i,this.y=s[1]*t+s[5]*n+s[9]*i,this.z=s[2]*t+s[6]*n+s[10]*i,this.normalize()}divide(e){return this.x/=e.x,this.y/=e.y,this.z/=e.z,this}divideScalar(e){return this.multiplyScalar(1/e)}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this.z=Math.min(this.z,e.z),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this.z=Math.max(this.z,e.z),this}clamp(e,t){return this.x=it(this.x,e.x,t.x),this.y=it(this.y,e.y,t.y),this.z=it(this.z,e.z,t.z),this}clampScalar(e,t){return this.x=it(this.x,e,t),this.y=it(this.y,e,t),this.z=it(this.z,e,t),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(it(n,e,t))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this}dot(e){return this.x*e.x+this.y*e.y+this.z*e.z}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)}normalize(){return this.divideScalar(this.length()||1)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this.z+=(e.z-this.z)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this.z=e.z+(t.z-e.z)*n,this}cross(e){return this.crossVectors(this,e)}crossVectors(e,t){const n=e.x,i=e.y,s=e.z,a=t.x,o=t.y,c=t.z;return this.x=i*c-s*o,this.y=s*a-n*c,this.z=n*o-i*a,this}projectOnVector(e){const t=e.lengthSq();if(t===0)return this.set(0,0,0);const n=e.dot(this)/t;return this.copy(e).multiplyScalar(n)}projectOnPlane(e){return fo.copy(this).projectOnVector(e),this.sub(fo)}reflect(e){return this.sub(fo.copy(e).multiplyScalar(2*this.dot(e)))}angleTo(e){const t=Math.sqrt(this.lengthSq()*e.lengthSq());if(t===0)return Math.PI/2;const n=this.dot(e)/t;return Math.acos(it(n,-1,1))}distanceTo(e){return Math.sqrt(this.distanceToSquared(e))}distanceToSquared(e){const t=this.x-e.x,n=this.y-e.y,i=this.z-e.z;return t*t+n*n+i*i}manhattanDistanceTo(e){return Math.abs(this.x-e.x)+Math.abs(this.y-e.y)+Math.abs(this.z-e.z)}setFromSpherical(e){return this.setFromSphericalCoords(e.radius,e.phi,e.theta)}setFromSphericalCoords(e,t,n){const i=Math.sin(t)*e;return this.x=i*Math.sin(n),this.y=Math.cos(t)*e,this.z=i*Math.cos(n),this}setFromCylindrical(e){return this.setFromCylindricalCoords(e.radius,e.theta,e.y)}setFromCylindricalCoords(e,t,n){return this.x=e*Math.sin(t),this.y=n,this.z=e*Math.cos(t),this}setFromMatrixPosition(e){const t=e.elements;return this.x=t[12],this.y=t[13],this.z=t[14],this}setFromMatrixScale(e){const t=this.setFromMatrixColumn(e,0).length(),n=this.setFromMatrixColumn(e,1).length(),i=this.setFromMatrixColumn(e,2).length();return this.x=t,this.y=n,this.z=i,this}setFromMatrixColumn(e,t){return this.fromArray(e.elements,t*4)}setFromMatrix3Column(e,t){return this.fromArray(e.elements,t*3)}setFromEuler(e){return this.x=e._x,this.y=e._y,this.z=e._z,this}setFromColor(e){return this.x=e.r,this.y=e.g,this.z=e.b,this}equals(e){return e.x===this.x&&e.y===this.y&&e.z===this.z}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this.z=e[t+2],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e[t+2]=this.z,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this.z=e.getZ(t),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this}randomDirection(){const e=Math.random()*Math.PI*2,t=Math.random()*2-1,n=Math.sqrt(1-t*t);return this.x=n*Math.cos(e),this.y=t,this.z=n*Math.sin(e),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z}}const fo=new k,eu=new ji;class Ke{constructor(e,t,n,i,s,a,o,c,l){Ke.prototype.isMatrix3=!0,this.elements=[1,0,0,0,1,0,0,0,1],e!==void 0&&this.set(e,t,n,i,s,a,o,c,l)}set(e,t,n,i,s,a,o,c,l){const u=this.elements;return u[0]=e,u[1]=i,u[2]=o,u[3]=t,u[4]=s,u[5]=c,u[6]=n,u[7]=a,u[8]=l,this}identity(){return this.set(1,0,0,0,1,0,0,0,1),this}copy(e){const t=this.elements,n=e.elements;return t[0]=n[0],t[1]=n[1],t[2]=n[2],t[3]=n[3],t[4]=n[4],t[5]=n[5],t[6]=n[6],t[7]=n[7],t[8]=n[8],this}extractBasis(e,t,n){return e.setFromMatrix3Column(this,0),t.setFromMatrix3Column(this,1),n.setFromMatrix3Column(this,2),this}setFromMatrix4(e){const t=e.elements;return this.set(t[0],t[4],t[8],t[1],t[5],t[9],t[2],t[6],t[10]),this}multiply(e){return this.multiplyMatrices(this,e)}premultiply(e){return this.multiplyMatrices(e,this)}multiplyMatrices(e,t){const n=e.elements,i=t.elements,s=this.elements,a=n[0],o=n[3],c=n[6],l=n[1],u=n[4],f=n[7],h=n[2],m=n[5],_=n[8],g=i[0],d=i[3],p=i[6],M=i[1],y=i[4],b=i[7],T=i[2],A=i[5],R=i[8];return s[0]=a*g+o*M+c*T,s[3]=a*d+o*y+c*A,s[6]=a*p+o*b+c*R,s[1]=l*g+u*M+f*T,s[4]=l*d+u*y+f*A,s[7]=l*p+u*b+f*R,s[2]=h*g+m*M+_*T,s[5]=h*d+m*y+_*A,s[8]=h*p+m*b+_*R,this}multiplyScalar(e){const t=this.elements;return t[0]*=e,t[3]*=e,t[6]*=e,t[1]*=e,t[4]*=e,t[7]*=e,t[2]*=e,t[5]*=e,t[8]*=e,this}determinant(){const e=this.elements,t=e[0],n=e[1],i=e[2],s=e[3],a=e[4],o=e[5],c=e[6],l=e[7],u=e[8];return t*a*u-t*o*l-n*s*u+n*o*c+i*s*l-i*a*c}invert(){const e=this.elements,t=e[0],n=e[1],i=e[2],s=e[3],a=e[4],o=e[5],c=e[6],l=e[7],u=e[8],f=u*a-o*l,h=o*c-u*s,m=l*s-a*c,_=t*f+n*h+i*m;if(_===0)return this.set(0,0,0,0,0,0,0,0,0);const g=1/_;return e[0]=f*g,e[1]=(i*l-u*n)*g,e[2]=(o*n-i*a)*g,e[3]=h*g,e[4]=(u*t-i*c)*g,e[5]=(i*s-o*t)*g,e[6]=m*g,e[7]=(n*c-l*t)*g,e[8]=(a*t-n*s)*g,this}transpose(){let e;const t=this.elements;return e=t[1],t[1]=t[3],t[3]=e,e=t[2],t[2]=t[6],t[6]=e,e=t[5],t[5]=t[7],t[7]=e,this}getNormalMatrix(e){return this.setFromMatrix4(e).invert().transpose()}transposeIntoArray(e){const t=this.elements;return e[0]=t[0],e[1]=t[3],e[2]=t[6],e[3]=t[1],e[4]=t[4],e[5]=t[7],e[6]=t[2],e[7]=t[5],e[8]=t[8],this}setUvTransform(e,t,n,i,s,a,o){const c=Math.cos(s),l=Math.sin(s);return this.set(n*c,n*l,-n*(c*a+l*o)+a+e,-i*l,i*c,-i*(-l*a+c*o)+o+t,0,0,1),this}scale(e,t){return this.premultiply(po.makeScale(e,t)),this}rotate(e){return this.premultiply(po.makeRotation(-e)),this}translate(e,t){return this.premultiply(po.makeTranslation(e,t)),this}makeTranslation(e,t){return e.isVector2?this.set(1,0,e.x,0,1,e.y,0,0,1):this.set(1,0,e,0,1,t,0,0,1),this}makeRotation(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,-n,0,n,t,0,0,0,1),this}makeScale(e,t){return this.set(e,0,0,0,t,0,0,0,1),this}equals(e){const t=this.elements,n=e.elements;for(let i=0;i<9;i++)if(t[i]!==n[i])return!1;return!0}fromArray(e,t=0){for(let n=0;n<9;n++)this.elements[n]=e[n+t];return this}toArray(e=[],t=0){const n=this.elements;return e[t]=n[0],e[t+1]=n[1],e[t+2]=n[2],e[t+3]=n[3],e[t+4]=n[4],e[t+5]=n[5],e[t+6]=n[6],e[t+7]=n[7],e[t+8]=n[8],e}clone(){return new this.constructor().fromArray(this.elements)}}const po=new Ke,tu=new Ke().set(.4123908,.3575843,.1804808,.212639,.7151687,.0721923,.0193308,.1191948,.9505322),nu=new Ke().set(3.2409699,-1.5373832,-.4986108,-.9692436,1.8759675,.0415551,.0556301,-.203977,1.0569715);function np(){const r={enabled:!0,workingColorSpace:ls,spaces:{},convert:function(i,s,a){return this.enabled===!1||s===a||!s||!a||(this.spaces[s].transfer===mt&&(i.r=Ai(i.r),i.g=Ai(i.g),i.b=Ai(i.b)),this.spaces[s].primaries!==this.spaces[a].primaries&&(i.applyMatrix3(this.spaces[s].toXYZ),i.applyMatrix3(this.spaces[a].fromXYZ)),this.spaces[a].transfer===mt&&(i.r=ts(i.r),i.g=ts(i.g),i.b=ts(i.b))),i},workingToColorSpace:function(i,s){return this.convert(i,this.workingColorSpace,s)},colorSpaceToWorking:function(i,s){return this.convert(i,s,this.workingColorSpace)},getPrimaries:function(i){return this.spaces[i].primaries},getTransfer:function(i){return i===Hi?Ha:this.spaces[i].transfer},getToneMappingMode:function(i){return this.spaces[i].outputColorSpaceConfig.toneMappingMode||"standard"},getLuminanceCoefficients:function(i,s=this.workingColorSpace){return i.fromArray(this.spaces[s].luminanceCoefficients)},define:function(i){Object.assign(this.spaces,i)},_getMatrix:function(i,s,a){return i.copy(this.spaces[s].toXYZ).multiply(this.spaces[a].fromXYZ)},_getDrawingBufferColorSpace:function(i){return this.spaces[i].outputColorSpaceConfig.drawingBufferColorSpace},_getUnpackColorSpace:function(i=this.workingColorSpace){return this.spaces[i].workingColorSpaceConfig.unpackColorSpace},fromWorkingColorSpace:function(i,s){return Xa("ColorManagement: .fromWorkingColorSpace() has been renamed to .workingToColorSpace()."),r.workingToColorSpace(i,s)},toWorkingColorSpace:function(i,s){return Xa("ColorManagement: .toWorkingColorSpace() has been renamed to .colorSpaceToWorking()."),r.colorSpaceToWorking(i,s)}},e=[.64,.33,.3,.6,.15,.06],t=[.2126,.7152,.0722],n=[.3127,.329];return r.define({[ls]:{primaries:e,whitePoint:n,transfer:Ha,toXYZ:tu,fromXYZ:nu,luminanceCoefficients:t,workingColorSpaceConfig:{unpackColorSpace:An},outputColorSpaceConfig:{drawingBufferColorSpace:An}},[An]:{primaries:e,whitePoint:n,transfer:mt,toXYZ:tu,fromXYZ:nu,luminanceCoefficients:t,outputColorSpaceConfig:{drawingBufferColorSpace:An}}}),r}const ct=np();function Ai(r){return r<.04045?r*.0773993808:Math.pow(r*.9478672986+.0521327014,2.4)}function ts(r){return r<.0031308?r*12.92:1.055*Math.pow(r,.41666)-.055}let Ur;class ip{static getDataURL(e,t="image/png"){if(/^data:/i.test(e.src)||typeof HTMLCanvasElement>"u")return e.src;let n;if(e instanceof HTMLCanvasElement)n=e;else{Ur===void 0&&(Ur=Wa("canvas")),Ur.width=e.width,Ur.height=e.height;const i=Ur.getContext("2d");e instanceof ImageData?i.putImageData(e,0,0):i.drawImage(e,0,0,e.width,e.height),n=Ur}return n.toDataURL(t)}static sRGBToLinear(e){if(typeof HTMLImageElement<"u"&&e instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&e instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&e instanceof ImageBitmap){const t=Wa("canvas");t.width=e.width,t.height=e.height;const n=t.getContext("2d");n.drawImage(e,0,0,e.width,e.height);const i=n.getImageData(0,0,e.width,e.height),s=i.data;for(let a=0;a<s.length;a++)s[a]=Ai(s[a]/255)*255;return n.putImageData(i,0,0),t}else if(e.data){const t=e.data.slice(0);for(let n=0;n<t.length;n++)t instanceof Uint8Array||t instanceof Uint8ClampedArray?t[n]=Math.floor(Ai(t[n]/255)*255):t[n]=Ai(t[n]);return{data:t,width:e.width,height:e.height}}else return Xe("ImageUtils.sRGBToLinear(): Unsupported image type. No color space conversion applied."),e}}let rp=0;class vc{constructor(e=null){this.isSource=!0,Object.defineProperty(this,"id",{value:rp++}),this.uuid=Zs(),this.data=e,this.dataReady=!0,this.version=0}getSize(e){const t=this.data;return typeof HTMLVideoElement<"u"&&t instanceof HTMLVideoElement?e.set(t.videoWidth,t.videoHeight,0):typeof VideoFrame<"u"&&t instanceof VideoFrame?e.set(t.displayHeight,t.displayWidth,0):t!==null?e.set(t.width,t.height,t.depth||0):e.set(0,0,0),e}set needsUpdate(e){e===!0&&this.version++}toJSON(e){const t=e===void 0||typeof e=="string";if(!t&&e.images[this.uuid]!==void 0)return e.images[this.uuid];const n={uuid:this.uuid,url:""},i=this.data;if(i!==null){let s;if(Array.isArray(i)){s=[];for(let a=0,o=i.length;a<o;a++)i[a].isDataTexture?s.push(mo(i[a].image)):s.push(mo(i[a]))}else s=mo(i);n.url=s}return t||(e.images[this.uuid]=n),n}}function mo(r){return typeof HTMLImageElement<"u"&&r instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&r instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&r instanceof ImageBitmap?ip.getDataURL(r):r.data?{data:Array.from(r.data),width:r.width,height:r.height,type:r.data.constructor.name}:(Xe("Texture: Unable to serialize Texture."),{})}let sp=0;const _o=new k;class ln extends Tr{constructor(e=ln.DEFAULT_IMAGE,t=ln.DEFAULT_MAPPING,n=Ei,i=Ei,s=nn,a=gr,o=Kn,c=Cn,l=ln.DEFAULT_ANISOTROPY,u=Hi){super(),this.isTexture=!0,Object.defineProperty(this,"id",{value:sp++}),this.uuid=Zs(),this.name="",this.source=new vc(e),this.mipmaps=[],this.mapping=t,this.channel=0,this.wrapS=n,this.wrapT=i,this.magFilter=s,this.minFilter=a,this.anisotropy=l,this.format=o,this.internalFormat=null,this.type=c,this.offset=new $e(0,0),this.repeat=new $e(1,1),this.center=new $e(0,0),this.rotation=0,this.matrixAutoUpdate=!0,this.matrix=new Ke,this.generateMipmaps=!0,this.premultiplyAlpha=!1,this.flipY=!0,this.unpackAlignment=4,this.colorSpace=u,this.userData={},this.updateRanges=[],this.version=0,this.onUpdate=null,this.renderTarget=null,this.isRenderTargetTexture=!1,this.isArrayTexture=!!(e&&e.depth&&e.depth>1),this.pmremVersion=0}get width(){return this.source.getSize(_o).x}get height(){return this.source.getSize(_o).y}get depth(){return this.source.getSize(_o).z}get image(){return this.source.data}set image(e=null){this.source.data=e}updateMatrix(){this.matrix.setUvTransform(this.offset.x,this.offset.y,this.repeat.x,this.repeat.y,this.rotation,this.center.x,this.center.y)}addUpdateRange(e,t){this.updateRanges.push({start:e,count:t})}clearUpdateRanges(){this.updateRanges.length=0}clone(){return new this.constructor().copy(this)}copy(e){return this.name=e.name,this.source=e.source,this.mipmaps=e.mipmaps.slice(0),this.mapping=e.mapping,this.channel=e.channel,this.wrapS=e.wrapS,this.wrapT=e.wrapT,this.magFilter=e.magFilter,this.minFilter=e.minFilter,this.anisotropy=e.anisotropy,this.format=e.format,this.internalFormat=e.internalFormat,this.type=e.type,this.offset.copy(e.offset),this.repeat.copy(e.repeat),this.center.copy(e.center),this.rotation=e.rotation,this.matrixAutoUpdate=e.matrixAutoUpdate,this.matrix.copy(e.matrix),this.generateMipmaps=e.generateMipmaps,this.premultiplyAlpha=e.premultiplyAlpha,this.flipY=e.flipY,this.unpackAlignment=e.unpackAlignment,this.colorSpace=e.colorSpace,this.renderTarget=e.renderTarget,this.isRenderTargetTexture=e.isRenderTargetTexture,this.isArrayTexture=e.isArrayTexture,this.userData=JSON.parse(JSON.stringify(e.userData)),this.needsUpdate=!0,this}setValues(e){for(const t in e){const n=e[t];if(n===void 0){Xe(`Texture.setValues(): parameter '${t}' has value of undefined.`);continue}const i=this[t];if(i===void 0){Xe(`Texture.setValues(): property '${t}' does not exist.`);continue}i&&n&&i.isVector2&&n.isVector2||i&&n&&i.isVector3&&n.isVector3||i&&n&&i.isMatrix3&&n.isMatrix3?i.copy(n):this[t]=n}}toJSON(e){const t=e===void 0||typeof e=="string";if(!t&&e.textures[this.uuid]!==void 0)return e.textures[this.uuid];const n={metadata:{version:4.7,type:"Texture",generator:"Texture.toJSON"},uuid:this.uuid,name:this.name,image:this.source.toJSON(e).uuid,mapping:this.mapping,channel:this.channel,repeat:[this.repeat.x,this.repeat.y],offset:[this.offset.x,this.offset.y],center:[this.center.x,this.center.y],rotation:this.rotation,wrap:[this.wrapS,this.wrapT],format:this.format,internalFormat:this.internalFormat,type:this.type,colorSpace:this.colorSpace,minFilter:this.minFilter,magFilter:this.magFilter,anisotropy:this.anisotropy,flipY:this.flipY,generateMipmaps:this.generateMipmaps,premultiplyAlpha:this.premultiplyAlpha,unpackAlignment:this.unpackAlignment};return Object.keys(this.userData).length>0&&(n.userData=this.userData),t||(e.textures[this.uuid]=n),n}dispose(){this.dispatchEvent({type:"dispose"})}transformUv(e){if(this.mapping!==Nh)return e;if(e.applyMatrix3(this.matrix),e.x<0||e.x>1)switch(this.wrapS){case cl:e.x=e.x-Math.floor(e.x);break;case Ei:e.x=e.x<0?0:1;break;case ul:Math.abs(Math.floor(e.x)%2)===1?e.x=Math.ceil(e.x)-e.x:e.x=e.x-Math.floor(e.x);break}if(e.y<0||e.y>1)switch(this.wrapT){case cl:e.y=e.y-Math.floor(e.y);break;case Ei:e.y=e.y<0?0:1;break;case ul:Math.abs(Math.floor(e.y)%2)===1?e.y=Math.ceil(e.y)-e.y:e.y=e.y-Math.floor(e.y);break}return this.flipY&&(e.y=1-e.y),e}set needsUpdate(e){e===!0&&(this.version++,this.source.needsUpdate=!0)}set needsPMREMUpdate(e){e===!0&&this.pmremVersion++}}ln.DEFAULT_IMAGE=null;ln.DEFAULT_MAPPING=Nh;ln.DEFAULT_ANISOTROPY=1;class Dt{constructor(e=0,t=0,n=0,i=1){Dt.prototype.isVector4=!0,this.x=e,this.y=t,this.z=n,this.w=i}get width(){return this.z}set width(e){this.z=e}get height(){return this.w}set height(e){this.w=e}set(e,t,n,i){return this.x=e,this.y=t,this.z=n,this.w=i,this}setScalar(e){return this.x=e,this.y=e,this.z=e,this.w=e,this}setX(e){return this.x=e,this}setY(e){return this.y=e,this}setZ(e){return this.z=e,this}setW(e){return this.w=e,this}setComponent(e,t){switch(e){case 0:this.x=t;break;case 1:this.y=t;break;case 2:this.z=t;break;case 3:this.w=t;break;default:throw new Error("index is out of range: "+e)}return this}getComponent(e){switch(e){case 0:return this.x;case 1:return this.y;case 2:return this.z;case 3:return this.w;default:throw new Error("index is out of range: "+e)}}clone(){return new this.constructor(this.x,this.y,this.z,this.w)}copy(e){return this.x=e.x,this.y=e.y,this.z=e.z,this.w=e.w!==void 0?e.w:1,this}add(e){return this.x+=e.x,this.y+=e.y,this.z+=e.z,this.w+=e.w,this}addScalar(e){return this.x+=e,this.y+=e,this.z+=e,this.w+=e,this}addVectors(e,t){return this.x=e.x+t.x,this.y=e.y+t.y,this.z=e.z+t.z,this.w=e.w+t.w,this}addScaledVector(e,t){return this.x+=e.x*t,this.y+=e.y*t,this.z+=e.z*t,this.w+=e.w*t,this}sub(e){return this.x-=e.x,this.y-=e.y,this.z-=e.z,this.w-=e.w,this}subScalar(e){return this.x-=e,this.y-=e,this.z-=e,this.w-=e,this}subVectors(e,t){return this.x=e.x-t.x,this.y=e.y-t.y,this.z=e.z-t.z,this.w=e.w-t.w,this}multiply(e){return this.x*=e.x,this.y*=e.y,this.z*=e.z,this.w*=e.w,this}multiplyScalar(e){return this.x*=e,this.y*=e,this.z*=e,this.w*=e,this}applyMatrix4(e){const t=this.x,n=this.y,i=this.z,s=this.w,a=e.elements;return this.x=a[0]*t+a[4]*n+a[8]*i+a[12]*s,this.y=a[1]*t+a[5]*n+a[9]*i+a[13]*s,this.z=a[2]*t+a[6]*n+a[10]*i+a[14]*s,this.w=a[3]*t+a[7]*n+a[11]*i+a[15]*s,this}divide(e){return this.x/=e.x,this.y/=e.y,this.z/=e.z,this.w/=e.w,this}divideScalar(e){return this.multiplyScalar(1/e)}setAxisAngleFromQuaternion(e){this.w=2*Math.acos(e.w);const t=Math.sqrt(1-e.w*e.w);return t<1e-4?(this.x=1,this.y=0,this.z=0):(this.x=e.x/t,this.y=e.y/t,this.z=e.z/t),this}setAxisAngleFromRotationMatrix(e){let t,n,i,s;const c=e.elements,l=c[0],u=c[4],f=c[8],h=c[1],m=c[5],_=c[9],g=c[2],d=c[6],p=c[10];if(Math.abs(u-h)<.01&&Math.abs(f-g)<.01&&Math.abs(_-d)<.01){if(Math.abs(u+h)<.1&&Math.abs(f+g)<.1&&Math.abs(_+d)<.1&&Math.abs(l+m+p-3)<.1)return this.set(1,0,0,0),this;t=Math.PI;const y=(l+1)/2,b=(m+1)/2,T=(p+1)/2,A=(u+h)/4,R=(f+g)/4,x=(_+d)/4;return y>b&&y>T?y<.01?(n=0,i=.707106781,s=.707106781):(n=Math.sqrt(y),i=A/n,s=R/n):b>T?b<.01?(n=.707106781,i=0,s=.707106781):(i=Math.sqrt(b),n=A/i,s=x/i):T<.01?(n=.707106781,i=.707106781,s=0):(s=Math.sqrt(T),n=R/s,i=x/s),this.set(n,i,s,t),this}let M=Math.sqrt((d-_)*(d-_)+(f-g)*(f-g)+(h-u)*(h-u));return Math.abs(M)<.001&&(M=1),this.x=(d-_)/M,this.y=(f-g)/M,this.z=(h-u)/M,this.w=Math.acos((l+m+p-1)/2),this}setFromMatrixPosition(e){const t=e.elements;return this.x=t[12],this.y=t[13],this.z=t[14],this.w=t[15],this}min(e){return this.x=Math.min(this.x,e.x),this.y=Math.min(this.y,e.y),this.z=Math.min(this.z,e.z),this.w=Math.min(this.w,e.w),this}max(e){return this.x=Math.max(this.x,e.x),this.y=Math.max(this.y,e.y),this.z=Math.max(this.z,e.z),this.w=Math.max(this.w,e.w),this}clamp(e,t){return this.x=it(this.x,e.x,t.x),this.y=it(this.y,e.y,t.y),this.z=it(this.z,e.z,t.z),this.w=it(this.w,e.w,t.w),this}clampScalar(e,t){return this.x=it(this.x,e,t),this.y=it(this.y,e,t),this.z=it(this.z,e,t),this.w=it(this.w,e,t),this}clampLength(e,t){const n=this.length();return this.divideScalar(n||1).multiplyScalar(it(n,e,t))}floor(){return this.x=Math.floor(this.x),this.y=Math.floor(this.y),this.z=Math.floor(this.z),this.w=Math.floor(this.w),this}ceil(){return this.x=Math.ceil(this.x),this.y=Math.ceil(this.y),this.z=Math.ceil(this.z),this.w=Math.ceil(this.w),this}round(){return this.x=Math.round(this.x),this.y=Math.round(this.y),this.z=Math.round(this.z),this.w=Math.round(this.w),this}roundToZero(){return this.x=Math.trunc(this.x),this.y=Math.trunc(this.y),this.z=Math.trunc(this.z),this.w=Math.trunc(this.w),this}negate(){return this.x=-this.x,this.y=-this.y,this.z=-this.z,this.w=-this.w,this}dot(e){return this.x*e.x+this.y*e.y+this.z*e.z+this.w*e.w}lengthSq(){return this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w}length(){return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z+this.w*this.w)}manhattanLength(){return Math.abs(this.x)+Math.abs(this.y)+Math.abs(this.z)+Math.abs(this.w)}normalize(){return this.divideScalar(this.length()||1)}setLength(e){return this.normalize().multiplyScalar(e)}lerp(e,t){return this.x+=(e.x-this.x)*t,this.y+=(e.y-this.y)*t,this.z+=(e.z-this.z)*t,this.w+=(e.w-this.w)*t,this}lerpVectors(e,t,n){return this.x=e.x+(t.x-e.x)*n,this.y=e.y+(t.y-e.y)*n,this.z=e.z+(t.z-e.z)*n,this.w=e.w+(t.w-e.w)*n,this}equals(e){return e.x===this.x&&e.y===this.y&&e.z===this.z&&e.w===this.w}fromArray(e,t=0){return this.x=e[t],this.y=e[t+1],this.z=e[t+2],this.w=e[t+3],this}toArray(e=[],t=0){return e[t]=this.x,e[t+1]=this.y,e[t+2]=this.z,e[t+3]=this.w,e}fromBufferAttribute(e,t){return this.x=e.getX(t),this.y=e.getY(t),this.z=e.getZ(t),this.w=e.getW(t),this}random(){return this.x=Math.random(),this.y=Math.random(),this.z=Math.random(),this.w=Math.random(),this}*[Symbol.iterator](){yield this.x,yield this.y,yield this.z,yield this.w}}class ap extends Tr{constructor(e=1,t=1,n={}){super(),n=Object.assign({generateMipmaps:!1,internalFormat:null,minFilter:nn,depthBuffer:!0,stencilBuffer:!1,resolveDepthBuffer:!0,resolveStencilBuffer:!0,depthTexture:null,samples:0,count:1,depth:1,multiview:!1},n),this.isRenderTarget=!0,this.width=e,this.height=t,this.depth=n.depth,this.scissor=new Dt(0,0,e,t),this.scissorTest=!1,this.viewport=new Dt(0,0,e,t),this.textures=[];const i={width:e,height:t,depth:n.depth},s=new ln(i),a=n.count;for(let o=0;o<a;o++)this.textures[o]=s.clone(),this.textures[o].isRenderTargetTexture=!0,this.textures[o].renderTarget=this;this._setTextureOptions(n),this.depthBuffer=n.depthBuffer,this.stencilBuffer=n.stencilBuffer,this.resolveDepthBuffer=n.resolveDepthBuffer,this.resolveStencilBuffer=n.resolveStencilBuffer,this._depthTexture=null,this.depthTexture=n.depthTexture,this.samples=n.samples,this.multiview=n.multiview}_setTextureOptions(e={}){const t={minFilter:nn,generateMipmaps:!1,flipY:!1,internalFormat:null};e.mapping!==void 0&&(t.mapping=e.mapping),e.wrapS!==void 0&&(t.wrapS=e.wrapS),e.wrapT!==void 0&&(t.wrapT=e.wrapT),e.wrapR!==void 0&&(t.wrapR=e.wrapR),e.magFilter!==void 0&&(t.magFilter=e.magFilter),e.minFilter!==void 0&&(t.minFilter=e.minFilter),e.format!==void 0&&(t.format=e.format),e.type!==void 0&&(t.type=e.type),e.anisotropy!==void 0&&(t.anisotropy=e.anisotropy),e.colorSpace!==void 0&&(t.colorSpace=e.colorSpace),e.flipY!==void 0&&(t.flipY=e.flipY),e.generateMipmaps!==void 0&&(t.generateMipmaps=e.generateMipmaps),e.internalFormat!==void 0&&(t.internalFormat=e.internalFormat);for(let n=0;n<this.textures.length;n++)this.textures[n].setValues(t)}get texture(){return this.textures[0]}set texture(e){this.textures[0]=e}set depthTexture(e){this._depthTexture!==null&&(this._depthTexture.renderTarget=null),e!==null&&(e.renderTarget=this),this._depthTexture=e}get depthTexture(){return this._depthTexture}setSize(e,t,n=1){if(this.width!==e||this.height!==t||this.depth!==n){this.width=e,this.height=t,this.depth=n;for(let i=0,s=this.textures.length;i<s;i++)this.textures[i].image.width=e,this.textures[i].image.height=t,this.textures[i].image.depth=n,this.textures[i].isData3DTexture!==!0&&(this.textures[i].isArrayTexture=this.textures[i].image.depth>1);this.dispose()}this.viewport.set(0,0,e,t),this.scissor.set(0,0,e,t)}clone(){return new this.constructor().copy(this)}copy(e){this.width=e.width,this.height=e.height,this.depth=e.depth,this.scissor.copy(e.scissor),this.scissorTest=e.scissorTest,this.viewport.copy(e.viewport),this.textures.length=0;for(let t=0,n=e.textures.length;t<n;t++){this.textures[t]=e.textures[t].clone(),this.textures[t].isRenderTargetTexture=!0,this.textures[t].renderTarget=this;const i=Object.assign({},e.textures[t].image);this.textures[t].source=new vc(i)}return this.depthBuffer=e.depthBuffer,this.stencilBuffer=e.stencilBuffer,this.resolveDepthBuffer=e.resolveDepthBuffer,this.resolveStencilBuffer=e.resolveStencilBuffer,e.depthTexture!==null&&(this.depthTexture=e.depthTexture.clone()),this.samples=e.samples,this}dispose(){this.dispatchEvent({type:"dispose"})}}class ui extends ap{constructor(e=1,t=1,n={}){super(e,t,n),this.isWebGLRenderTarget=!0}}class Xh extends ln{constructor(e=null,t=1,n=1,i=1){super(null),this.isDataArrayTexture=!0,this.image={data:e,width:t,height:n,depth:i},this.magFilter=$t,this.minFilter=$t,this.wrapR=Ei,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1,this.layerUpdates=new Set}addLayerUpdate(e){this.layerUpdates.add(e)}clearLayerUpdates(){this.layerUpdates.clear()}}class op extends ln{constructor(e=null,t=1,n=1,i=1){super(null),this.isData3DTexture=!0,this.image={data:e,width:t,height:n,depth:i},this.magFilter=$t,this.minFilter=$t,this.wrapR=Ei,this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}class yt{constructor(e,t,n,i,s,a,o,c,l,u,f,h,m,_,g,d){yt.prototype.isMatrix4=!0,this.elements=[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1],e!==void 0&&this.set(e,t,n,i,s,a,o,c,l,u,f,h,m,_,g,d)}set(e,t,n,i,s,a,o,c,l,u,f,h,m,_,g,d){const p=this.elements;return p[0]=e,p[4]=t,p[8]=n,p[12]=i,p[1]=s,p[5]=a,p[9]=o,p[13]=c,p[2]=l,p[6]=u,p[10]=f,p[14]=h,p[3]=m,p[7]=_,p[11]=g,p[15]=d,this}identity(){return this.set(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1),this}clone(){return new yt().fromArray(this.elements)}copy(e){const t=this.elements,n=e.elements;return t[0]=n[0],t[1]=n[1],t[2]=n[2],t[3]=n[3],t[4]=n[4],t[5]=n[5],t[6]=n[6],t[7]=n[7],t[8]=n[8],t[9]=n[9],t[10]=n[10],t[11]=n[11],t[12]=n[12],t[13]=n[13],t[14]=n[14],t[15]=n[15],this}copyPosition(e){const t=this.elements,n=e.elements;return t[12]=n[12],t[13]=n[13],t[14]=n[14],this}setFromMatrix3(e){const t=e.elements;return this.set(t[0],t[3],t[6],0,t[1],t[4],t[7],0,t[2],t[5],t[8],0,0,0,0,1),this}extractBasis(e,t,n){return this.determinant()===0?(e.set(1,0,0),t.set(0,1,0),n.set(0,0,1),this):(e.setFromMatrixColumn(this,0),t.setFromMatrixColumn(this,1),n.setFromMatrixColumn(this,2),this)}makeBasis(e,t,n){return this.set(e.x,t.x,n.x,0,e.y,t.y,n.y,0,e.z,t.z,n.z,0,0,0,0,1),this}extractRotation(e){if(e.determinant()===0)return this.identity();const t=this.elements,n=e.elements,i=1/Nr.setFromMatrixColumn(e,0).length(),s=1/Nr.setFromMatrixColumn(e,1).length(),a=1/Nr.setFromMatrixColumn(e,2).length();return t[0]=n[0]*i,t[1]=n[1]*i,t[2]=n[2]*i,t[3]=0,t[4]=n[4]*s,t[5]=n[5]*s,t[6]=n[6]*s,t[7]=0,t[8]=n[8]*a,t[9]=n[9]*a,t[10]=n[10]*a,t[11]=0,t[12]=0,t[13]=0,t[14]=0,t[15]=1,this}makeRotationFromEuler(e){const t=this.elements,n=e.x,i=e.y,s=e.z,a=Math.cos(n),o=Math.sin(n),c=Math.cos(i),l=Math.sin(i),u=Math.cos(s),f=Math.sin(s);if(e.order==="XYZ"){const h=a*u,m=a*f,_=o*u,g=o*f;t[0]=c*u,t[4]=-c*f,t[8]=l,t[1]=m+_*l,t[5]=h-g*l,t[9]=-o*c,t[2]=g-h*l,t[6]=_+m*l,t[10]=a*c}else if(e.order==="YXZ"){const h=c*u,m=c*f,_=l*u,g=l*f;t[0]=h+g*o,t[4]=_*o-m,t[8]=a*l,t[1]=a*f,t[5]=a*u,t[9]=-o,t[2]=m*o-_,t[6]=g+h*o,t[10]=a*c}else if(e.order==="ZXY"){const h=c*u,m=c*f,_=l*u,g=l*f;t[0]=h-g*o,t[4]=-a*f,t[8]=_+m*o,t[1]=m+_*o,t[5]=a*u,t[9]=g-h*o,t[2]=-a*l,t[6]=o,t[10]=a*c}else if(e.order==="ZYX"){const h=a*u,m=a*f,_=o*u,g=o*f;t[0]=c*u,t[4]=_*l-m,t[8]=h*l+g,t[1]=c*f,t[5]=g*l+h,t[9]=m*l-_,t[2]=-l,t[6]=o*c,t[10]=a*c}else if(e.order==="YZX"){const h=a*c,m=a*l,_=o*c,g=o*l;t[0]=c*u,t[4]=g-h*f,t[8]=_*f+m,t[1]=f,t[5]=a*u,t[9]=-o*u,t[2]=-l*u,t[6]=m*f+_,t[10]=h-g*f}else if(e.order==="XZY"){const h=a*c,m=a*l,_=o*c,g=o*l;t[0]=c*u,t[4]=-f,t[8]=l*u,t[1]=h*f+g,t[5]=a*u,t[9]=m*f-_,t[2]=_*f-m,t[6]=o*u,t[10]=g*f+h}return t[3]=0,t[7]=0,t[11]=0,t[12]=0,t[13]=0,t[14]=0,t[15]=1,this}makeRotationFromQuaternion(e){return this.compose(lp,e,cp)}lookAt(e,t,n){const i=this.elements;return yn.subVectors(e,t),yn.lengthSq()===0&&(yn.z=1),yn.normalize(),Ni.crossVectors(n,yn),Ni.lengthSq()===0&&(Math.abs(n.z)===1?yn.x+=1e-4:yn.z+=1e-4,yn.normalize(),Ni.crossVectors(n,yn)),Ni.normalize(),ia.crossVectors(yn,Ni),i[0]=Ni.x,i[4]=ia.x,i[8]=yn.x,i[1]=Ni.y,i[5]=ia.y,i[9]=yn.y,i[2]=Ni.z,i[6]=ia.z,i[10]=yn.z,this}multiply(e){return this.multiplyMatrices(this,e)}premultiply(e){return this.multiplyMatrices(e,this)}multiplyMatrices(e,t){const n=e.elements,i=t.elements,s=this.elements,a=n[0],o=n[4],c=n[8],l=n[12],u=n[1],f=n[5],h=n[9],m=n[13],_=n[2],g=n[6],d=n[10],p=n[14],M=n[3],y=n[7],b=n[11],T=n[15],A=i[0],R=i[4],x=i[8],S=i[12],W=i[1],P=i[5],z=i[9],F=i[13],B=i[2],O=i[6],V=i[10],G=i[14],ne=i[3],Q=i[7],me=i[11],be=i[15];return s[0]=a*A+o*W+c*B+l*ne,s[4]=a*R+o*P+c*O+l*Q,s[8]=a*x+o*z+c*V+l*me,s[12]=a*S+o*F+c*G+l*be,s[1]=u*A+f*W+h*B+m*ne,s[5]=u*R+f*P+h*O+m*Q,s[9]=u*x+f*z+h*V+m*me,s[13]=u*S+f*F+h*G+m*be,s[2]=_*A+g*W+d*B+p*ne,s[6]=_*R+g*P+d*O+p*Q,s[10]=_*x+g*z+d*V+p*me,s[14]=_*S+g*F+d*G+p*be,s[3]=M*A+y*W+b*B+T*ne,s[7]=M*R+y*P+b*O+T*Q,s[11]=M*x+y*z+b*V+T*me,s[15]=M*S+y*F+b*G+T*be,this}multiplyScalar(e){const t=this.elements;return t[0]*=e,t[4]*=e,t[8]*=e,t[12]*=e,t[1]*=e,t[5]*=e,t[9]*=e,t[13]*=e,t[2]*=e,t[6]*=e,t[10]*=e,t[14]*=e,t[3]*=e,t[7]*=e,t[11]*=e,t[15]*=e,this}determinant(){const e=this.elements,t=e[0],n=e[4],i=e[8],s=e[12],a=e[1],o=e[5],c=e[9],l=e[13],u=e[2],f=e[6],h=e[10],m=e[14],_=e[3],g=e[7],d=e[11],p=e[15],M=c*m-l*h,y=o*m-l*f,b=o*h-c*f,T=a*m-l*u,A=a*h-c*u,R=a*f-o*u;return t*(g*M-d*y+p*b)-n*(_*M-d*T+p*A)+i*(_*y-g*T+p*R)-s*(_*b-g*A+d*R)}transpose(){const e=this.elements;let t;return t=e[1],e[1]=e[4],e[4]=t,t=e[2],e[2]=e[8],e[8]=t,t=e[6],e[6]=e[9],e[9]=t,t=e[3],e[3]=e[12],e[12]=t,t=e[7],e[7]=e[13],e[13]=t,t=e[11],e[11]=e[14],e[14]=t,this}setPosition(e,t,n){const i=this.elements;return e.isVector3?(i[12]=e.x,i[13]=e.y,i[14]=e.z):(i[12]=e,i[13]=t,i[14]=n),this}invert(){const e=this.elements,t=e[0],n=e[1],i=e[2],s=e[3],a=e[4],o=e[5],c=e[6],l=e[7],u=e[8],f=e[9],h=e[10],m=e[11],_=e[12],g=e[13],d=e[14],p=e[15],M=t*o-n*a,y=t*c-i*a,b=t*l-s*a,T=n*c-i*o,A=n*l-s*o,R=i*l-s*c,x=u*g-f*_,S=u*d-h*_,W=u*p-m*_,P=f*d-h*g,z=f*p-m*g,F=h*p-m*d,B=M*F-y*z+b*P+T*W-A*S+R*x;if(B===0)return this.set(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);const O=1/B;return e[0]=(o*F-c*z+l*P)*O,e[1]=(i*z-n*F-s*P)*O,e[2]=(g*R-d*A+p*T)*O,e[3]=(h*A-f*R-m*T)*O,e[4]=(c*W-a*F-l*S)*O,e[5]=(t*F-i*W+s*S)*O,e[6]=(d*b-_*R-p*y)*O,e[7]=(u*R-h*b+m*y)*O,e[8]=(a*z-o*W+l*x)*O,e[9]=(n*W-t*z-s*x)*O,e[10]=(_*A-g*b+p*M)*O,e[11]=(f*b-u*A-m*M)*O,e[12]=(o*S-a*P-c*x)*O,e[13]=(t*P-n*S+i*x)*O,e[14]=(g*y-_*T-d*M)*O,e[15]=(u*T-f*y+h*M)*O,this}scale(e){const t=this.elements,n=e.x,i=e.y,s=e.z;return t[0]*=n,t[4]*=i,t[8]*=s,t[1]*=n,t[5]*=i,t[9]*=s,t[2]*=n,t[6]*=i,t[10]*=s,t[3]*=n,t[7]*=i,t[11]*=s,this}getMaxScaleOnAxis(){const e=this.elements,t=e[0]*e[0]+e[1]*e[1]+e[2]*e[2],n=e[4]*e[4]+e[5]*e[5]+e[6]*e[6],i=e[8]*e[8]+e[9]*e[9]+e[10]*e[10];return Math.sqrt(Math.max(t,n,i))}makeTranslation(e,t,n){return e.isVector3?this.set(1,0,0,e.x,0,1,0,e.y,0,0,1,e.z,0,0,0,1):this.set(1,0,0,e,0,1,0,t,0,0,1,n,0,0,0,1),this}makeRotationX(e){const t=Math.cos(e),n=Math.sin(e);return this.set(1,0,0,0,0,t,-n,0,0,n,t,0,0,0,0,1),this}makeRotationY(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,0,n,0,0,1,0,0,-n,0,t,0,0,0,0,1),this}makeRotationZ(e){const t=Math.cos(e),n=Math.sin(e);return this.set(t,-n,0,0,n,t,0,0,0,0,1,0,0,0,0,1),this}makeRotationAxis(e,t){const n=Math.cos(t),i=Math.sin(t),s=1-n,a=e.x,o=e.y,c=e.z,l=s*a,u=s*o;return this.set(l*a+n,l*o-i*c,l*c+i*o,0,l*o+i*c,u*o+n,u*c-i*a,0,l*c-i*o,u*c+i*a,s*c*c+n,0,0,0,0,1),this}makeScale(e,t,n){return this.set(e,0,0,0,0,t,0,0,0,0,n,0,0,0,0,1),this}makeShear(e,t,n,i,s,a){return this.set(1,n,s,0,e,1,a,0,t,i,1,0,0,0,0,1),this}compose(e,t,n){const i=this.elements,s=t._x,a=t._y,o=t._z,c=t._w,l=s+s,u=a+a,f=o+o,h=s*l,m=s*u,_=s*f,g=a*u,d=a*f,p=o*f,M=c*l,y=c*u,b=c*f,T=n.x,A=n.y,R=n.z;return i[0]=(1-(g+p))*T,i[1]=(m+b)*T,i[2]=(_-y)*T,i[3]=0,i[4]=(m-b)*A,i[5]=(1-(h+p))*A,i[6]=(d+M)*A,i[7]=0,i[8]=(_+y)*R,i[9]=(d-M)*R,i[10]=(1-(h+g))*R,i[11]=0,i[12]=e.x,i[13]=e.y,i[14]=e.z,i[15]=1,this}decompose(e,t,n){const i=this.elements;e.x=i[12],e.y=i[13],e.z=i[14];const s=this.determinant();if(s===0)return n.set(1,1,1),t.identity(),this;let a=Nr.set(i[0],i[1],i[2]).length();const o=Nr.set(i[4],i[5],i[6]).length(),c=Nr.set(i[8],i[9],i[10]).length();s<0&&(a=-a),Xn.copy(this);const l=1/a,u=1/o,f=1/c;return Xn.elements[0]*=l,Xn.elements[1]*=l,Xn.elements[2]*=l,Xn.elements[4]*=u,Xn.elements[5]*=u,Xn.elements[6]*=u,Xn.elements[8]*=f,Xn.elements[9]*=f,Xn.elements[10]*=f,t.setFromRotationMatrix(Xn),n.x=a,n.y=o,n.z=c,this}makePerspective(e,t,n,i,s,a,o=oi,c=!1){const l=this.elements,u=2*s/(t-e),f=2*s/(n-i),h=(t+e)/(t-e),m=(n+i)/(n-i);let _,g;if(c)_=s/(a-s),g=a*s/(a-s);else if(o===oi)_=-(a+s)/(a-s),g=-2*a*s/(a-s);else if(o===Vs)_=-a/(a-s),g=-a*s/(a-s);else throw new Error("THREE.Matrix4.makePerspective(): Invalid coordinate system: "+o);return l[0]=u,l[4]=0,l[8]=h,l[12]=0,l[1]=0,l[5]=f,l[9]=m,l[13]=0,l[2]=0,l[6]=0,l[10]=_,l[14]=g,l[3]=0,l[7]=0,l[11]=-1,l[15]=0,this}makeOrthographic(e,t,n,i,s,a,o=oi,c=!1){const l=this.elements,u=2/(t-e),f=2/(n-i),h=-(t+e)/(t-e),m=-(n+i)/(n-i);let _,g;if(c)_=1/(a-s),g=a/(a-s);else if(o===oi)_=-2/(a-s),g=-(a+s)/(a-s);else if(o===Vs)_=-1/(a-s),g=-s/(a-s);else throw new Error("THREE.Matrix4.makeOrthographic(): Invalid coordinate system: "+o);return l[0]=u,l[4]=0,l[8]=0,l[12]=h,l[1]=0,l[5]=f,l[9]=0,l[13]=m,l[2]=0,l[6]=0,l[10]=_,l[14]=g,l[3]=0,l[7]=0,l[11]=0,l[15]=1,this}equals(e){const t=this.elements,n=e.elements;for(let i=0;i<16;i++)if(t[i]!==n[i])return!1;return!0}fromArray(e,t=0){for(let n=0;n<16;n++)this.elements[n]=e[n+t];return this}toArray(e=[],t=0){const n=this.elements;return e[t]=n[0],e[t+1]=n[1],e[t+2]=n[2],e[t+3]=n[3],e[t+4]=n[4],e[t+5]=n[5],e[t+6]=n[6],e[t+7]=n[7],e[t+8]=n[8],e[t+9]=n[9],e[t+10]=n[10],e[t+11]=n[11],e[t+12]=n[12],e[t+13]=n[13],e[t+14]=n[14],e[t+15]=n[15],e}}const Nr=new k,Xn=new yt,lp=new k(0,0,0),cp=new k(1,1,1),Ni=new k,ia=new k,yn=new k,iu=new yt,ru=new ji;class fi{constructor(e=0,t=0,n=0,i=fi.DEFAULT_ORDER){this.isEuler=!0,this._x=e,this._y=t,this._z=n,this._order=i}get x(){return this._x}set x(e){this._x=e,this._onChangeCallback()}get y(){return this._y}set y(e){this._y=e,this._onChangeCallback()}get z(){return this._z}set z(e){this._z=e,this._onChangeCallback()}get order(){return this._order}set order(e){this._order=e,this._onChangeCallback()}set(e,t,n,i=this._order){return this._x=e,this._y=t,this._z=n,this._order=i,this._onChangeCallback(),this}clone(){return new this.constructor(this._x,this._y,this._z,this._order)}copy(e){return this._x=e._x,this._y=e._y,this._z=e._z,this._order=e._order,this._onChangeCallback(),this}setFromRotationMatrix(e,t=this._order,n=!0){const i=e.elements,s=i[0],a=i[4],o=i[8],c=i[1],l=i[5],u=i[9],f=i[2],h=i[6],m=i[10];switch(t){case"XYZ":this._y=Math.asin(it(o,-1,1)),Math.abs(o)<.9999999?(this._x=Math.atan2(-u,m),this._z=Math.atan2(-a,s)):(this._x=Math.atan2(h,l),this._z=0);break;case"YXZ":this._x=Math.asin(-it(u,-1,1)),Math.abs(u)<.9999999?(this._y=Math.atan2(o,m),this._z=Math.atan2(c,l)):(this._y=Math.atan2(-f,s),this._z=0);break;case"ZXY":this._x=Math.asin(it(h,-1,1)),Math.abs(h)<.9999999?(this._y=Math.atan2(-f,m),this._z=Math.atan2(-a,l)):(this._y=0,this._z=Math.atan2(c,s));break;case"ZYX":this._y=Math.asin(-it(f,-1,1)),Math.abs(f)<.9999999?(this._x=Math.atan2(h,m),this._z=Math.atan2(c,s)):(this._x=0,this._z=Math.atan2(-a,l));break;case"YZX":this._z=Math.asin(it(c,-1,1)),Math.abs(c)<.9999999?(this._x=Math.atan2(-u,l),this._y=Math.atan2(-f,s)):(this._x=0,this._y=Math.atan2(o,m));break;case"XZY":this._z=Math.asin(-it(a,-1,1)),Math.abs(a)<.9999999?(this._x=Math.atan2(h,l),this._y=Math.atan2(o,s)):(this._x=Math.atan2(-u,m),this._y=0);break;default:Xe("Euler: .setFromRotationMatrix() encountered an unknown order: "+t)}return this._order=t,n===!0&&this._onChangeCallback(),this}setFromQuaternion(e,t,n){return iu.makeRotationFromQuaternion(e),this.setFromRotationMatrix(iu,t,n)}setFromVector3(e,t=this._order){return this.set(e.x,e.y,e.z,t)}reorder(e){return ru.setFromEuler(this),this.setFromQuaternion(ru,e)}equals(e){return e._x===this._x&&e._y===this._y&&e._z===this._z&&e._order===this._order}fromArray(e){return this._x=e[0],this._y=e[1],this._z=e[2],e[3]!==void 0&&(this._order=e[3]),this._onChangeCallback(),this}toArray(e=[],t=0){return e[t]=this._x,e[t+1]=this._y,e[t+2]=this._z,e[t+3]=this._order,e}_onChange(e){return this._onChangeCallback=e,this}_onChangeCallback(){}*[Symbol.iterator](){yield this._x,yield this._y,yield this._z,yield this._order}}fi.DEFAULT_ORDER="XYZ";class xc{constructor(){this.mask=1}set(e){this.mask=(1<<e|0)>>>0}enable(e){this.mask|=1<<e|0}enableAll(){this.mask=-1}toggle(e){this.mask^=1<<e|0}disable(e){this.mask&=~(1<<e|0)}disableAll(){this.mask=0}test(e){return(this.mask&e.mask)!==0}isEnabled(e){return(this.mask&(1<<e|0))!==0}}let up=0;const su=new k,Fr=new ji,_i=new yt,ra=new k,ys=new k,hp=new k,fp=new ji,au=new k(1,0,0),ou=new k(0,1,0),lu=new k(0,0,1),cu={type:"added"},dp={type:"removed"},Or={type:"childadded",child:null},go={type:"childremoved",child:null};class Bt extends Tr{constructor(){super(),this.isObject3D=!0,Object.defineProperty(this,"id",{value:up++}),this.uuid=Zs(),this.name="",this.type="Object3D",this.parent=null,this.children=[],this.up=Bt.DEFAULT_UP.clone();const e=new k,t=new fi,n=new ji,i=new k(1,1,1);function s(){n.setFromEuler(t,!1)}function a(){t.setFromQuaternion(n,void 0,!1)}t._onChange(s),n._onChange(a),Object.defineProperties(this,{position:{configurable:!0,enumerable:!0,value:e},rotation:{configurable:!0,enumerable:!0,value:t},quaternion:{configurable:!0,enumerable:!0,value:n},scale:{configurable:!0,enumerable:!0,value:i},modelViewMatrix:{value:new yt},normalMatrix:{value:new Ke}}),this.matrix=new yt,this.matrixWorld=new yt,this.matrixAutoUpdate=Bt.DEFAULT_MATRIX_AUTO_UPDATE,this.matrixWorldAutoUpdate=Bt.DEFAULT_MATRIX_WORLD_AUTO_UPDATE,this.matrixWorldNeedsUpdate=!1,this.layers=new xc,this.visible=!0,this.castShadow=!1,this.receiveShadow=!1,this.frustumCulled=!0,this.renderOrder=0,this.animations=[],this.customDepthMaterial=void 0,this.customDistanceMaterial=void 0,this.static=!1,this.userData={},this.pivot=null}onBeforeShadow(){}onAfterShadow(){}onBeforeRender(){}onAfterRender(){}applyMatrix4(e){this.matrixAutoUpdate&&this.updateMatrix(),this.matrix.premultiply(e),this.matrix.decompose(this.position,this.quaternion,this.scale)}applyQuaternion(e){return this.quaternion.premultiply(e),this}setRotationFromAxisAngle(e,t){this.quaternion.setFromAxisAngle(e,t)}setRotationFromEuler(e){this.quaternion.setFromEuler(e,!0)}setRotationFromMatrix(e){this.quaternion.setFromRotationMatrix(e)}setRotationFromQuaternion(e){this.quaternion.copy(e)}rotateOnAxis(e,t){return Fr.setFromAxisAngle(e,t),this.quaternion.multiply(Fr),this}rotateOnWorldAxis(e,t){return Fr.setFromAxisAngle(e,t),this.quaternion.premultiply(Fr),this}rotateX(e){return this.rotateOnAxis(au,e)}rotateY(e){return this.rotateOnAxis(ou,e)}rotateZ(e){return this.rotateOnAxis(lu,e)}translateOnAxis(e,t){return su.copy(e).applyQuaternion(this.quaternion),this.position.add(su.multiplyScalar(t)),this}translateX(e){return this.translateOnAxis(au,e)}translateY(e){return this.translateOnAxis(ou,e)}translateZ(e){return this.translateOnAxis(lu,e)}localToWorld(e){return this.updateWorldMatrix(!0,!1),e.applyMatrix4(this.matrixWorld)}worldToLocal(e){return this.updateWorldMatrix(!0,!1),e.applyMatrix4(_i.copy(this.matrixWorld).invert())}lookAt(e,t,n){e.isVector3?ra.copy(e):ra.set(e,t,n);const i=this.parent;this.updateWorldMatrix(!0,!1),ys.setFromMatrixPosition(this.matrixWorld),this.isCamera||this.isLight?_i.lookAt(ys,ra,this.up):_i.lookAt(ra,ys,this.up),this.quaternion.setFromRotationMatrix(_i),i&&(_i.extractRotation(i.matrixWorld),Fr.setFromRotationMatrix(_i),this.quaternion.premultiply(Fr.invert()))}add(e){if(arguments.length>1){for(let t=0;t<arguments.length;t++)this.add(arguments[t]);return this}return e===this?(lt("Object3D.add: object can't be added as a child of itself.",e),this):(e&&e.isObject3D?(e.removeFromParent(),e.parent=this,this.children.push(e),e.dispatchEvent(cu),Or.child=e,this.dispatchEvent(Or),Or.child=null):lt("Object3D.add: object not an instance of THREE.Object3D.",e),this)}remove(e){if(arguments.length>1){for(let n=0;n<arguments.length;n++)this.remove(arguments[n]);return this}const t=this.children.indexOf(e);return t!==-1&&(e.parent=null,this.children.splice(t,1),e.dispatchEvent(dp),go.child=e,this.dispatchEvent(go),go.child=null),this}removeFromParent(){const e=this.parent;return e!==null&&e.remove(this),this}clear(){return this.remove(...this.children)}attach(e){return this.updateWorldMatrix(!0,!1),_i.copy(this.matrixWorld).invert(),e.parent!==null&&(e.parent.updateWorldMatrix(!0,!1),_i.multiply(e.parent.matrixWorld)),e.applyMatrix4(_i),e.removeFromParent(),e.parent=this,this.children.push(e),e.updateWorldMatrix(!1,!0),e.dispatchEvent(cu),Or.child=e,this.dispatchEvent(Or),Or.child=null,this}getObjectById(e){return this.getObjectByProperty("id",e)}getObjectByName(e){return this.getObjectByProperty("name",e)}getObjectByProperty(e,t){if(this[e]===t)return this;for(let n=0,i=this.children.length;n<i;n++){const a=this.children[n].getObjectByProperty(e,t);if(a!==void 0)return a}}getObjectsByProperty(e,t,n=[]){this[e]===t&&n.push(this);const i=this.children;for(let s=0,a=i.length;s<a;s++)i[s].getObjectsByProperty(e,t,n);return n}getWorldPosition(e){return this.updateWorldMatrix(!0,!1),e.setFromMatrixPosition(this.matrixWorld)}getWorldQuaternion(e){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(ys,e,hp),e}getWorldScale(e){return this.updateWorldMatrix(!0,!1),this.matrixWorld.decompose(ys,fp,e),e}getWorldDirection(e){this.updateWorldMatrix(!0,!1);const t=this.matrixWorld.elements;return e.set(t[8],t[9],t[10]).normalize()}raycast(){}traverse(e){e(this);const t=this.children;for(let n=0,i=t.length;n<i;n++)t[n].traverse(e)}traverseVisible(e){if(this.visible===!1)return;e(this);const t=this.children;for(let n=0,i=t.length;n<i;n++)t[n].traverseVisible(e)}traverseAncestors(e){const t=this.parent;t!==null&&(e(t),t.traverseAncestors(e))}updateMatrix(){this.matrix.compose(this.position,this.quaternion,this.scale);const e=this.pivot;if(e!==null){const t=e.x,n=e.y,i=e.z,s=this.matrix.elements;s[12]+=t-s[0]*t-s[4]*n-s[8]*i,s[13]+=n-s[1]*t-s[5]*n-s[9]*i,s[14]+=i-s[2]*t-s[6]*n-s[10]*i}this.matrixWorldNeedsUpdate=!0}updateMatrixWorld(e){this.matrixAutoUpdate&&this.updateMatrix(),(this.matrixWorldNeedsUpdate||e)&&(this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),this.matrixWorldNeedsUpdate=!1,e=!0);const t=this.children;for(let n=0,i=t.length;n<i;n++)t[n].updateMatrixWorld(e)}updateWorldMatrix(e,t){const n=this.parent;if(e===!0&&n!==null&&n.updateWorldMatrix(!0,!1),this.matrixAutoUpdate&&this.updateMatrix(),this.matrixWorldAutoUpdate===!0&&(this.parent===null?this.matrixWorld.copy(this.matrix):this.matrixWorld.multiplyMatrices(this.parent.matrixWorld,this.matrix)),t===!0){const i=this.children;for(let s=0,a=i.length;s<a;s++)i[s].updateWorldMatrix(!1,!0)}}toJSON(e){const t=e===void 0||typeof e=="string",n={};t&&(e={geometries:{},materials:{},textures:{},images:{},shapes:{},skeletons:{},animations:{},nodes:{}},n.metadata={version:4.7,type:"Object",generator:"Object3D.toJSON"});const i={};i.uuid=this.uuid,i.type=this.type,this.name!==""&&(i.name=this.name),this.castShadow===!0&&(i.castShadow=!0),this.receiveShadow===!0&&(i.receiveShadow=!0),this.visible===!1&&(i.visible=!1),this.frustumCulled===!1&&(i.frustumCulled=!1),this.renderOrder!==0&&(i.renderOrder=this.renderOrder),this.static!==!1&&(i.static=this.static),Object.keys(this.userData).length>0&&(i.userData=this.userData),i.layers=this.layers.mask,i.matrix=this.matrix.toArray(),i.up=this.up.toArray(),this.pivot!==null&&(i.pivot=this.pivot.toArray()),this.matrixAutoUpdate===!1&&(i.matrixAutoUpdate=!1),this.morphTargetDictionary!==void 0&&(i.morphTargetDictionary=Object.assign({},this.morphTargetDictionary)),this.morphTargetInfluences!==void 0&&(i.morphTargetInfluences=this.morphTargetInfluences.slice()),this.isInstancedMesh&&(i.type="InstancedMesh",i.count=this.count,i.instanceMatrix=this.instanceMatrix.toJSON(),this.instanceColor!==null&&(i.instanceColor=this.instanceColor.toJSON())),this.isBatchedMesh&&(i.type="BatchedMesh",i.perObjectFrustumCulled=this.perObjectFrustumCulled,i.sortObjects=this.sortObjects,i.drawRanges=this._drawRanges,i.reservedRanges=this._reservedRanges,i.geometryInfo=this._geometryInfo.map(o=>({...o,boundingBox:o.boundingBox?o.boundingBox.toJSON():void 0,boundingSphere:o.boundingSphere?o.boundingSphere.toJSON():void 0})),i.instanceInfo=this._instanceInfo.map(o=>({...o})),i.availableInstanceIds=this._availableInstanceIds.slice(),i.availableGeometryIds=this._availableGeometryIds.slice(),i.nextIndexStart=this._nextIndexStart,i.nextVertexStart=this._nextVertexStart,i.geometryCount=this._geometryCount,i.maxInstanceCount=this._maxInstanceCount,i.maxVertexCount=this._maxVertexCount,i.maxIndexCount=this._maxIndexCount,i.geometryInitialized=this._geometryInitialized,i.matricesTexture=this._matricesTexture.toJSON(e),i.indirectTexture=this._indirectTexture.toJSON(e),this._colorsTexture!==null&&(i.colorsTexture=this._colorsTexture.toJSON(e)),this.boundingSphere!==null&&(i.boundingSphere=this.boundingSphere.toJSON()),this.boundingBox!==null&&(i.boundingBox=this.boundingBox.toJSON()));function s(o,c){return o[c.uuid]===void 0&&(o[c.uuid]=c.toJSON(e)),c.uuid}if(this.isScene)this.background&&(this.background.isColor?i.background=this.background.toJSON():this.background.isTexture&&(i.background=this.background.toJSON(e).uuid)),this.environment&&this.environment.isTexture&&this.environment.isRenderTargetTexture!==!0&&(i.environment=this.environment.toJSON(e).uuid);else if(this.isMesh||this.isLine||this.isPoints){i.geometry=s(e.geometries,this.geometry);const o=this.geometry.parameters;if(o!==void 0&&o.shapes!==void 0){const c=o.shapes;if(Array.isArray(c))for(let l=0,u=c.length;l<u;l++){const f=c[l];s(e.shapes,f)}else s(e.shapes,c)}}if(this.isSkinnedMesh&&(i.bindMode=this.bindMode,i.bindMatrix=this.bindMatrix.toArray(),this.skeleton!==void 0&&(s(e.skeletons,this.skeleton),i.skeleton=this.skeleton.uuid)),this.material!==void 0)if(Array.isArray(this.material)){const o=[];for(let c=0,l=this.material.length;c<l;c++)o.push(s(e.materials,this.material[c]));i.material=o}else i.material=s(e.materials,this.material);if(this.children.length>0){i.children=[];for(let o=0;o<this.children.length;o++)i.children.push(this.children[o].toJSON(e).object)}if(this.animations.length>0){i.animations=[];for(let o=0;o<this.animations.length;o++){const c=this.animations[o];i.animations.push(s(e.animations,c))}}if(t){const o=a(e.geometries),c=a(e.materials),l=a(e.textures),u=a(e.images),f=a(e.shapes),h=a(e.skeletons),m=a(e.animations),_=a(e.nodes);o.length>0&&(n.geometries=o),c.length>0&&(n.materials=c),l.length>0&&(n.textures=l),u.length>0&&(n.images=u),f.length>0&&(n.shapes=f),h.length>0&&(n.skeletons=h),m.length>0&&(n.animations=m),_.length>0&&(n.nodes=_)}return n.object=i,n;function a(o){const c=[];for(const l in o){const u=o[l];delete u.metadata,c.push(u)}return c}}clone(e){return new this.constructor().copy(this,e)}copy(e,t=!0){if(this.name=e.name,this.up.copy(e.up),this.position.copy(e.position),this.rotation.order=e.rotation.order,this.quaternion.copy(e.quaternion),this.scale.copy(e.scale),e.pivot!==null&&(this.pivot=e.pivot.clone()),this.matrix.copy(e.matrix),this.matrixWorld.copy(e.matrixWorld),this.matrixAutoUpdate=e.matrixAutoUpdate,this.matrixWorldAutoUpdate=e.matrixWorldAutoUpdate,this.matrixWorldNeedsUpdate=e.matrixWorldNeedsUpdate,this.layers.mask=e.layers.mask,this.visible=e.visible,this.castShadow=e.castShadow,this.receiveShadow=e.receiveShadow,this.frustumCulled=e.frustumCulled,this.renderOrder=e.renderOrder,this.static=e.static,this.animations=e.animations.slice(),this.userData=JSON.parse(JSON.stringify(e.userData)),t===!0)for(let n=0;n<e.children.length;n++){const i=e.children[n];this.add(i.clone())}return this}}Bt.DEFAULT_UP=new k(0,1,0);Bt.DEFAULT_MATRIX_AUTO_UPDATE=!0;Bt.DEFAULT_MATRIX_WORLD_AUTO_UPDATE=!0;class bi extends Bt{constructor(){super(),this.isGroup=!0,this.type="Group"}}const pp={type:"move"};class vo{constructor(){this._targetRay=null,this._grip=null,this._hand=null}getHandSpace(){return this._hand===null&&(this._hand=new bi,this._hand.matrixAutoUpdate=!1,this._hand.visible=!1,this._hand.joints={},this._hand.inputState={pinching:!1}),this._hand}getTargetRaySpace(){return this._targetRay===null&&(this._targetRay=new bi,this._targetRay.matrixAutoUpdate=!1,this._targetRay.visible=!1,this._targetRay.hasLinearVelocity=!1,this._targetRay.linearVelocity=new k,this._targetRay.hasAngularVelocity=!1,this._targetRay.angularVelocity=new k),this._targetRay}getGripSpace(){return this._grip===null&&(this._grip=new bi,this._grip.matrixAutoUpdate=!1,this._grip.visible=!1,this._grip.hasLinearVelocity=!1,this._grip.linearVelocity=new k,this._grip.hasAngularVelocity=!1,this._grip.angularVelocity=new k),this._grip}dispatchEvent(e){return this._targetRay!==null&&this._targetRay.dispatchEvent(e),this._grip!==null&&this._grip.dispatchEvent(e),this._hand!==null&&this._hand.dispatchEvent(e),this}connect(e){if(e&&e.hand){const t=this._hand;if(t)for(const n of e.hand.values())this._getHandJoint(t,n)}return this.dispatchEvent({type:"connected",data:e}),this}disconnect(e){return this.dispatchEvent({type:"disconnected",data:e}),this._targetRay!==null&&(this._targetRay.visible=!1),this._grip!==null&&(this._grip.visible=!1),this._hand!==null&&(this._hand.visible=!1),this}update(e,t,n){let i=null,s=null,a=null;const o=this._targetRay,c=this._grip,l=this._hand;if(e&&t.session.visibilityState!=="visible-blurred"){if(l&&e.hand){a=!0;for(const g of e.hand.values()){const d=t.getJointPose(g,n),p=this._getHandJoint(l,g);d!==null&&(p.matrix.fromArray(d.transform.matrix),p.matrix.decompose(p.position,p.rotation,p.scale),p.matrixWorldNeedsUpdate=!0,p.jointRadius=d.radius),p.visible=d!==null}const u=l.joints["index-finger-tip"],f=l.joints["thumb-tip"],h=u.position.distanceTo(f.position),m=.02,_=.005;l.inputState.pinching&&h>m+_?(l.inputState.pinching=!1,this.dispatchEvent({type:"pinchend",handedness:e.handedness,target:this})):!l.inputState.pinching&&h<=m-_&&(l.inputState.pinching=!0,this.dispatchEvent({type:"pinchstart",handedness:e.handedness,target:this}))}else c!==null&&e.gripSpace&&(s=t.getPose(e.gripSpace,n),s!==null&&(c.matrix.fromArray(s.transform.matrix),c.matrix.decompose(c.position,c.rotation,c.scale),c.matrixWorldNeedsUpdate=!0,s.linearVelocity?(c.hasLinearVelocity=!0,c.linearVelocity.copy(s.linearVelocity)):c.hasLinearVelocity=!1,s.angularVelocity?(c.hasAngularVelocity=!0,c.angularVelocity.copy(s.angularVelocity)):c.hasAngularVelocity=!1));o!==null&&(i=t.getPose(e.targetRaySpace,n),i===null&&s!==null&&(i=s),i!==null&&(o.matrix.fromArray(i.transform.matrix),o.matrix.decompose(o.position,o.rotation,o.scale),o.matrixWorldNeedsUpdate=!0,i.linearVelocity?(o.hasLinearVelocity=!0,o.linearVelocity.copy(i.linearVelocity)):o.hasLinearVelocity=!1,i.angularVelocity?(o.hasAngularVelocity=!0,o.angularVelocity.copy(i.angularVelocity)):o.hasAngularVelocity=!1,this.dispatchEvent(pp)))}return o!==null&&(o.visible=i!==null),c!==null&&(c.visible=s!==null),l!==null&&(l.visible=a!==null),this}_getHandJoint(e,t){if(e.joints[t.jointName]===void 0){const n=new bi;n.matrixAutoUpdate=!1,n.visible=!1,e.joints[t.jointName]=n,e.add(n)}return e.joints[t.jointName]}}const qh={aliceblue:15792383,antiquewhite:16444375,aqua:65535,aquamarine:8388564,azure:15794175,beige:16119260,bisque:16770244,black:0,blanchedalmond:16772045,blue:255,blueviolet:9055202,brown:10824234,burlywood:14596231,cadetblue:6266528,chartreuse:8388352,chocolate:13789470,coral:16744272,cornflowerblue:6591981,cornsilk:16775388,crimson:14423100,cyan:65535,darkblue:139,darkcyan:35723,darkgoldenrod:12092939,darkgray:11119017,darkgreen:25600,darkgrey:11119017,darkkhaki:12433259,darkmagenta:9109643,darkolivegreen:5597999,darkorange:16747520,darkorchid:10040012,darkred:9109504,darksalmon:15308410,darkseagreen:9419919,darkslateblue:4734347,darkslategray:3100495,darkslategrey:3100495,darkturquoise:52945,darkviolet:9699539,deeppink:16716947,deepskyblue:49151,dimgray:6908265,dimgrey:6908265,dodgerblue:2003199,firebrick:11674146,floralwhite:16775920,forestgreen:2263842,fuchsia:16711935,gainsboro:14474460,ghostwhite:16316671,gold:16766720,goldenrod:14329120,gray:8421504,green:32768,greenyellow:11403055,grey:8421504,honeydew:15794160,hotpink:16738740,indianred:13458524,indigo:4915330,ivory:16777200,khaki:15787660,lavender:15132410,lavenderblush:16773365,lawngreen:8190976,lemonchiffon:16775885,lightblue:11393254,lightcoral:15761536,lightcyan:14745599,lightgoldenrodyellow:16448210,lightgray:13882323,lightgreen:9498256,lightgrey:13882323,lightpink:16758465,lightsalmon:16752762,lightseagreen:2142890,lightskyblue:8900346,lightslategray:7833753,lightslategrey:7833753,lightsteelblue:11584734,lightyellow:16777184,lime:65280,limegreen:3329330,linen:16445670,magenta:16711935,maroon:8388608,mediumaquamarine:6737322,mediumblue:205,mediumorchid:12211667,mediumpurple:9662683,mediumseagreen:3978097,mediumslateblue:8087790,mediumspringgreen:64154,mediumturquoise:4772300,mediumvioletred:13047173,midnightblue:1644912,mintcream:16121850,mistyrose:16770273,moccasin:16770229,navajowhite:16768685,navy:128,oldlace:16643558,olive:8421376,olivedrab:7048739,orange:16753920,orangered:16729344,orchid:14315734,palegoldenrod:15657130,palegreen:10025880,paleturquoise:11529966,palevioletred:14381203,papayawhip:16773077,peachpuff:16767673,peru:13468991,pink:16761035,plum:14524637,powderblue:11591910,purple:8388736,rebeccapurple:6697881,red:16711680,rosybrown:12357519,royalblue:4286945,saddlebrown:9127187,salmon:16416882,sandybrown:16032864,seagreen:3050327,seashell:16774638,sienna:10506797,silver:12632256,skyblue:8900331,slateblue:6970061,slategray:7372944,slategrey:7372944,snow:16775930,springgreen:65407,steelblue:4620980,tan:13808780,teal:32896,thistle:14204888,tomato:16737095,turquoise:4251856,violet:15631086,wheat:16113331,white:16777215,whitesmoke:16119285,yellow:16776960,yellowgreen:10145074},Fi={h:0,s:0,l:0},sa={h:0,s:0,l:0};function xo(r,e,t){return t<0&&(t+=1),t>1&&(t-=1),t<1/6?r+(e-r)*6*t:t<1/2?e:t<2/3?r+(e-r)*6*(2/3-t):r}class rt{constructor(e,t,n){return this.isColor=!0,this.r=1,this.g=1,this.b=1,this.set(e,t,n)}set(e,t,n){if(t===void 0&&n===void 0){const i=e;i&&i.isColor?this.copy(i):typeof i=="number"?this.setHex(i):typeof i=="string"&&this.setStyle(i)}else this.setRGB(e,t,n);return this}setScalar(e){return this.r=e,this.g=e,this.b=e,this}setHex(e,t=An){return e=Math.floor(e),this.r=(e>>16&255)/255,this.g=(e>>8&255)/255,this.b=(e&255)/255,ct.colorSpaceToWorking(this,t),this}setRGB(e,t,n,i=ct.workingColorSpace){return this.r=e,this.g=t,this.b=n,ct.colorSpaceToWorking(this,i),this}setHSL(e,t,n,i=ct.workingColorSpace){if(e=ep(e,1),t=it(t,0,1),n=it(n,0,1),t===0)this.r=this.g=this.b=n;else{const s=n<=.5?n*(1+t):n+t-n*t,a=2*n-s;this.r=xo(a,s,e+1/3),this.g=xo(a,s,e),this.b=xo(a,s,e-1/3)}return ct.colorSpaceToWorking(this,i),this}setStyle(e,t=An){function n(s){s!==void 0&&parseFloat(s)<1&&Xe("Color: Alpha component of "+e+" will be ignored.")}let i;if(i=/^(\w+)\(([^\)]*)\)/.exec(e)){let s;const a=i[1],o=i[2];switch(a){case"rgb":case"rgba":if(s=/^\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setRGB(Math.min(255,parseInt(s[1],10))/255,Math.min(255,parseInt(s[2],10))/255,Math.min(255,parseInt(s[3],10))/255,t);if(s=/^\s*(\d+)\%\s*,\s*(\d+)\%\s*,\s*(\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setRGB(Math.min(100,parseInt(s[1],10))/100,Math.min(100,parseInt(s[2],10))/100,Math.min(100,parseInt(s[3],10))/100,t);break;case"hsl":case"hsla":if(s=/^\s*(\d*\.?\d+)\s*,\s*(\d*\.?\d+)\%\s*,\s*(\d*\.?\d+)\%\s*(?:,\s*(\d*\.?\d+)\s*)?$/.exec(o))return n(s[4]),this.setHSL(parseFloat(s[1])/360,parseFloat(s[2])/100,parseFloat(s[3])/100,t);break;default:Xe("Color: Unknown color model "+e)}}else if(i=/^\#([A-Fa-f\d]+)$/.exec(e)){const s=i[1],a=s.length;if(a===3)return this.setRGB(parseInt(s.charAt(0),16)/15,parseInt(s.charAt(1),16)/15,parseInt(s.charAt(2),16)/15,t);if(a===6)return this.setHex(parseInt(s,16),t);Xe("Color: Invalid hex color "+e)}else if(e&&e.length>0)return this.setColorName(e,t);return this}setColorName(e,t=An){const n=qh[e.toLowerCase()];return n!==void 0?this.setHex(n,t):Xe("Color: Unknown color "+e),this}clone(){return new this.constructor(this.r,this.g,this.b)}copy(e){return this.r=e.r,this.g=e.g,this.b=e.b,this}copySRGBToLinear(e){return this.r=Ai(e.r),this.g=Ai(e.g),this.b=Ai(e.b),this}copyLinearToSRGB(e){return this.r=ts(e.r),this.g=ts(e.g),this.b=ts(e.b),this}convertSRGBToLinear(){return this.copySRGBToLinear(this),this}convertLinearToSRGB(){return this.copyLinearToSRGB(this),this}getHex(e=An){return ct.workingToColorSpace(Qt.copy(this),e),Math.round(it(Qt.r*255,0,255))*65536+Math.round(it(Qt.g*255,0,255))*256+Math.round(it(Qt.b*255,0,255))}getHexString(e=An){return("000000"+this.getHex(e).toString(16)).slice(-6)}getHSL(e,t=ct.workingColorSpace){ct.workingToColorSpace(Qt.copy(this),t);const n=Qt.r,i=Qt.g,s=Qt.b,a=Math.max(n,i,s),o=Math.min(n,i,s);let c,l;const u=(o+a)/2;if(o===a)c=0,l=0;else{const f=a-o;switch(l=u<=.5?f/(a+o):f/(2-a-o),a){case n:c=(i-s)/f+(i<s?6:0);break;case i:c=(s-n)/f+2;break;case s:c=(n-i)/f+4;break}c/=6}return e.h=c,e.s=l,e.l=u,e}getRGB(e,t=ct.workingColorSpace){return ct.workingToColorSpace(Qt.copy(this),t),e.r=Qt.r,e.g=Qt.g,e.b=Qt.b,e}getStyle(e=An){ct.workingToColorSpace(Qt.copy(this),e);const t=Qt.r,n=Qt.g,i=Qt.b;return e!==An?`color(${e} ${t.toFixed(3)} ${n.toFixed(3)} ${i.toFixed(3)})`:`rgb(${Math.round(t*255)},${Math.round(n*255)},${Math.round(i*255)})`}offsetHSL(e,t,n){return this.getHSL(Fi),this.setHSL(Fi.h+e,Fi.s+t,Fi.l+n)}add(e){return this.r+=e.r,this.g+=e.g,this.b+=e.b,this}addColors(e,t){return this.r=e.r+t.r,this.g=e.g+t.g,this.b=e.b+t.b,this}addScalar(e){return this.r+=e,this.g+=e,this.b+=e,this}sub(e){return this.r=Math.max(0,this.r-e.r),this.g=Math.max(0,this.g-e.g),this.b=Math.max(0,this.b-e.b),this}multiply(e){return this.r*=e.r,this.g*=e.g,this.b*=e.b,this}multiplyScalar(e){return this.r*=e,this.g*=e,this.b*=e,this}lerp(e,t){return this.r+=(e.r-this.r)*t,this.g+=(e.g-this.g)*t,this.b+=(e.b-this.b)*t,this}lerpColors(e,t,n){return this.r=e.r+(t.r-e.r)*n,this.g=e.g+(t.g-e.g)*n,this.b=e.b+(t.b-e.b)*n,this}lerpHSL(e,t){this.getHSL(Fi),e.getHSL(sa);const n=ho(Fi.h,sa.h,t),i=ho(Fi.s,sa.s,t),s=ho(Fi.l,sa.l,t);return this.setHSL(n,i,s),this}setFromVector3(e){return this.r=e.x,this.g=e.y,this.b=e.z,this}applyMatrix3(e){const t=this.r,n=this.g,i=this.b,s=e.elements;return this.r=s[0]*t+s[3]*n+s[6]*i,this.g=s[1]*t+s[4]*n+s[7]*i,this.b=s[2]*t+s[5]*n+s[8]*i,this}equals(e){return e.r===this.r&&e.g===this.g&&e.b===this.b}fromArray(e,t=0){return this.r=e[t],this.g=e[t+1],this.b=e[t+2],this}toArray(e=[],t=0){return e[t]=this.r,e[t+1]=this.g,e[t+2]=this.b,e}fromBufferAttribute(e,t){return this.r=e.getX(t),this.g=e.getY(t),this.b=e.getZ(t),this}toJSON(){return this.getHex()}*[Symbol.iterator](){yield this.r,yield this.g,yield this.b}}const Qt=new rt;rt.NAMES=qh;class Mc{constructor(e,t=25e-5){this.isFogExp2=!0,this.name="",this.color=new rt(e),this.density=t}clone(){return new Mc(this.color,this.density)}toJSON(){return{type:"FogExp2",name:this.name,color:this.color.getHex(),density:this.density}}}class uu extends Bt{constructor(){super(),this.isScene=!0,this.type="Scene",this.background=null,this.environment=null,this.fog=null,this.backgroundBlurriness=0,this.backgroundIntensity=1,this.backgroundRotation=new fi,this.environmentIntensity=1,this.environmentRotation=new fi,this.overrideMaterial=null,typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}copy(e,t){return super.copy(e,t),e.background!==null&&(this.background=e.background.clone()),e.environment!==null&&(this.environment=e.environment.clone()),e.fog!==null&&(this.fog=e.fog.clone()),this.backgroundBlurriness=e.backgroundBlurriness,this.backgroundIntensity=e.backgroundIntensity,this.backgroundRotation.copy(e.backgroundRotation),this.environmentIntensity=e.environmentIntensity,this.environmentRotation.copy(e.environmentRotation),e.overrideMaterial!==null&&(this.overrideMaterial=e.overrideMaterial.clone()),this.matrixAutoUpdate=e.matrixAutoUpdate,this}toJSON(e){const t=super.toJSON(e);return this.fog!==null&&(t.object.fog=this.fog.toJSON()),this.backgroundBlurriness>0&&(t.object.backgroundBlurriness=this.backgroundBlurriness),this.backgroundIntensity!==1&&(t.object.backgroundIntensity=this.backgroundIntensity),t.object.backgroundRotation=this.backgroundRotation.toArray(),this.environmentIntensity!==1&&(t.object.environmentIntensity=this.environmentIntensity),t.object.environmentRotation=this.environmentRotation.toArray(),t}}const qn=new k,gi=new k,Mo=new k,vi=new k,Br=new k,kr=new k,hu=new k,So=new k,yo=new k,bo=new k,Eo=new Dt,To=new Dt,Ao=new Dt;class Bn{constructor(e=new k,t=new k,n=new k){this.a=e,this.b=t,this.c=n}static getNormal(e,t,n,i){i.subVectors(n,t),qn.subVectors(e,t),i.cross(qn);const s=i.lengthSq();return s>0?i.multiplyScalar(1/Math.sqrt(s)):i.set(0,0,0)}static getBarycoord(e,t,n,i,s){qn.subVectors(i,t),gi.subVectors(n,t),Mo.subVectors(e,t);const a=qn.dot(qn),o=qn.dot(gi),c=qn.dot(Mo),l=gi.dot(gi),u=gi.dot(Mo),f=a*l-o*o;if(f===0)return s.set(0,0,0),null;const h=1/f,m=(l*c-o*u)*h,_=(a*u-o*c)*h;return s.set(1-m-_,_,m)}static containsPoint(e,t,n,i){return this.getBarycoord(e,t,n,i,vi)===null?!1:vi.x>=0&&vi.y>=0&&vi.x+vi.y<=1}static getInterpolation(e,t,n,i,s,a,o,c){return this.getBarycoord(e,t,n,i,vi)===null?(c.x=0,c.y=0,"z"in c&&(c.z=0),"w"in c&&(c.w=0),null):(c.setScalar(0),c.addScaledVector(s,vi.x),c.addScaledVector(a,vi.y),c.addScaledVector(o,vi.z),c)}static getInterpolatedAttribute(e,t,n,i,s,a){return Eo.setScalar(0),To.setScalar(0),Ao.setScalar(0),Eo.fromBufferAttribute(e,t),To.fromBufferAttribute(e,n),Ao.fromBufferAttribute(e,i),a.setScalar(0),a.addScaledVector(Eo,s.x),a.addScaledVector(To,s.y),a.addScaledVector(Ao,s.z),a}static isFrontFacing(e,t,n,i){return qn.subVectors(n,t),gi.subVectors(e,t),qn.cross(gi).dot(i)<0}set(e,t,n){return this.a.copy(e),this.b.copy(t),this.c.copy(n),this}setFromPointsAndIndices(e,t,n,i){return this.a.copy(e[t]),this.b.copy(e[n]),this.c.copy(e[i]),this}setFromAttributeAndIndices(e,t,n,i){return this.a.fromBufferAttribute(e,t),this.b.fromBufferAttribute(e,n),this.c.fromBufferAttribute(e,i),this}clone(){return new this.constructor().copy(this)}copy(e){return this.a.copy(e.a),this.b.copy(e.b),this.c.copy(e.c),this}getArea(){return qn.subVectors(this.c,this.b),gi.subVectors(this.a,this.b),qn.cross(gi).length()*.5}getMidpoint(e){return e.addVectors(this.a,this.b).add(this.c).multiplyScalar(1/3)}getNormal(e){return Bn.getNormal(this.a,this.b,this.c,e)}getPlane(e){return e.setFromCoplanarPoints(this.a,this.b,this.c)}getBarycoord(e,t){return Bn.getBarycoord(e,this.a,this.b,this.c,t)}getInterpolation(e,t,n,i,s){return Bn.getInterpolation(e,this.a,this.b,this.c,t,n,i,s)}containsPoint(e){return Bn.containsPoint(e,this.a,this.b,this.c)}isFrontFacing(e){return Bn.isFrontFacing(this.a,this.b,this.c,e)}intersectsBox(e){return e.intersectsTriangle(this)}closestPointToPoint(e,t){const n=this.a,i=this.b,s=this.c;let a,o;Br.subVectors(i,n),kr.subVectors(s,n),So.subVectors(e,n);const c=Br.dot(So),l=kr.dot(So);if(c<=0&&l<=0)return t.copy(n);yo.subVectors(e,i);const u=Br.dot(yo),f=kr.dot(yo);if(u>=0&&f<=u)return t.copy(i);const h=c*f-u*l;if(h<=0&&c>=0&&u<=0)return a=c/(c-u),t.copy(n).addScaledVector(Br,a);bo.subVectors(e,s);const m=Br.dot(bo),_=kr.dot(bo);if(_>=0&&m<=_)return t.copy(s);const g=m*l-c*_;if(g<=0&&l>=0&&_<=0)return o=l/(l-_),t.copy(n).addScaledVector(kr,o);const d=u*_-m*f;if(d<=0&&f-u>=0&&m-_>=0)return hu.subVectors(s,i),o=(f-u)/(f-u+(m-_)),t.copy(i).addScaledVector(hu,o);const p=1/(d+g+h);return a=g*p,o=h*p,t.copy(n).addScaledVector(Br,a).addScaledVector(kr,o)}equals(e){return e.a.equals(this.a)&&e.b.equals(this.b)&&e.c.equals(this.c)}}class _s{constructor(e=new k(1/0,1/0,1/0),t=new k(-1/0,-1/0,-1/0)){this.isBox3=!0,this.min=e,this.max=t}set(e,t){return this.min.copy(e),this.max.copy(t),this}setFromArray(e){this.makeEmpty();for(let t=0,n=e.length;t<n;t+=3)this.expandByPoint(Yn.fromArray(e,t));return this}setFromBufferAttribute(e){this.makeEmpty();for(let t=0,n=e.count;t<n;t++)this.expandByPoint(Yn.fromBufferAttribute(e,t));return this}setFromPoints(e){this.makeEmpty();for(let t=0,n=e.length;t<n;t++)this.expandByPoint(e[t]);return this}setFromCenterAndSize(e,t){const n=Yn.copy(t).multiplyScalar(.5);return this.min.copy(e).sub(n),this.max.copy(e).add(n),this}setFromObject(e,t=!1){return this.makeEmpty(),this.expandByObject(e,t)}clone(){return new this.constructor().copy(this)}copy(e){return this.min.copy(e.min),this.max.copy(e.max),this}makeEmpty(){return this.min.x=this.min.y=this.min.z=1/0,this.max.x=this.max.y=this.max.z=-1/0,this}isEmpty(){return this.max.x<this.min.x||this.max.y<this.min.y||this.max.z<this.min.z}getCenter(e){return this.isEmpty()?e.set(0,0,0):e.addVectors(this.min,this.max).multiplyScalar(.5)}getSize(e){return this.isEmpty()?e.set(0,0,0):e.subVectors(this.max,this.min)}expandByPoint(e){return this.min.min(e),this.max.max(e),this}expandByVector(e){return this.min.sub(e),this.max.add(e),this}expandByScalar(e){return this.min.addScalar(-e),this.max.addScalar(e),this}expandByObject(e,t=!1){e.updateWorldMatrix(!1,!1);const n=e.geometry;if(n!==void 0){const s=n.getAttribute("position");if(t===!0&&s!==void 0&&e.isInstancedMesh!==!0)for(let a=0,o=s.count;a<o;a++)e.isMesh===!0?e.getVertexPosition(a,Yn):Yn.fromBufferAttribute(s,a),Yn.applyMatrix4(e.matrixWorld),this.expandByPoint(Yn);else e.boundingBox!==void 0?(e.boundingBox===null&&e.computeBoundingBox(),aa.copy(e.boundingBox)):(n.boundingBox===null&&n.computeBoundingBox(),aa.copy(n.boundingBox)),aa.applyMatrix4(e.matrixWorld),this.union(aa)}const i=e.children;for(let s=0,a=i.length;s<a;s++)this.expandByObject(i[s],t);return this}containsPoint(e){return e.x>=this.min.x&&e.x<=this.max.x&&e.y>=this.min.y&&e.y<=this.max.y&&e.z>=this.min.z&&e.z<=this.max.z}containsBox(e){return this.min.x<=e.min.x&&e.max.x<=this.max.x&&this.min.y<=e.min.y&&e.max.y<=this.max.y&&this.min.z<=e.min.z&&e.max.z<=this.max.z}getParameter(e,t){return t.set((e.x-this.min.x)/(this.max.x-this.min.x),(e.y-this.min.y)/(this.max.y-this.min.y),(e.z-this.min.z)/(this.max.z-this.min.z))}intersectsBox(e){return e.max.x>=this.min.x&&e.min.x<=this.max.x&&e.max.y>=this.min.y&&e.min.y<=this.max.y&&e.max.z>=this.min.z&&e.min.z<=this.max.z}intersectsSphere(e){return this.clampPoint(e.center,Yn),Yn.distanceToSquared(e.center)<=e.radius*e.radius}intersectsPlane(e){let t,n;return e.normal.x>0?(t=e.normal.x*this.min.x,n=e.normal.x*this.max.x):(t=e.normal.x*this.max.x,n=e.normal.x*this.min.x),e.normal.y>0?(t+=e.normal.y*this.min.y,n+=e.normal.y*this.max.y):(t+=e.normal.y*this.max.y,n+=e.normal.y*this.min.y),e.normal.z>0?(t+=e.normal.z*this.min.z,n+=e.normal.z*this.max.z):(t+=e.normal.z*this.max.z,n+=e.normal.z*this.min.z),t<=-e.constant&&n>=-e.constant}intersectsTriangle(e){if(this.isEmpty())return!1;this.getCenter(bs),oa.subVectors(this.max,bs),zr.subVectors(e.a,bs),Vr.subVectors(e.b,bs),Gr.subVectors(e.c,bs),Oi.subVectors(Vr,zr),Bi.subVectors(Gr,Vr),sr.subVectors(zr,Gr);let t=[0,-Oi.z,Oi.y,0,-Bi.z,Bi.y,0,-sr.z,sr.y,Oi.z,0,-Oi.x,Bi.z,0,-Bi.x,sr.z,0,-sr.x,-Oi.y,Oi.x,0,-Bi.y,Bi.x,0,-sr.y,sr.x,0];return!wo(t,zr,Vr,Gr,oa)||(t=[1,0,0,0,1,0,0,0,1],!wo(t,zr,Vr,Gr,oa))?!1:(la.crossVectors(Oi,Bi),t=[la.x,la.y,la.z],wo(t,zr,Vr,Gr,oa))}clampPoint(e,t){return t.copy(e).clamp(this.min,this.max)}distanceToPoint(e){return this.clampPoint(e,Yn).distanceTo(e)}getBoundingSphere(e){return this.isEmpty()?e.makeEmpty():(this.getCenter(e.center),e.radius=this.getSize(Yn).length()*.5),e}intersect(e){return this.min.max(e.min),this.max.min(e.max),this.isEmpty()&&this.makeEmpty(),this}union(e){return this.min.min(e.min),this.max.max(e.max),this}applyMatrix4(e){return this.isEmpty()?this:(xi[0].set(this.min.x,this.min.y,this.min.z).applyMatrix4(e),xi[1].set(this.min.x,this.min.y,this.max.z).applyMatrix4(e),xi[2].set(this.min.x,this.max.y,this.min.z).applyMatrix4(e),xi[3].set(this.min.x,this.max.y,this.max.z).applyMatrix4(e),xi[4].set(this.max.x,this.min.y,this.min.z).applyMatrix4(e),xi[5].set(this.max.x,this.min.y,this.max.z).applyMatrix4(e),xi[6].set(this.max.x,this.max.y,this.min.z).applyMatrix4(e),xi[7].set(this.max.x,this.max.y,this.max.z).applyMatrix4(e),this.setFromPoints(xi),this)}translate(e){return this.min.add(e),this.max.add(e),this}equals(e){return e.min.equals(this.min)&&e.max.equals(this.max)}toJSON(){return{min:this.min.toArray(),max:this.max.toArray()}}fromJSON(e){return this.min.fromArray(e.min),this.max.fromArray(e.max),this}}const xi=[new k,new k,new k,new k,new k,new k,new k,new k],Yn=new k,aa=new _s,zr=new k,Vr=new k,Gr=new k,Oi=new k,Bi=new k,sr=new k,bs=new k,oa=new k,la=new k,ar=new k;function wo(r,e,t,n,i){for(let s=0,a=r.length-3;s<=a;s+=3){ar.fromArray(r,s);const o=i.x*Math.abs(ar.x)+i.y*Math.abs(ar.y)+i.z*Math.abs(ar.z),c=e.dot(ar),l=t.dot(ar),u=n.dot(ar);if(Math.max(-Math.max(c,l,u),Math.min(c,l,u))>o)return!1}return!0}const Nt=new k,ca=new $e;let mp=0;class Zn{constructor(e,t,n=!1){if(Array.isArray(e))throw new TypeError("THREE.BufferAttribute: array should be a Typed Array.");this.isBufferAttribute=!0,Object.defineProperty(this,"id",{value:mp++}),this.name="",this.array=e,this.itemSize=t,this.count=e!==void 0?e.length/t:0,this.normalized=n,this.usage=Zc,this.updateRanges=[],this.gpuType=ai,this.version=0}onUploadCallback(){}set needsUpdate(e){e===!0&&this.version++}setUsage(e){return this.usage=e,this}addUpdateRange(e,t){this.updateRanges.push({start:e,count:t})}clearUpdateRanges(){this.updateRanges.length=0}copy(e){return this.name=e.name,this.array=new e.array.constructor(e.array),this.itemSize=e.itemSize,this.count=e.count,this.normalized=e.normalized,this.usage=e.usage,this.gpuType=e.gpuType,this}copyAt(e,t,n){e*=this.itemSize,n*=t.itemSize;for(let i=0,s=this.itemSize;i<s;i++)this.array[e+i]=t.array[n+i];return this}copyArray(e){return this.array.set(e),this}applyMatrix3(e){if(this.itemSize===2)for(let t=0,n=this.count;t<n;t++)ca.fromBufferAttribute(this,t),ca.applyMatrix3(e),this.setXY(t,ca.x,ca.y);else if(this.itemSize===3)for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.applyMatrix3(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}applyMatrix4(e){for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.applyMatrix4(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}applyNormalMatrix(e){for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.applyNormalMatrix(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}transformDirection(e){for(let t=0,n=this.count;t<n;t++)Nt.fromBufferAttribute(this,t),Nt.transformDirection(e),this.setXYZ(t,Nt.x,Nt.y,Nt.z);return this}set(e,t=0){return this.array.set(e,t),this}getComponent(e,t){let n=this.array[e*this.itemSize+t];return this.normalized&&(n=Ss(n,this.array)),n}setComponent(e,t,n){return this.normalized&&(n=un(n,this.array)),this.array[e*this.itemSize+t]=n,this}getX(e){let t=this.array[e*this.itemSize];return this.normalized&&(t=Ss(t,this.array)),t}setX(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize]=t,this}getY(e){let t=this.array[e*this.itemSize+1];return this.normalized&&(t=Ss(t,this.array)),t}setY(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize+1]=t,this}getZ(e){let t=this.array[e*this.itemSize+2];return this.normalized&&(t=Ss(t,this.array)),t}setZ(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize+2]=t,this}getW(e){let t=this.array[e*this.itemSize+3];return this.normalized&&(t=Ss(t,this.array)),t}setW(e,t){return this.normalized&&(t=un(t,this.array)),this.array[e*this.itemSize+3]=t,this}setXY(e,t,n){return e*=this.itemSize,this.normalized&&(t=un(t,this.array),n=un(n,this.array)),this.array[e+0]=t,this.array[e+1]=n,this}setXYZ(e,t,n,i){return e*=this.itemSize,this.normalized&&(t=un(t,this.array),n=un(n,this.array),i=un(i,this.array)),this.array[e+0]=t,this.array[e+1]=n,this.array[e+2]=i,this}setXYZW(e,t,n,i,s){return e*=this.itemSize,this.normalized&&(t=un(t,this.array),n=un(n,this.array),i=un(i,this.array),s=un(s,this.array)),this.array[e+0]=t,this.array[e+1]=n,this.array[e+2]=i,this.array[e+3]=s,this}onUpload(e){return this.onUploadCallback=e,this}clone(){return new this.constructor(this.array,this.itemSize).copy(this)}toJSON(){const e={itemSize:this.itemSize,type:this.array.constructor.name,array:Array.from(this.array),normalized:this.normalized};return this.name!==""&&(e.name=this.name),this.usage!==Zc&&(e.usage=this.usage),e}}class Yh extends Zn{constructor(e,t,n){super(new Uint16Array(e),t,n)}}class $h extends Zn{constructor(e,t,n){super(new Uint32Array(e),t,n)}}class Kt extends Zn{constructor(e,t,n){super(new Float32Array(e),t,n)}}const _p=new _s,Es=new k,Ro=new k;class js{constructor(e=new k,t=-1){this.isSphere=!0,this.center=e,this.radius=t}set(e,t){return this.center.copy(e),this.radius=t,this}setFromPoints(e,t){const n=this.center;t!==void 0?n.copy(t):_p.setFromPoints(e).getCenter(n);let i=0;for(let s=0,a=e.length;s<a;s++)i=Math.max(i,n.distanceToSquared(e[s]));return this.radius=Math.sqrt(i),this}copy(e){return this.center.copy(e.center),this.radius=e.radius,this}isEmpty(){return this.radius<0}makeEmpty(){return this.center.set(0,0,0),this.radius=-1,this}containsPoint(e){return e.distanceToSquared(this.center)<=this.radius*this.radius}distanceToPoint(e){return e.distanceTo(this.center)-this.radius}intersectsSphere(e){const t=this.radius+e.radius;return e.center.distanceToSquared(this.center)<=t*t}intersectsBox(e){return e.intersectsSphere(this)}intersectsPlane(e){return Math.abs(e.distanceToPoint(this.center))<=this.radius}clampPoint(e,t){const n=this.center.distanceToSquared(e);return t.copy(e),n>this.radius*this.radius&&(t.sub(this.center).normalize(),t.multiplyScalar(this.radius).add(this.center)),t}getBoundingBox(e){return this.isEmpty()?(e.makeEmpty(),e):(e.set(this.center,this.center),e.expandByScalar(this.radius),e)}applyMatrix4(e){return this.center.applyMatrix4(e),this.radius=this.radius*e.getMaxScaleOnAxis(),this}translate(e){return this.center.add(e),this}expandByPoint(e){if(this.isEmpty())return this.center.copy(e),this.radius=0,this;Es.subVectors(e,this.center);const t=Es.lengthSq();if(t>this.radius*this.radius){const n=Math.sqrt(t),i=(n-this.radius)*.5;this.center.addScaledVector(Es,i/n),this.radius+=i}return this}union(e){return e.isEmpty()?this:this.isEmpty()?(this.copy(e),this):(this.center.equals(e.center)===!0?this.radius=Math.max(this.radius,e.radius):(Ro.subVectors(e.center,this.center).setLength(e.radius),this.expandByPoint(Es.copy(e.center).add(Ro)),this.expandByPoint(Es.copy(e.center).sub(Ro))),this)}equals(e){return e.center.equals(this.center)&&e.radius===this.radius}clone(){return new this.constructor().copy(this)}toJSON(){return{radius:this.radius,center:this.center.toArray()}}fromJSON(e){return this.radius=e.radius,this.center.fromArray(e.center),this}}let gp=0;const Fn=new yt,Co=new Bt,Hr=new k,bn=new _s,Ts=new _s,Gt=new k;class cn extends Tr{constructor(){super(),this.isBufferGeometry=!0,Object.defineProperty(this,"id",{value:gp++}),this.uuid=Zs(),this.name="",this.type="BufferGeometry",this.index=null,this.indirect=null,this.indirectOffset=0,this.attributes={},this.morphAttributes={},this.morphTargetsRelative=!1,this.groups=[],this.boundingBox=null,this.boundingSphere=null,this.drawRange={start:0,count:1/0},this.userData={}}getIndex(){return this.index}setIndex(e){return Array.isArray(e)?this.index=new(Zd(e)?$h:Yh)(e,1):this.index=e,this}setIndirect(e,t=0){return this.indirect=e,this.indirectOffset=t,this}getIndirect(){return this.indirect}getAttribute(e){return this.attributes[e]}setAttribute(e,t){return this.attributes[e]=t,this}deleteAttribute(e){return delete this.attributes[e],this}hasAttribute(e){return this.attributes[e]!==void 0}addGroup(e,t,n=0){this.groups.push({start:e,count:t,materialIndex:n})}clearGroups(){this.groups=[]}setDrawRange(e,t){this.drawRange.start=e,this.drawRange.count=t}applyMatrix4(e){const t=this.attributes.position;t!==void 0&&(t.applyMatrix4(e),t.needsUpdate=!0);const n=this.attributes.normal;if(n!==void 0){const s=new Ke().getNormalMatrix(e);n.applyNormalMatrix(s),n.needsUpdate=!0}const i=this.attributes.tangent;return i!==void 0&&(i.transformDirection(e),i.needsUpdate=!0),this.boundingBox!==null&&this.computeBoundingBox(),this.boundingSphere!==null&&this.computeBoundingSphere(),this}applyQuaternion(e){return Fn.makeRotationFromQuaternion(e),this.applyMatrix4(Fn),this}rotateX(e){return Fn.makeRotationX(e),this.applyMatrix4(Fn),this}rotateY(e){return Fn.makeRotationY(e),this.applyMatrix4(Fn),this}rotateZ(e){return Fn.makeRotationZ(e),this.applyMatrix4(Fn),this}translate(e,t,n){return Fn.makeTranslation(e,t,n),this.applyMatrix4(Fn),this}scale(e,t,n){return Fn.makeScale(e,t,n),this.applyMatrix4(Fn),this}lookAt(e){return Co.lookAt(e),Co.updateMatrix(),this.applyMatrix4(Co.matrix),this}center(){return this.computeBoundingBox(),this.boundingBox.getCenter(Hr).negate(),this.translate(Hr.x,Hr.y,Hr.z),this}setFromPoints(e){const t=this.getAttribute("position");if(t===void 0){const n=[];for(let i=0,s=e.length;i<s;i++){const a=e[i];n.push(a.x,a.y,a.z||0)}this.setAttribute("position",new Kt(n,3))}else{const n=Math.min(e.length,t.count);for(let i=0;i<n;i++){const s=e[i];t.setXYZ(i,s.x,s.y,s.z||0)}e.length>t.count&&Xe("BufferGeometry: Buffer size too small for points data. Use .dispose() and create a new geometry."),t.needsUpdate=!0}return this}computeBoundingBox(){this.boundingBox===null&&(this.boundingBox=new _s);const e=this.attributes.position,t=this.morphAttributes.position;if(e&&e.isGLBufferAttribute){lt("BufferGeometry.computeBoundingBox(): GLBufferAttribute requires a manual bounding box.",this),this.boundingBox.set(new k(-1/0,-1/0,-1/0),new k(1/0,1/0,1/0));return}if(e!==void 0){if(this.boundingBox.setFromBufferAttribute(e),t)for(let n=0,i=t.length;n<i;n++){const s=t[n];bn.setFromBufferAttribute(s),this.morphTargetsRelative?(Gt.addVectors(this.boundingBox.min,bn.min),this.boundingBox.expandByPoint(Gt),Gt.addVectors(this.boundingBox.max,bn.max),this.boundingBox.expandByPoint(Gt)):(this.boundingBox.expandByPoint(bn.min),this.boundingBox.expandByPoint(bn.max))}}else this.boundingBox.makeEmpty();(isNaN(this.boundingBox.min.x)||isNaN(this.boundingBox.min.y)||isNaN(this.boundingBox.min.z))&&lt('BufferGeometry.computeBoundingBox(): Computed min/max have NaN values. The "position" attribute is likely to have NaN values.',this)}computeBoundingSphere(){this.boundingSphere===null&&(this.boundingSphere=new js);const e=this.attributes.position,t=this.morphAttributes.position;if(e&&e.isGLBufferAttribute){lt("BufferGeometry.computeBoundingSphere(): GLBufferAttribute requires a manual bounding sphere.",this),this.boundingSphere.set(new k,1/0);return}if(e){const n=this.boundingSphere.center;if(bn.setFromBufferAttribute(e),t)for(let s=0,a=t.length;s<a;s++){const o=t[s];Ts.setFromBufferAttribute(o),this.morphTargetsRelative?(Gt.addVectors(bn.min,Ts.min),bn.expandByPoint(Gt),Gt.addVectors(bn.max,Ts.max),bn.expandByPoint(Gt)):(bn.expandByPoint(Ts.min),bn.expandByPoint(Ts.max))}bn.getCenter(n);let i=0;for(let s=0,a=e.count;s<a;s++)Gt.fromBufferAttribute(e,s),i=Math.max(i,n.distanceToSquared(Gt));if(t)for(let s=0,a=t.length;s<a;s++){const o=t[s],c=this.morphTargetsRelative;for(let l=0,u=o.count;l<u;l++)Gt.fromBufferAttribute(o,l),c&&(Hr.fromBufferAttribute(e,l),Gt.add(Hr)),i=Math.max(i,n.distanceToSquared(Gt))}this.boundingSphere.radius=Math.sqrt(i),isNaN(this.boundingSphere.radius)&&lt('BufferGeometry.computeBoundingSphere(): Computed radius is NaN. The "position" attribute is likely to have NaN values.',this)}}computeTangents(){const e=this.index,t=this.attributes;if(e===null||t.position===void 0||t.normal===void 0||t.uv===void 0){lt("BufferGeometry: .computeTangents() failed. Missing required attributes (index, position, normal or uv)");return}const n=t.position,i=t.normal,s=t.uv;this.hasAttribute("tangent")===!1&&this.setAttribute("tangent",new Zn(new Float32Array(4*n.count),4));const a=this.getAttribute("tangent"),o=[],c=[];for(let x=0;x<n.count;x++)o[x]=new k,c[x]=new k;const l=new k,u=new k,f=new k,h=new $e,m=new $e,_=new $e,g=new k,d=new k;function p(x,S,W){l.fromBufferAttribute(n,x),u.fromBufferAttribute(n,S),f.fromBufferAttribute(n,W),h.fromBufferAttribute(s,x),m.fromBufferAttribute(s,S),_.fromBufferAttribute(s,W),u.sub(l),f.sub(l),m.sub(h),_.sub(h);const P=1/(m.x*_.y-_.x*m.y);isFinite(P)&&(g.copy(u).multiplyScalar(_.y).addScaledVector(f,-m.y).multiplyScalar(P),d.copy(f).multiplyScalar(m.x).addScaledVector(u,-_.x).multiplyScalar(P),o[x].add(g),o[S].add(g),o[W].add(g),c[x].add(d),c[S].add(d),c[W].add(d))}let M=this.groups;M.length===0&&(M=[{start:0,count:e.count}]);for(let x=0,S=M.length;x<S;++x){const W=M[x],P=W.start,z=W.count;for(let F=P,B=P+z;F<B;F+=3)p(e.getX(F+0),e.getX(F+1),e.getX(F+2))}const y=new k,b=new k,T=new k,A=new k;function R(x){T.fromBufferAttribute(i,x),A.copy(T);const S=o[x];y.copy(S),y.sub(T.multiplyScalar(T.dot(S))).normalize(),b.crossVectors(A,S);const P=b.dot(c[x])<0?-1:1;a.setXYZW(x,y.x,y.y,y.z,P)}for(let x=0,S=M.length;x<S;++x){const W=M[x],P=W.start,z=W.count;for(let F=P,B=P+z;F<B;F+=3)R(e.getX(F+0)),R(e.getX(F+1)),R(e.getX(F+2))}}computeVertexNormals(){const e=this.index,t=this.getAttribute("position");if(t!==void 0){let n=this.getAttribute("normal");if(n===void 0)n=new Zn(new Float32Array(t.count*3),3),this.setAttribute("normal",n);else for(let h=0,m=n.count;h<m;h++)n.setXYZ(h,0,0,0);const i=new k,s=new k,a=new k,o=new k,c=new k,l=new k,u=new k,f=new k;if(e)for(let h=0,m=e.count;h<m;h+=3){const _=e.getX(h+0),g=e.getX(h+1),d=e.getX(h+2);i.fromBufferAttribute(t,_),s.fromBufferAttribute(t,g),a.fromBufferAttribute(t,d),u.subVectors(a,s),f.subVectors(i,s),u.cross(f),o.fromBufferAttribute(n,_),c.fromBufferAttribute(n,g),l.fromBufferAttribute(n,d),o.add(u),c.add(u),l.add(u),n.setXYZ(_,o.x,o.y,o.z),n.setXYZ(g,c.x,c.y,c.z),n.setXYZ(d,l.x,l.y,l.z)}else for(let h=0,m=t.count;h<m;h+=3)i.fromBufferAttribute(t,h+0),s.fromBufferAttribute(t,h+1),a.fromBufferAttribute(t,h+2),u.subVectors(a,s),f.subVectors(i,s),u.cross(f),n.setXYZ(h+0,u.x,u.y,u.z),n.setXYZ(h+1,u.x,u.y,u.z),n.setXYZ(h+2,u.x,u.y,u.z);this.normalizeNormals(),n.needsUpdate=!0}}normalizeNormals(){const e=this.attributes.normal;for(let t=0,n=e.count;t<n;t++)Gt.fromBufferAttribute(e,t),Gt.normalize(),e.setXYZ(t,Gt.x,Gt.y,Gt.z)}toNonIndexed(){function e(o,c){const l=o.array,u=o.itemSize,f=o.normalized,h=new l.constructor(c.length*u);let m=0,_=0;for(let g=0,d=c.length;g<d;g++){o.isInterleavedBufferAttribute?m=c[g]*o.data.stride+o.offset:m=c[g]*u;for(let p=0;p<u;p++)h[_++]=l[m++]}return new Zn(h,u,f)}if(this.index===null)return Xe("BufferGeometry.toNonIndexed(): BufferGeometry is already non-indexed."),this;const t=new cn,n=this.index.array,i=this.attributes;for(const o in i){const c=i[o],l=e(c,n);t.setAttribute(o,l)}const s=this.morphAttributes;for(const o in s){const c=[],l=s[o];for(let u=0,f=l.length;u<f;u++){const h=l[u],m=e(h,n);c.push(m)}t.morphAttributes[o]=c}t.morphTargetsRelative=this.morphTargetsRelative;const a=this.groups;for(let o=0,c=a.length;o<c;o++){const l=a[o];t.addGroup(l.start,l.count,l.materialIndex)}return t}toJSON(){const e={metadata:{version:4.7,type:"BufferGeometry",generator:"BufferGeometry.toJSON"}};if(e.uuid=this.uuid,e.type=this.type,this.name!==""&&(e.name=this.name),Object.keys(this.userData).length>0&&(e.userData=this.userData),this.parameters!==void 0){const c=this.parameters;for(const l in c)c[l]!==void 0&&(e[l]=c[l]);return e}e.data={attributes:{}};const t=this.index;t!==null&&(e.data.index={type:t.array.constructor.name,array:Array.prototype.slice.call(t.array)});const n=this.attributes;for(const c in n){const l=n[c];e.data.attributes[c]=l.toJSON(e.data)}const i={};let s=!1;for(const c in this.morphAttributes){const l=this.morphAttributes[c],u=[];for(let f=0,h=l.length;f<h;f++){const m=l[f];u.push(m.toJSON(e.data))}u.length>0&&(i[c]=u,s=!0)}s&&(e.data.morphAttributes=i,e.data.morphTargetsRelative=this.morphTargetsRelative);const a=this.groups;a.length>0&&(e.data.groups=JSON.parse(JSON.stringify(a)));const o=this.boundingSphere;return o!==null&&(e.data.boundingSphere=o.toJSON()),e}clone(){return new this.constructor().copy(this)}copy(e){this.index=null,this.attributes={},this.morphAttributes={},this.groups=[],this.boundingBox=null,this.boundingSphere=null;const t={};this.name=e.name;const n=e.index;n!==null&&this.setIndex(n.clone());const i=e.attributes;for(const l in i){const u=i[l];this.setAttribute(l,u.clone(t))}const s=e.morphAttributes;for(const l in s){const u=[],f=s[l];for(let h=0,m=f.length;h<m;h++)u.push(f[h].clone(t));this.morphAttributes[l]=u}this.morphTargetsRelative=e.morphTargetsRelative;const a=e.groups;for(let l=0,u=a.length;l<u;l++){const f=a[l];this.addGroup(f.start,f.count,f.materialIndex)}const o=e.boundingBox;o!==null&&(this.boundingBox=o.clone());const c=e.boundingSphere;return c!==null&&(this.boundingSphere=c.clone()),this.drawRange.start=e.drawRange.start,this.drawRange.count=e.drawRange.count,this.userData=e.userData,this}dispose(){this.dispatchEvent({type:"dispose"})}}let vp=0;class Ar extends Tr{constructor(){super(),this.isMaterial=!0,Object.defineProperty(this,"id",{value:vp++}),this.uuid=Zs(),this.name="",this.type="Material",this.blending=es,this.side=Zi,this.vertexColors=!1,this.opacity=1,this.transparent=!1,this.alphaHash=!1,this.blendSrc=el,this.blendDst=tl,this.blendEquation=pr,this.blendSrcAlpha=null,this.blendDstAlpha=null,this.blendEquationAlpha=null,this.blendColor=new rt(0,0,0),this.blendAlpha=0,this.depthFunc=ss,this.depthTest=!0,this.depthWrite=!0,this.stencilWriteMask=255,this.stencilFunc=Kc,this.stencilRef=0,this.stencilFuncMask=255,this.stencilFail=Ir,this.stencilZFail=Ir,this.stencilZPass=Ir,this.stencilWrite=!1,this.clippingPlanes=null,this.clipIntersection=!1,this.clipShadows=!1,this.shadowSide=null,this.colorWrite=!0,this.precision=null,this.polygonOffset=!1,this.polygonOffsetFactor=0,this.polygonOffsetUnits=0,this.dithering=!1,this.alphaToCoverage=!1,this.premultipliedAlpha=!1,this.forceSinglePass=!1,this.allowOverride=!0,this.visible=!0,this.toneMapped=!0,this.userData={},this.version=0,this._alphaTest=0}get alphaTest(){return this._alphaTest}set alphaTest(e){this._alphaTest>0!=e>0&&this.version++,this._alphaTest=e}onBeforeRender(){}onBeforeCompile(){}customProgramCacheKey(){return this.onBeforeCompile.toString()}setValues(e){if(e!==void 0)for(const t in e){const n=e[t];if(n===void 0){Xe(`Material: parameter '${t}' has value of undefined.`);continue}const i=this[t];if(i===void 0){Xe(`Material: '${t}' is not a property of THREE.${this.type}.`);continue}i&&i.isColor?i.set(n):i&&i.isVector3&&n&&n.isVector3?i.copy(n):this[t]=n}}toJSON(e){const t=e===void 0||typeof e=="string";t&&(e={textures:{},images:{}});const n={metadata:{version:4.7,type:"Material",generator:"Material.toJSON"}};n.uuid=this.uuid,n.type=this.type,this.name!==""&&(n.name=this.name),this.color&&this.color.isColor&&(n.color=this.color.getHex()),this.roughness!==void 0&&(n.roughness=this.roughness),this.metalness!==void 0&&(n.metalness=this.metalness),this.sheen!==void 0&&(n.sheen=this.sheen),this.sheenColor&&this.sheenColor.isColor&&(n.sheenColor=this.sheenColor.getHex()),this.sheenRoughness!==void 0&&(n.sheenRoughness=this.sheenRoughness),this.emissive&&this.emissive.isColor&&(n.emissive=this.emissive.getHex()),this.emissiveIntensity!==void 0&&this.emissiveIntensity!==1&&(n.emissiveIntensity=this.emissiveIntensity),this.specular&&this.specular.isColor&&(n.specular=this.specular.getHex()),this.specularIntensity!==void 0&&(n.specularIntensity=this.specularIntensity),this.specularColor&&this.specularColor.isColor&&(n.specularColor=this.specularColor.getHex()),this.shininess!==void 0&&(n.shininess=this.shininess),this.clearcoat!==void 0&&(n.clearcoat=this.clearcoat),this.clearcoatRoughness!==void 0&&(n.clearcoatRoughness=this.clearcoatRoughness),this.clearcoatMap&&this.clearcoatMap.isTexture&&(n.clearcoatMap=this.clearcoatMap.toJSON(e).uuid),this.clearcoatRoughnessMap&&this.clearcoatRoughnessMap.isTexture&&(n.clearcoatRoughnessMap=this.clearcoatRoughnessMap.toJSON(e).uuid),this.clearcoatNormalMap&&this.clearcoatNormalMap.isTexture&&(n.clearcoatNormalMap=this.clearcoatNormalMap.toJSON(e).uuid,n.clearcoatNormalScale=this.clearcoatNormalScale.toArray()),this.sheenColorMap&&this.sheenColorMap.isTexture&&(n.sheenColorMap=this.sheenColorMap.toJSON(e).uuid),this.sheenRoughnessMap&&this.sheenRoughnessMap.isTexture&&(n.sheenRoughnessMap=this.sheenRoughnessMap.toJSON(e).uuid),this.dispersion!==void 0&&(n.dispersion=this.dispersion),this.iridescence!==void 0&&(n.iridescence=this.iridescence),this.iridescenceIOR!==void 0&&(n.iridescenceIOR=this.iridescenceIOR),this.iridescenceThicknessRange!==void 0&&(n.iridescenceThicknessRange=this.iridescenceThicknessRange),this.iridescenceMap&&this.iridescenceMap.isTexture&&(n.iridescenceMap=this.iridescenceMap.toJSON(e).uuid),this.iridescenceThicknessMap&&this.iridescenceThicknessMap.isTexture&&(n.iridescenceThicknessMap=this.iridescenceThicknessMap.toJSON(e).uuid),this.anisotropy!==void 0&&(n.anisotropy=this.anisotropy),this.anisotropyRotation!==void 0&&(n.anisotropyRotation=this.anisotropyRotation),this.anisotropyMap&&this.anisotropyMap.isTexture&&(n.anisotropyMap=this.anisotropyMap.toJSON(e).uuid),this.map&&this.map.isTexture&&(n.map=this.map.toJSON(e).uuid),this.matcap&&this.matcap.isTexture&&(n.matcap=this.matcap.toJSON(e).uuid),this.alphaMap&&this.alphaMap.isTexture&&(n.alphaMap=this.alphaMap.toJSON(e).uuid),this.lightMap&&this.lightMap.isTexture&&(n.lightMap=this.lightMap.toJSON(e).uuid,n.lightMapIntensity=this.lightMapIntensity),this.aoMap&&this.aoMap.isTexture&&(n.aoMap=this.aoMap.toJSON(e).uuid,n.aoMapIntensity=this.aoMapIntensity),this.bumpMap&&this.bumpMap.isTexture&&(n.bumpMap=this.bumpMap.toJSON(e).uuid,n.bumpScale=this.bumpScale),this.normalMap&&this.normalMap.isTexture&&(n.normalMap=this.normalMap.toJSON(e).uuid,n.normalMapType=this.normalMapType,n.normalScale=this.normalScale.toArray()),this.displacementMap&&this.displacementMap.isTexture&&(n.displacementMap=this.displacementMap.toJSON(e).uuid,n.displacementScale=this.displacementScale,n.displacementBias=this.displacementBias),this.roughnessMap&&this.roughnessMap.isTexture&&(n.roughnessMap=this.roughnessMap.toJSON(e).uuid),this.metalnessMap&&this.metalnessMap.isTexture&&(n.metalnessMap=this.metalnessMap.toJSON(e).uuid),this.emissiveMap&&this.emissiveMap.isTexture&&(n.emissiveMap=this.emissiveMap.toJSON(e).uuid),this.specularMap&&this.specularMap.isTexture&&(n.specularMap=this.specularMap.toJSON(e).uuid),this.specularIntensityMap&&this.specularIntensityMap.isTexture&&(n.specularIntensityMap=this.specularIntensityMap.toJSON(e).uuid),this.specularColorMap&&this.specularColorMap.isTexture&&(n.specularColorMap=this.specularColorMap.toJSON(e).uuid),this.envMap&&this.envMap.isTexture&&(n.envMap=this.envMap.toJSON(e).uuid,this.combine!==void 0&&(n.combine=this.combine)),this.envMapRotation!==void 0&&(n.envMapRotation=this.envMapRotation.toArray()),this.envMapIntensity!==void 0&&(n.envMapIntensity=this.envMapIntensity),this.reflectivity!==void 0&&(n.reflectivity=this.reflectivity),this.refractionRatio!==void 0&&(n.refractionRatio=this.refractionRatio),this.gradientMap&&this.gradientMap.isTexture&&(n.gradientMap=this.gradientMap.toJSON(e).uuid),this.transmission!==void 0&&(n.transmission=this.transmission),this.transmissionMap&&this.transmissionMap.isTexture&&(n.transmissionMap=this.transmissionMap.toJSON(e).uuid),this.thickness!==void 0&&(n.thickness=this.thickness),this.thicknessMap&&this.thicknessMap.isTexture&&(n.thicknessMap=this.thicknessMap.toJSON(e).uuid),this.attenuationDistance!==void 0&&this.attenuationDistance!==1/0&&(n.attenuationDistance=this.attenuationDistance),this.attenuationColor!==void 0&&(n.attenuationColor=this.attenuationColor.getHex()),this.size!==void 0&&(n.size=this.size),this.shadowSide!==null&&(n.shadowSide=this.shadowSide),this.sizeAttenuation!==void 0&&(n.sizeAttenuation=this.sizeAttenuation),this.blending!==es&&(n.blending=this.blending),this.side!==Zi&&(n.side=this.side),this.vertexColors===!0&&(n.vertexColors=!0),this.opacity<1&&(n.opacity=this.opacity),this.transparent===!0&&(n.transparent=!0),this.blendSrc!==el&&(n.blendSrc=this.blendSrc),this.blendDst!==tl&&(n.blendDst=this.blendDst),this.blendEquation!==pr&&(n.blendEquation=this.blendEquation),this.blendSrcAlpha!==null&&(n.blendSrcAlpha=this.blendSrcAlpha),this.blendDstAlpha!==null&&(n.blendDstAlpha=this.blendDstAlpha),this.blendEquationAlpha!==null&&(n.blendEquationAlpha=this.blendEquationAlpha),this.blendColor&&this.blendColor.isColor&&(n.blendColor=this.blendColor.getHex()),this.blendAlpha!==0&&(n.blendAlpha=this.blendAlpha),this.depthFunc!==ss&&(n.depthFunc=this.depthFunc),this.depthTest===!1&&(n.depthTest=this.depthTest),this.depthWrite===!1&&(n.depthWrite=this.depthWrite),this.colorWrite===!1&&(n.colorWrite=this.colorWrite),this.stencilWriteMask!==255&&(n.stencilWriteMask=this.stencilWriteMask),this.stencilFunc!==Kc&&(n.stencilFunc=this.stencilFunc),this.stencilRef!==0&&(n.stencilRef=this.stencilRef),this.stencilFuncMask!==255&&(n.stencilFuncMask=this.stencilFuncMask),this.stencilFail!==Ir&&(n.stencilFail=this.stencilFail),this.stencilZFail!==Ir&&(n.stencilZFail=this.stencilZFail),this.stencilZPass!==Ir&&(n.stencilZPass=this.stencilZPass),this.stencilWrite===!0&&(n.stencilWrite=this.stencilWrite),this.rotation!==void 0&&this.rotation!==0&&(n.rotation=this.rotation),this.polygonOffset===!0&&(n.polygonOffset=!0),this.polygonOffsetFactor!==0&&(n.polygonOffsetFactor=this.polygonOffsetFactor),this.polygonOffsetUnits!==0&&(n.polygonOffsetUnits=this.polygonOffsetUnits),this.linewidth!==void 0&&this.linewidth!==1&&(n.linewidth=this.linewidth),this.dashSize!==void 0&&(n.dashSize=this.dashSize),this.gapSize!==void 0&&(n.gapSize=this.gapSize),this.scale!==void 0&&(n.scale=this.scale),this.dithering===!0&&(n.dithering=!0),this.alphaTest>0&&(n.alphaTest=this.alphaTest),this.alphaHash===!0&&(n.alphaHash=!0),this.alphaToCoverage===!0&&(n.alphaToCoverage=!0),this.premultipliedAlpha===!0&&(n.premultipliedAlpha=!0),this.forceSinglePass===!0&&(n.forceSinglePass=!0),this.allowOverride===!1&&(n.allowOverride=!1),this.wireframe===!0&&(n.wireframe=!0),this.wireframeLinewidth>1&&(n.wireframeLinewidth=this.wireframeLinewidth),this.wireframeLinecap!=="round"&&(n.wireframeLinecap=this.wireframeLinecap),this.wireframeLinejoin!=="round"&&(n.wireframeLinejoin=this.wireframeLinejoin),this.flatShading===!0&&(n.flatShading=!0),this.visible===!1&&(n.visible=!1),this.toneMapped===!1&&(n.toneMapped=!1),this.fog===!1&&(n.fog=!1),Object.keys(this.userData).length>0&&(n.userData=this.userData);function i(s){const a=[];for(const o in s){const c=s[o];delete c.metadata,a.push(c)}return a}if(t){const s=i(e.textures),a=i(e.images);s.length>0&&(n.textures=s),a.length>0&&(n.images=a)}return n}clone(){return new this.constructor().copy(this)}copy(e){this.name=e.name,this.blending=e.blending,this.side=e.side,this.vertexColors=e.vertexColors,this.opacity=e.opacity,this.transparent=e.transparent,this.blendSrc=e.blendSrc,this.blendDst=e.blendDst,this.blendEquation=e.blendEquation,this.blendSrcAlpha=e.blendSrcAlpha,this.blendDstAlpha=e.blendDstAlpha,this.blendEquationAlpha=e.blendEquationAlpha,this.blendColor.copy(e.blendColor),this.blendAlpha=e.blendAlpha,this.depthFunc=e.depthFunc,this.depthTest=e.depthTest,this.depthWrite=e.depthWrite,this.stencilWriteMask=e.stencilWriteMask,this.stencilFunc=e.stencilFunc,this.stencilRef=e.stencilRef,this.stencilFuncMask=e.stencilFuncMask,this.stencilFail=e.stencilFail,this.stencilZFail=e.stencilZFail,this.stencilZPass=e.stencilZPass,this.stencilWrite=e.stencilWrite;const t=e.clippingPlanes;let n=null;if(t!==null){const i=t.length;n=new Array(i);for(let s=0;s!==i;++s)n[s]=t[s].clone()}return this.clippingPlanes=n,this.clipIntersection=e.clipIntersection,this.clipShadows=e.clipShadows,this.shadowSide=e.shadowSide,this.colorWrite=e.colorWrite,this.precision=e.precision,this.polygonOffset=e.polygonOffset,this.polygonOffsetFactor=e.polygonOffsetFactor,this.polygonOffsetUnits=e.polygonOffsetUnits,this.dithering=e.dithering,this.alphaTest=e.alphaTest,this.alphaHash=e.alphaHash,this.alphaToCoverage=e.alphaToCoverage,this.premultipliedAlpha=e.premultipliedAlpha,this.forceSinglePass=e.forceSinglePass,this.allowOverride=e.allowOverride,this.visible=e.visible,this.toneMapped=e.toneMapped,this.userData=JSON.parse(JSON.stringify(e.userData)),this}dispose(){this.dispatchEvent({type:"dispose"})}set needsUpdate(e){e===!0&&this.version++}}const Mi=new k,Po=new k,ua=new k,ki=new k,Do=new k,ha=new k,Lo=new k;class Js{constructor(e=new k,t=new k(0,0,-1)){this.origin=e,this.direction=t}set(e,t){return this.origin.copy(e),this.direction.copy(t),this}copy(e){return this.origin.copy(e.origin),this.direction.copy(e.direction),this}at(e,t){return t.copy(this.origin).addScaledVector(this.direction,e)}lookAt(e){return this.direction.copy(e).sub(this.origin).normalize(),this}recast(e){return this.origin.copy(this.at(e,Mi)),this}closestPointToPoint(e,t){t.subVectors(e,this.origin);const n=t.dot(this.direction);return n<0?t.copy(this.origin):t.copy(this.origin).addScaledVector(this.direction,n)}distanceToPoint(e){return Math.sqrt(this.distanceSqToPoint(e))}distanceSqToPoint(e){const t=Mi.subVectors(e,this.origin).dot(this.direction);return t<0?this.origin.distanceToSquared(e):(Mi.copy(this.origin).addScaledVector(this.direction,t),Mi.distanceToSquared(e))}distanceSqToSegment(e,t,n,i){Po.copy(e).add(t).multiplyScalar(.5),ua.copy(t).sub(e).normalize(),ki.copy(this.origin).sub(Po);const s=e.distanceTo(t)*.5,a=-this.direction.dot(ua),o=ki.dot(this.direction),c=-ki.dot(ua),l=ki.lengthSq(),u=Math.abs(1-a*a);let f,h,m,_;if(u>0)if(f=a*c-o,h=a*o-c,_=s*u,f>=0)if(h>=-_)if(h<=_){const g=1/u;f*=g,h*=g,m=f*(f+a*h+2*o)+h*(a*f+h+2*c)+l}else h=s,f=Math.max(0,-(a*h+o)),m=-f*f+h*(h+2*c)+l;else h=-s,f=Math.max(0,-(a*h+o)),m=-f*f+h*(h+2*c)+l;else h<=-_?(f=Math.max(0,-(-a*s+o)),h=f>0?-s:Math.min(Math.max(-s,-c),s),m=-f*f+h*(h+2*c)+l):h<=_?(f=0,h=Math.min(Math.max(-s,-c),s),m=h*(h+2*c)+l):(f=Math.max(0,-(a*s+o)),h=f>0?s:Math.min(Math.max(-s,-c),s),m=-f*f+h*(h+2*c)+l);else h=a>0?-s:s,f=Math.max(0,-(a*h+o)),m=-f*f+h*(h+2*c)+l;return n&&n.copy(this.origin).addScaledVector(this.direction,f),i&&i.copy(Po).addScaledVector(ua,h),m}intersectSphere(e,t){Mi.subVectors(e.center,this.origin);const n=Mi.dot(this.direction),i=Mi.dot(Mi)-n*n,s=e.radius*e.radius;if(i>s)return null;const a=Math.sqrt(s-i),o=n-a,c=n+a;return c<0?null:o<0?this.at(c,t):this.at(o,t)}intersectsSphere(e){return e.radius<0?!1:this.distanceSqToPoint(e.center)<=e.radius*e.radius}distanceToPlane(e){const t=e.normal.dot(this.direction);if(t===0)return e.distanceToPoint(this.origin)===0?0:null;const n=-(this.origin.dot(e.normal)+e.constant)/t;return n>=0?n:null}intersectPlane(e,t){const n=this.distanceToPlane(e);return n===null?null:this.at(n,t)}intersectsPlane(e){const t=e.distanceToPoint(this.origin);return t===0||e.normal.dot(this.direction)*t<0}intersectBox(e,t){let n,i,s,a,o,c;const l=1/this.direction.x,u=1/this.direction.y,f=1/this.direction.z,h=this.origin;return l>=0?(n=(e.min.x-h.x)*l,i=(e.max.x-h.x)*l):(n=(e.max.x-h.x)*l,i=(e.min.x-h.x)*l),u>=0?(s=(e.min.y-h.y)*u,a=(e.max.y-h.y)*u):(s=(e.max.y-h.y)*u,a=(e.min.y-h.y)*u),n>a||s>i||((s>n||isNaN(n))&&(n=s),(a<i||isNaN(i))&&(i=a),f>=0?(o=(e.min.z-h.z)*f,c=(e.max.z-h.z)*f):(o=(e.max.z-h.z)*f,c=(e.min.z-h.z)*f),n>c||o>i)||((o>n||n!==n)&&(n=o),(c<i||i!==i)&&(i=c),i<0)?null:this.at(n>=0?n:i,t)}intersectsBox(e){return this.intersectBox(e,Mi)!==null}intersectTriangle(e,t,n,i,s){Do.subVectors(t,e),ha.subVectors(n,e),Lo.crossVectors(Do,ha);let a=this.direction.dot(Lo),o;if(a>0){if(i)return null;o=1}else if(a<0)o=-1,a=-a;else return null;ki.subVectors(this.origin,e);const c=o*this.direction.dot(ha.crossVectors(ki,ha));if(c<0)return null;const l=o*this.direction.dot(Do.cross(ki));if(l<0||c+l>a)return null;const u=-o*ki.dot(Lo);return u<0?null:this.at(u/a,s)}applyMatrix4(e){return this.origin.applyMatrix4(e),this.direction.transformDirection(e),this}equals(e){return e.origin.equals(this.origin)&&e.direction.equals(this.direction)}clone(){return new this.constructor().copy(this)}}class qa extends Ar{constructor(e){super(),this.isMeshBasicMaterial=!0,this.type="MeshBasicMaterial",this.color=new rt(16777215),this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.specularMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new fi,this.combine=Rh,this.reflectivity=1,this.refractionRatio=.98,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.lightMap=e.lightMap,this.lightMapIntensity=e.lightMapIntensity,this.aoMap=e.aoMap,this.aoMapIntensity=e.aoMapIntensity,this.specularMap=e.specularMap,this.alphaMap=e.alphaMap,this.envMap=e.envMap,this.envMapRotation.copy(e.envMapRotation),this.combine=e.combine,this.reflectivity=e.reflectivity,this.refractionRatio=e.refractionRatio,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.wireframeLinecap=e.wireframeLinecap,this.wireframeLinejoin=e.wireframeLinejoin,this.fog=e.fog,this}}const fu=new yt,or=new Js,fa=new js,du=new k,da=new k,pa=new k,ma=new k,Io=new k,_a=new k,pu=new k,ga=new k;class Yt extends Bt{constructor(e=new cn,t=new qa){super(),this.isMesh=!0,this.type="Mesh",this.geometry=e,this.material=t,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.count=1,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),e.morphTargetInfluences!==void 0&&(this.morphTargetInfluences=e.morphTargetInfluences.slice()),e.morphTargetDictionary!==void 0&&(this.morphTargetDictionary=Object.assign({},e.morphTargetDictionary)),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const i=t[n[0]];if(i!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=i.length;s<a;s++){const o=i[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}getVertexPosition(e,t){const n=this.geometry,i=n.attributes.position,s=n.morphAttributes.position,a=n.morphTargetsRelative;t.fromBufferAttribute(i,e);const o=this.morphTargetInfluences;if(s&&o){_a.set(0,0,0);for(let c=0,l=s.length;c<l;c++){const u=o[c],f=s[c];u!==0&&(Io.fromBufferAttribute(f,e),a?_a.addScaledVector(Io,u):_a.addScaledVector(Io.sub(t),u))}t.add(_a)}return t}raycast(e,t){const n=this.geometry,i=this.material,s=this.matrixWorld;i!==void 0&&(n.boundingSphere===null&&n.computeBoundingSphere(),fa.copy(n.boundingSphere),fa.applyMatrix4(s),or.copy(e.ray).recast(e.near),!(fa.containsPoint(or.origin)===!1&&(or.intersectSphere(fa,du)===null||or.origin.distanceToSquared(du)>(e.far-e.near)**2))&&(fu.copy(s).invert(),or.copy(e.ray).applyMatrix4(fu),!(n.boundingBox!==null&&or.intersectsBox(n.boundingBox)===!1)&&this._computeIntersections(e,t,or)))}_computeIntersections(e,t,n){let i;const s=this.geometry,a=this.material,o=s.index,c=s.attributes.position,l=s.attributes.uv,u=s.attributes.uv1,f=s.attributes.normal,h=s.groups,m=s.drawRange;if(o!==null)if(Array.isArray(a))for(let _=0,g=h.length;_<g;_++){const d=h[_],p=a[d.materialIndex],M=Math.max(d.start,m.start),y=Math.min(o.count,Math.min(d.start+d.count,m.start+m.count));for(let b=M,T=y;b<T;b+=3){const A=o.getX(b),R=o.getX(b+1),x=o.getX(b+2);i=va(this,p,e,n,l,u,f,A,R,x),i&&(i.faceIndex=Math.floor(b/3),i.face.materialIndex=d.materialIndex,t.push(i))}}else{const _=Math.max(0,m.start),g=Math.min(o.count,m.start+m.count);for(let d=_,p=g;d<p;d+=3){const M=o.getX(d),y=o.getX(d+1),b=o.getX(d+2);i=va(this,a,e,n,l,u,f,M,y,b),i&&(i.faceIndex=Math.floor(d/3),t.push(i))}}else if(c!==void 0)if(Array.isArray(a))for(let _=0,g=h.length;_<g;_++){const d=h[_],p=a[d.materialIndex],M=Math.max(d.start,m.start),y=Math.min(c.count,Math.min(d.start+d.count,m.start+m.count));for(let b=M,T=y;b<T;b+=3){const A=b,R=b+1,x=b+2;i=va(this,p,e,n,l,u,f,A,R,x),i&&(i.faceIndex=Math.floor(b/3),i.face.materialIndex=d.materialIndex,t.push(i))}}else{const _=Math.max(0,m.start),g=Math.min(c.count,m.start+m.count);for(let d=_,p=g;d<p;d+=3){const M=d,y=d+1,b=d+2;i=va(this,a,e,n,l,u,f,M,y,b),i&&(i.faceIndex=Math.floor(d/3),t.push(i))}}}}function xp(r,e,t,n,i,s,a,o){let c;if(e.side===pn?c=n.intersectTriangle(a,s,i,!0,o):c=n.intersectTriangle(i,s,a,e.side===Zi,o),c===null)return null;ga.copy(o),ga.applyMatrix4(r.matrixWorld);const l=t.ray.origin.distanceTo(ga);return l<t.near||l>t.far?null:{distance:l,point:ga.clone(),object:r}}function va(r,e,t,n,i,s,a,o,c,l){r.getVertexPosition(o,da),r.getVertexPosition(c,pa),r.getVertexPosition(l,ma);const u=xp(r,e,t,n,da,pa,ma,pu);if(u){const f=new k;Bn.getBarycoord(pu,da,pa,ma,f),i&&(u.uv=Bn.getInterpolatedAttribute(i,o,c,l,f,new $e)),s&&(u.uv1=Bn.getInterpolatedAttribute(s,o,c,l,f,new $e)),a&&(u.normal=Bn.getInterpolatedAttribute(a,o,c,l,f,new k),u.normal.dot(n.direction)>0&&u.normal.multiplyScalar(-1));const h={a:o,b:c,c:l,normal:new k,materialIndex:0};Bn.getNormal(da,pa,ma,h.normal),u.face=h,u.barycoord=f}return u}class Mp extends ln{constructor(e=null,t=1,n=1,i,s,a,o,c,l=$t,u=$t,f,h){super(null,a,o,c,l,u,i,s,f,h),this.isDataTexture=!0,this.image={data:e,width:t,height:n},this.generateMipmaps=!1,this.flipY=!1,this.unpackAlignment=1}}const Uo=new k,Sp=new k,yp=new Ke;class Gi{constructor(e=new k(1,0,0),t=0){this.isPlane=!0,this.normal=e,this.constant=t}set(e,t){return this.normal.copy(e),this.constant=t,this}setComponents(e,t,n,i){return this.normal.set(e,t,n),this.constant=i,this}setFromNormalAndCoplanarPoint(e,t){return this.normal.copy(e),this.constant=-t.dot(this.normal),this}setFromCoplanarPoints(e,t,n){const i=Uo.subVectors(n,t).cross(Sp.subVectors(e,t)).normalize();return this.setFromNormalAndCoplanarPoint(i,e),this}copy(e){return this.normal.copy(e.normal),this.constant=e.constant,this}normalize(){const e=1/this.normal.length();return this.normal.multiplyScalar(e),this.constant*=e,this}negate(){return this.constant*=-1,this.normal.negate(),this}distanceToPoint(e){return this.normal.dot(e)+this.constant}distanceToSphere(e){return this.distanceToPoint(e.center)-e.radius}projectPoint(e,t){return t.copy(e).addScaledVector(this.normal,-this.distanceToPoint(e))}intersectLine(e,t){const n=e.delta(Uo),i=this.normal.dot(n);if(i===0)return this.distanceToPoint(e.start)===0?t.copy(e.start):null;const s=-(e.start.dot(this.normal)+this.constant)/i;return s<0||s>1?null:t.copy(e.start).addScaledVector(n,s)}intersectsLine(e){const t=this.distanceToPoint(e.start),n=this.distanceToPoint(e.end);return t<0&&n>0||n<0&&t>0}intersectsBox(e){return e.intersectsPlane(this)}intersectsSphere(e){return e.intersectsPlane(this)}coplanarPoint(e){return e.copy(this.normal).multiplyScalar(-this.constant)}applyMatrix4(e,t){const n=t||yp.getNormalMatrix(e),i=this.coplanarPoint(Uo).applyMatrix4(e),s=this.normal.applyMatrix3(n).normalize();return this.constant=-i.dot(s),this}translate(e){return this.constant-=e.dot(this.normal),this}equals(e){return e.normal.equals(this.normal)&&e.constant===this.constant}clone(){return new this.constructor().copy(this)}}const lr=new js,bp=new $e(.5,.5),xa=new k;class Sc{constructor(e=new Gi,t=new Gi,n=new Gi,i=new Gi,s=new Gi,a=new Gi){this.planes=[e,t,n,i,s,a]}set(e,t,n,i,s,a){const o=this.planes;return o[0].copy(e),o[1].copy(t),o[2].copy(n),o[3].copy(i),o[4].copy(s),o[5].copy(a),this}copy(e){const t=this.planes;for(let n=0;n<6;n++)t[n].copy(e.planes[n]);return this}setFromProjectionMatrix(e,t=oi,n=!1){const i=this.planes,s=e.elements,a=s[0],o=s[1],c=s[2],l=s[3],u=s[4],f=s[5],h=s[6],m=s[7],_=s[8],g=s[9],d=s[10],p=s[11],M=s[12],y=s[13],b=s[14],T=s[15];if(i[0].setComponents(l-a,m-u,p-_,T-M).normalize(),i[1].setComponents(l+a,m+u,p+_,T+M).normalize(),i[2].setComponents(l+o,m+f,p+g,T+y).normalize(),i[3].setComponents(l-o,m-f,p-g,T-y).normalize(),n)i[4].setComponents(c,h,d,b).normalize(),i[5].setComponents(l-c,m-h,p-d,T-b).normalize();else if(i[4].setComponents(l-c,m-h,p-d,T-b).normalize(),t===oi)i[5].setComponents(l+c,m+h,p+d,T+b).normalize();else if(t===Vs)i[5].setComponents(c,h,d,b).normalize();else throw new Error("THREE.Frustum.setFromProjectionMatrix(): Invalid coordinate system: "+t);return this}intersectsObject(e){if(e.boundingSphere!==void 0)e.boundingSphere===null&&e.computeBoundingSphere(),lr.copy(e.boundingSphere).applyMatrix4(e.matrixWorld);else{const t=e.geometry;t.boundingSphere===null&&t.computeBoundingSphere(),lr.copy(t.boundingSphere).applyMatrix4(e.matrixWorld)}return this.intersectsSphere(lr)}intersectsSprite(e){lr.center.set(0,0,0);const t=bp.distanceTo(e.center);return lr.radius=.7071067811865476+t,lr.applyMatrix4(e.matrixWorld),this.intersectsSphere(lr)}intersectsSphere(e){const t=this.planes,n=e.center,i=-e.radius;for(let s=0;s<6;s++)if(t[s].distanceToPoint(n)<i)return!1;return!0}intersectsBox(e){const t=this.planes;for(let n=0;n<6;n++){const i=t[n];if(xa.x=i.normal.x>0?e.max.x:e.min.x,xa.y=i.normal.y>0?e.max.y:e.min.y,xa.z=i.normal.z>0?e.max.z:e.min.z,i.distanceToPoint(xa)<0)return!1}return!0}containsPoint(e){const t=this.planes;for(let n=0;n<6;n++)if(t[n].distanceToPoint(e)<0)return!1;return!0}clone(){return new this.constructor().copy(this)}}class Ns extends Ar{constructor(e){super(),this.isLineBasicMaterial=!0,this.type="LineBasicMaterial",this.color=new rt(16777215),this.map=null,this.linewidth=1,this.linecap="round",this.linejoin="round",this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.linewidth=e.linewidth,this.linecap=e.linecap,this.linejoin=e.linejoin,this.fog=e.fog,this}}const Ya=new k,$a=new k,mu=new yt,As=new Js,Ma=new js,No=new k,_u=new k;class Ep extends Bt{constructor(e=new cn,t=new Ns){super(),this.isLine=!0,this.type="Line",this.geometry=e,this.material=t,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}computeLineDistances(){const e=this.geometry;if(e.index===null){const t=e.attributes.position,n=[0];for(let i=1,s=t.count;i<s;i++)Ya.fromBufferAttribute(t,i-1),$a.fromBufferAttribute(t,i),n[i]=n[i-1],n[i]+=Ya.distanceTo($a);e.setAttribute("lineDistance",new Kt(n,1))}else Xe("Line.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}raycast(e,t){const n=this.geometry,i=this.matrixWorld,s=e.params.Line.threshold,a=n.drawRange;if(n.boundingSphere===null&&n.computeBoundingSphere(),Ma.copy(n.boundingSphere),Ma.applyMatrix4(i),Ma.radius+=s,e.ray.intersectsSphere(Ma)===!1)return;mu.copy(i).invert(),As.copy(e.ray).applyMatrix4(mu);const o=s/((this.scale.x+this.scale.y+this.scale.z)/3),c=o*o,l=this.isLineSegments?2:1,u=n.index,h=n.attributes.position;if(u!==null){const m=Math.max(0,a.start),_=Math.min(u.count,a.start+a.count);for(let g=m,d=_-1;g<d;g+=l){const p=u.getX(g),M=u.getX(g+1),y=Sa(this,e,As,c,p,M,g);y&&t.push(y)}if(this.isLineLoop){const g=u.getX(_-1),d=u.getX(m),p=Sa(this,e,As,c,g,d,_-1);p&&t.push(p)}}else{const m=Math.max(0,a.start),_=Math.min(h.count,a.start+a.count);for(let g=m,d=_-1;g<d;g+=l){const p=Sa(this,e,As,c,g,g+1,g);p&&t.push(p)}if(this.isLineLoop){const g=Sa(this,e,As,c,_-1,m,_-1);g&&t.push(g)}}}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const i=t[n[0]];if(i!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=i.length;s<a;s++){const o=i[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}}function Sa(r,e,t,n,i,s,a){const o=r.geometry.attributes.position;if(Ya.fromBufferAttribute(o,i),$a.fromBufferAttribute(o,s),t.distanceSqToSegment(Ya,$a,No,_u)>n)return;No.applyMatrix4(r.matrixWorld);const l=e.ray.origin.distanceTo(No);if(!(l<e.near||l>e.far))return{distance:l,point:_u.clone().applyMatrix4(r.matrixWorld),index:a,face:null,faceIndex:null,barycoord:null,object:r}}const gu=new k,vu=new k;class Ba extends Ep{constructor(e,t){super(e,t),this.isLineSegments=!0,this.type="LineSegments"}computeLineDistances(){const e=this.geometry;if(e.index===null){const t=e.attributes.position,n=[];for(let i=0,s=t.count;i<s;i+=2)gu.fromBufferAttribute(t,i),vu.fromBufferAttribute(t,i+1),n[i]=i===0?0:n[i-1],n[i+1]=n[i]+gu.distanceTo(vu);e.setAttribute("lineDistance",new Kt(n,1))}else Xe("LineSegments.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");return this}}class Kh extends Ar{constructor(e){super(),this.isPointsMaterial=!0,this.type="PointsMaterial",this.color=new rt(16777215),this.map=null,this.alphaMap=null,this.size=1,this.sizeAttenuation=!0,this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.color.copy(e.color),this.map=e.map,this.alphaMap=e.alphaMap,this.size=e.size,this.sizeAttenuation=e.sizeAttenuation,this.fog=e.fog,this}}const xu=new yt,Wl=new Js,ya=new js,ba=new k;class Tp extends Bt{constructor(e=new cn,t=new Kh){super(),this.isPoints=!0,this.type="Points",this.geometry=e,this.material=t,this.morphTargetDictionary=void 0,this.morphTargetInfluences=void 0,this.updateMorphTargets()}copy(e,t){return super.copy(e,t),this.material=Array.isArray(e.material)?e.material.slice():e.material,this.geometry=e.geometry,this}raycast(e,t){const n=this.geometry,i=this.matrixWorld,s=e.params.Points.threshold,a=n.drawRange;if(n.boundingSphere===null&&n.computeBoundingSphere(),ya.copy(n.boundingSphere),ya.applyMatrix4(i),ya.radius+=s,e.ray.intersectsSphere(ya)===!1)return;xu.copy(i).invert(),Wl.copy(e.ray).applyMatrix4(xu);const o=s/((this.scale.x+this.scale.y+this.scale.z)/3),c=o*o,l=n.index,f=n.attributes.position;if(l!==null){const h=Math.max(0,a.start),m=Math.min(l.count,a.start+a.count);for(let _=h,g=m;_<g;_++){const d=l.getX(_);ba.fromBufferAttribute(f,d),Mu(ba,d,c,i,e,t,this)}}else{const h=Math.max(0,a.start),m=Math.min(f.count,a.start+a.count);for(let _=h,g=m;_<g;_++)ba.fromBufferAttribute(f,_),Mu(ba,_,c,i,e,t,this)}}updateMorphTargets(){const t=this.geometry.morphAttributes,n=Object.keys(t);if(n.length>0){const i=t[n[0]];if(i!==void 0){this.morphTargetInfluences=[],this.morphTargetDictionary={};for(let s=0,a=i.length;s<a;s++){const o=i[s].name||String(s);this.morphTargetInfluences.push(0),this.morphTargetDictionary[o]=s}}}}}function Mu(r,e,t,n,i,s,a){const o=Wl.distanceSqToPoint(r);if(o<t){const c=new k;Wl.closestPointToPoint(r,c),c.applyMatrix4(n);const l=i.ray.origin.distanceTo(c);if(l<i.near||l>i.far)return;s.push({distance:l,distanceToRay:Math.sqrt(o),point:c,index:e,face:null,faceIndex:null,barycoord:null,object:a})}}class Zh extends ln{constructor(e=[],t=Er,n,i,s,a,o,c,l,u){super(e,t,n,i,s,a,o,c,l,u),this.isCubeTexture=!0,this.flipY=!1}get images(){return this.image}set images(e){this.image=e}}class Gs extends ln{constructor(e,t,n=hi,i,s,a,o=$t,c=$t,l,u=Ri,f=1){if(u!==Ri&&u!==vr)throw new Error("DepthTexture format must be either THREE.DepthFormat or THREE.DepthStencilFormat");const h={width:e,height:t,depth:f};super(h,i,s,a,o,c,u,n,l),this.isDepthTexture=!0,this.flipY=!1,this.generateMipmaps=!1,this.compareFunction=null}copy(e){return super.copy(e),this.source=new vc(Object.assign({},e.image)),this.compareFunction=e.compareFunction,this}toJSON(e){const t=super.toJSON(e);return this.compareFunction!==null&&(t.compareFunction=this.compareFunction),t}}class Ap extends Gs{constructor(e,t=hi,n=Er,i,s,a=$t,o=$t,c,l=Ri){const u={width:e,height:e,depth:1},f=[u,u,u,u,u,u];super(e,e,t,n,i,s,a,o,c,l),this.image=f,this.isCubeDepthTexture=!0,this.isCubeTexture=!0}get images(){return this.image}set images(e){this.image=e}}class jh extends ln{constructor(e=null){super(),this.sourceTexture=e,this.isExternalTexture=!0}copy(e){return super.copy(e),this.sourceTexture=e.sourceTexture,this}}class dn extends cn{constructor(e=1,t=1,n=1,i=1,s=1,a=1){super(),this.type="BoxGeometry",this.parameters={width:e,height:t,depth:n,widthSegments:i,heightSegments:s,depthSegments:a};const o=this;i=Math.floor(i),s=Math.floor(s),a=Math.floor(a);const c=[],l=[],u=[],f=[];let h=0,m=0;_("z","y","x",-1,-1,n,t,e,a,s,0),_("z","y","x",1,-1,n,t,-e,a,s,1),_("x","z","y",1,1,e,n,t,i,a,2),_("x","z","y",1,-1,e,n,-t,i,a,3),_("x","y","z",1,-1,e,t,n,i,s,4),_("x","y","z",-1,-1,e,t,-n,i,s,5),this.setIndex(c),this.setAttribute("position",new Kt(l,3)),this.setAttribute("normal",new Kt(u,3)),this.setAttribute("uv",new Kt(f,2));function _(g,d,p,M,y,b,T,A,R,x,S){const W=b/R,P=T/x,z=b/2,F=T/2,B=A/2,O=R+1,V=x+1;let G=0,ne=0;const Q=new k;for(let me=0;me<V;me++){const be=me*P-F;for(let xe=0;xe<O;xe++){const Ge=xe*W-z;Q[g]=Ge*M,Q[d]=be*y,Q[p]=B,l.push(Q.x,Q.y,Q.z),Q[g]=0,Q[d]=0,Q[p]=A>0?1:-1,u.push(Q.x,Q.y,Q.z),f.push(xe/R),f.push(1-me/x),G+=1}}for(let me=0;me<x;me++)for(let be=0;be<R;be++){const xe=h+be+O*me,Ge=h+be+O*(me+1),qe=h+(be+1)+O*(me+1),je=h+(be+1)+O*me;c.push(xe,Ge,je),c.push(Ge,qe,je),ne+=6}o.addGroup(m,ne,S),m+=ne,h+=G}}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new dn(e.width,e.height,e.depth,e.widthSegments,e.heightSegments,e.depthSegments)}}const Ea=new k,Ta=new k,Fo=new k,Aa=new Bn;class Oo extends cn{constructor(e=null,t=1){if(super(),this.type="EdgesGeometry",this.parameters={geometry:e,thresholdAngle:t},e!==null){const i=Math.pow(10,4),s=Math.cos(Us*t),a=e.getIndex(),o=e.getAttribute("position"),c=a?a.count:o.count,l=[0,0,0],u=["a","b","c"],f=new Array(3),h={},m=[];for(let _=0;_<c;_+=3){a?(l[0]=a.getX(_),l[1]=a.getX(_+1),l[2]=a.getX(_+2)):(l[0]=_,l[1]=_+1,l[2]=_+2);const{a:g,b:d,c:p}=Aa;if(g.fromBufferAttribute(o,l[0]),d.fromBufferAttribute(o,l[1]),p.fromBufferAttribute(o,l[2]),Aa.getNormal(Fo),f[0]=`${Math.round(g.x*i)},${Math.round(g.y*i)},${Math.round(g.z*i)}`,f[1]=`${Math.round(d.x*i)},${Math.round(d.y*i)},${Math.round(d.z*i)}`,f[2]=`${Math.round(p.x*i)},${Math.round(p.y*i)},${Math.round(p.z*i)}`,!(f[0]===f[1]||f[1]===f[2]||f[2]===f[0]))for(let M=0;M<3;M++){const y=(M+1)%3,b=f[M],T=f[y],A=Aa[u[M]],R=Aa[u[y]],x=`${b}_${T}`,S=`${T}_${b}`;S in h&&h[S]?(Fo.dot(h[S].normal)<=s&&(m.push(A.x,A.y,A.z),m.push(R.x,R.y,R.z)),h[S]=null):x in h||(h[x]={index0:l[M],index1:l[y],normal:Fo.clone()})}}for(const _ in h)if(h[_]){const{index0:g,index1:d}=h[_];Ea.fromBufferAttribute(o,g),Ta.fromBufferAttribute(o,d),m.push(Ea.x,Ea.y,Ea.z),m.push(Ta.x,Ta.y,Ta.z)}this.setAttribute("position",new Kt(m,3))}}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}}class Qs extends cn{constructor(e=1,t=1,n=1,i=1){super(),this.type="PlaneGeometry",this.parameters={width:e,height:t,widthSegments:n,heightSegments:i};const s=e/2,a=t/2,o=Math.floor(n),c=Math.floor(i),l=o+1,u=c+1,f=e/o,h=t/c,m=[],_=[],g=[],d=[];for(let p=0;p<u;p++){const M=p*h-a;for(let y=0;y<l;y++){const b=y*f-s;_.push(b,-M,0),g.push(0,0,1),d.push(y/o),d.push(1-p/c)}}for(let p=0;p<c;p++)for(let M=0;M<o;M++){const y=M+l*p,b=M+l*(p+1),T=M+1+l*(p+1),A=M+1+l*p;m.push(y,b,A),m.push(b,T,A)}this.setIndex(m),this.setAttribute("position",new Kt(_,3)),this.setAttribute("normal",new Kt(g,3)),this.setAttribute("uv",new Kt(d,2))}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new Qs(e.width,e.height,e.widthSegments,e.heightSegments)}}class yc extends cn{constructor(e=.5,t=1,n=32,i=1,s=0,a=Math.PI*2){super(),this.type="RingGeometry",this.parameters={innerRadius:e,outerRadius:t,thetaSegments:n,phiSegments:i,thetaStart:s,thetaLength:a},n=Math.max(3,n),i=Math.max(1,i);const o=[],c=[],l=[],u=[];let f=e;const h=(t-e)/i,m=new k,_=new $e;for(let g=0;g<=i;g++){for(let d=0;d<=n;d++){const p=s+d/n*a;m.x=f*Math.cos(p),m.y=f*Math.sin(p),c.push(m.x,m.y,m.z),l.push(0,0,1),_.x=(m.x/t+1)/2,_.y=(m.y/t+1)/2,u.push(_.x,_.y)}f+=h}for(let g=0;g<i;g++){const d=g*(n+1);for(let p=0;p<n;p++){const M=p+d,y=M,b=M+n+1,T=M+n+2,A=M+1;o.push(y,b,A),o.push(b,T,A)}}this.setIndex(o),this.setAttribute("position",new Kt(c,3)),this.setAttribute("normal",new Kt(l,3)),this.setAttribute("uv",new Kt(u,2))}copy(e){return super.copy(e),this.parameters=Object.assign({},e.parameters),this}static fromJSON(e){return new yc(e.innerRadius,e.outerRadius,e.thetaSegments,e.phiSegments,e.thetaStart,e.thetaLength)}}function cs(r){const e={};for(const t in r){e[t]={};for(const n in r[t]){const i=r[t][n];i&&(i.isColor||i.isMatrix3||i.isMatrix4||i.isVector2||i.isVector3||i.isVector4||i.isTexture||i.isQuaternion)?i.isRenderTargetTexture?(Xe("UniformsUtils: Textures of render targets cannot be cloned via cloneUniforms() or mergeUniforms()."),e[t][n]=null):e[t][n]=i.clone():Array.isArray(i)?e[t][n]=i.slice():e[t][n]=i}}return e}function an(r){const e={};for(let t=0;t<r.length;t++){const n=cs(r[t]);for(const i in n)e[i]=n[i]}return e}function wp(r){const e=[];for(let t=0;t<r.length;t++)e.push(r[t].clone());return e}function Jh(r){const e=r.getRenderTarget();return e===null?r.outputColorSpace:e.isXRRenderTarget===!0?e.texture.colorSpace:ct.workingColorSpace}const Rp={clone:cs,merge:an};var Cp=`void main() {
	gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
}`,Pp=`void main() {
	gl_FragColor = vec4( 1.0, 0.0, 0.0, 1.0 );
}`;class di extends Ar{constructor(e){super(),this.isShaderMaterial=!0,this.type="ShaderMaterial",this.defines={},this.uniforms={},this.uniformsGroups=[],this.vertexShader=Cp,this.fragmentShader=Pp,this.linewidth=1,this.wireframe=!1,this.wireframeLinewidth=1,this.fog=!1,this.lights=!1,this.clipping=!1,this.forceSinglePass=!0,this.extensions={clipCullDistance:!1,multiDraw:!1},this.defaultAttributeValues={color:[1,1,1],uv:[0,0],uv1:[0,0]},this.index0AttributeName=void 0,this.uniformsNeedUpdate=!1,this.glslVersion=null,e!==void 0&&this.setValues(e)}copy(e){return super.copy(e),this.fragmentShader=e.fragmentShader,this.vertexShader=e.vertexShader,this.uniforms=cs(e.uniforms),this.uniformsGroups=wp(e.uniformsGroups),this.defines=Object.assign({},e.defines),this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.fog=e.fog,this.lights=e.lights,this.clipping=e.clipping,this.extensions=Object.assign({},e.extensions),this.glslVersion=e.glslVersion,this.defaultAttributeValues=Object.assign({},e.defaultAttributeValues),this.index0AttributeName=e.index0AttributeName,this.uniformsNeedUpdate=e.uniformsNeedUpdate,this}toJSON(e){const t=super.toJSON(e);t.glslVersion=this.glslVersion,t.uniforms={};for(const i in this.uniforms){const a=this.uniforms[i].value;a&&a.isTexture?t.uniforms[i]={type:"t",value:a.toJSON(e).uuid}:a&&a.isColor?t.uniforms[i]={type:"c",value:a.getHex()}:a&&a.isVector2?t.uniforms[i]={type:"v2",value:a.toArray()}:a&&a.isVector3?t.uniforms[i]={type:"v3",value:a.toArray()}:a&&a.isVector4?t.uniforms[i]={type:"v4",value:a.toArray()}:a&&a.isMatrix3?t.uniforms[i]={type:"m3",value:a.toArray()}:a&&a.isMatrix4?t.uniforms[i]={type:"m4",value:a.toArray()}:t.uniforms[i]={value:a}}Object.keys(this.defines).length>0&&(t.defines=this.defines),t.vertexShader=this.vertexShader,t.fragmentShader=this.fragmentShader,t.lights=this.lights,t.clipping=this.clipping;const n={};for(const i in this.extensions)this.extensions[i]===!0&&(n[i]=!0);return Object.keys(n).length>0&&(t.extensions=n),t}}class Dp extends di{constructor(e){super(e),this.isRawShaderMaterial=!0,this.type="RawShaderMaterial"}}class En extends Ar{constructor(e){super(),this.isMeshStandardMaterial=!0,this.type="MeshStandardMaterial",this.defines={STANDARD:""},this.color=new rt(16777215),this.roughness=1,this.metalness=0,this.map=null,this.lightMap=null,this.lightMapIntensity=1,this.aoMap=null,this.aoMapIntensity=1,this.emissive=new rt(0),this.emissiveIntensity=1,this.emissiveMap=null,this.bumpMap=null,this.bumpScale=1,this.normalMap=null,this.normalMapType=Hh,this.normalScale=new $e(1,1),this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.roughnessMap=null,this.metalnessMap=null,this.alphaMap=null,this.envMap=null,this.envMapRotation=new fi,this.envMapIntensity=1,this.wireframe=!1,this.wireframeLinewidth=1,this.wireframeLinecap="round",this.wireframeLinejoin="round",this.flatShading=!1,this.fog=!0,this.setValues(e)}copy(e){return super.copy(e),this.defines={STANDARD:""},this.color.copy(e.color),this.roughness=e.roughness,this.metalness=e.metalness,this.map=e.map,this.lightMap=e.lightMap,this.lightMapIntensity=e.lightMapIntensity,this.aoMap=e.aoMap,this.aoMapIntensity=e.aoMapIntensity,this.emissive.copy(e.emissive),this.emissiveMap=e.emissiveMap,this.emissiveIntensity=e.emissiveIntensity,this.bumpMap=e.bumpMap,this.bumpScale=e.bumpScale,this.normalMap=e.normalMap,this.normalMapType=e.normalMapType,this.normalScale.copy(e.normalScale),this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this.roughnessMap=e.roughnessMap,this.metalnessMap=e.metalnessMap,this.alphaMap=e.alphaMap,this.envMap=e.envMap,this.envMapRotation.copy(e.envMapRotation),this.envMapIntensity=e.envMapIntensity,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this.wireframeLinecap=e.wireframeLinecap,this.wireframeLinejoin=e.wireframeLinejoin,this.flatShading=e.flatShading,this.fog=e.fog,this}}class Lp extends Ar{constructor(e){super(),this.isMeshDepthMaterial=!0,this.type="MeshDepthMaterial",this.depthPacking=Gd,this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.wireframe=!1,this.wireframeLinewidth=1,this.setValues(e)}copy(e){return super.copy(e),this.depthPacking=e.depthPacking,this.map=e.map,this.alphaMap=e.alphaMap,this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this.wireframe=e.wireframe,this.wireframeLinewidth=e.wireframeLinewidth,this}}class Ip extends Ar{constructor(e){super(),this.isMeshDistanceMaterial=!0,this.type="MeshDistanceMaterial",this.map=null,this.alphaMap=null,this.displacementMap=null,this.displacementScale=1,this.displacementBias=0,this.setValues(e)}copy(e){return super.copy(e),this.map=e.map,this.alphaMap=e.alphaMap,this.displacementMap=e.displacementMap,this.displacementScale=e.displacementScale,this.displacementBias=e.displacementBias,this}}class bc extends Bt{constructor(e,t=1){super(),this.isLight=!0,this.type="Light",this.color=new rt(e),this.intensity=t}dispose(){this.dispatchEvent({type:"dispose"})}copy(e,t){return super.copy(e,t),this.color.copy(e.color),this.intensity=e.intensity,this}toJSON(e){const t=super.toJSON(e);return t.object.color=this.color.getHex(),t.object.intensity=this.intensity,t}}const Bo=new yt,Su=new k,yu=new k;class Qh{constructor(e){this.camera=e,this.intensity=1,this.bias=0,this.biasNode=null,this.normalBias=0,this.radius=1,this.blurSamples=8,this.mapSize=new $e(512,512),this.mapType=Cn,this.map=null,this.mapPass=null,this.matrix=new yt,this.autoUpdate=!0,this.needsUpdate=!1,this._frustum=new Sc,this._frameExtents=new $e(1,1),this._viewportCount=1,this._viewports=[new Dt(0,0,1,1)]}getViewportCount(){return this._viewportCount}getFrustum(){return this._frustum}updateMatrices(e){const t=this.camera,n=this.matrix;Su.setFromMatrixPosition(e.matrixWorld),t.position.copy(Su),yu.setFromMatrixPosition(e.target.matrixWorld),t.lookAt(yu),t.updateMatrixWorld(),Bo.multiplyMatrices(t.projectionMatrix,t.matrixWorldInverse),this._frustum.setFromProjectionMatrix(Bo,t.coordinateSystem,t.reversedDepth),t.coordinateSystem===Vs||t.reversedDepth?n.set(.5,0,0,.5,0,.5,0,.5,0,0,1,0,0,0,0,1):n.set(.5,0,0,.5,0,.5,0,.5,0,0,.5,.5,0,0,0,1),n.multiply(Bo)}getViewport(e){return this._viewports[e]}getFrameExtents(){return this._frameExtents}dispose(){this.map&&this.map.dispose(),this.mapPass&&this.mapPass.dispose()}copy(e){return this.camera=e.camera.clone(),this.intensity=e.intensity,this.bias=e.bias,this.radius=e.radius,this.autoUpdate=e.autoUpdate,this.needsUpdate=e.needsUpdate,this.normalBias=e.normalBias,this.blurSamples=e.blurSamples,this.mapSize.copy(e.mapSize),this.biasNode=e.biasNode,this}clone(){return new this.constructor().copy(this)}toJSON(){const e={};return this.intensity!==1&&(e.intensity=this.intensity),this.bias!==0&&(e.bias=this.bias),this.normalBias!==0&&(e.normalBias=this.normalBias),this.radius!==1&&(e.radius=this.radius),(this.mapSize.x!==512||this.mapSize.y!==512)&&(e.mapSize=this.mapSize.toArray()),e.camera=this.camera.toJSON(!1).object,delete e.camera.matrix,e}}const wa=new k,Ra=new ji,ti=new k;class ef extends Bt{constructor(){super(),this.isCamera=!0,this.type="Camera",this.matrixWorldInverse=new yt,this.projectionMatrix=new yt,this.projectionMatrixInverse=new yt,this.coordinateSystem=oi,this._reversedDepth=!1}get reversedDepth(){return this._reversedDepth}copy(e,t){return super.copy(e,t),this.matrixWorldInverse.copy(e.matrixWorldInverse),this.projectionMatrix.copy(e.projectionMatrix),this.projectionMatrixInverse.copy(e.projectionMatrixInverse),this.coordinateSystem=e.coordinateSystem,this}getWorldDirection(e){return super.getWorldDirection(e).negate()}updateMatrixWorld(e){super.updateMatrixWorld(e),this.matrixWorld.decompose(wa,Ra,ti),ti.x===1&&ti.y===1&&ti.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(wa,Ra,ti.set(1,1,1)).invert()}updateWorldMatrix(e,t){super.updateWorldMatrix(e,t),this.matrixWorld.decompose(wa,Ra,ti),ti.x===1&&ti.y===1&&ti.z===1?this.matrixWorldInverse.copy(this.matrixWorld).invert():this.matrixWorldInverse.compose(wa,Ra,ti.set(1,1,1)).invert()}clone(){return new this.constructor().copy(this)}}const zi=new k,bu=new $e,Eu=new $e;class Rn extends ef{constructor(e=50,t=1,n=.1,i=2e3){super(),this.isPerspectiveCamera=!0,this.type="PerspectiveCamera",this.fov=e,this.zoom=1,this.near=n,this.far=i,this.focus=10,this.aspect=t,this.view=null,this.filmGauge=35,this.filmOffset=0,this.updateProjectionMatrix()}copy(e,t){return super.copy(e,t),this.fov=e.fov,this.zoom=e.zoom,this.near=e.near,this.far=e.far,this.focus=e.focus,this.aspect=e.aspect,this.view=e.view===null?null:Object.assign({},e.view),this.filmGauge=e.filmGauge,this.filmOffset=e.filmOffset,this}setFocalLength(e){const t=.5*this.getFilmHeight()/e;this.fov=Hl*2*Math.atan(t),this.updateProjectionMatrix()}getFocalLength(){const e=Math.tan(Us*.5*this.fov);return .5*this.getFilmHeight()/e}getEffectiveFOV(){return Hl*2*Math.atan(Math.tan(Us*.5*this.fov)/this.zoom)}getFilmWidth(){return this.filmGauge*Math.min(this.aspect,1)}getFilmHeight(){return this.filmGauge/Math.max(this.aspect,1)}getViewBounds(e,t,n){zi.set(-1,-1,.5).applyMatrix4(this.projectionMatrixInverse),t.set(zi.x,zi.y).multiplyScalar(-e/zi.z),zi.set(1,1,.5).applyMatrix4(this.projectionMatrixInverse),n.set(zi.x,zi.y).multiplyScalar(-e/zi.z)}getViewSize(e,t){return this.getViewBounds(e,bu,Eu),t.subVectors(Eu,bu)}setViewOffset(e,t,n,i,s,a){this.aspect=e/t,this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=e,this.view.fullHeight=t,this.view.offsetX=n,this.view.offsetY=i,this.view.width=s,this.view.height=a,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const e=this.near;let t=e*Math.tan(Us*.5*this.fov)/this.zoom,n=2*t,i=this.aspect*n,s=-.5*i;const a=this.view;if(this.view!==null&&this.view.enabled){const c=a.fullWidth,l=a.fullHeight;s+=a.offsetX*i/c,t-=a.offsetY*n/l,i*=a.width/c,n*=a.height/l}const o=this.filmOffset;o!==0&&(s+=e*o/this.getFilmWidth()),this.projectionMatrix.makePerspective(s,s+i,t,t-n,e,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(e){const t=super.toJSON(e);return t.object.fov=this.fov,t.object.zoom=this.zoom,t.object.near=this.near,t.object.far=this.far,t.object.focus=this.focus,t.object.aspect=this.aspect,this.view!==null&&(t.object.view=Object.assign({},this.view)),t.object.filmGauge=this.filmGauge,t.object.filmOffset=this.filmOffset,t}}class Up extends Qh{constructor(){super(new Rn(90,1,.5,500)),this.isPointLightShadow=!0}}class Np extends bc{constructor(e,t,n=0,i=2){super(e,t),this.isPointLight=!0,this.type="PointLight",this.distance=n,this.decay=i,this.shadow=new Up}get power(){return this.intensity*4*Math.PI}set power(e){this.intensity=e/(4*Math.PI)}dispose(){super.dispose(),this.shadow.dispose()}copy(e,t){return super.copy(e,t),this.distance=e.distance,this.decay=e.decay,this.shadow=e.shadow.clone(),this}toJSON(e){const t=super.toJSON(e);return t.object.distance=this.distance,t.object.decay=this.decay,t.object.shadow=this.shadow.toJSON(),t}}class Ec extends ef{constructor(e=-1,t=1,n=1,i=-1,s=.1,a=2e3){super(),this.isOrthographicCamera=!0,this.type="OrthographicCamera",this.zoom=1,this.view=null,this.left=e,this.right=t,this.top=n,this.bottom=i,this.near=s,this.far=a,this.updateProjectionMatrix()}copy(e,t){return super.copy(e,t),this.left=e.left,this.right=e.right,this.top=e.top,this.bottom=e.bottom,this.near=e.near,this.far=e.far,this.zoom=e.zoom,this.view=e.view===null?null:Object.assign({},e.view),this}setViewOffset(e,t,n,i,s,a){this.view===null&&(this.view={enabled:!0,fullWidth:1,fullHeight:1,offsetX:0,offsetY:0,width:1,height:1}),this.view.enabled=!0,this.view.fullWidth=e,this.view.fullHeight=t,this.view.offsetX=n,this.view.offsetY=i,this.view.width=s,this.view.height=a,this.updateProjectionMatrix()}clearViewOffset(){this.view!==null&&(this.view.enabled=!1),this.updateProjectionMatrix()}updateProjectionMatrix(){const e=(this.right-this.left)/(2*this.zoom),t=(this.top-this.bottom)/(2*this.zoom),n=(this.right+this.left)/2,i=(this.top+this.bottom)/2;let s=n-e,a=n+e,o=i+t,c=i-t;if(this.view!==null&&this.view.enabled){const l=(this.right-this.left)/this.view.fullWidth/this.zoom,u=(this.top-this.bottom)/this.view.fullHeight/this.zoom;s+=l*this.view.offsetX,a=s+l*this.view.width,o-=u*this.view.offsetY,c=o-u*this.view.height}this.projectionMatrix.makeOrthographic(s,a,o,c,this.near,this.far,this.coordinateSystem,this.reversedDepth),this.projectionMatrixInverse.copy(this.projectionMatrix).invert()}toJSON(e){const t=super.toJSON(e);return t.object.zoom=this.zoom,t.object.left=this.left,t.object.right=this.right,t.object.top=this.top,t.object.bottom=this.bottom,t.object.near=this.near,t.object.far=this.far,this.view!==null&&(t.object.view=Object.assign({},this.view)),t}}class Fp extends Qh{constructor(){super(new Ec(-5,5,5,-5,.5,500)),this.isDirectionalLightShadow=!0}}class Tu extends bc{constructor(e,t){super(e,t),this.isDirectionalLight=!0,this.type="DirectionalLight",this.position.copy(Bt.DEFAULT_UP),this.updateMatrix(),this.target=new Bt,this.shadow=new Fp}dispose(){super.dispose(),this.shadow.dispose()}copy(e){return super.copy(e),this.target=e.target.clone(),this.shadow=e.shadow.clone(),this}toJSON(e){const t=super.toJSON(e);return t.object.shadow=this.shadow.toJSON(),t.object.target=this.target.uuid,t}}class Op extends bc{constructor(e,t){super(e,t),this.isAmbientLight=!0,this.type="AmbientLight"}}const Wr=-90,Xr=1;class Bp extends Bt{constructor(e,t,n){super(),this.type="CubeCamera",this.renderTarget=n,this.coordinateSystem=null,this.activeMipmapLevel=0;const i=new Rn(Wr,Xr,e,t);i.layers=this.layers,this.add(i);const s=new Rn(Wr,Xr,e,t);s.layers=this.layers,this.add(s);const a=new Rn(Wr,Xr,e,t);a.layers=this.layers,this.add(a);const o=new Rn(Wr,Xr,e,t);o.layers=this.layers,this.add(o);const c=new Rn(Wr,Xr,e,t);c.layers=this.layers,this.add(c);const l=new Rn(Wr,Xr,e,t);l.layers=this.layers,this.add(l)}updateCoordinateSystem(){const e=this.coordinateSystem,t=this.children.concat(),[n,i,s,a,o,c]=t;for(const l of t)this.remove(l);if(e===oi)n.up.set(0,1,0),n.lookAt(1,0,0),i.up.set(0,1,0),i.lookAt(-1,0,0),s.up.set(0,0,-1),s.lookAt(0,1,0),a.up.set(0,0,1),a.lookAt(0,-1,0),o.up.set(0,1,0),o.lookAt(0,0,1),c.up.set(0,1,0),c.lookAt(0,0,-1);else if(e===Vs)n.up.set(0,-1,0),n.lookAt(-1,0,0),i.up.set(0,-1,0),i.lookAt(1,0,0),s.up.set(0,0,1),s.lookAt(0,1,0),a.up.set(0,0,-1),a.lookAt(0,-1,0),o.up.set(0,-1,0),o.lookAt(0,0,1),c.up.set(0,-1,0),c.lookAt(0,0,-1);else throw new Error("THREE.CubeCamera.updateCoordinateSystem(): Invalid coordinate system: "+e);for(const l of t)this.add(l),l.updateMatrixWorld()}update(e,t){this.parent===null&&this.updateMatrixWorld();const{renderTarget:n,activeMipmapLevel:i}=this;this.coordinateSystem!==e.coordinateSystem&&(this.coordinateSystem=e.coordinateSystem,this.updateCoordinateSystem());const[s,a,o,c,l,u]=this.children,f=e.getRenderTarget(),h=e.getActiveCubeFace(),m=e.getActiveMipmapLevel(),_=e.xr.enabled;e.xr.enabled=!1;const g=n.texture.generateMipmaps;n.texture.generateMipmaps=!1;let d=!1;e.isWebGLRenderer===!0?d=e.state.buffers.depth.getReversed():d=e.reversedDepthBuffer,e.setRenderTarget(n,0,i),d&&e.autoClear===!1&&e.clearDepth(),e.render(t,s),e.setRenderTarget(n,1,i),d&&e.autoClear===!1&&e.clearDepth(),e.render(t,a),e.setRenderTarget(n,2,i),d&&e.autoClear===!1&&e.clearDepth(),e.render(t,o),e.setRenderTarget(n,3,i),d&&e.autoClear===!1&&e.clearDepth(),e.render(t,c),e.setRenderTarget(n,4,i),d&&e.autoClear===!1&&e.clearDepth(),e.render(t,l),n.texture.generateMipmaps=g,e.setRenderTarget(n,5,i),d&&e.autoClear===!1&&e.clearDepth(),e.render(t,u),e.setRenderTarget(f,h,m),e.xr.enabled=_,n.texture.needsPMREMUpdate=!0}}class kp extends Rn{constructor(e=[]){super(),this.isArrayCamera=!0,this.isMultiViewCamera=!1,this.cameras=e}}const Au=new yt;class zp{constructor(e,t,n=0,i=1/0){this.ray=new Js(e,t),this.near=n,this.far=i,this.camera=null,this.layers=new xc,this.params={Mesh:{},Line:{threshold:1},LOD:{},Points:{threshold:1},Sprite:{}}}set(e,t){this.ray.set(e,t)}setFromCamera(e,t){t.isPerspectiveCamera?(this.ray.origin.setFromMatrixPosition(t.matrixWorld),this.ray.direction.set(e.x,e.y,.5).unproject(t).sub(this.ray.origin).normalize(),this.camera=t):t.isOrthographicCamera?(this.ray.origin.set(e.x,e.y,(t.near+t.far)/(t.near-t.far)).unproject(t),this.ray.direction.set(0,0,-1).transformDirection(t.matrixWorld),this.camera=t):lt("Raycaster: Unsupported camera type: "+t.type)}setFromXRController(e){return Au.identity().extractRotation(e.matrixWorld),this.ray.origin.setFromMatrixPosition(e.matrixWorld),this.ray.direction.set(0,0,-1).applyMatrix4(Au),this}intersectObject(e,t=!0,n=[]){return Xl(e,this,n,t),n.sort(wu),n}intersectObjects(e,t=!0,n=[]){for(let i=0,s=e.length;i<s;i++)Xl(e[i],this,n,t);return n.sort(wu),n}}function wu(r,e){return r.distance-e.distance}function Xl(r,e,t,n){let i=!0;if(r.layers.test(e.layers)&&r.raycast(e,t)===!1&&(i=!1),i===!0&&n===!0){const s=r.children;for(let a=0,o=s.length;a<o;a++)Xl(s[a],e,t,!0)}}class Ru{constructor(e=1,t=0,n=0){this.radius=e,this.phi=t,this.theta=n}set(e,t,n){return this.radius=e,this.phi=t,this.theta=n,this}copy(e){return this.radius=e.radius,this.phi=e.phi,this.theta=e.theta,this}makeSafe(){return this.phi=it(this.phi,1e-6,Math.PI-1e-6),this}setFromVector3(e){return this.setFromCartesianCoords(e.x,e.y,e.z)}setFromCartesianCoords(e,t,n){return this.radius=Math.sqrt(e*e+t*t+n*n),this.radius===0?(this.theta=0,this.phi=0):(this.theta=Math.atan2(e,n),this.phi=Math.acos(it(t/this.radius,-1,1))),this}clone(){return new this.constructor().copy(this)}}class Vp extends Ba{constructor(e=10,t=10,n=4473924,i=8947848){n=new rt(n),i=new rt(i);const s=t/2,a=e/t,o=e/2,c=[],l=[];for(let h=0,m=0,_=-o;h<=t;h++,_+=a){c.push(-o,0,_,o,0,_),c.push(_,0,-o,_,0,o);const g=h===s?n:i;g.toArray(l,m),m+=3,g.toArray(l,m),m+=3,g.toArray(l,m),m+=3,g.toArray(l,m),m+=3}const u=new cn;u.setAttribute("position",new Kt(c,3)),u.setAttribute("color",new Kt(l,3));const f=new Ns({vertexColors:!0,toneMapped:!1});super(u,f),this.type="GridHelper"}dispose(){this.geometry.dispose(),this.material.dispose()}}class Gp extends Tr{constructor(e,t=null){super(),this.object=e,this.domElement=t,this.enabled=!0,this.state=-1,this.keys={},this.mouseButtons={LEFT:null,MIDDLE:null,RIGHT:null},this.touches={ONE:null,TWO:null}}connect(e){if(e===void 0){Xe("Controls: connect() now requires an element.");return}this.domElement!==null&&this.disconnect(),this.domElement=e}disconnect(){}dispose(){}update(){}}function Cu(r,e,t,n){const i=Hp(n);switch(t){case zh:return r*e;case Gh:return r*e/i.components*i.byteLength;case dc:return r*e/i.components*i.byteLength;case os:return r*e*2/i.components*i.byteLength;case pc:return r*e*2/i.components*i.byteLength;case Vh:return r*e*3/i.components*i.byteLength;case Kn:return r*e*4/i.components*i.byteLength;case mc:return r*e*4/i.components*i.byteLength;case Ua:case Na:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*8;case Fa:case Oa:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*16;case fl:case pl:return Math.max(r,16)*Math.max(e,8)/4;case hl:case dl:return Math.max(r,8)*Math.max(e,8)/2;case ml:case _l:case vl:case xl:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*8;case gl:case Ml:case Sl:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*16;case yl:return Math.floor((r+3)/4)*Math.floor((e+3)/4)*16;case bl:return Math.floor((r+4)/5)*Math.floor((e+3)/4)*16;case El:return Math.floor((r+4)/5)*Math.floor((e+4)/5)*16;case Tl:return Math.floor((r+5)/6)*Math.floor((e+4)/5)*16;case Al:return Math.floor((r+5)/6)*Math.floor((e+5)/6)*16;case wl:return Math.floor((r+7)/8)*Math.floor((e+4)/5)*16;case Rl:return Math.floor((r+7)/8)*Math.floor((e+5)/6)*16;case Cl:return Math.floor((r+7)/8)*Math.floor((e+7)/8)*16;case Pl:return Math.floor((r+9)/10)*Math.floor((e+4)/5)*16;case Dl:return Math.floor((r+9)/10)*Math.floor((e+5)/6)*16;case Ll:return Math.floor((r+9)/10)*Math.floor((e+7)/8)*16;case Il:return Math.floor((r+9)/10)*Math.floor((e+9)/10)*16;case Ul:return Math.floor((r+11)/12)*Math.floor((e+9)/10)*16;case Nl:return Math.floor((r+11)/12)*Math.floor((e+11)/12)*16;case Fl:case Ol:case Bl:return Math.ceil(r/4)*Math.ceil(e/4)*16;case kl:case zl:return Math.ceil(r/4)*Math.ceil(e/4)*8;case Vl:case Gl:return Math.ceil(r/4)*Math.ceil(e/4)*16}throw new Error(`Unable to determine texture byte length for ${t} format.`)}function Hp(r){switch(r){case Cn:case Fh:return{byteLength:1,components:1};case ks:case Oh:case wi:return{byteLength:2,components:1};case hc:case fc:return{byteLength:2,components:4};case hi:case uc:case ai:return{byteLength:4,components:1};case Bh:case kh:return{byteLength:4,components:3}}throw new Error(`Unknown texture type ${r}.`)}typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("register",{detail:{revision:lc}}));typeof window<"u"&&(window.__THREE__?Xe("WARNING: Multiple instances of Three.js being imported."):window.__THREE__=lc);/**
 * @license
 * Copyright 2010-2026 Three.js Authors
 * SPDX-License-Identifier: MIT
 */function tf(){let r=null,e=!1,t=null,n=null;function i(s,a){t(s,a),n=r.requestAnimationFrame(i)}return{start:function(){e!==!0&&t!==null&&(n=r.requestAnimationFrame(i),e=!0)},stop:function(){r.cancelAnimationFrame(n),e=!1},setAnimationLoop:function(s){t=s},setContext:function(s){r=s}}}function Wp(r){const e=new WeakMap;function t(o,c){const l=o.array,u=o.usage,f=l.byteLength,h=r.createBuffer();r.bindBuffer(c,h),r.bufferData(c,l,u),o.onUploadCallback();let m;if(l instanceof Float32Array)m=r.FLOAT;else if(typeof Float16Array<"u"&&l instanceof Float16Array)m=r.HALF_FLOAT;else if(l instanceof Uint16Array)o.isFloat16BufferAttribute?m=r.HALF_FLOAT:m=r.UNSIGNED_SHORT;else if(l instanceof Int16Array)m=r.SHORT;else if(l instanceof Uint32Array)m=r.UNSIGNED_INT;else if(l instanceof Int32Array)m=r.INT;else if(l instanceof Int8Array)m=r.BYTE;else if(l instanceof Uint8Array)m=r.UNSIGNED_BYTE;else if(l instanceof Uint8ClampedArray)m=r.UNSIGNED_BYTE;else throw new Error("THREE.WebGLAttributes: Unsupported buffer data format: "+l);return{buffer:h,type:m,bytesPerElement:l.BYTES_PER_ELEMENT,version:o.version,size:f}}function n(o,c,l){const u=c.array,f=c.updateRanges;if(r.bindBuffer(l,o),f.length===0)r.bufferSubData(l,0,u);else{f.sort((m,_)=>m.start-_.start);let h=0;for(let m=1;m<f.length;m++){const _=f[h],g=f[m];g.start<=_.start+_.count+1?_.count=Math.max(_.count,g.start+g.count-_.start):(++h,f[h]=g)}f.length=h+1;for(let m=0,_=f.length;m<_;m++){const g=f[m];r.bufferSubData(l,g.start*u.BYTES_PER_ELEMENT,u,g.start,g.count)}c.clearUpdateRanges()}c.onUploadCallback()}function i(o){return o.isInterleavedBufferAttribute&&(o=o.data),e.get(o)}function s(o){o.isInterleavedBufferAttribute&&(o=o.data);const c=e.get(o);c&&(r.deleteBuffer(c.buffer),e.delete(o))}function a(o,c){if(o.isInterleavedBufferAttribute&&(o=o.data),o.isGLBufferAttribute){const u=e.get(o);(!u||u.version<o.version)&&e.set(o,{buffer:o.buffer,type:o.type,bytesPerElement:o.elementSize,version:o.version});return}const l=e.get(o);if(l===void 0)e.set(o,t(o,c));else if(l.version<o.version){if(l.size!==o.array.byteLength)throw new Error("THREE.WebGLAttributes: The size of the buffer attribute's array buffer does not match the original size. Resizing buffer attributes is not supported.");n(l.buffer,o,c),l.version=o.version}}return{get:i,remove:s,update:a}}var Xp=`#ifdef USE_ALPHAHASH
	if ( diffuseColor.a < getAlphaHashThreshold( vPosition ) ) discard;
#endif`,qp=`#ifdef USE_ALPHAHASH
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
#endif`,Yp=`#ifdef USE_ALPHAMAP
	diffuseColor.a *= texture2D( alphaMap, vAlphaMapUv ).g;
#endif`,$p=`#ifdef USE_ALPHAMAP
	uniform sampler2D alphaMap;
#endif`,Kp=`#ifdef USE_ALPHATEST
	#ifdef ALPHA_TO_COVERAGE
	diffuseColor.a = smoothstep( alphaTest, alphaTest + fwidth( diffuseColor.a ), diffuseColor.a );
	if ( diffuseColor.a == 0.0 ) discard;
	#else
	if ( diffuseColor.a < alphaTest ) discard;
	#endif
#endif`,Zp=`#ifdef USE_ALPHATEST
	uniform float alphaTest;
#endif`,jp=`#ifdef USE_AOMAP
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
#endif`,Jp=`#ifdef USE_AOMAP
	uniform sampler2D aoMap;
	uniform float aoMapIntensity;
#endif`,Qp=`#ifdef USE_BATCHING
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
#endif`,em=`#ifdef USE_BATCHING
	mat4 batchingMatrix = getBatchingMatrix( getIndirectIndex( gl_DrawID ) );
#endif`,tm=`vec3 transformed = vec3( position );
#ifdef USE_ALPHAHASH
	vPosition = vec3( position );
#endif`,nm=`vec3 objectNormal = vec3( normal );
#ifdef USE_TANGENT
	vec3 objectTangent = vec3( tangent.xyz );
#endif`,im=`float G_BlinnPhong_Implicit( ) {
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
} // validated`,rm=`#ifdef USE_IRIDESCENCE
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
#endif`,sm=`#ifdef USE_BUMPMAP
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
#endif`,am=`#if NUM_CLIPPING_PLANES > 0
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
#endif`,om=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
	uniform vec4 clippingPlanes[ NUM_CLIPPING_PLANES ];
#endif`,lm=`#if NUM_CLIPPING_PLANES > 0
	varying vec3 vClipPosition;
#endif`,cm=`#if NUM_CLIPPING_PLANES > 0
	vClipPosition = - mvPosition.xyz;
#endif`,um=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	diffuseColor *= vColor;
#endif`,hm=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA )
	varying vec4 vColor;
#endif`,fm=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
	varying vec4 vColor;
#endif`,dm=`#if defined( USE_COLOR ) || defined( USE_COLOR_ALPHA ) || defined( USE_INSTANCING_COLOR ) || defined( USE_BATCHING_COLOR )
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
#endif`,pm=`#define PI 3.141592653589793
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
} // validated`,mm=`#ifdef ENVMAP_TYPE_CUBE_UV
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
#endif`,_m=`vec3 transformedNormal = objectNormal;
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
#endif`,gm=`#ifdef USE_DISPLACEMENTMAP
	uniform sampler2D displacementMap;
	uniform float displacementScale;
	uniform float displacementBias;
#endif`,vm=`#ifdef USE_DISPLACEMENTMAP
	transformed += normalize( objectNormal ) * ( texture2D( displacementMap, vDisplacementMapUv ).x * displacementScale + displacementBias );
#endif`,xm=`#ifdef USE_EMISSIVEMAP
	vec4 emissiveColor = texture2D( emissiveMap, vEmissiveMapUv );
	#ifdef DECODE_VIDEO_TEXTURE_EMISSIVE
		emissiveColor = sRGBTransferEOTF( emissiveColor );
	#endif
	totalEmissiveRadiance *= emissiveColor.rgb;
#endif`,Mm=`#ifdef USE_EMISSIVEMAP
	uniform sampler2D emissiveMap;
#endif`,Sm="gl_FragColor = linearToOutputTexel( gl_FragColor );",ym=`vec4 LinearTransferOETF( in vec4 value ) {
	return value;
}
vec4 sRGBTransferEOTF( in vec4 value ) {
	return vec4( mix( pow( value.rgb * 0.9478672986 + vec3( 0.0521327014 ), vec3( 2.4 ) ), value.rgb * 0.0773993808, vec3( lessThanEqual( value.rgb, vec3( 0.04045 ) ) ) ), value.a );
}
vec4 sRGBTransferOETF( in vec4 value ) {
	return vec4( mix( pow( value.rgb, vec3( 0.41666 ) ) * 1.055 - vec3( 0.055 ), value.rgb * 12.92, vec3( lessThanEqual( value.rgb, vec3( 0.0031308 ) ) ) ), value.a );
}`,bm=`#ifdef USE_ENVMAP
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
#endif`,Em=`#ifdef USE_ENVMAP
	uniform float envMapIntensity;
	uniform float flipEnvMap;
	uniform mat3 envMapRotation;
	#ifdef ENVMAP_TYPE_CUBE
		uniform samplerCube envMap;
	#else
		uniform sampler2D envMap;
	#endif
#endif`,Tm=`#ifdef USE_ENVMAP
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
#endif`,Am=`#ifdef USE_ENVMAP
	#if defined( USE_BUMPMAP ) || defined( USE_NORMALMAP ) || defined( PHONG ) || defined( LAMBERT )
		#define ENV_WORLDPOS
	#endif
	#ifdef ENV_WORLDPOS
		
		varying vec3 vWorldPosition;
	#else
		varying vec3 vReflect;
		uniform float refractionRatio;
	#endif
#endif`,wm=`#ifdef USE_ENVMAP
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
#endif`,Rm=`#ifdef USE_FOG
	vFogDepth = - mvPosition.z;
#endif`,Cm=`#ifdef USE_FOG
	varying float vFogDepth;
#endif`,Pm=`#ifdef USE_FOG
	#ifdef FOG_EXP2
		float fogFactor = 1.0 - exp( - fogDensity * fogDensity * vFogDepth * vFogDepth );
	#else
		float fogFactor = smoothstep( fogNear, fogFar, vFogDepth );
	#endif
	gl_FragColor.rgb = mix( gl_FragColor.rgb, fogColor, fogFactor );
#endif`,Dm=`#ifdef USE_FOG
	uniform vec3 fogColor;
	varying float vFogDepth;
	#ifdef FOG_EXP2
		uniform float fogDensity;
	#else
		uniform float fogNear;
		uniform float fogFar;
	#endif
#endif`,Lm=`#ifdef USE_GRADIENTMAP
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
}`,Im=`#ifdef USE_LIGHTMAP
	uniform sampler2D lightMap;
	uniform float lightMapIntensity;
#endif`,Um=`LambertMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularStrength = specularStrength;`,Nm=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Lambert`,Fm=`uniform bool receiveShadow;
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
#endif`,Om=`#ifdef USE_ENVMAP
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
#endif`,Bm=`ToonMaterial material;
material.diffuseColor = diffuseColor.rgb;`,km=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_Toon`,zm=`BlinnPhongMaterial material;
material.diffuseColor = diffuseColor.rgb;
material.specularColor = specular;
material.specularShininess = shininess;
material.specularStrength = specularStrength;`,Vm=`varying vec3 vViewPosition;
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
#define RE_IndirectDiffuse		RE_IndirectDiffuse_BlinnPhong`,Gm=`PhysicalMaterial material;
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
#endif`,Hm=`uniform sampler2D dfgLUT;
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
}`,Wm=`
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
#endif`,Xm=`#if defined( RE_IndirectDiffuse )
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
#endif`,qm=`#if defined( RE_IndirectDiffuse )
	#if defined( LAMBERT ) || defined( PHONG )
		irradiance += iblIrradiance;
	#endif
	RE_IndirectDiffuse( irradiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif
#if defined( RE_IndirectSpecular )
	RE_IndirectSpecular( radiance, iblIrradiance, clearcoatRadiance, geometryPosition, geometryNormal, geometryViewDir, geometryClearcoatNormal, material, reflectedLight );
#endif`,Ym=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	gl_FragDepth = vIsPerspective == 0.0 ? gl_FragCoord.z : log2( vFragDepth ) * logDepthBufFC * 0.5;
#endif`,$m=`#if defined( USE_LOGARITHMIC_DEPTH_BUFFER )
	uniform float logDepthBufFC;
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,Km=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	varying float vFragDepth;
	varying float vIsPerspective;
#endif`,Zm=`#ifdef USE_LOGARITHMIC_DEPTH_BUFFER
	vFragDepth = 1.0 + gl_Position.w;
	vIsPerspective = float( isPerspectiveMatrix( projectionMatrix ) );
#endif`,jm=`#ifdef USE_MAP
	vec4 sampledDiffuseColor = texture2D( map, vMapUv );
	#ifdef DECODE_VIDEO_TEXTURE
		sampledDiffuseColor = sRGBTransferEOTF( sampledDiffuseColor );
	#endif
	diffuseColor *= sampledDiffuseColor;
#endif`,Jm=`#ifdef USE_MAP
	uniform sampler2D map;
#endif`,Qm=`#if defined( USE_MAP ) || defined( USE_ALPHAMAP )
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
#endif`,e_=`#if defined( USE_POINTS_UV )
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
#endif`,t_=`float metalnessFactor = metalness;
#ifdef USE_METALNESSMAP
	vec4 texelMetalness = texture2D( metalnessMap, vMetalnessMapUv );
	metalnessFactor *= texelMetalness.b;
#endif`,n_=`#ifdef USE_METALNESSMAP
	uniform sampler2D metalnessMap;
#endif`,i_=`#ifdef USE_INSTANCING_MORPH
	float morphTargetInfluences[ MORPHTARGETS_COUNT ];
	float morphTargetBaseInfluence = texelFetch( morphTexture, ivec2( 0, gl_InstanceID ), 0 ).r;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		morphTargetInfluences[i] =  texelFetch( morphTexture, ivec2( i + 1, gl_InstanceID ), 0 ).r;
	}
#endif`,r_=`#if defined( USE_MORPHCOLORS )
	vColor *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		#if defined( USE_COLOR_ALPHA )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ) * morphTargetInfluences[ i ];
		#elif defined( USE_COLOR )
			if ( morphTargetInfluences[ i ] != 0.0 ) vColor += getMorph( gl_VertexID, i, 2 ).rgb * morphTargetInfluences[ i ];
		#endif
	}
#endif`,s_=`#ifdef USE_MORPHNORMALS
	objectNormal *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) objectNormal += getMorph( gl_VertexID, i, 1 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,a_=`#ifdef USE_MORPHTARGETS
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
#endif`,o_=`#ifdef USE_MORPHTARGETS
	transformed *= morphTargetBaseInfluence;
	for ( int i = 0; i < MORPHTARGETS_COUNT; i ++ ) {
		if ( morphTargetInfluences[ i ] != 0.0 ) transformed += getMorph( gl_VertexID, i, 0 ).xyz * morphTargetInfluences[ i ];
	}
#endif`,l_=`float faceDirection = gl_FrontFacing ? 1.0 : - 1.0;
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
vec3 nonPerturbedNormal = normal;`,c_=`#ifdef USE_NORMALMAP_OBJECTSPACE
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
#endif`,u_=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,h_=`#ifndef FLAT_SHADED
	varying vec3 vNormal;
	#ifdef USE_TANGENT
		varying vec3 vTangent;
		varying vec3 vBitangent;
	#endif
#endif`,f_=`#ifndef FLAT_SHADED
	vNormal = normalize( transformedNormal );
	#ifdef USE_TANGENT
		vTangent = normalize( transformedTangent );
		vBitangent = normalize( cross( vNormal, vTangent ) * tangent.w );
	#endif
#endif`,d_=`#ifdef USE_NORMALMAP
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
#endif`,p_=`#ifdef USE_CLEARCOAT
	vec3 clearcoatNormal = nonPerturbedNormal;
#endif`,m_=`#ifdef USE_CLEARCOAT_NORMALMAP
	vec3 clearcoatMapN = texture2D( clearcoatNormalMap, vClearcoatNormalMapUv ).xyz * 2.0 - 1.0;
	clearcoatMapN.xy *= clearcoatNormalScale;
	clearcoatNormal = normalize( tbn2 * clearcoatMapN );
#endif`,__=`#ifdef USE_CLEARCOATMAP
	uniform sampler2D clearcoatMap;
#endif
#ifdef USE_CLEARCOAT_NORMALMAP
	uniform sampler2D clearcoatNormalMap;
	uniform vec2 clearcoatNormalScale;
#endif
#ifdef USE_CLEARCOAT_ROUGHNESSMAP
	uniform sampler2D clearcoatRoughnessMap;
#endif`,g_=`#ifdef USE_IRIDESCENCEMAP
	uniform sampler2D iridescenceMap;
#endif
#ifdef USE_IRIDESCENCE_THICKNESSMAP
	uniform sampler2D iridescenceThicknessMap;
#endif`,v_=`#ifdef OPAQUE
diffuseColor.a = 1.0;
#endif
#ifdef USE_TRANSMISSION
diffuseColor.a *= material.transmissionAlpha;
#endif
gl_FragColor = vec4( outgoingLight, diffuseColor.a );`,x_=`vec3 packNormalToRGB( const in vec3 normal ) {
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
}`,M_=`#ifdef PREMULTIPLIED_ALPHA
	gl_FragColor.rgb *= gl_FragColor.a;
#endif`,S_=`vec4 mvPosition = vec4( transformed, 1.0 );
#ifdef USE_BATCHING
	mvPosition = batchingMatrix * mvPosition;
#endif
#ifdef USE_INSTANCING
	mvPosition = instanceMatrix * mvPosition;
#endif
mvPosition = modelViewMatrix * mvPosition;
gl_Position = projectionMatrix * mvPosition;`,y_=`#ifdef DITHERING
	gl_FragColor.rgb = dithering( gl_FragColor.rgb );
#endif`,b_=`#ifdef DITHERING
	vec3 dithering( vec3 color ) {
		float grid_position = rand( gl_FragCoord.xy );
		vec3 dither_shift_RGB = vec3( 0.25 / 255.0, -0.25 / 255.0, 0.25 / 255.0 );
		dither_shift_RGB = mix( 2.0 * dither_shift_RGB, -2.0 * dither_shift_RGB, grid_position );
		return color + dither_shift_RGB;
	}
#endif`,E_=`float roughnessFactor = roughness;
#ifdef USE_ROUGHNESSMAP
	vec4 texelRoughness = texture2D( roughnessMap, vRoughnessMapUv );
	roughnessFactor *= texelRoughness.g;
#endif`,T_=`#ifdef USE_ROUGHNESSMAP
	uniform sampler2D roughnessMap;
#endif`,A_=`#if NUM_SPOT_LIGHT_COORDS > 0
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
#endif`,w_=`#if NUM_SPOT_LIGHT_COORDS > 0
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
#endif`,R_=`#if ( defined( USE_SHADOWMAP ) && ( NUM_DIR_LIGHT_SHADOWS > 0 || NUM_POINT_LIGHT_SHADOWS > 0 ) ) || ( NUM_SPOT_LIGHT_COORDS > 0 )
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
#endif`,C_=`float getShadowMask() {
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
}`,P_=`#ifdef USE_SKINNING
	mat4 boneMatX = getBoneMatrix( skinIndex.x );
	mat4 boneMatY = getBoneMatrix( skinIndex.y );
	mat4 boneMatZ = getBoneMatrix( skinIndex.z );
	mat4 boneMatW = getBoneMatrix( skinIndex.w );
#endif`,D_=`#ifdef USE_SKINNING
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
#endif`,L_=`#ifdef USE_SKINNING
	vec4 skinVertex = bindMatrix * vec4( transformed, 1.0 );
	vec4 skinned = vec4( 0.0 );
	skinned += boneMatX * skinVertex * skinWeight.x;
	skinned += boneMatY * skinVertex * skinWeight.y;
	skinned += boneMatZ * skinVertex * skinWeight.z;
	skinned += boneMatW * skinVertex * skinWeight.w;
	transformed = ( bindMatrixInverse * skinned ).xyz;
#endif`,I_=`#ifdef USE_SKINNING
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
#endif`,U_=`float specularStrength;
#ifdef USE_SPECULARMAP
	vec4 texelSpecular = texture2D( specularMap, vSpecularMapUv );
	specularStrength = texelSpecular.r;
#else
	specularStrength = 1.0;
#endif`,N_=`#ifdef USE_SPECULARMAP
	uniform sampler2D specularMap;
#endif`,F_=`#if defined( TONE_MAPPING )
	gl_FragColor.rgb = toneMapping( gl_FragColor.rgb );
#endif`,O_=`#ifndef saturate
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
vec3 CustomToneMapping( vec3 color ) { return color; }`,B_=`#ifdef USE_TRANSMISSION
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
#endif`,k_=`#ifdef USE_TRANSMISSION
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
#endif`,z_=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,V_=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,G_=`#if defined( USE_UV ) || defined( USE_ANISOTROPY )
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
#endif`,H_=`#if defined( USE_ENVMAP ) || defined( DISTANCE ) || defined ( USE_SHADOWMAP ) || defined ( USE_TRANSMISSION ) || NUM_SPOT_LIGHT_COORDS > 0
	vec4 worldPosition = vec4( transformed, 1.0 );
	#ifdef USE_BATCHING
		worldPosition = batchingMatrix * worldPosition;
	#endif
	#ifdef USE_INSTANCING
		worldPosition = instanceMatrix * worldPosition;
	#endif
	worldPosition = modelMatrix * worldPosition;
#endif`;const W_=`varying vec2 vUv;
uniform mat3 uvTransform;
void main() {
	vUv = ( uvTransform * vec3( uv, 1 ) ).xy;
	gl_Position = vec4( position.xy, 1.0, 1.0 );
}`,X_=`uniform sampler2D t2D;
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
}`,q_=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,Y_=`#ifdef ENVMAP_TYPE_CUBE
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
}`,$_=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
	gl_Position.z = gl_Position.w;
}`,K_=`uniform samplerCube tCube;
uniform float tFlip;
uniform float opacity;
varying vec3 vWorldDirection;
void main() {
	vec4 texColor = textureCube( tCube, vec3( tFlip * vWorldDirection.x, vWorldDirection.yz ) );
	gl_FragColor = texColor;
	gl_FragColor.a *= opacity;
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,Z_=`#include <common>
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
}`,j_=`#if DEPTH_PACKING == 3200
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
}`,J_=`#define DISTANCE
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
}`,Q_=`#define DISTANCE
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
}`,eg=`varying vec3 vWorldDirection;
#include <common>
void main() {
	vWorldDirection = transformDirection( position, modelMatrix );
	#include <begin_vertex>
	#include <project_vertex>
}`,tg=`uniform sampler2D tEquirect;
varying vec3 vWorldDirection;
#include <common>
void main() {
	vec3 direction = normalize( vWorldDirection );
	vec2 sampleUV = equirectUv( direction );
	gl_FragColor = texture2D( tEquirect, sampleUV );
	#include <tonemapping_fragment>
	#include <colorspace_fragment>
}`,ng=`uniform float scale;
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
}`,ig=`uniform vec3 diffuse;
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
}`,rg=`#include <common>
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
}`,sg=`uniform vec3 diffuse;
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
}`,ag=`#define LAMBERT
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
}`,og=`#define LAMBERT
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
}`,lg=`#define MATCAP
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
}`,cg=`#define MATCAP
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
}`,ug=`#define NORMAL
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
}`,hg=`#define NORMAL
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
}`,fg=`#define PHONG
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
}`,dg=`#define PHONG
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
}`,pg=`#define STANDARD
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
}`,mg=`#define STANDARD
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
}`,_g=`#define TOON
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
}`,gg=`#define TOON
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
}`,vg=`uniform float size;
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
}`,xg=`uniform vec3 diffuse;
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
}`,Mg=`#include <common>
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
}`,Sg=`uniform vec3 color;
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
}`,yg=`uniform float rotation;
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
}`,bg=`uniform vec3 diffuse;
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
}`,Ze={alphahash_fragment:Xp,alphahash_pars_fragment:qp,alphamap_fragment:Yp,alphamap_pars_fragment:$p,alphatest_fragment:Kp,alphatest_pars_fragment:Zp,aomap_fragment:jp,aomap_pars_fragment:Jp,batching_pars_vertex:Qp,batching_vertex:em,begin_vertex:tm,beginnormal_vertex:nm,bsdfs:im,iridescence_fragment:rm,bumpmap_pars_fragment:sm,clipping_planes_fragment:am,clipping_planes_pars_fragment:om,clipping_planes_pars_vertex:lm,clipping_planes_vertex:cm,color_fragment:um,color_pars_fragment:hm,color_pars_vertex:fm,color_vertex:dm,common:pm,cube_uv_reflection_fragment:mm,defaultnormal_vertex:_m,displacementmap_pars_vertex:gm,displacementmap_vertex:vm,emissivemap_fragment:xm,emissivemap_pars_fragment:Mm,colorspace_fragment:Sm,colorspace_pars_fragment:ym,envmap_fragment:bm,envmap_common_pars_fragment:Em,envmap_pars_fragment:Tm,envmap_pars_vertex:Am,envmap_physical_pars_fragment:Om,envmap_vertex:wm,fog_vertex:Rm,fog_pars_vertex:Cm,fog_fragment:Pm,fog_pars_fragment:Dm,gradientmap_pars_fragment:Lm,lightmap_pars_fragment:Im,lights_lambert_fragment:Um,lights_lambert_pars_fragment:Nm,lights_pars_begin:Fm,lights_toon_fragment:Bm,lights_toon_pars_fragment:km,lights_phong_fragment:zm,lights_phong_pars_fragment:Vm,lights_physical_fragment:Gm,lights_physical_pars_fragment:Hm,lights_fragment_begin:Wm,lights_fragment_maps:Xm,lights_fragment_end:qm,logdepthbuf_fragment:Ym,logdepthbuf_pars_fragment:$m,logdepthbuf_pars_vertex:Km,logdepthbuf_vertex:Zm,map_fragment:jm,map_pars_fragment:Jm,map_particle_fragment:Qm,map_particle_pars_fragment:e_,metalnessmap_fragment:t_,metalnessmap_pars_fragment:n_,morphinstance_vertex:i_,morphcolor_vertex:r_,morphnormal_vertex:s_,morphtarget_pars_vertex:a_,morphtarget_vertex:o_,normal_fragment_begin:l_,normal_fragment_maps:c_,normal_pars_fragment:u_,normal_pars_vertex:h_,normal_vertex:f_,normalmap_pars_fragment:d_,clearcoat_normal_fragment_begin:p_,clearcoat_normal_fragment_maps:m_,clearcoat_pars_fragment:__,iridescence_pars_fragment:g_,opaque_fragment:v_,packing:x_,premultiplied_alpha_fragment:M_,project_vertex:S_,dithering_fragment:y_,dithering_pars_fragment:b_,roughnessmap_fragment:E_,roughnessmap_pars_fragment:T_,shadowmap_pars_fragment:A_,shadowmap_pars_vertex:w_,shadowmap_vertex:R_,shadowmask_pars_fragment:C_,skinbase_vertex:P_,skinning_pars_vertex:D_,skinning_vertex:L_,skinnormal_vertex:I_,specularmap_fragment:U_,specularmap_pars_fragment:N_,tonemapping_fragment:F_,tonemapping_pars_fragment:O_,transmission_fragment:B_,transmission_pars_fragment:k_,uv_pars_fragment:z_,uv_pars_vertex:V_,uv_vertex:G_,worldpos_vertex:H_,background_vert:W_,background_frag:X_,backgroundCube_vert:q_,backgroundCube_frag:Y_,cube_vert:$_,cube_frag:K_,depth_vert:Z_,depth_frag:j_,distance_vert:J_,distance_frag:Q_,equirect_vert:eg,equirect_frag:tg,linedashed_vert:ng,linedashed_frag:ig,meshbasic_vert:rg,meshbasic_frag:sg,meshlambert_vert:ag,meshlambert_frag:og,meshmatcap_vert:lg,meshmatcap_frag:cg,meshnormal_vert:ug,meshnormal_frag:hg,meshphong_vert:fg,meshphong_frag:dg,meshphysical_vert:pg,meshphysical_frag:mg,meshtoon_vert:_g,meshtoon_frag:gg,points_vert:vg,points_frag:xg,shadow_vert:Mg,shadow_frag:Sg,sprite_vert:yg,sprite_frag:bg},ye={common:{diffuse:{value:new rt(16777215)},opacity:{value:1},map:{value:null},mapTransform:{value:new Ke},alphaMap:{value:null},alphaMapTransform:{value:new Ke},alphaTest:{value:0}},specularmap:{specularMap:{value:null},specularMapTransform:{value:new Ke}},envmap:{envMap:{value:null},envMapRotation:{value:new Ke},flipEnvMap:{value:-1},reflectivity:{value:1},ior:{value:1.5},refractionRatio:{value:.98},dfgLUT:{value:null}},aomap:{aoMap:{value:null},aoMapIntensity:{value:1},aoMapTransform:{value:new Ke}},lightmap:{lightMap:{value:null},lightMapIntensity:{value:1},lightMapTransform:{value:new Ke}},bumpmap:{bumpMap:{value:null},bumpMapTransform:{value:new Ke},bumpScale:{value:1}},normalmap:{normalMap:{value:null},normalMapTransform:{value:new Ke},normalScale:{value:new $e(1,1)}},displacementmap:{displacementMap:{value:null},displacementMapTransform:{value:new Ke},displacementScale:{value:1},displacementBias:{value:0}},emissivemap:{emissiveMap:{value:null},emissiveMapTransform:{value:new Ke}},metalnessmap:{metalnessMap:{value:null},metalnessMapTransform:{value:new Ke}},roughnessmap:{roughnessMap:{value:null},roughnessMapTransform:{value:new Ke}},gradientmap:{gradientMap:{value:null}},fog:{fogDensity:{value:25e-5},fogNear:{value:1},fogFar:{value:2e3},fogColor:{value:new rt(16777215)}},lights:{ambientLightColor:{value:[]},lightProbe:{value:[]},directionalLights:{value:[],properties:{direction:{},color:{}}},directionalLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},directionalShadowMatrix:{value:[]},spotLights:{value:[],properties:{color:{},position:{},direction:{},distance:{},coneCos:{},penumbraCos:{},decay:{}}},spotLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{}}},spotLightMap:{value:[]},spotLightMatrix:{value:[]},pointLights:{value:[],properties:{color:{},position:{},decay:{},distance:{}}},pointLightShadows:{value:[],properties:{shadowIntensity:1,shadowBias:{},shadowNormalBias:{},shadowRadius:{},shadowMapSize:{},shadowCameraNear:{},shadowCameraFar:{}}},pointShadowMatrix:{value:[]},hemisphereLights:{value:[],properties:{direction:{},skyColor:{},groundColor:{}}},rectAreaLights:{value:[],properties:{color:{},position:{},width:{},height:{}}},ltc_1:{value:null},ltc_2:{value:null}},points:{diffuse:{value:new rt(16777215)},opacity:{value:1},size:{value:1},scale:{value:1},map:{value:null},alphaMap:{value:null},alphaMapTransform:{value:new Ke},alphaTest:{value:0},uvTransform:{value:new Ke}},sprite:{diffuse:{value:new rt(16777215)},opacity:{value:1},center:{value:new $e(.5,.5)},rotation:{value:0},map:{value:null},mapTransform:{value:new Ke},alphaMap:{value:null},alphaMapTransform:{value:new Ke},alphaTest:{value:0}}},ri={basic:{uniforms:an([ye.common,ye.specularmap,ye.envmap,ye.aomap,ye.lightmap,ye.fog]),vertexShader:Ze.meshbasic_vert,fragmentShader:Ze.meshbasic_frag},lambert:{uniforms:an([ye.common,ye.specularmap,ye.envmap,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.fog,ye.lights,{emissive:{value:new rt(0)},envMapIntensity:{value:1}}]),vertexShader:Ze.meshlambert_vert,fragmentShader:Ze.meshlambert_frag},phong:{uniforms:an([ye.common,ye.specularmap,ye.envmap,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.fog,ye.lights,{emissive:{value:new rt(0)},specular:{value:new rt(1118481)},shininess:{value:30},envMapIntensity:{value:1}}]),vertexShader:Ze.meshphong_vert,fragmentShader:Ze.meshphong_frag},standard:{uniforms:an([ye.common,ye.envmap,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.roughnessmap,ye.metalnessmap,ye.fog,ye.lights,{emissive:{value:new rt(0)},roughness:{value:1},metalness:{value:0},envMapIntensity:{value:1}}]),vertexShader:Ze.meshphysical_vert,fragmentShader:Ze.meshphysical_frag},toon:{uniforms:an([ye.common,ye.aomap,ye.lightmap,ye.emissivemap,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.gradientmap,ye.fog,ye.lights,{emissive:{value:new rt(0)}}]),vertexShader:Ze.meshtoon_vert,fragmentShader:Ze.meshtoon_frag},matcap:{uniforms:an([ye.common,ye.bumpmap,ye.normalmap,ye.displacementmap,ye.fog,{matcap:{value:null}}]),vertexShader:Ze.meshmatcap_vert,fragmentShader:Ze.meshmatcap_frag},points:{uniforms:an([ye.points,ye.fog]),vertexShader:Ze.points_vert,fragmentShader:Ze.points_frag},dashed:{uniforms:an([ye.common,ye.fog,{scale:{value:1},dashSize:{value:1},totalSize:{value:2}}]),vertexShader:Ze.linedashed_vert,fragmentShader:Ze.linedashed_frag},depth:{uniforms:an([ye.common,ye.displacementmap]),vertexShader:Ze.depth_vert,fragmentShader:Ze.depth_frag},normal:{uniforms:an([ye.common,ye.bumpmap,ye.normalmap,ye.displacementmap,{opacity:{value:1}}]),vertexShader:Ze.meshnormal_vert,fragmentShader:Ze.meshnormal_frag},sprite:{uniforms:an([ye.sprite,ye.fog]),vertexShader:Ze.sprite_vert,fragmentShader:Ze.sprite_frag},background:{uniforms:{uvTransform:{value:new Ke},t2D:{value:null},backgroundIntensity:{value:1}},vertexShader:Ze.background_vert,fragmentShader:Ze.background_frag},backgroundCube:{uniforms:{envMap:{value:null},flipEnvMap:{value:-1},backgroundBlurriness:{value:0},backgroundIntensity:{value:1},backgroundRotation:{value:new Ke}},vertexShader:Ze.backgroundCube_vert,fragmentShader:Ze.backgroundCube_frag},cube:{uniforms:{tCube:{value:null},tFlip:{value:-1},opacity:{value:1}},vertexShader:Ze.cube_vert,fragmentShader:Ze.cube_frag},equirect:{uniforms:{tEquirect:{value:null}},vertexShader:Ze.equirect_vert,fragmentShader:Ze.equirect_frag},distance:{uniforms:an([ye.common,ye.displacementmap,{referencePosition:{value:new k},nearDistance:{value:1},farDistance:{value:1e3}}]),vertexShader:Ze.distance_vert,fragmentShader:Ze.distance_frag},shadow:{uniforms:an([ye.lights,ye.fog,{color:{value:new rt(0)},opacity:{value:1}}]),vertexShader:Ze.shadow_vert,fragmentShader:Ze.shadow_frag}};ri.physical={uniforms:an([ri.standard.uniforms,{clearcoat:{value:0},clearcoatMap:{value:null},clearcoatMapTransform:{value:new Ke},clearcoatNormalMap:{value:null},clearcoatNormalMapTransform:{value:new Ke},clearcoatNormalScale:{value:new $e(1,1)},clearcoatRoughness:{value:0},clearcoatRoughnessMap:{value:null},clearcoatRoughnessMapTransform:{value:new Ke},dispersion:{value:0},iridescence:{value:0},iridescenceMap:{value:null},iridescenceMapTransform:{value:new Ke},iridescenceIOR:{value:1.3},iridescenceThicknessMinimum:{value:100},iridescenceThicknessMaximum:{value:400},iridescenceThicknessMap:{value:null},iridescenceThicknessMapTransform:{value:new Ke},sheen:{value:0},sheenColor:{value:new rt(0)},sheenColorMap:{value:null},sheenColorMapTransform:{value:new Ke},sheenRoughness:{value:1},sheenRoughnessMap:{value:null},sheenRoughnessMapTransform:{value:new Ke},transmission:{value:0},transmissionMap:{value:null},transmissionMapTransform:{value:new Ke},transmissionSamplerSize:{value:new $e},transmissionSamplerMap:{value:null},thickness:{value:0},thicknessMap:{value:null},thicknessMapTransform:{value:new Ke},attenuationDistance:{value:0},attenuationColor:{value:new rt(0)},specularColor:{value:new rt(1,1,1)},specularColorMap:{value:null},specularColorMapTransform:{value:new Ke},specularIntensity:{value:1},specularIntensityMap:{value:null},specularIntensityMapTransform:{value:new Ke},anisotropyVector:{value:new $e},anisotropyMap:{value:null},anisotropyMapTransform:{value:new Ke}}]),vertexShader:Ze.meshphysical_vert,fragmentShader:Ze.meshphysical_frag};const Ca={r:0,b:0,g:0},cr=new fi,Eg=new yt;function Tg(r,e,t,n,i,s){const a=new rt(0);let o=i===!0?0:1,c,l,u=null,f=0,h=null;function m(M){let y=M.isScene===!0?M.background:null;if(y&&y.isTexture){const b=M.backgroundBlurriness>0;y=e.get(y,b)}return y}function _(M){let y=!1;const b=m(M);b===null?d(a,o):b&&b.isColor&&(d(b,1),y=!0);const T=r.xr.getEnvironmentBlendMode();T==="additive"?t.buffers.color.setClear(0,0,0,1,s):T==="alpha-blend"&&t.buffers.color.setClear(0,0,0,0,s),(r.autoClear||y)&&(t.buffers.depth.setTest(!0),t.buffers.depth.setMask(!0),t.buffers.color.setMask(!0),r.clear(r.autoClearColor,r.autoClearDepth,r.autoClearStencil))}function g(M,y){const b=m(y);b&&(b.isCubeTexture||b.mapping===no)?(l===void 0&&(l=new Yt(new dn(1,1,1),new di({name:"BackgroundCubeMaterial",uniforms:cs(ri.backgroundCube.uniforms),vertexShader:ri.backgroundCube.vertexShader,fragmentShader:ri.backgroundCube.fragmentShader,side:pn,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),l.geometry.deleteAttribute("normal"),l.geometry.deleteAttribute("uv"),l.onBeforeRender=function(T,A,R){this.matrixWorld.copyPosition(R.matrixWorld)},Object.defineProperty(l.material,"envMap",{get:function(){return this.uniforms.envMap.value}}),n.update(l)),cr.copy(y.backgroundRotation),cr.x*=-1,cr.y*=-1,cr.z*=-1,b.isCubeTexture&&b.isRenderTargetTexture===!1&&(cr.y*=-1,cr.z*=-1),l.material.uniforms.envMap.value=b,l.material.uniforms.flipEnvMap.value=b.isCubeTexture&&b.isRenderTargetTexture===!1?-1:1,l.material.uniforms.backgroundBlurriness.value=y.backgroundBlurriness,l.material.uniforms.backgroundIntensity.value=y.backgroundIntensity,l.material.uniforms.backgroundRotation.value.setFromMatrix4(Eg.makeRotationFromEuler(cr)),l.material.toneMapped=ct.getTransfer(b.colorSpace)!==mt,(u!==b||f!==b.version||h!==r.toneMapping)&&(l.material.needsUpdate=!0,u=b,f=b.version,h=r.toneMapping),l.layers.enableAll(),M.unshift(l,l.geometry,l.material,0,0,null)):b&&b.isTexture&&(c===void 0&&(c=new Yt(new Qs(2,2),new di({name:"BackgroundMaterial",uniforms:cs(ri.background.uniforms),vertexShader:ri.background.vertexShader,fragmentShader:ri.background.fragmentShader,side:Zi,depthTest:!1,depthWrite:!1,fog:!1,allowOverride:!1})),c.geometry.deleteAttribute("normal"),Object.defineProperty(c.material,"map",{get:function(){return this.uniforms.t2D.value}}),n.update(c)),c.material.uniforms.t2D.value=b,c.material.uniforms.backgroundIntensity.value=y.backgroundIntensity,c.material.toneMapped=ct.getTransfer(b.colorSpace)!==mt,b.matrixAutoUpdate===!0&&b.updateMatrix(),c.material.uniforms.uvTransform.value.copy(b.matrix),(u!==b||f!==b.version||h!==r.toneMapping)&&(c.material.needsUpdate=!0,u=b,f=b.version,h=r.toneMapping),c.layers.enableAll(),M.unshift(c,c.geometry,c.material,0,0,null))}function d(M,y){M.getRGB(Ca,Jh(r)),t.buffers.color.setClear(Ca.r,Ca.g,Ca.b,y,s)}function p(){l!==void 0&&(l.geometry.dispose(),l.material.dispose(),l=void 0),c!==void 0&&(c.geometry.dispose(),c.material.dispose(),c=void 0)}return{getClearColor:function(){return a},setClearColor:function(M,y=1){a.set(M),o=y,d(a,o)},getClearAlpha:function(){return o},setClearAlpha:function(M){o=M,d(a,o)},render:_,addToRenderList:g,dispose:p}}function Ag(r,e){const t=r.getParameter(r.MAX_VERTEX_ATTRIBS),n={},i=h(null);let s=i,a=!1;function o(P,z,F,B,O){let V=!1;const G=f(P,B,F,z);s!==G&&(s=G,l(s.object)),V=m(P,B,F,O),V&&_(P,B,F,O),O!==null&&e.update(O,r.ELEMENT_ARRAY_BUFFER),(V||a)&&(a=!1,b(P,z,F,B),O!==null&&r.bindBuffer(r.ELEMENT_ARRAY_BUFFER,e.get(O).buffer))}function c(){return r.createVertexArray()}function l(P){return r.bindVertexArray(P)}function u(P){return r.deleteVertexArray(P)}function f(P,z,F,B){const O=B.wireframe===!0;let V=n[z.id];V===void 0&&(V={},n[z.id]=V);const G=P.isInstancedMesh===!0?P.id:0;let ne=V[G];ne===void 0&&(ne={},V[G]=ne);let Q=ne[F.id];Q===void 0&&(Q={},ne[F.id]=Q);let me=Q[O];return me===void 0&&(me=h(c()),Q[O]=me),me}function h(P){const z=[],F=[],B=[];for(let O=0;O<t;O++)z[O]=0,F[O]=0,B[O]=0;return{geometry:null,program:null,wireframe:!1,newAttributes:z,enabledAttributes:F,attributeDivisors:B,object:P,attributes:{},index:null}}function m(P,z,F,B){const O=s.attributes,V=z.attributes;let G=0;const ne=F.getAttributes();for(const Q in ne)if(ne[Q].location>=0){const be=O[Q];let xe=V[Q];if(xe===void 0&&(Q==="instanceMatrix"&&P.instanceMatrix&&(xe=P.instanceMatrix),Q==="instanceColor"&&P.instanceColor&&(xe=P.instanceColor)),be===void 0||be.attribute!==xe||xe&&be.data!==xe.data)return!0;G++}return s.attributesNum!==G||s.index!==B}function _(P,z,F,B){const O={},V=z.attributes;let G=0;const ne=F.getAttributes();for(const Q in ne)if(ne[Q].location>=0){let be=V[Q];be===void 0&&(Q==="instanceMatrix"&&P.instanceMatrix&&(be=P.instanceMatrix),Q==="instanceColor"&&P.instanceColor&&(be=P.instanceColor));const xe={};xe.attribute=be,be&&be.data&&(xe.data=be.data),O[Q]=xe,G++}s.attributes=O,s.attributesNum=G,s.index=B}function g(){const P=s.newAttributes;for(let z=0,F=P.length;z<F;z++)P[z]=0}function d(P){p(P,0)}function p(P,z){const F=s.newAttributes,B=s.enabledAttributes,O=s.attributeDivisors;F[P]=1,B[P]===0&&(r.enableVertexAttribArray(P),B[P]=1),O[P]!==z&&(r.vertexAttribDivisor(P,z),O[P]=z)}function M(){const P=s.newAttributes,z=s.enabledAttributes;for(let F=0,B=z.length;F<B;F++)z[F]!==P[F]&&(r.disableVertexAttribArray(F),z[F]=0)}function y(P,z,F,B,O,V,G){G===!0?r.vertexAttribIPointer(P,z,F,O,V):r.vertexAttribPointer(P,z,F,B,O,V)}function b(P,z,F,B){g();const O=B.attributes,V=F.getAttributes(),G=z.defaultAttributeValues;for(const ne in V){const Q=V[ne];if(Q.location>=0){let me=O[ne];if(me===void 0&&(ne==="instanceMatrix"&&P.instanceMatrix&&(me=P.instanceMatrix),ne==="instanceColor"&&P.instanceColor&&(me=P.instanceColor)),me!==void 0){const be=me.normalized,xe=me.itemSize,Ge=e.get(me);if(Ge===void 0)continue;const qe=Ge.buffer,je=Ge.type,ee=Ge.bytesPerElement,he=je===r.INT||je===r.UNSIGNED_INT||me.gpuType===uc;if(me.isInterleavedBufferAttribute){const pe=me.data,He=pe.stride,ke=me.offset;if(pe.isInstancedInterleavedBuffer){for(let ze=0;ze<Q.locationSize;ze++)p(Q.location+ze,pe.meshPerAttribute);P.isInstancedMesh!==!0&&B._maxInstanceCount===void 0&&(B._maxInstanceCount=pe.meshPerAttribute*pe.count)}else for(let ze=0;ze<Q.locationSize;ze++)d(Q.location+ze);r.bindBuffer(r.ARRAY_BUFFER,qe);for(let ze=0;ze<Q.locationSize;ze++)y(Q.location+ze,xe/Q.locationSize,je,be,He*ee,(ke+xe/Q.locationSize*ze)*ee,he)}else{if(me.isInstancedBufferAttribute){for(let pe=0;pe<Q.locationSize;pe++)p(Q.location+pe,me.meshPerAttribute);P.isInstancedMesh!==!0&&B._maxInstanceCount===void 0&&(B._maxInstanceCount=me.meshPerAttribute*me.count)}else for(let pe=0;pe<Q.locationSize;pe++)d(Q.location+pe);r.bindBuffer(r.ARRAY_BUFFER,qe);for(let pe=0;pe<Q.locationSize;pe++)y(Q.location+pe,xe/Q.locationSize,je,be,xe*ee,xe/Q.locationSize*pe*ee,he)}}else if(G!==void 0){const be=G[ne];if(be!==void 0)switch(be.length){case 2:r.vertexAttrib2fv(Q.location,be);break;case 3:r.vertexAttrib3fv(Q.location,be);break;case 4:r.vertexAttrib4fv(Q.location,be);break;default:r.vertexAttrib1fv(Q.location,be)}}}}M()}function T(){S();for(const P in n){const z=n[P];for(const F in z){const B=z[F];for(const O in B){const V=B[O];for(const G in V)u(V[G].object),delete V[G];delete B[O]}}delete n[P]}}function A(P){if(n[P.id]===void 0)return;const z=n[P.id];for(const F in z){const B=z[F];for(const O in B){const V=B[O];for(const G in V)u(V[G].object),delete V[G];delete B[O]}}delete n[P.id]}function R(P){for(const z in n){const F=n[z];for(const B in F){const O=F[B];if(O[P.id]===void 0)continue;const V=O[P.id];for(const G in V)u(V[G].object),delete V[G];delete O[P.id]}}}function x(P){for(const z in n){const F=n[z],B=P.isInstancedMesh===!0?P.id:0,O=F[B];if(O!==void 0){for(const V in O){const G=O[V];for(const ne in G)u(G[ne].object),delete G[ne];delete O[V]}delete F[B],Object.keys(F).length===0&&delete n[z]}}}function S(){W(),a=!0,s!==i&&(s=i,l(s.object))}function W(){i.geometry=null,i.program=null,i.wireframe=!1}return{setup:o,reset:S,resetDefaultState:W,dispose:T,releaseStatesOfGeometry:A,releaseStatesOfObject:x,releaseStatesOfProgram:R,initAttributes:g,enableAttribute:d,disableUnusedAttributes:M}}function wg(r,e,t){let n;function i(l){n=l}function s(l,u){r.drawArrays(n,l,u),t.update(u,n,1)}function a(l,u,f){f!==0&&(r.drawArraysInstanced(n,l,u,f),t.update(u,n,f))}function o(l,u,f){if(f===0)return;e.get("WEBGL_multi_draw").multiDrawArraysWEBGL(n,l,0,u,0,f);let m=0;for(let _=0;_<f;_++)m+=u[_];t.update(m,n,1)}function c(l,u,f,h){if(f===0)return;const m=e.get("WEBGL_multi_draw");if(m===null)for(let _=0;_<l.length;_++)a(l[_],u[_],h[_]);else{m.multiDrawArraysInstancedWEBGL(n,l,0,u,0,h,0,f);let _=0;for(let g=0;g<f;g++)_+=u[g]*h[g];t.update(_,n,1)}}this.setMode=i,this.render=s,this.renderInstances=a,this.renderMultiDraw=o,this.renderMultiDrawInstances=c}function Rg(r,e,t,n){let i;function s(){if(i!==void 0)return i;if(e.has("EXT_texture_filter_anisotropic")===!0){const R=e.get("EXT_texture_filter_anisotropic");i=r.getParameter(R.MAX_TEXTURE_MAX_ANISOTROPY_EXT)}else i=0;return i}function a(R){return!(R!==Kn&&n.convert(R)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_FORMAT))}function o(R){const x=R===wi&&(e.has("EXT_color_buffer_half_float")||e.has("EXT_color_buffer_float"));return!(R!==Cn&&n.convert(R)!==r.getParameter(r.IMPLEMENTATION_COLOR_READ_TYPE)&&R!==ai&&!x)}function c(R){if(R==="highp"){if(r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.HIGH_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.HIGH_FLOAT).precision>0)return"highp";R="mediump"}return R==="mediump"&&r.getShaderPrecisionFormat(r.VERTEX_SHADER,r.MEDIUM_FLOAT).precision>0&&r.getShaderPrecisionFormat(r.FRAGMENT_SHADER,r.MEDIUM_FLOAT).precision>0?"mediump":"lowp"}let l=t.precision!==void 0?t.precision:"highp";const u=c(l);u!==l&&(Xe("WebGLRenderer:",l,"not supported, using",u,"instead."),l=u);const f=t.logarithmicDepthBuffer===!0,h=t.reversedDepthBuffer===!0&&e.has("EXT_clip_control"),m=r.getParameter(r.MAX_TEXTURE_IMAGE_UNITS),_=r.getParameter(r.MAX_VERTEX_TEXTURE_IMAGE_UNITS),g=r.getParameter(r.MAX_TEXTURE_SIZE),d=r.getParameter(r.MAX_CUBE_MAP_TEXTURE_SIZE),p=r.getParameter(r.MAX_VERTEX_ATTRIBS),M=r.getParameter(r.MAX_VERTEX_UNIFORM_VECTORS),y=r.getParameter(r.MAX_VARYING_VECTORS),b=r.getParameter(r.MAX_FRAGMENT_UNIFORM_VECTORS),T=r.getParameter(r.MAX_SAMPLES),A=r.getParameter(r.SAMPLES);return{isWebGL2:!0,getMaxAnisotropy:s,getMaxPrecision:c,textureFormatReadable:a,textureTypeReadable:o,precision:l,logarithmicDepthBuffer:f,reversedDepthBuffer:h,maxTextures:m,maxVertexTextures:_,maxTextureSize:g,maxCubemapSize:d,maxAttributes:p,maxVertexUniforms:M,maxVaryings:y,maxFragmentUniforms:b,maxSamples:T,samples:A}}function Cg(r){const e=this;let t=null,n=0,i=!1,s=!1;const a=new Gi,o=new Ke,c={value:null,needsUpdate:!1};this.uniform=c,this.numPlanes=0,this.numIntersection=0,this.init=function(f,h){const m=f.length!==0||h||n!==0||i;return i=h,n=f.length,m},this.beginShadows=function(){s=!0,u(null)},this.endShadows=function(){s=!1},this.setGlobalState=function(f,h){t=u(f,h,0)},this.setState=function(f,h,m){const _=f.clippingPlanes,g=f.clipIntersection,d=f.clipShadows,p=r.get(f);if(!i||_===null||_.length===0||s&&!d)s?u(null):l();else{const M=s?0:n,y=M*4;let b=p.clippingState||null;c.value=b,b=u(_,h,y,m);for(let T=0;T!==y;++T)b[T]=t[T];p.clippingState=b,this.numIntersection=g?this.numPlanes:0,this.numPlanes+=M}};function l(){c.value!==t&&(c.value=t,c.needsUpdate=n>0),e.numPlanes=n,e.numIntersection=0}function u(f,h,m,_){const g=f!==null?f.length:0;let d=null;if(g!==0){if(d=c.value,_!==!0||d===null){const p=m+g*4,M=h.matrixWorldInverse;o.getNormalMatrix(M),(d===null||d.length<p)&&(d=new Float32Array(p));for(let y=0,b=m;y!==g;++y,b+=4)a.copy(f[y]).applyMatrix4(M,o),a.normal.toArray(d,b),d[b+3]=a.constant}c.value=d,c.needsUpdate=!0}return e.numPlanes=g,e.numIntersection=0,d}}const Wi=4,Pu=[.125,.215,.35,.446,.526,.582],mr=20,Pg=256,ws=new Ec,Du=new rt;let ko=null,zo=0,Vo=0,Go=!1;const Dg=new k;class Lu{constructor(e){this._renderer=e,this._pingPongRenderTarget=null,this._lodMax=0,this._cubeSize=0,this._sizeLods=[],this._sigmas=[],this._lodMeshes=[],this._backgroundBox=null,this._cubemapMaterial=null,this._equirectMaterial=null,this._blurMaterial=null,this._ggxMaterial=null}fromScene(e,t=0,n=.1,i=100,s={}){const{size:a=256,position:o=Dg}=s;ko=this._renderer.getRenderTarget(),zo=this._renderer.getActiveCubeFace(),Vo=this._renderer.getActiveMipmapLevel(),Go=this._renderer.xr.enabled,this._renderer.xr.enabled=!1,this._setSize(a);const c=this._allocateTargets();return c.depthBuffer=!0,this._sceneToCubeUV(e,n,i,c,o),t>0&&this._blur(c,0,0,t),this._applyPMREM(c),this._cleanup(c),c}fromEquirectangular(e,t=null){return this._fromTexture(e,t)}fromCubemap(e,t=null){return this._fromTexture(e,t)}compileCubemapShader(){this._cubemapMaterial===null&&(this._cubemapMaterial=Nu(),this._compileMaterial(this._cubemapMaterial))}compileEquirectangularShader(){this._equirectMaterial===null&&(this._equirectMaterial=Uu(),this._compileMaterial(this._equirectMaterial))}dispose(){this._dispose(),this._cubemapMaterial!==null&&this._cubemapMaterial.dispose(),this._equirectMaterial!==null&&this._equirectMaterial.dispose(),this._backgroundBox!==null&&(this._backgroundBox.geometry.dispose(),this._backgroundBox.material.dispose())}_setSize(e){this._lodMax=Math.floor(Math.log2(e)),this._cubeSize=Math.pow(2,this._lodMax)}_dispose(){this._blurMaterial!==null&&this._blurMaterial.dispose(),this._ggxMaterial!==null&&this._ggxMaterial.dispose(),this._pingPongRenderTarget!==null&&this._pingPongRenderTarget.dispose();for(let e=0;e<this._lodMeshes.length;e++)this._lodMeshes[e].geometry.dispose()}_cleanup(e){this._renderer.setRenderTarget(ko,zo,Vo),this._renderer.xr.enabled=Go,e.scissorTest=!1,qr(e,0,0,e.width,e.height)}_fromTexture(e,t){e.mapping===Er||e.mapping===as?this._setSize(e.image.length===0?16:e.image[0].width||e.image[0].image.width):this._setSize(e.image.width/4),ko=this._renderer.getRenderTarget(),zo=this._renderer.getActiveCubeFace(),Vo=this._renderer.getActiveMipmapLevel(),Go=this._renderer.xr.enabled,this._renderer.xr.enabled=!1;const n=t||this._allocateTargets();return this._textureToCubeUV(e,n),this._applyPMREM(n),this._cleanup(n),n}_allocateTargets(){const e=3*Math.max(this._cubeSize,112),t=4*this._cubeSize,n={magFilter:nn,minFilter:nn,generateMipmaps:!1,type:wi,format:Kn,colorSpace:ls,depthBuffer:!1},i=Iu(e,t,n);if(this._pingPongRenderTarget===null||this._pingPongRenderTarget.width!==e||this._pingPongRenderTarget.height!==t){this._pingPongRenderTarget!==null&&this._dispose(),this._pingPongRenderTarget=Iu(e,t,n);const{_lodMax:s}=this;({lodMeshes:this._lodMeshes,sizeLods:this._sizeLods,sigmas:this._sigmas}=Lg(s)),this._blurMaterial=Ug(s,e,t),this._ggxMaterial=Ig(s,e,t)}return i}_compileMaterial(e){const t=new Yt(new cn,e);this._renderer.compile(t,ws)}_sceneToCubeUV(e,t,n,i,s){const c=new Rn(90,1,t,n),l=[1,-1,1,1,1,1],u=[1,1,1,-1,-1,-1],f=this._renderer,h=f.autoClear,m=f.toneMapping;f.getClearColor(Du),f.toneMapping=ci,f.autoClear=!1,f.state.buffers.depth.getReversed()&&(f.setRenderTarget(i),f.clearDepth(),f.setRenderTarget(null)),this._backgroundBox===null&&(this._backgroundBox=new Yt(new dn,new qa({name:"PMREM.Background",side:pn,depthWrite:!1,depthTest:!1})));const g=this._backgroundBox,d=g.material;let p=!1;const M=e.background;M?M.isColor&&(d.color.copy(M),e.background=null,p=!0):(d.color.copy(Du),p=!0);for(let y=0;y<6;y++){const b=y%3;b===0?(c.up.set(0,l[y],0),c.position.set(s.x,s.y,s.z),c.lookAt(s.x+u[y],s.y,s.z)):b===1?(c.up.set(0,0,l[y]),c.position.set(s.x,s.y,s.z),c.lookAt(s.x,s.y+u[y],s.z)):(c.up.set(0,l[y],0),c.position.set(s.x,s.y,s.z),c.lookAt(s.x,s.y,s.z+u[y]));const T=this._cubeSize;qr(i,b*T,y>2?T:0,T,T),f.setRenderTarget(i),p&&f.render(g,c),f.render(e,c)}f.toneMapping=m,f.autoClear=h,e.background=M}_textureToCubeUV(e,t){const n=this._renderer,i=e.mapping===Er||e.mapping===as;i?(this._cubemapMaterial===null&&(this._cubemapMaterial=Nu()),this._cubemapMaterial.uniforms.flipEnvMap.value=e.isRenderTargetTexture===!1?-1:1):this._equirectMaterial===null&&(this._equirectMaterial=Uu());const s=i?this._cubemapMaterial:this._equirectMaterial,a=this._lodMeshes[0];a.material=s;const o=s.uniforms;o.envMap.value=e;const c=this._cubeSize;qr(t,0,0,3*c,2*c),n.setRenderTarget(t),n.render(a,ws)}_applyPMREM(e){const t=this._renderer,n=t.autoClear;t.autoClear=!1;const i=this._lodMeshes.length;for(let s=1;s<i;s++)this._applyGGXFilter(e,s-1,s);t.autoClear=n}_applyGGXFilter(e,t,n){const i=this._renderer,s=this._pingPongRenderTarget,a=this._ggxMaterial,o=this._lodMeshes[n];o.material=a;const c=a.uniforms,l=n/(this._lodMeshes.length-1),u=t/(this._lodMeshes.length-1),f=Math.sqrt(l*l-u*u),h=0+l*1.25,m=f*h,{_lodMax:_}=this,g=this._sizeLods[n],d=3*g*(n>_-Wi?n-_+Wi:0),p=4*(this._cubeSize-g);c.envMap.value=e.texture,c.roughness.value=m,c.mipInt.value=_-t,qr(s,d,p,3*g,2*g),i.setRenderTarget(s),i.render(o,ws),c.envMap.value=s.texture,c.roughness.value=0,c.mipInt.value=_-n,qr(e,d,p,3*g,2*g),i.setRenderTarget(e),i.render(o,ws)}_blur(e,t,n,i,s){const a=this._pingPongRenderTarget;this._halfBlur(e,a,t,n,i,"latitudinal",s),this._halfBlur(a,e,n,n,i,"longitudinal",s)}_halfBlur(e,t,n,i,s,a,o){const c=this._renderer,l=this._blurMaterial;a!=="latitudinal"&&a!=="longitudinal"&&lt("blur direction must be either latitudinal or longitudinal!");const u=3,f=this._lodMeshes[i];f.material=l;const h=l.uniforms,m=this._sizeLods[n]-1,_=isFinite(s)?Math.PI/(2*m):2*Math.PI/(2*mr-1),g=s/_,d=isFinite(s)?1+Math.floor(u*g):mr;d>mr&&Xe(`sigmaRadians, ${s}, is too large and will clip, as it requested ${d} samples when the maximum is set to ${mr}`);const p=[];let M=0;for(let R=0;R<mr;++R){const x=R/g,S=Math.exp(-x*x/2);p.push(S),R===0?M+=S:R<d&&(M+=2*S)}for(let R=0;R<p.length;R++)p[R]=p[R]/M;h.envMap.value=e.texture,h.samples.value=d,h.weights.value=p,h.latitudinal.value=a==="latitudinal",o&&(h.poleAxis.value=o);const{_lodMax:y}=this;h.dTheta.value=_,h.mipInt.value=y-n;const b=this._sizeLods[i],T=3*b*(i>y-Wi?i-y+Wi:0),A=4*(this._cubeSize-b);qr(t,T,A,3*b,2*b),c.setRenderTarget(t),c.render(f,ws)}}function Lg(r){const e=[],t=[],n=[];let i=r;const s=r-Wi+1+Pu.length;for(let a=0;a<s;a++){const o=Math.pow(2,i);e.push(o);let c=1/o;a>r-Wi?c=Pu[a-r+Wi-1]:a===0&&(c=0),t.push(c);const l=1/(o-2),u=-l,f=1+l,h=[u,u,f,u,f,f,u,u,f,f,u,f],m=6,_=6,g=3,d=2,p=1,M=new Float32Array(g*_*m),y=new Float32Array(d*_*m),b=new Float32Array(p*_*m);for(let A=0;A<m;A++){const R=A%3*2/3-1,x=A>2?0:-1,S=[R,x,0,R+2/3,x,0,R+2/3,x+1,0,R,x,0,R+2/3,x+1,0,R,x+1,0];M.set(S,g*_*A),y.set(h,d*_*A);const W=[A,A,A,A,A,A];b.set(W,p*_*A)}const T=new cn;T.setAttribute("position",new Zn(M,g)),T.setAttribute("uv",new Zn(y,d)),T.setAttribute("faceIndex",new Zn(b,p)),n.push(new Yt(T,null)),i>Wi&&i--}return{lodMeshes:n,sizeLods:e,sigmas:t}}function Iu(r,e,t){const n=new ui(r,e,t);return n.texture.mapping=no,n.texture.name="PMREM.cubeUv",n.scissorTest=!0,n}function qr(r,e,t,n,i){r.viewport.set(e,t,n,i),r.scissor.set(e,t,n,i)}function Ig(r,e,t){return new di({name:"PMREMGGXConvolution",defines:{GGX_SAMPLES:Pg,CUBEUV_TEXEL_WIDTH:1/e,CUBEUV_TEXEL_HEIGHT:1/t,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},roughness:{value:0},mipInt:{value:0}},vertexShader:io(),fragmentShader:`

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
		`,blending:Ti,depthTest:!1,depthWrite:!1})}function Ug(r,e,t){const n=new Float32Array(mr),i=new k(0,1,0);return new di({name:"SphericalGaussianBlur",defines:{n:mr,CUBEUV_TEXEL_WIDTH:1/e,CUBEUV_TEXEL_HEIGHT:1/t,CUBEUV_MAX_MIP:`${r}.0`},uniforms:{envMap:{value:null},samples:{value:1},weights:{value:n},latitudinal:{value:!1},dTheta:{value:0},mipInt:{value:0},poleAxis:{value:i}},vertexShader:io(),fragmentShader:`

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
		`,blending:Ti,depthTest:!1,depthWrite:!1})}function Uu(){return new di({name:"EquirectangularToCubeUV",uniforms:{envMap:{value:null}},vertexShader:io(),fragmentShader:`

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
		`,blending:Ti,depthTest:!1,depthWrite:!1})}function Nu(){return new di({name:"CubemapToCubeUV",uniforms:{envMap:{value:null},flipEnvMap:{value:-1}},vertexShader:io(),fragmentShader:`

			precision mediump float;
			precision mediump int;

			uniform float flipEnvMap;

			varying vec3 vOutputDirection;

			uniform samplerCube envMap;

			void main() {

				gl_FragColor = textureCube( envMap, vec3( flipEnvMap * vOutputDirection.x, vOutputDirection.yz ) );

			}
		`,blending:Ti,depthTest:!1,depthWrite:!1})}function io(){return`

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
	`}class nf extends ui{constructor(e=1,t={}){super(e,e,t),this.isWebGLCubeRenderTarget=!0;const n={width:e,height:e,depth:1},i=[n,n,n,n,n,n];this.texture=new Zh(i),this._setTextureOptions(t),this.texture.isRenderTargetTexture=!0}fromEquirectangularTexture(e,t){this.texture.type=t.type,this.texture.colorSpace=t.colorSpace,this.texture.generateMipmaps=t.generateMipmaps,this.texture.minFilter=t.minFilter,this.texture.magFilter=t.magFilter;const n={uniforms:{tEquirect:{value:null}},vertexShader:`

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
			`},i=new dn(5,5,5),s=new di({name:"CubemapFromEquirect",uniforms:cs(n.uniforms),vertexShader:n.vertexShader,fragmentShader:n.fragmentShader,side:pn,blending:Ti});s.uniforms.tEquirect.value=t;const a=new Yt(i,s),o=t.minFilter;return t.minFilter===gr&&(t.minFilter=nn),new Bp(1,10,this).update(e,a),t.minFilter=o,a.geometry.dispose(),a.material.dispose(),this}clear(e,t=!0,n=!0,i=!0){const s=e.getRenderTarget();for(let a=0;a<6;a++)e.setRenderTarget(this,a),e.clear(t,n,i);e.setRenderTarget(s)}}function Ng(r){let e=new WeakMap,t=new WeakMap,n=null;function i(h,m=!1){return h==null?null:m?a(h):s(h)}function s(h){if(h&&h.isTexture){const m=h.mapping;if(m===lo||m===co)if(e.has(h)){const _=e.get(h).texture;return o(_,h.mapping)}else{const _=h.image;if(_&&_.height>0){const g=new nf(_.height);return g.fromEquirectangularTexture(r,h),e.set(h,g),h.addEventListener("dispose",l),o(g.texture,h.mapping)}else return null}}return h}function a(h){if(h&&h.isTexture){const m=h.mapping,_=m===lo||m===co,g=m===Er||m===as;if(_||g){let d=t.get(h);const p=d!==void 0?d.texture.pmremVersion:0;if(h.isRenderTargetTexture&&h.pmremVersion!==p)return n===null&&(n=new Lu(r)),d=_?n.fromEquirectangular(h,d):n.fromCubemap(h,d),d.texture.pmremVersion=h.pmremVersion,t.set(h,d),d.texture;if(d!==void 0)return d.texture;{const M=h.image;return _&&M&&M.height>0||g&&M&&c(M)?(n===null&&(n=new Lu(r)),d=_?n.fromEquirectangular(h):n.fromCubemap(h),d.texture.pmremVersion=h.pmremVersion,t.set(h,d),h.addEventListener("dispose",u),d.texture):null}}}return h}function o(h,m){return m===lo?h.mapping=Er:m===co&&(h.mapping=as),h}function c(h){let m=0;const _=6;for(let g=0;g<_;g++)h[g]!==void 0&&m++;return m===_}function l(h){const m=h.target;m.removeEventListener("dispose",l);const _=e.get(m);_!==void 0&&(e.delete(m),_.dispose())}function u(h){const m=h.target;m.removeEventListener("dispose",u);const _=t.get(m);_!==void 0&&(t.delete(m),_.dispose())}function f(){e=new WeakMap,t=new WeakMap,n!==null&&(n.dispose(),n=null)}return{get:i,dispose:f}}function Fg(r){const e={};function t(n){if(e[n]!==void 0)return e[n];const i=r.getExtension(n);return e[n]=i,i}return{has:function(n){return t(n)!==null},init:function(){t("EXT_color_buffer_float"),t("WEBGL_clip_cull_distance"),t("OES_texture_float_linear"),t("EXT_color_buffer_half_float"),t("WEBGL_multisampled_render_to_texture"),t("WEBGL_render_shared_exponent")},get:function(n){const i=t(n);return i===null&&Xa("WebGLRenderer: "+n+" extension not supported."),i}}}function Og(r,e,t,n){const i={},s=new WeakMap;function a(f){const h=f.target;h.index!==null&&e.remove(h.index);for(const _ in h.attributes)e.remove(h.attributes[_]);h.removeEventListener("dispose",a),delete i[h.id];const m=s.get(h);m&&(e.remove(m),s.delete(h)),n.releaseStatesOfGeometry(h),h.isInstancedBufferGeometry===!0&&delete h._maxInstanceCount,t.memory.geometries--}function o(f,h){return i[h.id]===!0||(h.addEventListener("dispose",a),i[h.id]=!0,t.memory.geometries++),h}function c(f){const h=f.attributes;for(const m in h)e.update(h[m],r.ARRAY_BUFFER)}function l(f){const h=[],m=f.index,_=f.attributes.position;let g=0;if(_===void 0)return;if(m!==null){const M=m.array;g=m.version;for(let y=0,b=M.length;y<b;y+=3){const T=M[y+0],A=M[y+1],R=M[y+2];h.push(T,A,A,R,R,T)}}else{const M=_.array;g=_.version;for(let y=0,b=M.length/3-1;y<b;y+=3){const T=y+0,A=y+1,R=y+2;h.push(T,A,A,R,R,T)}}const d=new(_.count>=65535?$h:Yh)(h,1);d.version=g;const p=s.get(f);p&&e.remove(p),s.set(f,d)}function u(f){const h=s.get(f);if(h){const m=f.index;m!==null&&h.version<m.version&&l(f)}else l(f);return s.get(f)}return{get:o,update:c,getWireframeAttribute:u}}function Bg(r,e,t){let n;function i(h){n=h}let s,a;function o(h){s=h.type,a=h.bytesPerElement}function c(h,m){r.drawElements(n,m,s,h*a),t.update(m,n,1)}function l(h,m,_){_!==0&&(r.drawElementsInstanced(n,m,s,h*a,_),t.update(m,n,_))}function u(h,m,_){if(_===0)return;e.get("WEBGL_multi_draw").multiDrawElementsWEBGL(n,m,0,s,h,0,_);let d=0;for(let p=0;p<_;p++)d+=m[p];t.update(d,n,1)}function f(h,m,_,g){if(_===0)return;const d=e.get("WEBGL_multi_draw");if(d===null)for(let p=0;p<h.length;p++)l(h[p]/a,m[p],g[p]);else{d.multiDrawElementsInstancedWEBGL(n,m,0,s,h,0,g,0,_);let p=0;for(let M=0;M<_;M++)p+=m[M]*g[M];t.update(p,n,1)}}this.setMode=i,this.setIndex=o,this.render=c,this.renderInstances=l,this.renderMultiDraw=u,this.renderMultiDrawInstances=f}function kg(r){const e={geometries:0,textures:0},t={frame:0,calls:0,triangles:0,points:0,lines:0};function n(s,a,o){switch(t.calls++,a){case r.TRIANGLES:t.triangles+=o*(s/3);break;case r.LINES:t.lines+=o*(s/2);break;case r.LINE_STRIP:t.lines+=o*(s-1);break;case r.LINE_LOOP:t.lines+=o*s;break;case r.POINTS:t.points+=o*s;break;default:lt("WebGLInfo: Unknown draw mode:",a);break}}function i(){t.calls=0,t.triangles=0,t.points=0,t.lines=0}return{memory:e,render:t,programs:null,autoReset:!0,reset:i,update:n}}function zg(r,e,t){const n=new WeakMap,i=new Dt;function s(a,o,c){const l=a.morphTargetInfluences,u=o.morphAttributes.position||o.morphAttributes.normal||o.morphAttributes.color,f=u!==void 0?u.length:0;let h=n.get(o);if(h===void 0||h.count!==f){let S=function(){R.dispose(),n.delete(o),o.removeEventListener("dispose",S)};h!==void 0&&h.texture.dispose();const m=o.morphAttributes.position!==void 0,_=o.morphAttributes.normal!==void 0,g=o.morphAttributes.color!==void 0,d=o.morphAttributes.position||[],p=o.morphAttributes.normal||[],M=o.morphAttributes.color||[];let y=0;m===!0&&(y=1),_===!0&&(y=2),g===!0&&(y=3);let b=o.attributes.position.count*y,T=1;b>e.maxTextureSize&&(T=Math.ceil(b/e.maxTextureSize),b=e.maxTextureSize);const A=new Float32Array(b*T*4*f),R=new Xh(A,b,T,f);R.type=ai,R.needsUpdate=!0;const x=y*4;for(let W=0;W<f;W++){const P=d[W],z=p[W],F=M[W],B=b*T*4*W;for(let O=0;O<P.count;O++){const V=O*x;m===!0&&(i.fromBufferAttribute(P,O),A[B+V+0]=i.x,A[B+V+1]=i.y,A[B+V+2]=i.z,A[B+V+3]=0),_===!0&&(i.fromBufferAttribute(z,O),A[B+V+4]=i.x,A[B+V+5]=i.y,A[B+V+6]=i.z,A[B+V+7]=0),g===!0&&(i.fromBufferAttribute(F,O),A[B+V+8]=i.x,A[B+V+9]=i.y,A[B+V+10]=i.z,A[B+V+11]=F.itemSize===4?i.w:1)}}h={count:f,texture:R,size:new $e(b,T)},n.set(o,h),o.addEventListener("dispose",S)}if(a.isInstancedMesh===!0&&a.morphTexture!==null)c.getUniforms().setValue(r,"morphTexture",a.morphTexture,t);else{let m=0;for(let g=0;g<l.length;g++)m+=l[g];const _=o.morphTargetsRelative?1:1-m;c.getUniforms().setValue(r,"morphTargetBaseInfluence",_),c.getUniforms().setValue(r,"morphTargetInfluences",l)}c.getUniforms().setValue(r,"morphTargetsTexture",h.texture,t),c.getUniforms().setValue(r,"morphTargetsTextureSize",h.size)}return{update:s}}function Vg(r,e,t,n,i){let s=new WeakMap;function a(l){const u=i.render.frame,f=l.geometry,h=e.get(l,f);if(s.get(h)!==u&&(e.update(h),s.set(h,u)),l.isInstancedMesh&&(l.hasEventListener("dispose",c)===!1&&l.addEventListener("dispose",c),s.get(l)!==u&&(t.update(l.instanceMatrix,r.ARRAY_BUFFER),l.instanceColor!==null&&t.update(l.instanceColor,r.ARRAY_BUFFER),s.set(l,u))),l.isSkinnedMesh){const m=l.skeleton;s.get(m)!==u&&(m.update(),s.set(m,u))}return h}function o(){s=new WeakMap}function c(l){const u=l.target;u.removeEventListener("dispose",c),n.releaseStatesOfObject(u),t.remove(u.instanceMatrix),u.instanceColor!==null&&t.remove(u.instanceColor)}return{update:a,dispose:o}}const Gg={[Ch]:"LINEAR_TONE_MAPPING",[Ph]:"REINHARD_TONE_MAPPING",[Dh]:"CINEON_TONE_MAPPING",[cc]:"ACES_FILMIC_TONE_MAPPING",[Ih]:"AGX_TONE_MAPPING",[Uh]:"NEUTRAL_TONE_MAPPING",[Lh]:"CUSTOM_TONE_MAPPING"};function Hg(r,e,t,n,i){const s=new ui(e,t,{type:r,depthBuffer:n,stencilBuffer:i}),a=new ui(e,t,{type:wi,depthBuffer:!1,stencilBuffer:!1}),o=new cn;o.setAttribute("position",new Kt([-1,3,0,-1,-1,0,3,-1,0],3)),o.setAttribute("uv",new Kt([0,2,0,0,2,0],2));const c=new Dp({uniforms:{tDiffuse:{value:null}},vertexShader:`
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
			}`,depthTest:!1,depthWrite:!1}),l=new Yt(o,c),u=new Ec(-1,1,1,-1,0,1);let f=null,h=null,m=!1,_,g=null,d=[],p=!1;this.setSize=function(M,y){s.setSize(M,y),a.setSize(M,y);for(let b=0;b<d.length;b++){const T=d[b];T.setSize&&T.setSize(M,y)}},this.setEffects=function(M){d=M,p=d.length>0&&d[0].isRenderPass===!0;const y=s.width,b=s.height;for(let T=0;T<d.length;T++){const A=d[T];A.setSize&&A.setSize(y,b)}},this.begin=function(M,y){if(m||M.toneMapping===ci&&d.length===0)return!1;if(g=y,y!==null){const b=y.width,T=y.height;(s.width!==b||s.height!==T)&&this.setSize(b,T)}return p===!1&&M.setRenderTarget(s),_=M.toneMapping,M.toneMapping=ci,!0},this.hasRenderPass=function(){return p},this.end=function(M,y){M.toneMapping=_,m=!0;let b=s,T=a;for(let A=0;A<d.length;A++){const R=d[A];if(R.enabled!==!1&&(R.render(M,T,b,y),R.needsSwap!==!1)){const x=b;b=T,T=x}}if(f!==M.outputColorSpace||h!==M.toneMapping){f=M.outputColorSpace,h=M.toneMapping,c.defines={},ct.getTransfer(f)===mt&&(c.defines.SRGB_TRANSFER="");const A=Gg[h];A&&(c.defines[A]=""),c.needsUpdate=!0}c.uniforms.tDiffuse.value=b.texture,M.setRenderTarget(g),M.render(l,u),g=null,m=!1},this.isCompositing=function(){return m},this.dispose=function(){s.dispose(),a.dispose(),o.dispose(),c.dispose()}}const rf=new ln,ql=new Gs(1,1),sf=new Xh,af=new op,of=new Zh,Fu=[],Ou=[],Bu=new Float32Array(16),ku=new Float32Array(9),zu=new Float32Array(4);function gs(r,e,t){const n=r[0];if(n<=0||n>0)return r;const i=e*t;let s=Fu[i];if(s===void 0&&(s=new Float32Array(i),Fu[i]=s),e!==0){n.toArray(s,0);for(let a=1,o=0;a!==e;++a)o+=t,r[a].toArray(s,o)}return s}function kt(r,e){if(r.length!==e.length)return!1;for(let t=0,n=r.length;t<n;t++)if(r[t]!==e[t])return!1;return!0}function zt(r,e){for(let t=0,n=e.length;t<n;t++)r[t]=e[t]}function ro(r,e){let t=Ou[e];t===void 0&&(t=new Int32Array(e),Ou[e]=t);for(let n=0;n!==e;++n)t[n]=r.allocateTextureUnit();return t}function Wg(r,e){const t=this.cache;t[0]!==e&&(r.uniform1f(this.addr,e),t[0]=e)}function Xg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(r.uniform2f(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(kt(t,e))return;r.uniform2fv(this.addr,e),zt(t,e)}}function qg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(r.uniform3f(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else if(e.r!==void 0)(t[0]!==e.r||t[1]!==e.g||t[2]!==e.b)&&(r.uniform3f(this.addr,e.r,e.g,e.b),t[0]=e.r,t[1]=e.g,t[2]=e.b);else{if(kt(t,e))return;r.uniform3fv(this.addr,e),zt(t,e)}}function Yg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(r.uniform4f(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(kt(t,e))return;r.uniform4fv(this.addr,e),zt(t,e)}}function $g(r,e){const t=this.cache,n=e.elements;if(n===void 0){if(kt(t,e))return;r.uniformMatrix2fv(this.addr,!1,e),zt(t,e)}else{if(kt(t,n))return;zu.set(n),r.uniformMatrix2fv(this.addr,!1,zu),zt(t,n)}}function Kg(r,e){const t=this.cache,n=e.elements;if(n===void 0){if(kt(t,e))return;r.uniformMatrix3fv(this.addr,!1,e),zt(t,e)}else{if(kt(t,n))return;ku.set(n),r.uniformMatrix3fv(this.addr,!1,ku),zt(t,n)}}function Zg(r,e){const t=this.cache,n=e.elements;if(n===void 0){if(kt(t,e))return;r.uniformMatrix4fv(this.addr,!1,e),zt(t,e)}else{if(kt(t,n))return;Bu.set(n),r.uniformMatrix4fv(this.addr,!1,Bu),zt(t,n)}}function jg(r,e){const t=this.cache;t[0]!==e&&(r.uniform1i(this.addr,e),t[0]=e)}function Jg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(r.uniform2i(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(kt(t,e))return;r.uniform2iv(this.addr,e),zt(t,e)}}function Qg(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(r.uniform3i(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else{if(kt(t,e))return;r.uniform3iv(this.addr,e),zt(t,e)}}function e0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(r.uniform4i(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(kt(t,e))return;r.uniform4iv(this.addr,e),zt(t,e)}}function t0(r,e){const t=this.cache;t[0]!==e&&(r.uniform1ui(this.addr,e),t[0]=e)}function n0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y)&&(r.uniform2ui(this.addr,e.x,e.y),t[0]=e.x,t[1]=e.y);else{if(kt(t,e))return;r.uniform2uiv(this.addr,e),zt(t,e)}}function i0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z)&&(r.uniform3ui(this.addr,e.x,e.y,e.z),t[0]=e.x,t[1]=e.y,t[2]=e.z);else{if(kt(t,e))return;r.uniform3uiv(this.addr,e),zt(t,e)}}function r0(r,e){const t=this.cache;if(e.x!==void 0)(t[0]!==e.x||t[1]!==e.y||t[2]!==e.z||t[3]!==e.w)&&(r.uniform4ui(this.addr,e.x,e.y,e.z,e.w),t[0]=e.x,t[1]=e.y,t[2]=e.z,t[3]=e.w);else{if(kt(t,e))return;r.uniform4uiv(this.addr,e),zt(t,e)}}function s0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i);let s;this.type===r.SAMPLER_2D_SHADOW?(ql.compareFunction=t.isReversedDepthBuffer()?gc:_c,s=ql):s=rf,t.setTexture2D(e||s,i)}function a0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i),t.setTexture3D(e||af,i)}function o0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i),t.setTextureCube(e||of,i)}function l0(r,e,t){const n=this.cache,i=t.allocateTextureUnit();n[0]!==i&&(r.uniform1i(this.addr,i),n[0]=i),t.setTexture2DArray(e||sf,i)}function c0(r){switch(r){case 5126:return Wg;case 35664:return Xg;case 35665:return qg;case 35666:return Yg;case 35674:return $g;case 35675:return Kg;case 35676:return Zg;case 5124:case 35670:return jg;case 35667:case 35671:return Jg;case 35668:case 35672:return Qg;case 35669:case 35673:return e0;case 5125:return t0;case 36294:return n0;case 36295:return i0;case 36296:return r0;case 35678:case 36198:case 36298:case 36306:case 35682:return s0;case 35679:case 36299:case 36307:return a0;case 35680:case 36300:case 36308:case 36293:return o0;case 36289:case 36303:case 36311:case 36292:return l0}}function u0(r,e){r.uniform1fv(this.addr,e)}function h0(r,e){const t=gs(e,this.size,2);r.uniform2fv(this.addr,t)}function f0(r,e){const t=gs(e,this.size,3);r.uniform3fv(this.addr,t)}function d0(r,e){const t=gs(e,this.size,4);r.uniform4fv(this.addr,t)}function p0(r,e){const t=gs(e,this.size,4);r.uniformMatrix2fv(this.addr,!1,t)}function m0(r,e){const t=gs(e,this.size,9);r.uniformMatrix3fv(this.addr,!1,t)}function _0(r,e){const t=gs(e,this.size,16);r.uniformMatrix4fv(this.addr,!1,t)}function g0(r,e){r.uniform1iv(this.addr,e)}function v0(r,e){r.uniform2iv(this.addr,e)}function x0(r,e){r.uniform3iv(this.addr,e)}function M0(r,e){r.uniform4iv(this.addr,e)}function S0(r,e){r.uniform1uiv(this.addr,e)}function y0(r,e){r.uniform2uiv(this.addr,e)}function b0(r,e){r.uniform3uiv(this.addr,e)}function E0(r,e){r.uniform4uiv(this.addr,e)}function T0(r,e,t){const n=this.cache,i=e.length,s=ro(t,i);kt(n,s)||(r.uniform1iv(this.addr,s),zt(n,s));let a;this.type===r.SAMPLER_2D_SHADOW?a=ql:a=rf;for(let o=0;o!==i;++o)t.setTexture2D(e[o]||a,s[o])}function A0(r,e,t){const n=this.cache,i=e.length,s=ro(t,i);kt(n,s)||(r.uniform1iv(this.addr,s),zt(n,s));for(let a=0;a!==i;++a)t.setTexture3D(e[a]||af,s[a])}function w0(r,e,t){const n=this.cache,i=e.length,s=ro(t,i);kt(n,s)||(r.uniform1iv(this.addr,s),zt(n,s));for(let a=0;a!==i;++a)t.setTextureCube(e[a]||of,s[a])}function R0(r,e,t){const n=this.cache,i=e.length,s=ro(t,i);kt(n,s)||(r.uniform1iv(this.addr,s),zt(n,s));for(let a=0;a!==i;++a)t.setTexture2DArray(e[a]||sf,s[a])}function C0(r){switch(r){case 5126:return u0;case 35664:return h0;case 35665:return f0;case 35666:return d0;case 35674:return p0;case 35675:return m0;case 35676:return _0;case 5124:case 35670:return g0;case 35667:case 35671:return v0;case 35668:case 35672:return x0;case 35669:case 35673:return M0;case 5125:return S0;case 36294:return y0;case 36295:return b0;case 36296:return E0;case 35678:case 36198:case 36298:case 36306:case 35682:return T0;case 35679:case 36299:case 36307:return A0;case 35680:case 36300:case 36308:case 36293:return w0;case 36289:case 36303:case 36311:case 36292:return R0}}class P0{constructor(e,t,n){this.id=e,this.addr=n,this.cache=[],this.type=t.type,this.setValue=c0(t.type)}}class D0{constructor(e,t,n){this.id=e,this.addr=n,this.cache=[],this.type=t.type,this.size=t.size,this.setValue=C0(t.type)}}class L0{constructor(e){this.id=e,this.seq=[],this.map={}}setValue(e,t,n){const i=this.seq;for(let s=0,a=i.length;s!==a;++s){const o=i[s];o.setValue(e,t[o.id],n)}}}const Ho=/(\w+)(\])?(\[|\.)?/g;function Vu(r,e){r.seq.push(e),r.map[e.id]=e}function I0(r,e,t){const n=r.name,i=n.length;for(Ho.lastIndex=0;;){const s=Ho.exec(n),a=Ho.lastIndex;let o=s[1];const c=s[2]==="]",l=s[3];if(c&&(o=o|0),l===void 0||l==="["&&a+2===i){Vu(t,l===void 0?new P0(o,r,e):new D0(o,r,e));break}else{let f=t.map[o];f===void 0&&(f=new L0(o),Vu(t,f)),t=f}}}class ka{constructor(e,t){this.seq=[],this.map={};const n=e.getProgramParameter(t,e.ACTIVE_UNIFORMS);for(let a=0;a<n;++a){const o=e.getActiveUniform(t,a),c=e.getUniformLocation(t,o.name);I0(o,c,this)}const i=[],s=[];for(const a of this.seq)a.type===e.SAMPLER_2D_SHADOW||a.type===e.SAMPLER_CUBE_SHADOW||a.type===e.SAMPLER_2D_ARRAY_SHADOW?i.push(a):s.push(a);i.length>0&&(this.seq=i.concat(s))}setValue(e,t,n,i){const s=this.map[t];s!==void 0&&s.setValue(e,n,i)}setOptional(e,t,n){const i=t[n];i!==void 0&&this.setValue(e,n,i)}static upload(e,t,n,i){for(let s=0,a=t.length;s!==a;++s){const o=t[s],c=n[o.id];c.needsUpdate!==!1&&o.setValue(e,c.value,i)}}static seqWithValue(e,t){const n=[];for(let i=0,s=e.length;i!==s;++i){const a=e[i];a.id in t&&n.push(a)}return n}}function Gu(r,e,t){const n=r.createShader(e);return r.shaderSource(n,t),r.compileShader(n),n}const U0=37297;let N0=0;function F0(r,e){const t=r.split(`
`),n=[],i=Math.max(e-6,0),s=Math.min(e+6,t.length);for(let a=i;a<s;a++){const o=a+1;n.push(`${o===e?">":" "} ${o}: ${t[a]}`)}return n.join(`
`)}const Hu=new Ke;function O0(r){ct._getMatrix(Hu,ct.workingColorSpace,r);const e=`mat3( ${Hu.elements.map(t=>t.toFixed(4))} )`;switch(ct.getTransfer(r)){case Ha:return[e,"LinearTransferOETF"];case mt:return[e,"sRGBTransferOETF"];default:return Xe("WebGLProgram: Unsupported color space: ",r),[e,"LinearTransferOETF"]}}function Wu(r,e,t){const n=r.getShaderParameter(e,r.COMPILE_STATUS),s=(r.getShaderInfoLog(e)||"").trim();if(n&&s==="")return"";const a=/ERROR: 0:(\d+)/.exec(s);if(a){const o=parseInt(a[1]);return t.toUpperCase()+`

`+s+`

`+F0(r.getShaderSource(e),o)}else return s}function B0(r,e){const t=O0(e);return[`vec4 ${r}( vec4 value ) {`,`	return ${t[1]}( vec4( value.rgb * ${t[0]}, value.a ) );`,"}"].join(`
`)}const k0={[Ch]:"Linear",[Ph]:"Reinhard",[Dh]:"Cineon",[cc]:"ACESFilmic",[Ih]:"AgX",[Uh]:"Neutral",[Lh]:"Custom"};function z0(r,e){const t=k0[e];return t===void 0?(Xe("WebGLProgram: Unsupported toneMapping:",e),"vec3 "+r+"( vec3 color ) { return LinearToneMapping( color ); }"):"vec3 "+r+"( vec3 color ) { return "+t+"ToneMapping( color ); }"}const Pa=new k;function V0(){ct.getLuminanceCoefficients(Pa);const r=Pa.x.toFixed(4),e=Pa.y.toFixed(4),t=Pa.z.toFixed(4);return["float luminance( const in vec3 rgb ) {",`	const vec3 weights = vec3( ${r}, ${e}, ${t} );`,"	return dot( weights, rgb );","}"].join(`
`)}function G0(r){return[r.extensionClipCullDistance?"#extension GL_ANGLE_clip_cull_distance : require":"",r.extensionMultiDraw?"#extension GL_ANGLE_multi_draw : require":""].filter(Ds).join(`
`)}function H0(r){const e=[];for(const t in r){const n=r[t];n!==!1&&e.push("#define "+t+" "+n)}return e.join(`
`)}function W0(r,e){const t={},n=r.getProgramParameter(e,r.ACTIVE_ATTRIBUTES);for(let i=0;i<n;i++){const s=r.getActiveAttrib(e,i),a=s.name;let o=1;s.type===r.FLOAT_MAT2&&(o=2),s.type===r.FLOAT_MAT3&&(o=3),s.type===r.FLOAT_MAT4&&(o=4),t[a]={type:s.type,location:r.getAttribLocation(e,a),locationSize:o}}return t}function Ds(r){return r!==""}function Xu(r,e){const t=e.numSpotLightShadows+e.numSpotLightMaps-e.numSpotLightShadowsWithMaps;return r.replace(/NUM_DIR_LIGHTS/g,e.numDirLights).replace(/NUM_SPOT_LIGHTS/g,e.numSpotLights).replace(/NUM_SPOT_LIGHT_MAPS/g,e.numSpotLightMaps).replace(/NUM_SPOT_LIGHT_COORDS/g,t).replace(/NUM_RECT_AREA_LIGHTS/g,e.numRectAreaLights).replace(/NUM_POINT_LIGHTS/g,e.numPointLights).replace(/NUM_HEMI_LIGHTS/g,e.numHemiLights).replace(/NUM_DIR_LIGHT_SHADOWS/g,e.numDirLightShadows).replace(/NUM_SPOT_LIGHT_SHADOWS_WITH_MAPS/g,e.numSpotLightShadowsWithMaps).replace(/NUM_SPOT_LIGHT_SHADOWS/g,e.numSpotLightShadows).replace(/NUM_POINT_LIGHT_SHADOWS/g,e.numPointLightShadows)}function qu(r,e){return r.replace(/NUM_CLIPPING_PLANES/g,e.numClippingPlanes).replace(/UNION_CLIPPING_PLANES/g,e.numClippingPlanes-e.numClipIntersection)}const X0=/^[ \t]*#include +<([\w\d./]+)>/gm;function Yl(r){return r.replace(X0,Y0)}const q0=new Map;function Y0(r,e){let t=Ze[e];if(t===void 0){const n=q0.get(e);if(n!==void 0)t=Ze[n],Xe('WebGLRenderer: Shader chunk "%s" has been deprecated. Use "%s" instead.',e,n);else throw new Error("Can not resolve #include <"+e+">")}return Yl(t)}const $0=/#pragma unroll_loop_start\s+for\s*\(\s*int\s+i\s*=\s*(\d+)\s*;\s*i\s*<\s*(\d+)\s*;\s*i\s*\+\+\s*\)\s*{([\s\S]+?)}\s+#pragma unroll_loop_end/g;function Yu(r){return r.replace($0,K0)}function K0(r,e,t,n){let i="";for(let s=parseInt(e);s<parseInt(t);s++)i+=n.replace(/\[\s*i\s*\]/g,"[ "+s+" ]").replace(/UNROLLED_LOOP_INDEX/g,s);return i}function $u(r){let e=`precision ${r.precision} float;
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
#define LOW_PRECISION`),e}const Z0={[Ia]:"SHADOWMAP_TYPE_PCF",[Ps]:"SHADOWMAP_TYPE_VSM"};function j0(r){return Z0[r.shadowMapType]||"SHADOWMAP_TYPE_BASIC"}const J0={[Er]:"ENVMAP_TYPE_CUBE",[as]:"ENVMAP_TYPE_CUBE",[no]:"ENVMAP_TYPE_CUBE_UV"};function Q0(r){return r.envMap===!1?"ENVMAP_TYPE_CUBE":J0[r.envMapMode]||"ENVMAP_TYPE_CUBE"}const ev={[as]:"ENVMAP_MODE_REFRACTION"};function tv(r){return r.envMap===!1?"ENVMAP_MODE_REFLECTION":ev[r.envMapMode]||"ENVMAP_MODE_REFLECTION"}const nv={[Rh]:"ENVMAP_BLENDING_MULTIPLY",[kd]:"ENVMAP_BLENDING_MIX",[zd]:"ENVMAP_BLENDING_ADD"};function iv(r){return r.envMap===!1?"ENVMAP_BLENDING_NONE":nv[r.combine]||"ENVMAP_BLENDING_NONE"}function rv(r){const e=r.envMapCubeUVHeight;if(e===null)return null;const t=Math.log2(e)-2,n=1/e;return{texelWidth:1/(3*Math.max(Math.pow(2,t),7*16)),texelHeight:n,maxMip:t}}function sv(r,e,t,n){const i=r.getContext(),s=t.defines;let a=t.vertexShader,o=t.fragmentShader;const c=j0(t),l=Q0(t),u=tv(t),f=iv(t),h=rv(t),m=G0(t),_=H0(s),g=i.createProgram();let d,p,M=t.glslVersion?"#version "+t.glslVersion+`
`:"";t.isRawShaderMaterial?(d=["#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_].filter(Ds).join(`
`),d.length>0&&(d+=`
`),p=["#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_].filter(Ds).join(`
`),p.length>0&&(p+=`
`)):(d=[$u(t),"#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_,t.extensionClipCullDistance?"#define USE_CLIP_DISTANCE":"",t.batching?"#define USE_BATCHING":"",t.batchingColor?"#define USE_BATCHING_COLOR":"",t.instancing?"#define USE_INSTANCING":"",t.instancingColor?"#define USE_INSTANCING_COLOR":"",t.instancingMorph?"#define USE_INSTANCING_MORPH":"",t.useFog&&t.fog?"#define USE_FOG":"",t.useFog&&t.fogExp2?"#define FOG_EXP2":"",t.map?"#define USE_MAP":"",t.envMap?"#define USE_ENVMAP":"",t.envMap?"#define "+u:"",t.lightMap?"#define USE_LIGHTMAP":"",t.aoMap?"#define USE_AOMAP":"",t.bumpMap?"#define USE_BUMPMAP":"",t.normalMap?"#define USE_NORMALMAP":"",t.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",t.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",t.displacementMap?"#define USE_DISPLACEMENTMAP":"",t.emissiveMap?"#define USE_EMISSIVEMAP":"",t.anisotropy?"#define USE_ANISOTROPY":"",t.anisotropyMap?"#define USE_ANISOTROPYMAP":"",t.clearcoatMap?"#define USE_CLEARCOATMAP":"",t.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",t.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",t.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",t.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",t.specularMap?"#define USE_SPECULARMAP":"",t.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",t.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",t.roughnessMap?"#define USE_ROUGHNESSMAP":"",t.metalnessMap?"#define USE_METALNESSMAP":"",t.alphaMap?"#define USE_ALPHAMAP":"",t.alphaHash?"#define USE_ALPHAHASH":"",t.transmission?"#define USE_TRANSMISSION":"",t.transmissionMap?"#define USE_TRANSMISSIONMAP":"",t.thicknessMap?"#define USE_THICKNESSMAP":"",t.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",t.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",t.mapUv?"#define MAP_UV "+t.mapUv:"",t.alphaMapUv?"#define ALPHAMAP_UV "+t.alphaMapUv:"",t.lightMapUv?"#define LIGHTMAP_UV "+t.lightMapUv:"",t.aoMapUv?"#define AOMAP_UV "+t.aoMapUv:"",t.emissiveMapUv?"#define EMISSIVEMAP_UV "+t.emissiveMapUv:"",t.bumpMapUv?"#define BUMPMAP_UV "+t.bumpMapUv:"",t.normalMapUv?"#define NORMALMAP_UV "+t.normalMapUv:"",t.displacementMapUv?"#define DISPLACEMENTMAP_UV "+t.displacementMapUv:"",t.metalnessMapUv?"#define METALNESSMAP_UV "+t.metalnessMapUv:"",t.roughnessMapUv?"#define ROUGHNESSMAP_UV "+t.roughnessMapUv:"",t.anisotropyMapUv?"#define ANISOTROPYMAP_UV "+t.anisotropyMapUv:"",t.clearcoatMapUv?"#define CLEARCOATMAP_UV "+t.clearcoatMapUv:"",t.clearcoatNormalMapUv?"#define CLEARCOAT_NORMALMAP_UV "+t.clearcoatNormalMapUv:"",t.clearcoatRoughnessMapUv?"#define CLEARCOAT_ROUGHNESSMAP_UV "+t.clearcoatRoughnessMapUv:"",t.iridescenceMapUv?"#define IRIDESCENCEMAP_UV "+t.iridescenceMapUv:"",t.iridescenceThicknessMapUv?"#define IRIDESCENCE_THICKNESSMAP_UV "+t.iridescenceThicknessMapUv:"",t.sheenColorMapUv?"#define SHEEN_COLORMAP_UV "+t.sheenColorMapUv:"",t.sheenRoughnessMapUv?"#define SHEEN_ROUGHNESSMAP_UV "+t.sheenRoughnessMapUv:"",t.specularMapUv?"#define SPECULARMAP_UV "+t.specularMapUv:"",t.specularColorMapUv?"#define SPECULAR_COLORMAP_UV "+t.specularColorMapUv:"",t.specularIntensityMapUv?"#define SPECULAR_INTENSITYMAP_UV "+t.specularIntensityMapUv:"",t.transmissionMapUv?"#define TRANSMISSIONMAP_UV "+t.transmissionMapUv:"",t.thicknessMapUv?"#define THICKNESSMAP_UV "+t.thicknessMapUv:"",t.vertexTangents&&t.flatShading===!1?"#define USE_TANGENT":"",t.vertexColors?"#define USE_COLOR":"",t.vertexAlphas?"#define USE_COLOR_ALPHA":"",t.vertexUv1s?"#define USE_UV1":"",t.vertexUv2s?"#define USE_UV2":"",t.vertexUv3s?"#define USE_UV3":"",t.pointsUvs?"#define USE_POINTS_UV":"",t.flatShading?"#define FLAT_SHADED":"",t.skinning?"#define USE_SKINNING":"",t.morphTargets?"#define USE_MORPHTARGETS":"",t.morphNormals&&t.flatShading===!1?"#define USE_MORPHNORMALS":"",t.morphColors?"#define USE_MORPHCOLORS":"",t.morphTargetsCount>0?"#define MORPHTARGETS_TEXTURE_STRIDE "+t.morphTextureStride:"",t.morphTargetsCount>0?"#define MORPHTARGETS_COUNT "+t.morphTargetsCount:"",t.doubleSided?"#define DOUBLE_SIDED":"",t.flipSided?"#define FLIP_SIDED":"",t.shadowMapEnabled?"#define USE_SHADOWMAP":"",t.shadowMapEnabled?"#define "+c:"",t.sizeAttenuation?"#define USE_SIZEATTENUATION":"",t.numLightProbes>0?"#define USE_LIGHT_PROBES":"",t.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",t.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 modelMatrix;","uniform mat4 modelViewMatrix;","uniform mat4 projectionMatrix;","uniform mat4 viewMatrix;","uniform mat3 normalMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;","#ifdef USE_INSTANCING","	attribute mat4 instanceMatrix;","#endif","#ifdef USE_INSTANCING_COLOR","	attribute vec3 instanceColor;","#endif","#ifdef USE_INSTANCING_MORPH","	uniform sampler2D morphTexture;","#endif","attribute vec3 position;","attribute vec3 normal;","attribute vec2 uv;","#ifdef USE_UV1","	attribute vec2 uv1;","#endif","#ifdef USE_UV2","	attribute vec2 uv2;","#endif","#ifdef USE_UV3","	attribute vec2 uv3;","#endif","#ifdef USE_TANGENT","	attribute vec4 tangent;","#endif","#if defined( USE_COLOR_ALPHA )","	attribute vec4 color;","#elif defined( USE_COLOR )","	attribute vec3 color;","#endif","#ifdef USE_SKINNING","	attribute vec4 skinIndex;","	attribute vec4 skinWeight;","#endif",`
`].filter(Ds).join(`
`),p=[$u(t),"#define SHADER_TYPE "+t.shaderType,"#define SHADER_NAME "+t.shaderName,_,t.useFog&&t.fog?"#define USE_FOG":"",t.useFog&&t.fogExp2?"#define FOG_EXP2":"",t.alphaToCoverage?"#define ALPHA_TO_COVERAGE":"",t.map?"#define USE_MAP":"",t.matcap?"#define USE_MATCAP":"",t.envMap?"#define USE_ENVMAP":"",t.envMap?"#define "+l:"",t.envMap?"#define "+u:"",t.envMap?"#define "+f:"",h?"#define CUBEUV_TEXEL_WIDTH "+h.texelWidth:"",h?"#define CUBEUV_TEXEL_HEIGHT "+h.texelHeight:"",h?"#define CUBEUV_MAX_MIP "+h.maxMip+".0":"",t.lightMap?"#define USE_LIGHTMAP":"",t.aoMap?"#define USE_AOMAP":"",t.bumpMap?"#define USE_BUMPMAP":"",t.normalMap?"#define USE_NORMALMAP":"",t.normalMapObjectSpace?"#define USE_NORMALMAP_OBJECTSPACE":"",t.normalMapTangentSpace?"#define USE_NORMALMAP_TANGENTSPACE":"",t.emissiveMap?"#define USE_EMISSIVEMAP":"",t.anisotropy?"#define USE_ANISOTROPY":"",t.anisotropyMap?"#define USE_ANISOTROPYMAP":"",t.clearcoat?"#define USE_CLEARCOAT":"",t.clearcoatMap?"#define USE_CLEARCOATMAP":"",t.clearcoatRoughnessMap?"#define USE_CLEARCOAT_ROUGHNESSMAP":"",t.clearcoatNormalMap?"#define USE_CLEARCOAT_NORMALMAP":"",t.dispersion?"#define USE_DISPERSION":"",t.iridescence?"#define USE_IRIDESCENCE":"",t.iridescenceMap?"#define USE_IRIDESCENCEMAP":"",t.iridescenceThicknessMap?"#define USE_IRIDESCENCE_THICKNESSMAP":"",t.specularMap?"#define USE_SPECULARMAP":"",t.specularColorMap?"#define USE_SPECULAR_COLORMAP":"",t.specularIntensityMap?"#define USE_SPECULAR_INTENSITYMAP":"",t.roughnessMap?"#define USE_ROUGHNESSMAP":"",t.metalnessMap?"#define USE_METALNESSMAP":"",t.alphaMap?"#define USE_ALPHAMAP":"",t.alphaTest?"#define USE_ALPHATEST":"",t.alphaHash?"#define USE_ALPHAHASH":"",t.sheen?"#define USE_SHEEN":"",t.sheenColorMap?"#define USE_SHEEN_COLORMAP":"",t.sheenRoughnessMap?"#define USE_SHEEN_ROUGHNESSMAP":"",t.transmission?"#define USE_TRANSMISSION":"",t.transmissionMap?"#define USE_TRANSMISSIONMAP":"",t.thicknessMap?"#define USE_THICKNESSMAP":"",t.vertexTangents&&t.flatShading===!1?"#define USE_TANGENT":"",t.vertexColors||t.instancingColor?"#define USE_COLOR":"",t.vertexAlphas||t.batchingColor?"#define USE_COLOR_ALPHA":"",t.vertexUv1s?"#define USE_UV1":"",t.vertexUv2s?"#define USE_UV2":"",t.vertexUv3s?"#define USE_UV3":"",t.pointsUvs?"#define USE_POINTS_UV":"",t.gradientMap?"#define USE_GRADIENTMAP":"",t.flatShading?"#define FLAT_SHADED":"",t.doubleSided?"#define DOUBLE_SIDED":"",t.flipSided?"#define FLIP_SIDED":"",t.shadowMapEnabled?"#define USE_SHADOWMAP":"",t.shadowMapEnabled?"#define "+c:"",t.premultipliedAlpha?"#define PREMULTIPLIED_ALPHA":"",t.numLightProbes>0?"#define USE_LIGHT_PROBES":"",t.decodeVideoTexture?"#define DECODE_VIDEO_TEXTURE":"",t.decodeVideoTextureEmissive?"#define DECODE_VIDEO_TEXTURE_EMISSIVE":"",t.logarithmicDepthBuffer?"#define USE_LOGARITHMIC_DEPTH_BUFFER":"",t.reversedDepthBuffer?"#define USE_REVERSED_DEPTH_BUFFER":"","uniform mat4 viewMatrix;","uniform vec3 cameraPosition;","uniform bool isOrthographic;",t.toneMapping!==ci?"#define TONE_MAPPING":"",t.toneMapping!==ci?Ze.tonemapping_pars_fragment:"",t.toneMapping!==ci?z0("toneMapping",t.toneMapping):"",t.dithering?"#define DITHERING":"",t.opaque?"#define OPAQUE":"",Ze.colorspace_pars_fragment,B0("linearToOutputTexel",t.outputColorSpace),V0(),t.useDepthPacking?"#define DEPTH_PACKING "+t.depthPacking:"",`
`].filter(Ds).join(`
`)),a=Yl(a),a=Xu(a,t),a=qu(a,t),o=Yl(o),o=Xu(o,t),o=qu(o,t),a=Yu(a),o=Yu(o),t.isRawShaderMaterial!==!0&&(M=`#version 300 es
`,d=[m,"#define attribute in","#define varying out","#define texture2D texture"].join(`
`)+`
`+d,p=["#define varying in",t.glslVersion===jc?"":"layout(location = 0) out highp vec4 pc_fragColor;",t.glslVersion===jc?"":"#define gl_FragColor pc_fragColor","#define gl_FragDepthEXT gl_FragDepth","#define texture2D texture","#define textureCube texture","#define texture2DProj textureProj","#define texture2DLodEXT textureLod","#define texture2DProjLodEXT textureProjLod","#define textureCubeLodEXT textureLod","#define texture2DGradEXT textureGrad","#define texture2DProjGradEXT textureProjGrad","#define textureCubeGradEXT textureGrad"].join(`
`)+`
`+p);const y=M+d+a,b=M+p+o,T=Gu(i,i.VERTEX_SHADER,y),A=Gu(i,i.FRAGMENT_SHADER,b);i.attachShader(g,T),i.attachShader(g,A),t.index0AttributeName!==void 0?i.bindAttribLocation(g,0,t.index0AttributeName):t.morphTargets===!0&&i.bindAttribLocation(g,0,"position"),i.linkProgram(g);function R(P){if(r.debug.checkShaderErrors){const z=i.getProgramInfoLog(g)||"",F=i.getShaderInfoLog(T)||"",B=i.getShaderInfoLog(A)||"",O=z.trim(),V=F.trim(),G=B.trim();let ne=!0,Q=!0;if(i.getProgramParameter(g,i.LINK_STATUS)===!1)if(ne=!1,typeof r.debug.onShaderError=="function")r.debug.onShaderError(i,g,T,A);else{const me=Wu(i,T,"vertex"),be=Wu(i,A,"fragment");lt("THREE.WebGLProgram: Shader Error "+i.getError()+" - VALIDATE_STATUS "+i.getProgramParameter(g,i.VALIDATE_STATUS)+`

Material Name: `+P.name+`
Material Type: `+P.type+`

Program Info Log: `+O+`
`+me+`
`+be)}else O!==""?Xe("WebGLProgram: Program Info Log:",O):(V===""||G==="")&&(Q=!1);Q&&(P.diagnostics={runnable:ne,programLog:O,vertexShader:{log:V,prefix:d},fragmentShader:{log:G,prefix:p}})}i.deleteShader(T),i.deleteShader(A),x=new ka(i,g),S=W0(i,g)}let x;this.getUniforms=function(){return x===void 0&&R(this),x};let S;this.getAttributes=function(){return S===void 0&&R(this),S};let W=t.rendererExtensionParallelShaderCompile===!1;return this.isReady=function(){return W===!1&&(W=i.getProgramParameter(g,U0)),W},this.destroy=function(){n.releaseStatesOfProgram(this),i.deleteProgram(g),this.program=void 0},this.type=t.shaderType,this.name=t.shaderName,this.id=N0++,this.cacheKey=e,this.usedTimes=1,this.program=g,this.vertexShader=T,this.fragmentShader=A,this}let av=0;class ov{constructor(){this.shaderCache=new Map,this.materialCache=new Map}update(e){const t=e.vertexShader,n=e.fragmentShader,i=this._getShaderStage(t),s=this._getShaderStage(n),a=this._getShaderCacheForMaterial(e);return a.has(i)===!1&&(a.add(i),i.usedTimes++),a.has(s)===!1&&(a.add(s),s.usedTimes++),this}remove(e){const t=this.materialCache.get(e);for(const n of t)n.usedTimes--,n.usedTimes===0&&this.shaderCache.delete(n.code);return this.materialCache.delete(e),this}getVertexShaderID(e){return this._getShaderStage(e.vertexShader).id}getFragmentShaderID(e){return this._getShaderStage(e.fragmentShader).id}dispose(){this.shaderCache.clear(),this.materialCache.clear()}_getShaderCacheForMaterial(e){const t=this.materialCache;let n=t.get(e);return n===void 0&&(n=new Set,t.set(e,n)),n}_getShaderStage(e){const t=this.shaderCache;let n=t.get(e);return n===void 0&&(n=new lv(e),t.set(e,n)),n}}class lv{constructor(e){this.id=av++,this.code=e,this.usedTimes=0}}function cv(r,e,t,n,i,s){const a=new xc,o=new ov,c=new Set,l=[],u=new Map,f=n.logarithmicDepthBuffer;let h=n.precision;const m={MeshDepthMaterial:"depth",MeshDistanceMaterial:"distance",MeshNormalMaterial:"normal",MeshBasicMaterial:"basic",MeshLambertMaterial:"lambert",MeshPhongMaterial:"phong",MeshToonMaterial:"toon",MeshStandardMaterial:"physical",MeshPhysicalMaterial:"physical",MeshMatcapMaterial:"matcap",LineBasicMaterial:"basic",LineDashedMaterial:"dashed",PointsMaterial:"points",ShadowMaterial:"shadow",SpriteMaterial:"sprite"};function _(x){return c.add(x),x===0?"uv":`uv${x}`}function g(x,S,W,P,z){const F=P.fog,B=z.geometry,O=x.isMeshStandardMaterial||x.isMeshLambertMaterial||x.isMeshPhongMaterial?P.environment:null,V=x.isMeshStandardMaterial||x.isMeshLambertMaterial&&!x.envMap||x.isMeshPhongMaterial&&!x.envMap,G=e.get(x.envMap||O,V),ne=G&&G.mapping===no?G.image.height:null,Q=m[x.type];x.precision!==null&&(h=n.getMaxPrecision(x.precision),h!==x.precision&&Xe("WebGLProgram.getParameters:",x.precision,"not supported, using",h,"instead."));const me=B.morphAttributes.position||B.morphAttributes.normal||B.morphAttributes.color,be=me!==void 0?me.length:0;let xe=0;B.morphAttributes.position!==void 0&&(xe=1),B.morphAttributes.normal!==void 0&&(xe=2),B.morphAttributes.color!==void 0&&(xe=3);let Ge,qe,je,ee;if(Q){const ot=ri[Q];Ge=ot.vertexShader,qe=ot.fragmentShader}else Ge=x.vertexShader,qe=x.fragmentShader,o.update(x),je=o.getVertexShaderID(x),ee=o.getFragmentShaderID(x);const he=r.getRenderTarget(),pe=r.state.buffers.depth.getReversed(),He=z.isInstancedMesh===!0,ke=z.isBatchedMesh===!0,ze=!!x.map,Et=!!x.matcap,Je=!!G,nt=!!x.aoMap,st=!!x.lightMap,K=!!x.bumpMap,re=!!x.normalMap,C=!!x.displacementMap,Ee=!!x.emissiveMap,oe=!!x.metalnessMap,ue=!!x.roughnessMap,ae=x.anisotropy>0,w=x.clearcoat>0,v=x.dispersion>0,L=x.iridescence>0,Z=x.sheen>0,te=x.transmission>0,j=ae&&!!x.anisotropyMap,Ce=w&&!!x.clearcoatMap,fe=w&&!!x.clearcoatNormalMap,we=w&&!!x.clearcoatRoughnessMap,Fe=L&&!!x.iridescenceMap,ie=L&&!!x.iridescenceThicknessMap,le=Z&&!!x.sheenColorMap,Re=Z&&!!x.sheenRoughnessMap,Te=!!x.specularMap,ge=!!x.specularColorMap,Ye=!!x.specularIntensityMap,I=te&&!!x.transmissionMap,de=te&&!!x.thicknessMap,ce=!!x.gradientMap,Ae=!!x.alphaMap,se=x.alphaTest>0,$=!!x.alphaHash,Pe=!!x.extensions;let Oe=ci;x.toneMapped&&(he===null||he.isXRRenderTarget===!0)&&(Oe=r.toneMapping);const ht={shaderID:Q,shaderType:x.type,shaderName:x.name,vertexShader:Ge,fragmentShader:qe,defines:x.defines,customVertexShaderID:je,customFragmentShaderID:ee,isRawShaderMaterial:x.isRawShaderMaterial===!0,glslVersion:x.glslVersion,precision:h,batching:ke,batchingColor:ke&&z._colorsTexture!==null,instancing:He,instancingColor:He&&z.instanceColor!==null,instancingMorph:He&&z.morphTexture!==null,outputColorSpace:he===null?r.outputColorSpace:he.isXRRenderTarget===!0?he.texture.colorSpace:ls,alphaToCoverage:!!x.alphaToCoverage,map:ze,matcap:Et,envMap:Je,envMapMode:Je&&G.mapping,envMapCubeUVHeight:ne,aoMap:nt,lightMap:st,bumpMap:K,normalMap:re,displacementMap:C,emissiveMap:Ee,normalMapObjectSpace:re&&x.normalMapType===Hd,normalMapTangentSpace:re&&x.normalMapType===Hh,metalnessMap:oe,roughnessMap:ue,anisotropy:ae,anisotropyMap:j,clearcoat:w,clearcoatMap:Ce,clearcoatNormalMap:fe,clearcoatRoughnessMap:we,dispersion:v,iridescence:L,iridescenceMap:Fe,iridescenceThicknessMap:ie,sheen:Z,sheenColorMap:le,sheenRoughnessMap:Re,specularMap:Te,specularColorMap:ge,specularIntensityMap:Ye,transmission:te,transmissionMap:I,thicknessMap:de,gradientMap:ce,opaque:x.transparent===!1&&x.blending===es&&x.alphaToCoverage===!1,alphaMap:Ae,alphaTest:se,alphaHash:$,combine:x.combine,mapUv:ze&&_(x.map.channel),aoMapUv:nt&&_(x.aoMap.channel),lightMapUv:st&&_(x.lightMap.channel),bumpMapUv:K&&_(x.bumpMap.channel),normalMapUv:re&&_(x.normalMap.channel),displacementMapUv:C&&_(x.displacementMap.channel),emissiveMapUv:Ee&&_(x.emissiveMap.channel),metalnessMapUv:oe&&_(x.metalnessMap.channel),roughnessMapUv:ue&&_(x.roughnessMap.channel),anisotropyMapUv:j&&_(x.anisotropyMap.channel),clearcoatMapUv:Ce&&_(x.clearcoatMap.channel),clearcoatNormalMapUv:fe&&_(x.clearcoatNormalMap.channel),clearcoatRoughnessMapUv:we&&_(x.clearcoatRoughnessMap.channel),iridescenceMapUv:Fe&&_(x.iridescenceMap.channel),iridescenceThicknessMapUv:ie&&_(x.iridescenceThicknessMap.channel),sheenColorMapUv:le&&_(x.sheenColorMap.channel),sheenRoughnessMapUv:Re&&_(x.sheenRoughnessMap.channel),specularMapUv:Te&&_(x.specularMap.channel),specularColorMapUv:ge&&_(x.specularColorMap.channel),specularIntensityMapUv:Ye&&_(x.specularIntensityMap.channel),transmissionMapUv:I&&_(x.transmissionMap.channel),thicknessMapUv:de&&_(x.thicknessMap.channel),alphaMapUv:Ae&&_(x.alphaMap.channel),vertexTangents:!!B.attributes.tangent&&(re||ae),vertexColors:x.vertexColors,vertexAlphas:x.vertexColors===!0&&!!B.attributes.color&&B.attributes.color.itemSize===4,pointsUvs:z.isPoints===!0&&!!B.attributes.uv&&(ze||Ae),fog:!!F,useFog:x.fog===!0,fogExp2:!!F&&F.isFogExp2,flatShading:x.wireframe===!1&&(x.flatShading===!0||B.attributes.normal===void 0&&re===!1&&(x.isMeshLambertMaterial||x.isMeshPhongMaterial||x.isMeshStandardMaterial||x.isMeshPhysicalMaterial)),sizeAttenuation:x.sizeAttenuation===!0,logarithmicDepthBuffer:f,reversedDepthBuffer:pe,skinning:z.isSkinnedMesh===!0,morphTargets:B.morphAttributes.position!==void 0,morphNormals:B.morphAttributes.normal!==void 0,morphColors:B.morphAttributes.color!==void 0,morphTargetsCount:be,morphTextureStride:xe,numDirLights:S.directional.length,numPointLights:S.point.length,numSpotLights:S.spot.length,numSpotLightMaps:S.spotLightMap.length,numRectAreaLights:S.rectArea.length,numHemiLights:S.hemi.length,numDirLightShadows:S.directionalShadowMap.length,numPointLightShadows:S.pointShadowMap.length,numSpotLightShadows:S.spotShadowMap.length,numSpotLightShadowsWithMaps:S.numSpotLightShadowsWithMaps,numLightProbes:S.numLightProbes,numClippingPlanes:s.numPlanes,numClipIntersection:s.numIntersection,dithering:x.dithering,shadowMapEnabled:r.shadowMap.enabled&&W.length>0,shadowMapType:r.shadowMap.type,toneMapping:Oe,decodeVideoTexture:ze&&x.map.isVideoTexture===!0&&ct.getTransfer(x.map.colorSpace)===mt,decodeVideoTextureEmissive:Ee&&x.emissiveMap.isVideoTexture===!0&&ct.getTransfer(x.emissiveMap.colorSpace)===mt,premultipliedAlpha:x.premultipliedAlpha,doubleSided:x.side===$n,flipSided:x.side===pn,useDepthPacking:x.depthPacking>=0,depthPacking:x.depthPacking||0,index0AttributeName:x.index0AttributeName,extensionClipCullDistance:Pe&&x.extensions.clipCullDistance===!0&&t.has("WEBGL_clip_cull_distance"),extensionMultiDraw:(Pe&&x.extensions.multiDraw===!0||ke)&&t.has("WEBGL_multi_draw"),rendererExtensionParallelShaderCompile:t.has("KHR_parallel_shader_compile"),customProgramCacheKey:x.customProgramCacheKey()};return ht.vertexUv1s=c.has(1),ht.vertexUv2s=c.has(2),ht.vertexUv3s=c.has(3),c.clear(),ht}function d(x){const S=[];if(x.shaderID?S.push(x.shaderID):(S.push(x.customVertexShaderID),S.push(x.customFragmentShaderID)),x.defines!==void 0)for(const W in x.defines)S.push(W),S.push(x.defines[W]);return x.isRawShaderMaterial===!1&&(p(S,x),M(S,x),S.push(r.outputColorSpace)),S.push(x.customProgramCacheKey),S.join()}function p(x,S){x.push(S.precision),x.push(S.outputColorSpace),x.push(S.envMapMode),x.push(S.envMapCubeUVHeight),x.push(S.mapUv),x.push(S.alphaMapUv),x.push(S.lightMapUv),x.push(S.aoMapUv),x.push(S.bumpMapUv),x.push(S.normalMapUv),x.push(S.displacementMapUv),x.push(S.emissiveMapUv),x.push(S.metalnessMapUv),x.push(S.roughnessMapUv),x.push(S.anisotropyMapUv),x.push(S.clearcoatMapUv),x.push(S.clearcoatNormalMapUv),x.push(S.clearcoatRoughnessMapUv),x.push(S.iridescenceMapUv),x.push(S.iridescenceThicknessMapUv),x.push(S.sheenColorMapUv),x.push(S.sheenRoughnessMapUv),x.push(S.specularMapUv),x.push(S.specularColorMapUv),x.push(S.specularIntensityMapUv),x.push(S.transmissionMapUv),x.push(S.thicknessMapUv),x.push(S.combine),x.push(S.fogExp2),x.push(S.sizeAttenuation),x.push(S.morphTargetsCount),x.push(S.morphAttributeCount),x.push(S.numDirLights),x.push(S.numPointLights),x.push(S.numSpotLights),x.push(S.numSpotLightMaps),x.push(S.numHemiLights),x.push(S.numRectAreaLights),x.push(S.numDirLightShadows),x.push(S.numPointLightShadows),x.push(S.numSpotLightShadows),x.push(S.numSpotLightShadowsWithMaps),x.push(S.numLightProbes),x.push(S.shadowMapType),x.push(S.toneMapping),x.push(S.numClippingPlanes),x.push(S.numClipIntersection),x.push(S.depthPacking)}function M(x,S){a.disableAll(),S.instancing&&a.enable(0),S.instancingColor&&a.enable(1),S.instancingMorph&&a.enable(2),S.matcap&&a.enable(3),S.envMap&&a.enable(4),S.normalMapObjectSpace&&a.enable(5),S.normalMapTangentSpace&&a.enable(6),S.clearcoat&&a.enable(7),S.iridescence&&a.enable(8),S.alphaTest&&a.enable(9),S.vertexColors&&a.enable(10),S.vertexAlphas&&a.enable(11),S.vertexUv1s&&a.enable(12),S.vertexUv2s&&a.enable(13),S.vertexUv3s&&a.enable(14),S.vertexTangents&&a.enable(15),S.anisotropy&&a.enable(16),S.alphaHash&&a.enable(17),S.batching&&a.enable(18),S.dispersion&&a.enable(19),S.batchingColor&&a.enable(20),S.gradientMap&&a.enable(21),x.push(a.mask),a.disableAll(),S.fog&&a.enable(0),S.useFog&&a.enable(1),S.flatShading&&a.enable(2),S.logarithmicDepthBuffer&&a.enable(3),S.reversedDepthBuffer&&a.enable(4),S.skinning&&a.enable(5),S.morphTargets&&a.enable(6),S.morphNormals&&a.enable(7),S.morphColors&&a.enable(8),S.premultipliedAlpha&&a.enable(9),S.shadowMapEnabled&&a.enable(10),S.doubleSided&&a.enable(11),S.flipSided&&a.enable(12),S.useDepthPacking&&a.enable(13),S.dithering&&a.enable(14),S.transmission&&a.enable(15),S.sheen&&a.enable(16),S.opaque&&a.enable(17),S.pointsUvs&&a.enable(18),S.decodeVideoTexture&&a.enable(19),S.decodeVideoTextureEmissive&&a.enable(20),S.alphaToCoverage&&a.enable(21),x.push(a.mask)}function y(x){const S=m[x.type];let W;if(S){const P=ri[S];W=Rp.clone(P.uniforms)}else W=x.uniforms;return W}function b(x,S){let W=u.get(S);return W!==void 0?++W.usedTimes:(W=new sv(r,S,x,i),l.push(W),u.set(S,W)),W}function T(x){if(--x.usedTimes===0){const S=l.indexOf(x);l[S]=l[l.length-1],l.pop(),u.delete(x.cacheKey),x.destroy()}}function A(x){o.remove(x)}function R(){o.dispose()}return{getParameters:g,getProgramCacheKey:d,getUniforms:y,acquireProgram:b,releaseProgram:T,releaseShaderCache:A,programs:l,dispose:R}}function uv(){let r=new WeakMap;function e(a){return r.has(a)}function t(a){let o=r.get(a);return o===void 0&&(o={},r.set(a,o)),o}function n(a){r.delete(a)}function i(a,o,c){r.get(a)[o]=c}function s(){r=new WeakMap}return{has:e,get:t,remove:n,update:i,dispose:s}}function hv(r,e){return r.groupOrder!==e.groupOrder?r.groupOrder-e.groupOrder:r.renderOrder!==e.renderOrder?r.renderOrder-e.renderOrder:r.material.id!==e.material.id?r.material.id-e.material.id:r.materialVariant!==e.materialVariant?r.materialVariant-e.materialVariant:r.z!==e.z?r.z-e.z:r.id-e.id}function Ku(r,e){return r.groupOrder!==e.groupOrder?r.groupOrder-e.groupOrder:r.renderOrder!==e.renderOrder?r.renderOrder-e.renderOrder:r.z!==e.z?e.z-r.z:r.id-e.id}function Zu(){const r=[];let e=0;const t=[],n=[],i=[];function s(){e=0,t.length=0,n.length=0,i.length=0}function a(h){let m=0;return h.isInstancedMesh&&(m+=2),h.isSkinnedMesh&&(m+=1),m}function o(h,m,_,g,d,p){let M=r[e];return M===void 0?(M={id:h.id,object:h,geometry:m,material:_,materialVariant:a(h),groupOrder:g,renderOrder:h.renderOrder,z:d,group:p},r[e]=M):(M.id=h.id,M.object=h,M.geometry=m,M.material=_,M.materialVariant=a(h),M.groupOrder=g,M.renderOrder=h.renderOrder,M.z=d,M.group=p),e++,M}function c(h,m,_,g,d,p){const M=o(h,m,_,g,d,p);_.transmission>0?n.push(M):_.transparent===!0?i.push(M):t.push(M)}function l(h,m,_,g,d,p){const M=o(h,m,_,g,d,p);_.transmission>0?n.unshift(M):_.transparent===!0?i.unshift(M):t.unshift(M)}function u(h,m){t.length>1&&t.sort(h||hv),n.length>1&&n.sort(m||Ku),i.length>1&&i.sort(m||Ku)}function f(){for(let h=e,m=r.length;h<m;h++){const _=r[h];if(_.id===null)break;_.id=null,_.object=null,_.geometry=null,_.material=null,_.group=null}}return{opaque:t,transmissive:n,transparent:i,init:s,push:c,unshift:l,finish:f,sort:u}}function fv(){let r=new WeakMap;function e(n,i){const s=r.get(n);let a;return s===void 0?(a=new Zu,r.set(n,[a])):i>=s.length?(a=new Zu,s.push(a)):a=s[i],a}function t(){r=new WeakMap}return{get:e,dispose:t}}function dv(){const r={};return{get:function(e){if(r[e.id]!==void 0)return r[e.id];let t;switch(e.type){case"DirectionalLight":t={direction:new k,color:new rt};break;case"SpotLight":t={position:new k,direction:new k,color:new rt,distance:0,coneCos:0,penumbraCos:0,decay:0};break;case"PointLight":t={position:new k,color:new rt,distance:0,decay:0};break;case"HemisphereLight":t={direction:new k,skyColor:new rt,groundColor:new rt};break;case"RectAreaLight":t={color:new rt,position:new k,halfWidth:new k,halfHeight:new k};break}return r[e.id]=t,t}}}function pv(){const r={};return{get:function(e){if(r[e.id]!==void 0)return r[e.id];let t;switch(e.type){case"DirectionalLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new $e};break;case"SpotLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new $e};break;case"PointLight":t={shadowIntensity:1,shadowBias:0,shadowNormalBias:0,shadowRadius:1,shadowMapSize:new $e,shadowCameraNear:1,shadowCameraFar:1e3};break}return r[e.id]=t,t}}}let mv=0;function _v(r,e){return(e.castShadow?2:0)-(r.castShadow?2:0)+(e.map?1:0)-(r.map?1:0)}function gv(r){const e=new dv,t=pv(),n={version:0,hash:{directionalLength:-1,pointLength:-1,spotLength:-1,rectAreaLength:-1,hemiLength:-1,numDirectionalShadows:-1,numPointShadows:-1,numSpotShadows:-1,numSpotMaps:-1,numLightProbes:-1},ambient:[0,0,0],probe:[],directional:[],directionalShadow:[],directionalShadowMap:[],directionalShadowMatrix:[],spot:[],spotLightMap:[],spotShadow:[],spotShadowMap:[],spotLightMatrix:[],rectArea:[],rectAreaLTC1:null,rectAreaLTC2:null,point:[],pointShadow:[],pointShadowMap:[],pointShadowMatrix:[],hemi:[],numSpotLightShadowsWithMaps:0,numLightProbes:0};for(let l=0;l<9;l++)n.probe.push(new k);const i=new k,s=new yt,a=new yt;function o(l){let u=0,f=0,h=0;for(let S=0;S<9;S++)n.probe[S].set(0,0,0);let m=0,_=0,g=0,d=0,p=0,M=0,y=0,b=0,T=0,A=0,R=0;l.sort(_v);for(let S=0,W=l.length;S<W;S++){const P=l[S],z=P.color,F=P.intensity,B=P.distance;let O=null;if(P.shadow&&P.shadow.map&&(P.shadow.map.texture.format===os?O=P.shadow.map.texture:O=P.shadow.map.depthTexture||P.shadow.map.texture),P.isAmbientLight)u+=z.r*F,f+=z.g*F,h+=z.b*F;else if(P.isLightProbe){for(let V=0;V<9;V++)n.probe[V].addScaledVector(P.sh.coefficients[V],F);R++}else if(P.isDirectionalLight){const V=e.get(P);if(V.color.copy(P.color).multiplyScalar(P.intensity),P.castShadow){const G=P.shadow,ne=t.get(P);ne.shadowIntensity=G.intensity,ne.shadowBias=G.bias,ne.shadowNormalBias=G.normalBias,ne.shadowRadius=G.radius,ne.shadowMapSize=G.mapSize,n.directionalShadow[m]=ne,n.directionalShadowMap[m]=O,n.directionalShadowMatrix[m]=P.shadow.matrix,M++}n.directional[m]=V,m++}else if(P.isSpotLight){const V=e.get(P);V.position.setFromMatrixPosition(P.matrixWorld),V.color.copy(z).multiplyScalar(F),V.distance=B,V.coneCos=Math.cos(P.angle),V.penumbraCos=Math.cos(P.angle*(1-P.penumbra)),V.decay=P.decay,n.spot[g]=V;const G=P.shadow;if(P.map&&(n.spotLightMap[T]=P.map,T++,G.updateMatrices(P),P.castShadow&&A++),n.spotLightMatrix[g]=G.matrix,P.castShadow){const ne=t.get(P);ne.shadowIntensity=G.intensity,ne.shadowBias=G.bias,ne.shadowNormalBias=G.normalBias,ne.shadowRadius=G.radius,ne.shadowMapSize=G.mapSize,n.spotShadow[g]=ne,n.spotShadowMap[g]=O,b++}g++}else if(P.isRectAreaLight){const V=e.get(P);V.color.copy(z).multiplyScalar(F),V.halfWidth.set(P.width*.5,0,0),V.halfHeight.set(0,P.height*.5,0),n.rectArea[d]=V,d++}else if(P.isPointLight){const V=e.get(P);if(V.color.copy(P.color).multiplyScalar(P.intensity),V.distance=P.distance,V.decay=P.decay,P.castShadow){const G=P.shadow,ne=t.get(P);ne.shadowIntensity=G.intensity,ne.shadowBias=G.bias,ne.shadowNormalBias=G.normalBias,ne.shadowRadius=G.radius,ne.shadowMapSize=G.mapSize,ne.shadowCameraNear=G.camera.near,ne.shadowCameraFar=G.camera.far,n.pointShadow[_]=ne,n.pointShadowMap[_]=O,n.pointShadowMatrix[_]=P.shadow.matrix,y++}n.point[_]=V,_++}else if(P.isHemisphereLight){const V=e.get(P);V.skyColor.copy(P.color).multiplyScalar(F),V.groundColor.copy(P.groundColor).multiplyScalar(F),n.hemi[p]=V,p++}}d>0&&(r.has("OES_texture_float_linear")===!0?(n.rectAreaLTC1=ye.LTC_FLOAT_1,n.rectAreaLTC2=ye.LTC_FLOAT_2):(n.rectAreaLTC1=ye.LTC_HALF_1,n.rectAreaLTC2=ye.LTC_HALF_2)),n.ambient[0]=u,n.ambient[1]=f,n.ambient[2]=h;const x=n.hash;(x.directionalLength!==m||x.pointLength!==_||x.spotLength!==g||x.rectAreaLength!==d||x.hemiLength!==p||x.numDirectionalShadows!==M||x.numPointShadows!==y||x.numSpotShadows!==b||x.numSpotMaps!==T||x.numLightProbes!==R)&&(n.directional.length=m,n.spot.length=g,n.rectArea.length=d,n.point.length=_,n.hemi.length=p,n.directionalShadow.length=M,n.directionalShadowMap.length=M,n.pointShadow.length=y,n.pointShadowMap.length=y,n.spotShadow.length=b,n.spotShadowMap.length=b,n.directionalShadowMatrix.length=M,n.pointShadowMatrix.length=y,n.spotLightMatrix.length=b+T-A,n.spotLightMap.length=T,n.numSpotLightShadowsWithMaps=A,n.numLightProbes=R,x.directionalLength=m,x.pointLength=_,x.spotLength=g,x.rectAreaLength=d,x.hemiLength=p,x.numDirectionalShadows=M,x.numPointShadows=y,x.numSpotShadows=b,x.numSpotMaps=T,x.numLightProbes=R,n.version=mv++)}function c(l,u){let f=0,h=0,m=0,_=0,g=0;const d=u.matrixWorldInverse;for(let p=0,M=l.length;p<M;p++){const y=l[p];if(y.isDirectionalLight){const b=n.directional[f];b.direction.setFromMatrixPosition(y.matrixWorld),i.setFromMatrixPosition(y.target.matrixWorld),b.direction.sub(i),b.direction.transformDirection(d),f++}else if(y.isSpotLight){const b=n.spot[m];b.position.setFromMatrixPosition(y.matrixWorld),b.position.applyMatrix4(d),b.direction.setFromMatrixPosition(y.matrixWorld),i.setFromMatrixPosition(y.target.matrixWorld),b.direction.sub(i),b.direction.transformDirection(d),m++}else if(y.isRectAreaLight){const b=n.rectArea[_];b.position.setFromMatrixPosition(y.matrixWorld),b.position.applyMatrix4(d),a.identity(),s.copy(y.matrixWorld),s.premultiply(d),a.extractRotation(s),b.halfWidth.set(y.width*.5,0,0),b.halfHeight.set(0,y.height*.5,0),b.halfWidth.applyMatrix4(a),b.halfHeight.applyMatrix4(a),_++}else if(y.isPointLight){const b=n.point[h];b.position.setFromMatrixPosition(y.matrixWorld),b.position.applyMatrix4(d),h++}else if(y.isHemisphereLight){const b=n.hemi[g];b.direction.setFromMatrixPosition(y.matrixWorld),b.direction.transformDirection(d),g++}}}return{setup:o,setupView:c,state:n}}function ju(r){const e=new gv(r),t=[],n=[];function i(u){l.camera=u,t.length=0,n.length=0}function s(u){t.push(u)}function a(u){n.push(u)}function o(){e.setup(t)}function c(u){e.setupView(t,u)}const l={lightsArray:t,shadowsArray:n,camera:null,lights:e,transmissionRenderTarget:{}};return{init:i,state:l,setupLights:o,setupLightsView:c,pushLight:s,pushShadow:a}}function vv(r){let e=new WeakMap;function t(i,s=0){const a=e.get(i);let o;return a===void 0?(o=new ju(r),e.set(i,[o])):s>=a.length?(o=new ju(r),a.push(o)):o=a[s],o}function n(){e=new WeakMap}return{get:t,dispose:n}}const xv=`void main() {
	gl_Position = vec4( position, 1.0 );
}`,Mv=`uniform sampler2D shadow_pass;
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
}`,Sv=[new k(1,0,0),new k(-1,0,0),new k(0,1,0),new k(0,-1,0),new k(0,0,1),new k(0,0,-1)],yv=[new k(0,-1,0),new k(0,-1,0),new k(0,0,1),new k(0,0,-1),new k(0,-1,0),new k(0,-1,0)],Ju=new yt,Rs=new k,Wo=new k;function bv(r,e,t){let n=new Sc;const i=new $e,s=new $e,a=new Dt,o=new Lp,c=new Ip,l={},u=t.maxTextureSize,f={[Zi]:pn,[pn]:Zi,[$n]:$n},h=new di({defines:{VSM_SAMPLES:8},uniforms:{shadow_pass:{value:null},resolution:{value:new $e},radius:{value:4}},vertexShader:xv,fragmentShader:Mv}),m=h.clone();m.defines.HORIZONTAL_PASS=1;const _=new cn;_.setAttribute("position",new Zn(new Float32Array([-1,-1,.5,3,-1,.5,-1,3,.5]),3));const g=new Yt(_,h),d=this;this.enabled=!1,this.autoUpdate=!0,this.needsUpdate=!1,this.type=Ia;let p=this.type;this.render=function(A,R,x){if(d.enabled===!1||d.autoUpdate===!1&&d.needsUpdate===!1||A.length===0)return;this.type===Md&&(Xe("WebGLShadowMap: PCFSoftShadowMap has been deprecated. Using PCFShadowMap instead."),this.type=Ia);const S=r.getRenderTarget(),W=r.getActiveCubeFace(),P=r.getActiveMipmapLevel(),z=r.state;z.setBlending(Ti),z.buffers.depth.getReversed()===!0?z.buffers.color.setClear(0,0,0,0):z.buffers.color.setClear(1,1,1,1),z.buffers.depth.setTest(!0),z.setScissorTest(!1);const F=p!==this.type;F&&R.traverse(function(B){B.material&&(Array.isArray(B.material)?B.material.forEach(O=>O.needsUpdate=!0):B.material.needsUpdate=!0)});for(let B=0,O=A.length;B<O;B++){const V=A[B],G=V.shadow;if(G===void 0){Xe("WebGLShadowMap:",V,"has no shadow.");continue}if(G.autoUpdate===!1&&G.needsUpdate===!1)continue;i.copy(G.mapSize);const ne=G.getFrameExtents();i.multiply(ne),s.copy(G.mapSize),(i.x>u||i.y>u)&&(i.x>u&&(s.x=Math.floor(u/ne.x),i.x=s.x*ne.x,G.mapSize.x=s.x),i.y>u&&(s.y=Math.floor(u/ne.y),i.y=s.y*ne.y,G.mapSize.y=s.y));const Q=r.state.buffers.depth.getReversed();if(G.camera._reversedDepth=Q,G.map===null||F===!0){if(G.map!==null&&(G.map.depthTexture!==null&&(G.map.depthTexture.dispose(),G.map.depthTexture=null),G.map.dispose()),this.type===Ps){if(V.isPointLight){Xe("WebGLShadowMap: VSM shadow maps are not supported for PointLights. Use PCF or BasicShadowMap instead.");continue}G.map=new ui(i.x,i.y,{format:os,type:wi,minFilter:nn,magFilter:nn,generateMipmaps:!1}),G.map.texture.name=V.name+".shadowMap",G.map.depthTexture=new Gs(i.x,i.y,ai),G.map.depthTexture.name=V.name+".shadowMapDepth",G.map.depthTexture.format=Ri,G.map.depthTexture.compareFunction=null,G.map.depthTexture.minFilter=$t,G.map.depthTexture.magFilter=$t}else V.isPointLight?(G.map=new nf(i.x),G.map.depthTexture=new Ap(i.x,hi)):(G.map=new ui(i.x,i.y),G.map.depthTexture=new Gs(i.x,i.y,hi)),G.map.depthTexture.name=V.name+".shadowMap",G.map.depthTexture.format=Ri,this.type===Ia?(G.map.depthTexture.compareFunction=Q?gc:_c,G.map.depthTexture.minFilter=nn,G.map.depthTexture.magFilter=nn):(G.map.depthTexture.compareFunction=null,G.map.depthTexture.minFilter=$t,G.map.depthTexture.magFilter=$t);G.camera.updateProjectionMatrix()}const me=G.map.isWebGLCubeRenderTarget?6:1;for(let be=0;be<me;be++){if(G.map.isWebGLCubeRenderTarget)r.setRenderTarget(G.map,be),r.clear();else{be===0&&(r.setRenderTarget(G.map),r.clear());const xe=G.getViewport(be);a.set(s.x*xe.x,s.y*xe.y,s.x*xe.z,s.y*xe.w),z.viewport(a)}if(V.isPointLight){const xe=G.camera,Ge=G.matrix,qe=V.distance||xe.far;qe!==xe.far&&(xe.far=qe,xe.updateProjectionMatrix()),Rs.setFromMatrixPosition(V.matrixWorld),xe.position.copy(Rs),Wo.copy(xe.position),Wo.add(Sv[be]),xe.up.copy(yv[be]),xe.lookAt(Wo),xe.updateMatrixWorld(),Ge.makeTranslation(-Rs.x,-Rs.y,-Rs.z),Ju.multiplyMatrices(xe.projectionMatrix,xe.matrixWorldInverse),G._frustum.setFromProjectionMatrix(Ju,xe.coordinateSystem,xe.reversedDepth)}else G.updateMatrices(V);n=G.getFrustum(),b(R,x,G.camera,V,this.type)}G.isPointLightShadow!==!0&&this.type===Ps&&M(G,x),G.needsUpdate=!1}p=this.type,d.needsUpdate=!1,r.setRenderTarget(S,W,P)};function M(A,R){const x=e.update(g);h.defines.VSM_SAMPLES!==A.blurSamples&&(h.defines.VSM_SAMPLES=A.blurSamples,m.defines.VSM_SAMPLES=A.blurSamples,h.needsUpdate=!0,m.needsUpdate=!0),A.mapPass===null&&(A.mapPass=new ui(i.x,i.y,{format:os,type:wi})),h.uniforms.shadow_pass.value=A.map.depthTexture,h.uniforms.resolution.value=A.mapSize,h.uniforms.radius.value=A.radius,r.setRenderTarget(A.mapPass),r.clear(),r.renderBufferDirect(R,null,x,h,g,null),m.uniforms.shadow_pass.value=A.mapPass.texture,m.uniforms.resolution.value=A.mapSize,m.uniforms.radius.value=A.radius,r.setRenderTarget(A.map),r.clear(),r.renderBufferDirect(R,null,x,m,g,null)}function y(A,R,x,S){let W=null;const P=x.isPointLight===!0?A.customDistanceMaterial:A.customDepthMaterial;if(P!==void 0)W=P;else if(W=x.isPointLight===!0?c:o,r.localClippingEnabled&&R.clipShadows===!0&&Array.isArray(R.clippingPlanes)&&R.clippingPlanes.length!==0||R.displacementMap&&R.displacementScale!==0||R.alphaMap&&R.alphaTest>0||R.map&&R.alphaTest>0||R.alphaToCoverage===!0){const z=W.uuid,F=R.uuid;let B=l[z];B===void 0&&(B={},l[z]=B);let O=B[F];O===void 0&&(O=W.clone(),B[F]=O,R.addEventListener("dispose",T)),W=O}if(W.visible=R.visible,W.wireframe=R.wireframe,S===Ps?W.side=R.shadowSide!==null?R.shadowSide:R.side:W.side=R.shadowSide!==null?R.shadowSide:f[R.side],W.alphaMap=R.alphaMap,W.alphaTest=R.alphaToCoverage===!0?.5:R.alphaTest,W.map=R.map,W.clipShadows=R.clipShadows,W.clippingPlanes=R.clippingPlanes,W.clipIntersection=R.clipIntersection,W.displacementMap=R.displacementMap,W.displacementScale=R.displacementScale,W.displacementBias=R.displacementBias,W.wireframeLinewidth=R.wireframeLinewidth,W.linewidth=R.linewidth,x.isPointLight===!0&&W.isMeshDistanceMaterial===!0){const z=r.properties.get(W);z.light=x}return W}function b(A,R,x,S,W){if(A.visible===!1)return;if(A.layers.test(R.layers)&&(A.isMesh||A.isLine||A.isPoints)&&(A.castShadow||A.receiveShadow&&W===Ps)&&(!A.frustumCulled||n.intersectsObject(A))){A.modelViewMatrix.multiplyMatrices(x.matrixWorldInverse,A.matrixWorld);const F=e.update(A),B=A.material;if(Array.isArray(B)){const O=F.groups;for(let V=0,G=O.length;V<G;V++){const ne=O[V],Q=B[ne.materialIndex];if(Q&&Q.visible){const me=y(A,Q,S,W);A.onBeforeShadow(r,A,R,x,F,me,ne),r.renderBufferDirect(x,null,F,me,A,ne),A.onAfterShadow(r,A,R,x,F,me,ne)}}}else if(B.visible){const O=y(A,B,S,W);A.onBeforeShadow(r,A,R,x,F,O,null),r.renderBufferDirect(x,null,F,O,A,null),A.onAfterShadow(r,A,R,x,F,O,null)}}const z=A.children;for(let F=0,B=z.length;F<B;F++)b(z[F],R,x,S,W)}function T(A){A.target.removeEventListener("dispose",T);for(const x in l){const S=l[x],W=A.target.uuid;W in S&&(S[W].dispose(),delete S[W])}}}function Ev(r,e){function t(){let I=!1;const de=new Dt;let ce=null;const Ae=new Dt(0,0,0,0);return{setMask:function(se){ce!==se&&!I&&(r.colorMask(se,se,se,se),ce=se)},setLocked:function(se){I=se},setClear:function(se,$,Pe,Oe,ht){ht===!0&&(se*=Oe,$*=Oe,Pe*=Oe),de.set(se,$,Pe,Oe),Ae.equals(de)===!1&&(r.clearColor(se,$,Pe,Oe),Ae.copy(de))},reset:function(){I=!1,ce=null,Ae.set(-1,0,0,0)}}}function n(){let I=!1,de=!1,ce=null,Ae=null,se=null;return{setReversed:function($){if(de!==$){const Pe=e.get("EXT_clip_control");$?Pe.clipControlEXT(Pe.LOWER_LEFT_EXT,Pe.ZERO_TO_ONE_EXT):Pe.clipControlEXT(Pe.LOWER_LEFT_EXT,Pe.NEGATIVE_ONE_TO_ONE_EXT),de=$;const Oe=se;se=null,this.setClear(Oe)}},getReversed:function(){return de},setTest:function($){$?he(r.DEPTH_TEST):pe(r.DEPTH_TEST)},setMask:function($){ce!==$&&!I&&(r.depthMask($),ce=$)},setFunc:function($){if(de&&($=Qd[$]),Ae!==$){switch($){case nl:r.depthFunc(r.NEVER);break;case il:r.depthFunc(r.ALWAYS);break;case rl:r.depthFunc(r.LESS);break;case ss:r.depthFunc(r.LEQUAL);break;case sl:r.depthFunc(r.EQUAL);break;case al:r.depthFunc(r.GEQUAL);break;case ol:r.depthFunc(r.GREATER);break;case ll:r.depthFunc(r.NOTEQUAL);break;default:r.depthFunc(r.LEQUAL)}Ae=$}},setLocked:function($){I=$},setClear:function($){se!==$&&(se=$,de&&($=1-$),r.clearDepth($))},reset:function(){I=!1,ce=null,Ae=null,se=null,de=!1}}}function i(){let I=!1,de=null,ce=null,Ae=null,se=null,$=null,Pe=null,Oe=null,ht=null;return{setTest:function(ot){I||(ot?he(r.STENCIL_TEST):pe(r.STENCIL_TEST))},setMask:function(ot){de!==ot&&!I&&(r.stencilMask(ot),de=ot)},setFunc:function(ot,Wt,sn){(ce!==ot||Ae!==Wt||se!==sn)&&(r.stencilFunc(ot,Wt,sn),ce=ot,Ae=Wt,se=sn)},setOp:function(ot,Wt,sn){($!==ot||Pe!==Wt||Oe!==sn)&&(r.stencilOp(ot,Wt,sn),$=ot,Pe=Wt,Oe=sn)},setLocked:function(ot){I=ot},setClear:function(ot){ht!==ot&&(r.clearStencil(ot),ht=ot)},reset:function(){I=!1,de=null,ce=null,Ae=null,se=null,$=null,Pe=null,Oe=null,ht=null}}}const s=new t,a=new n,o=new i,c=new WeakMap,l=new WeakMap;let u={},f={},h=new WeakMap,m=[],_=null,g=!1,d=null,p=null,M=null,y=null,b=null,T=null,A=null,R=new rt(0,0,0),x=0,S=!1,W=null,P=null,z=null,F=null,B=null;const O=r.getParameter(r.MAX_COMBINED_TEXTURE_IMAGE_UNITS);let V=!1,G=0;const ne=r.getParameter(r.VERSION);ne.indexOf("WebGL")!==-1?(G=parseFloat(/^WebGL (\d)/.exec(ne)[1]),V=G>=1):ne.indexOf("OpenGL ES")!==-1&&(G=parseFloat(/^OpenGL ES (\d)/.exec(ne)[1]),V=G>=2);let Q=null,me={};const be=r.getParameter(r.SCISSOR_BOX),xe=r.getParameter(r.VIEWPORT),Ge=new Dt().fromArray(be),qe=new Dt().fromArray(xe);function je(I,de,ce,Ae){const se=new Uint8Array(4),$=r.createTexture();r.bindTexture(I,$),r.texParameteri(I,r.TEXTURE_MIN_FILTER,r.NEAREST),r.texParameteri(I,r.TEXTURE_MAG_FILTER,r.NEAREST);for(let Pe=0;Pe<ce;Pe++)I===r.TEXTURE_3D||I===r.TEXTURE_2D_ARRAY?r.texImage3D(de,0,r.RGBA,1,1,Ae,0,r.RGBA,r.UNSIGNED_BYTE,se):r.texImage2D(de+Pe,0,r.RGBA,1,1,0,r.RGBA,r.UNSIGNED_BYTE,se);return $}const ee={};ee[r.TEXTURE_2D]=je(r.TEXTURE_2D,r.TEXTURE_2D,1),ee[r.TEXTURE_CUBE_MAP]=je(r.TEXTURE_CUBE_MAP,r.TEXTURE_CUBE_MAP_POSITIVE_X,6),ee[r.TEXTURE_2D_ARRAY]=je(r.TEXTURE_2D_ARRAY,r.TEXTURE_2D_ARRAY,1,1),ee[r.TEXTURE_3D]=je(r.TEXTURE_3D,r.TEXTURE_3D,1,1),s.setClear(0,0,0,1),a.setClear(1),o.setClear(0),he(r.DEPTH_TEST),a.setFunc(ss),K(!1),re(Xc),he(r.CULL_FACE),nt(Ti);function he(I){u[I]!==!0&&(r.enable(I),u[I]=!0)}function pe(I){u[I]!==!1&&(r.disable(I),u[I]=!1)}function He(I,de){return f[I]!==de?(r.bindFramebuffer(I,de),f[I]=de,I===r.DRAW_FRAMEBUFFER&&(f[r.FRAMEBUFFER]=de),I===r.FRAMEBUFFER&&(f[r.DRAW_FRAMEBUFFER]=de),!0):!1}function ke(I,de){let ce=m,Ae=!1;if(I){ce=h.get(de),ce===void 0&&(ce=[],h.set(de,ce));const se=I.textures;if(ce.length!==se.length||ce[0]!==r.COLOR_ATTACHMENT0){for(let $=0,Pe=se.length;$<Pe;$++)ce[$]=r.COLOR_ATTACHMENT0+$;ce.length=se.length,Ae=!0}}else ce[0]!==r.BACK&&(ce[0]=r.BACK,Ae=!0);Ae&&r.drawBuffers(ce)}function ze(I){return _!==I?(r.useProgram(I),_=I,!0):!1}const Et={[pr]:r.FUNC_ADD,[yd]:r.FUNC_SUBTRACT,[bd]:r.FUNC_REVERSE_SUBTRACT};Et[Ed]=r.MIN,Et[Td]=r.MAX;const Je={[Ad]:r.ZERO,[wd]:r.ONE,[Rd]:r.SRC_COLOR,[el]:r.SRC_ALPHA,[Ud]:r.SRC_ALPHA_SATURATE,[Ld]:r.DST_COLOR,[Pd]:r.DST_ALPHA,[Cd]:r.ONE_MINUS_SRC_COLOR,[tl]:r.ONE_MINUS_SRC_ALPHA,[Id]:r.ONE_MINUS_DST_COLOR,[Dd]:r.ONE_MINUS_DST_ALPHA,[Nd]:r.CONSTANT_COLOR,[Fd]:r.ONE_MINUS_CONSTANT_COLOR,[Od]:r.CONSTANT_ALPHA,[Bd]:r.ONE_MINUS_CONSTANT_ALPHA};function nt(I,de,ce,Ae,se,$,Pe,Oe,ht,ot){if(I===Ti){g===!0&&(pe(r.BLEND),g=!1);return}if(g===!1&&(he(r.BLEND),g=!0),I!==Sd){if(I!==d||ot!==S){if((p!==pr||b!==pr)&&(r.blendEquation(r.FUNC_ADD),p=pr,b=pr),ot)switch(I){case es:r.blendFuncSeparate(r.ONE,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case qc:r.blendFunc(r.ONE,r.ONE);break;case Yc:r.blendFuncSeparate(r.ZERO,r.ONE_MINUS_SRC_COLOR,r.ZERO,r.ONE);break;case $c:r.blendFuncSeparate(r.DST_COLOR,r.ONE_MINUS_SRC_ALPHA,r.ZERO,r.ONE);break;default:lt("WebGLState: Invalid blending: ",I);break}else switch(I){case es:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE_MINUS_SRC_ALPHA,r.ONE,r.ONE_MINUS_SRC_ALPHA);break;case qc:r.blendFuncSeparate(r.SRC_ALPHA,r.ONE,r.ONE,r.ONE);break;case Yc:lt("WebGLState: SubtractiveBlending requires material.premultipliedAlpha = true");break;case $c:lt("WebGLState: MultiplyBlending requires material.premultipliedAlpha = true");break;default:lt("WebGLState: Invalid blending: ",I);break}M=null,y=null,T=null,A=null,R.set(0,0,0),x=0,d=I,S=ot}return}se=se||de,$=$||ce,Pe=Pe||Ae,(de!==p||se!==b)&&(r.blendEquationSeparate(Et[de],Et[se]),p=de,b=se),(ce!==M||Ae!==y||$!==T||Pe!==A)&&(r.blendFuncSeparate(Je[ce],Je[Ae],Je[$],Je[Pe]),M=ce,y=Ae,T=$,A=Pe),(Oe.equals(R)===!1||ht!==x)&&(r.blendColor(Oe.r,Oe.g,Oe.b,ht),R.copy(Oe),x=ht),d=I,S=!1}function st(I,de){I.side===$n?pe(r.CULL_FACE):he(r.CULL_FACE);let ce=I.side===pn;de&&(ce=!ce),K(ce),I.blending===es&&I.transparent===!1?nt(Ti):nt(I.blending,I.blendEquation,I.blendSrc,I.blendDst,I.blendEquationAlpha,I.blendSrcAlpha,I.blendDstAlpha,I.blendColor,I.blendAlpha,I.premultipliedAlpha),a.setFunc(I.depthFunc),a.setTest(I.depthTest),a.setMask(I.depthWrite),s.setMask(I.colorWrite);const Ae=I.stencilWrite;o.setTest(Ae),Ae&&(o.setMask(I.stencilWriteMask),o.setFunc(I.stencilFunc,I.stencilRef,I.stencilFuncMask),o.setOp(I.stencilFail,I.stencilZFail,I.stencilZPass)),Ee(I.polygonOffset,I.polygonOffsetFactor,I.polygonOffsetUnits),I.alphaToCoverage===!0?he(r.SAMPLE_ALPHA_TO_COVERAGE):pe(r.SAMPLE_ALPHA_TO_COVERAGE)}function K(I){W!==I&&(I?r.frontFace(r.CW):r.frontFace(r.CCW),W=I)}function re(I){I!==vd?(he(r.CULL_FACE),I!==P&&(I===Xc?r.cullFace(r.BACK):I===xd?r.cullFace(r.FRONT):r.cullFace(r.FRONT_AND_BACK))):pe(r.CULL_FACE),P=I}function C(I){I!==z&&(V&&r.lineWidth(I),z=I)}function Ee(I,de,ce){I?(he(r.POLYGON_OFFSET_FILL),(F!==de||B!==ce)&&(F=de,B=ce,a.getReversed()&&(de=-de),r.polygonOffset(de,ce))):pe(r.POLYGON_OFFSET_FILL)}function oe(I){I?he(r.SCISSOR_TEST):pe(r.SCISSOR_TEST)}function ue(I){I===void 0&&(I=r.TEXTURE0+O-1),Q!==I&&(r.activeTexture(I),Q=I)}function ae(I,de,ce){ce===void 0&&(Q===null?ce=r.TEXTURE0+O-1:ce=Q);let Ae=me[ce];Ae===void 0&&(Ae={type:void 0,texture:void 0},me[ce]=Ae),(Ae.type!==I||Ae.texture!==de)&&(Q!==ce&&(r.activeTexture(ce),Q=ce),r.bindTexture(I,de||ee[I]),Ae.type=I,Ae.texture=de)}function w(){const I=me[Q];I!==void 0&&I.type!==void 0&&(r.bindTexture(I.type,null),I.type=void 0,I.texture=void 0)}function v(){try{r.compressedTexImage2D(...arguments)}catch(I){lt("WebGLState:",I)}}function L(){try{r.compressedTexImage3D(...arguments)}catch(I){lt("WebGLState:",I)}}function Z(){try{r.texSubImage2D(...arguments)}catch(I){lt("WebGLState:",I)}}function te(){try{r.texSubImage3D(...arguments)}catch(I){lt("WebGLState:",I)}}function j(){try{r.compressedTexSubImage2D(...arguments)}catch(I){lt("WebGLState:",I)}}function Ce(){try{r.compressedTexSubImage3D(...arguments)}catch(I){lt("WebGLState:",I)}}function fe(){try{r.texStorage2D(...arguments)}catch(I){lt("WebGLState:",I)}}function we(){try{r.texStorage3D(...arguments)}catch(I){lt("WebGLState:",I)}}function Fe(){try{r.texImage2D(...arguments)}catch(I){lt("WebGLState:",I)}}function ie(){try{r.texImage3D(...arguments)}catch(I){lt("WebGLState:",I)}}function le(I){Ge.equals(I)===!1&&(r.scissor(I.x,I.y,I.z,I.w),Ge.copy(I))}function Re(I){qe.equals(I)===!1&&(r.viewport(I.x,I.y,I.z,I.w),qe.copy(I))}function Te(I,de){let ce=l.get(de);ce===void 0&&(ce=new WeakMap,l.set(de,ce));let Ae=ce.get(I);Ae===void 0&&(Ae=r.getUniformBlockIndex(de,I.name),ce.set(I,Ae))}function ge(I,de){const Ae=l.get(de).get(I);c.get(de)!==Ae&&(r.uniformBlockBinding(de,Ae,I.__bindingPointIndex),c.set(de,Ae))}function Ye(){r.disable(r.BLEND),r.disable(r.CULL_FACE),r.disable(r.DEPTH_TEST),r.disable(r.POLYGON_OFFSET_FILL),r.disable(r.SCISSOR_TEST),r.disable(r.STENCIL_TEST),r.disable(r.SAMPLE_ALPHA_TO_COVERAGE),r.blendEquation(r.FUNC_ADD),r.blendFunc(r.ONE,r.ZERO),r.blendFuncSeparate(r.ONE,r.ZERO,r.ONE,r.ZERO),r.blendColor(0,0,0,0),r.colorMask(!0,!0,!0,!0),r.clearColor(0,0,0,0),r.depthMask(!0),r.depthFunc(r.LESS),a.setReversed(!1),r.clearDepth(1),r.stencilMask(4294967295),r.stencilFunc(r.ALWAYS,0,4294967295),r.stencilOp(r.KEEP,r.KEEP,r.KEEP),r.clearStencil(0),r.cullFace(r.BACK),r.frontFace(r.CCW),r.polygonOffset(0,0),r.activeTexture(r.TEXTURE0),r.bindFramebuffer(r.FRAMEBUFFER,null),r.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),r.bindFramebuffer(r.READ_FRAMEBUFFER,null),r.useProgram(null),r.lineWidth(1),r.scissor(0,0,r.canvas.width,r.canvas.height),r.viewport(0,0,r.canvas.width,r.canvas.height),u={},Q=null,me={},f={},h=new WeakMap,m=[],_=null,g=!1,d=null,p=null,M=null,y=null,b=null,T=null,A=null,R=new rt(0,0,0),x=0,S=!1,W=null,P=null,z=null,F=null,B=null,Ge.set(0,0,r.canvas.width,r.canvas.height),qe.set(0,0,r.canvas.width,r.canvas.height),s.reset(),a.reset(),o.reset()}return{buffers:{color:s,depth:a,stencil:o},enable:he,disable:pe,bindFramebuffer:He,drawBuffers:ke,useProgram:ze,setBlending:nt,setMaterial:st,setFlipSided:K,setCullFace:re,setLineWidth:C,setPolygonOffset:Ee,setScissorTest:oe,activeTexture:ue,bindTexture:ae,unbindTexture:w,compressedTexImage2D:v,compressedTexImage3D:L,texImage2D:Fe,texImage3D:ie,updateUBOMapping:Te,uniformBlockBinding:ge,texStorage2D:fe,texStorage3D:we,texSubImage2D:Z,texSubImage3D:te,compressedTexSubImage2D:j,compressedTexSubImage3D:Ce,scissor:le,viewport:Re,reset:Ye}}function Tv(r,e,t,n,i,s,a){const o=e.has("WEBGL_multisampled_render_to_texture")?e.get("WEBGL_multisampled_render_to_texture"):null,c=typeof navigator>"u"?!1:/OculusBrowser/g.test(navigator.userAgent),l=new $e,u=new WeakMap;let f;const h=new WeakMap;let m=!1;try{m=typeof OffscreenCanvas<"u"&&new OffscreenCanvas(1,1).getContext("2d")!==null}catch{}function _(w,v){return m?new OffscreenCanvas(w,v):Wa("canvas")}function g(w,v,L){let Z=1;const te=ae(w);if((te.width>L||te.height>L)&&(Z=L/Math.max(te.width,te.height)),Z<1)if(typeof HTMLImageElement<"u"&&w instanceof HTMLImageElement||typeof HTMLCanvasElement<"u"&&w instanceof HTMLCanvasElement||typeof ImageBitmap<"u"&&w instanceof ImageBitmap||typeof VideoFrame<"u"&&w instanceof VideoFrame){const j=Math.floor(Z*te.width),Ce=Math.floor(Z*te.height);f===void 0&&(f=_(j,Ce));const fe=v?_(j,Ce):f;return fe.width=j,fe.height=Ce,fe.getContext("2d").drawImage(w,0,0,j,Ce),Xe("WebGLRenderer: Texture has been resized from ("+te.width+"x"+te.height+") to ("+j+"x"+Ce+")."),fe}else return"data"in w&&Xe("WebGLRenderer: Image in DataTexture is too big ("+te.width+"x"+te.height+")."),w;return w}function d(w){return w.generateMipmaps}function p(w){r.generateMipmap(w)}function M(w){return w.isWebGLCubeRenderTarget?r.TEXTURE_CUBE_MAP:w.isWebGL3DRenderTarget?r.TEXTURE_3D:w.isWebGLArrayRenderTarget||w.isCompressedArrayTexture?r.TEXTURE_2D_ARRAY:r.TEXTURE_2D}function y(w,v,L,Z,te=!1){if(w!==null){if(r[w]!==void 0)return r[w];Xe("WebGLRenderer: Attempt to use non-existing WebGL internal format '"+w+"'")}let j=v;if(v===r.RED&&(L===r.FLOAT&&(j=r.R32F),L===r.HALF_FLOAT&&(j=r.R16F),L===r.UNSIGNED_BYTE&&(j=r.R8)),v===r.RED_INTEGER&&(L===r.UNSIGNED_BYTE&&(j=r.R8UI),L===r.UNSIGNED_SHORT&&(j=r.R16UI),L===r.UNSIGNED_INT&&(j=r.R32UI),L===r.BYTE&&(j=r.R8I),L===r.SHORT&&(j=r.R16I),L===r.INT&&(j=r.R32I)),v===r.RG&&(L===r.FLOAT&&(j=r.RG32F),L===r.HALF_FLOAT&&(j=r.RG16F),L===r.UNSIGNED_BYTE&&(j=r.RG8)),v===r.RG_INTEGER&&(L===r.UNSIGNED_BYTE&&(j=r.RG8UI),L===r.UNSIGNED_SHORT&&(j=r.RG16UI),L===r.UNSIGNED_INT&&(j=r.RG32UI),L===r.BYTE&&(j=r.RG8I),L===r.SHORT&&(j=r.RG16I),L===r.INT&&(j=r.RG32I)),v===r.RGB_INTEGER&&(L===r.UNSIGNED_BYTE&&(j=r.RGB8UI),L===r.UNSIGNED_SHORT&&(j=r.RGB16UI),L===r.UNSIGNED_INT&&(j=r.RGB32UI),L===r.BYTE&&(j=r.RGB8I),L===r.SHORT&&(j=r.RGB16I),L===r.INT&&(j=r.RGB32I)),v===r.RGBA_INTEGER&&(L===r.UNSIGNED_BYTE&&(j=r.RGBA8UI),L===r.UNSIGNED_SHORT&&(j=r.RGBA16UI),L===r.UNSIGNED_INT&&(j=r.RGBA32UI),L===r.BYTE&&(j=r.RGBA8I),L===r.SHORT&&(j=r.RGBA16I),L===r.INT&&(j=r.RGBA32I)),v===r.RGB&&(L===r.UNSIGNED_INT_5_9_9_9_REV&&(j=r.RGB9_E5),L===r.UNSIGNED_INT_10F_11F_11F_REV&&(j=r.R11F_G11F_B10F)),v===r.RGBA){const Ce=te?Ha:ct.getTransfer(Z);L===r.FLOAT&&(j=r.RGBA32F),L===r.HALF_FLOAT&&(j=r.RGBA16F),L===r.UNSIGNED_BYTE&&(j=Ce===mt?r.SRGB8_ALPHA8:r.RGBA8),L===r.UNSIGNED_SHORT_4_4_4_4&&(j=r.RGBA4),L===r.UNSIGNED_SHORT_5_5_5_1&&(j=r.RGB5_A1)}return(j===r.R16F||j===r.R32F||j===r.RG16F||j===r.RG32F||j===r.RGBA16F||j===r.RGBA32F)&&e.get("EXT_color_buffer_float"),j}function b(w,v){let L;return w?v===null||v===hi||v===zs?L=r.DEPTH24_STENCIL8:v===ai?L=r.DEPTH32F_STENCIL8:v===ks&&(L=r.DEPTH24_STENCIL8,Xe("DepthTexture: 16 bit depth attachment is not supported with stencil. Using 24-bit attachment.")):v===null||v===hi||v===zs?L=r.DEPTH_COMPONENT24:v===ai?L=r.DEPTH_COMPONENT32F:v===ks&&(L=r.DEPTH_COMPONENT16),L}function T(w,v){return d(w)===!0||w.isFramebufferTexture&&w.minFilter!==$t&&w.minFilter!==nn?Math.log2(Math.max(v.width,v.height))+1:w.mipmaps!==void 0&&w.mipmaps.length>0?w.mipmaps.length:w.isCompressedTexture&&Array.isArray(w.image)?v.mipmaps.length:1}function A(w){const v=w.target;v.removeEventListener("dispose",A),x(v),v.isVideoTexture&&u.delete(v)}function R(w){const v=w.target;v.removeEventListener("dispose",R),W(v)}function x(w){const v=n.get(w);if(v.__webglInit===void 0)return;const L=w.source,Z=h.get(L);if(Z){const te=Z[v.__cacheKey];te.usedTimes--,te.usedTimes===0&&S(w),Object.keys(Z).length===0&&h.delete(L)}n.remove(w)}function S(w){const v=n.get(w);r.deleteTexture(v.__webglTexture);const L=w.source,Z=h.get(L);delete Z[v.__cacheKey],a.memory.textures--}function W(w){const v=n.get(w);if(w.depthTexture&&(w.depthTexture.dispose(),n.remove(w.depthTexture)),w.isWebGLCubeRenderTarget)for(let Z=0;Z<6;Z++){if(Array.isArray(v.__webglFramebuffer[Z]))for(let te=0;te<v.__webglFramebuffer[Z].length;te++)r.deleteFramebuffer(v.__webglFramebuffer[Z][te]);else r.deleteFramebuffer(v.__webglFramebuffer[Z]);v.__webglDepthbuffer&&r.deleteRenderbuffer(v.__webglDepthbuffer[Z])}else{if(Array.isArray(v.__webglFramebuffer))for(let Z=0;Z<v.__webglFramebuffer.length;Z++)r.deleteFramebuffer(v.__webglFramebuffer[Z]);else r.deleteFramebuffer(v.__webglFramebuffer);if(v.__webglDepthbuffer&&r.deleteRenderbuffer(v.__webglDepthbuffer),v.__webglMultisampledFramebuffer&&r.deleteFramebuffer(v.__webglMultisampledFramebuffer),v.__webglColorRenderbuffer)for(let Z=0;Z<v.__webglColorRenderbuffer.length;Z++)v.__webglColorRenderbuffer[Z]&&r.deleteRenderbuffer(v.__webglColorRenderbuffer[Z]);v.__webglDepthRenderbuffer&&r.deleteRenderbuffer(v.__webglDepthRenderbuffer)}const L=w.textures;for(let Z=0,te=L.length;Z<te;Z++){const j=n.get(L[Z]);j.__webglTexture&&(r.deleteTexture(j.__webglTexture),a.memory.textures--),n.remove(L[Z])}n.remove(w)}let P=0;function z(){P=0}function F(){const w=P;return w>=i.maxTextures&&Xe("WebGLTextures: Trying to use "+w+" texture units while this GPU supports only "+i.maxTextures),P+=1,w}function B(w){const v=[];return v.push(w.wrapS),v.push(w.wrapT),v.push(w.wrapR||0),v.push(w.magFilter),v.push(w.minFilter),v.push(w.anisotropy),v.push(w.internalFormat),v.push(w.format),v.push(w.type),v.push(w.generateMipmaps),v.push(w.premultiplyAlpha),v.push(w.flipY),v.push(w.unpackAlignment),v.push(w.colorSpace),v.join()}function O(w,v){const L=n.get(w);if(w.isVideoTexture&&oe(w),w.isRenderTargetTexture===!1&&w.isExternalTexture!==!0&&w.version>0&&L.__version!==w.version){const Z=w.image;if(Z===null)Xe("WebGLRenderer: Texture marked for update but no image data found.");else if(Z.complete===!1)Xe("WebGLRenderer: Texture marked for update but image is incomplete");else{ee(L,w,v);return}}else w.isExternalTexture&&(L.__webglTexture=w.sourceTexture?w.sourceTexture:null);t.bindTexture(r.TEXTURE_2D,L.__webglTexture,r.TEXTURE0+v)}function V(w,v){const L=n.get(w);if(w.isRenderTargetTexture===!1&&w.version>0&&L.__version!==w.version){ee(L,w,v);return}else w.isExternalTexture&&(L.__webglTexture=w.sourceTexture?w.sourceTexture:null);t.bindTexture(r.TEXTURE_2D_ARRAY,L.__webglTexture,r.TEXTURE0+v)}function G(w,v){const L=n.get(w);if(w.isRenderTargetTexture===!1&&w.version>0&&L.__version!==w.version){ee(L,w,v);return}t.bindTexture(r.TEXTURE_3D,L.__webglTexture,r.TEXTURE0+v)}function ne(w,v){const L=n.get(w);if(w.isCubeDepthTexture!==!0&&w.version>0&&L.__version!==w.version){he(L,w,v);return}t.bindTexture(r.TEXTURE_CUBE_MAP,L.__webglTexture,r.TEXTURE0+v)}const Q={[cl]:r.REPEAT,[Ei]:r.CLAMP_TO_EDGE,[ul]:r.MIRRORED_REPEAT},me={[$t]:r.NEAREST,[Vd]:r.NEAREST_MIPMAP_NEAREST,[na]:r.NEAREST_MIPMAP_LINEAR,[nn]:r.LINEAR,[uo]:r.LINEAR_MIPMAP_NEAREST,[gr]:r.LINEAR_MIPMAP_LINEAR},be={[Wd]:r.NEVER,[Kd]:r.ALWAYS,[Xd]:r.LESS,[_c]:r.LEQUAL,[qd]:r.EQUAL,[gc]:r.GEQUAL,[Yd]:r.GREATER,[$d]:r.NOTEQUAL};function xe(w,v){if(v.type===ai&&e.has("OES_texture_float_linear")===!1&&(v.magFilter===nn||v.magFilter===uo||v.magFilter===na||v.magFilter===gr||v.minFilter===nn||v.minFilter===uo||v.minFilter===na||v.minFilter===gr)&&Xe("WebGLRenderer: Unable to use linear filtering with floating point textures. OES_texture_float_linear not supported on this device."),r.texParameteri(w,r.TEXTURE_WRAP_S,Q[v.wrapS]),r.texParameteri(w,r.TEXTURE_WRAP_T,Q[v.wrapT]),(w===r.TEXTURE_3D||w===r.TEXTURE_2D_ARRAY)&&r.texParameteri(w,r.TEXTURE_WRAP_R,Q[v.wrapR]),r.texParameteri(w,r.TEXTURE_MAG_FILTER,me[v.magFilter]),r.texParameteri(w,r.TEXTURE_MIN_FILTER,me[v.minFilter]),v.compareFunction&&(r.texParameteri(w,r.TEXTURE_COMPARE_MODE,r.COMPARE_REF_TO_TEXTURE),r.texParameteri(w,r.TEXTURE_COMPARE_FUNC,be[v.compareFunction])),e.has("EXT_texture_filter_anisotropic")===!0){if(v.magFilter===$t||v.minFilter!==na&&v.minFilter!==gr||v.type===ai&&e.has("OES_texture_float_linear")===!1)return;if(v.anisotropy>1||n.get(v).__currentAnisotropy){const L=e.get("EXT_texture_filter_anisotropic");r.texParameterf(w,L.TEXTURE_MAX_ANISOTROPY_EXT,Math.min(v.anisotropy,i.getMaxAnisotropy())),n.get(v).__currentAnisotropy=v.anisotropy}}}function Ge(w,v){let L=!1;w.__webglInit===void 0&&(w.__webglInit=!0,v.addEventListener("dispose",A));const Z=v.source;let te=h.get(Z);te===void 0&&(te={},h.set(Z,te));const j=B(v);if(j!==w.__cacheKey){te[j]===void 0&&(te[j]={texture:r.createTexture(),usedTimes:0},a.memory.textures++,L=!0),te[j].usedTimes++;const Ce=te[w.__cacheKey];Ce!==void 0&&(te[w.__cacheKey].usedTimes--,Ce.usedTimes===0&&S(v)),w.__cacheKey=j,w.__webglTexture=te[j].texture}return L}function qe(w,v,L){return Math.floor(Math.floor(w/L)/v)}function je(w,v,L,Z){const j=w.updateRanges;if(j.length===0)t.texSubImage2D(r.TEXTURE_2D,0,0,0,v.width,v.height,L,Z,v.data);else{j.sort((ie,le)=>ie.start-le.start);let Ce=0;for(let ie=1;ie<j.length;ie++){const le=j[Ce],Re=j[ie],Te=le.start+le.count,ge=qe(Re.start,v.width,4),Ye=qe(le.start,v.width,4);Re.start<=Te+1&&ge===Ye&&qe(Re.start+Re.count-1,v.width,4)===ge?le.count=Math.max(le.count,Re.start+Re.count-le.start):(++Ce,j[Ce]=Re)}j.length=Ce+1;const fe=r.getParameter(r.UNPACK_ROW_LENGTH),we=r.getParameter(r.UNPACK_SKIP_PIXELS),Fe=r.getParameter(r.UNPACK_SKIP_ROWS);r.pixelStorei(r.UNPACK_ROW_LENGTH,v.width);for(let ie=0,le=j.length;ie<le;ie++){const Re=j[ie],Te=Math.floor(Re.start/4),ge=Math.ceil(Re.count/4),Ye=Te%v.width,I=Math.floor(Te/v.width),de=ge,ce=1;r.pixelStorei(r.UNPACK_SKIP_PIXELS,Ye),r.pixelStorei(r.UNPACK_SKIP_ROWS,I),t.texSubImage2D(r.TEXTURE_2D,0,Ye,I,de,ce,L,Z,v.data)}w.clearUpdateRanges(),r.pixelStorei(r.UNPACK_ROW_LENGTH,fe),r.pixelStorei(r.UNPACK_SKIP_PIXELS,we),r.pixelStorei(r.UNPACK_SKIP_ROWS,Fe)}}function ee(w,v,L){let Z=r.TEXTURE_2D;(v.isDataArrayTexture||v.isCompressedArrayTexture)&&(Z=r.TEXTURE_2D_ARRAY),v.isData3DTexture&&(Z=r.TEXTURE_3D);const te=Ge(w,v),j=v.source;t.bindTexture(Z,w.__webglTexture,r.TEXTURE0+L);const Ce=n.get(j);if(j.version!==Ce.__version||te===!0){t.activeTexture(r.TEXTURE0+L);const fe=ct.getPrimaries(ct.workingColorSpace),we=v.colorSpace===Hi?null:ct.getPrimaries(v.colorSpace),Fe=v.colorSpace===Hi||fe===we?r.NONE:r.BROWSER_DEFAULT_WEBGL;r.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,v.flipY),r.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,v.premultiplyAlpha),r.pixelStorei(r.UNPACK_ALIGNMENT,v.unpackAlignment),r.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,Fe);let ie=g(v.image,!1,i.maxTextureSize);ie=ue(v,ie);const le=s.convert(v.format,v.colorSpace),Re=s.convert(v.type);let Te=y(v.internalFormat,le,Re,v.colorSpace,v.isVideoTexture);xe(Z,v);let ge;const Ye=v.mipmaps,I=v.isVideoTexture!==!0,de=Ce.__version===void 0||te===!0,ce=j.dataReady,Ae=T(v,ie);if(v.isDepthTexture)Te=b(v.format===vr,v.type),de&&(I?t.texStorage2D(r.TEXTURE_2D,1,Te,ie.width,ie.height):t.texImage2D(r.TEXTURE_2D,0,Te,ie.width,ie.height,0,le,Re,null));else if(v.isDataTexture)if(Ye.length>0){I&&de&&t.texStorage2D(r.TEXTURE_2D,Ae,Te,Ye[0].width,Ye[0].height);for(let se=0,$=Ye.length;se<$;se++)ge=Ye[se],I?ce&&t.texSubImage2D(r.TEXTURE_2D,se,0,0,ge.width,ge.height,le,Re,ge.data):t.texImage2D(r.TEXTURE_2D,se,Te,ge.width,ge.height,0,le,Re,ge.data);v.generateMipmaps=!1}else I?(de&&t.texStorage2D(r.TEXTURE_2D,Ae,Te,ie.width,ie.height),ce&&je(v,ie,le,Re)):t.texImage2D(r.TEXTURE_2D,0,Te,ie.width,ie.height,0,le,Re,ie.data);else if(v.isCompressedTexture)if(v.isCompressedArrayTexture){I&&de&&t.texStorage3D(r.TEXTURE_2D_ARRAY,Ae,Te,Ye[0].width,Ye[0].height,ie.depth);for(let se=0,$=Ye.length;se<$;se++)if(ge=Ye[se],v.format!==Kn)if(le!==null)if(I){if(ce)if(v.layerUpdates.size>0){const Pe=Cu(ge.width,ge.height,v.format,v.type);for(const Oe of v.layerUpdates){const ht=ge.data.subarray(Oe*Pe/ge.data.BYTES_PER_ELEMENT,(Oe+1)*Pe/ge.data.BYTES_PER_ELEMENT);t.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,se,0,0,Oe,ge.width,ge.height,1,le,ht)}v.clearLayerUpdates()}else t.compressedTexSubImage3D(r.TEXTURE_2D_ARRAY,se,0,0,0,ge.width,ge.height,ie.depth,le,ge.data)}else t.compressedTexImage3D(r.TEXTURE_2D_ARRAY,se,Te,ge.width,ge.height,ie.depth,0,ge.data,0,0);else Xe("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()");else I?ce&&t.texSubImage3D(r.TEXTURE_2D_ARRAY,se,0,0,0,ge.width,ge.height,ie.depth,le,Re,ge.data):t.texImage3D(r.TEXTURE_2D_ARRAY,se,Te,ge.width,ge.height,ie.depth,0,le,Re,ge.data)}else{I&&de&&t.texStorage2D(r.TEXTURE_2D,Ae,Te,Ye[0].width,Ye[0].height);for(let se=0,$=Ye.length;se<$;se++)ge=Ye[se],v.format!==Kn?le!==null?I?ce&&t.compressedTexSubImage2D(r.TEXTURE_2D,se,0,0,ge.width,ge.height,le,ge.data):t.compressedTexImage2D(r.TEXTURE_2D,se,Te,ge.width,ge.height,0,ge.data):Xe("WebGLRenderer: Attempt to load unsupported compressed texture format in .uploadTexture()"):I?ce&&t.texSubImage2D(r.TEXTURE_2D,se,0,0,ge.width,ge.height,le,Re,ge.data):t.texImage2D(r.TEXTURE_2D,se,Te,ge.width,ge.height,0,le,Re,ge.data)}else if(v.isDataArrayTexture)if(I){if(de&&t.texStorage3D(r.TEXTURE_2D_ARRAY,Ae,Te,ie.width,ie.height,ie.depth),ce)if(v.layerUpdates.size>0){const se=Cu(ie.width,ie.height,v.format,v.type);for(const $ of v.layerUpdates){const Pe=ie.data.subarray($*se/ie.data.BYTES_PER_ELEMENT,($+1)*se/ie.data.BYTES_PER_ELEMENT);t.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,$,ie.width,ie.height,1,le,Re,Pe)}v.clearLayerUpdates()}else t.texSubImage3D(r.TEXTURE_2D_ARRAY,0,0,0,0,ie.width,ie.height,ie.depth,le,Re,ie.data)}else t.texImage3D(r.TEXTURE_2D_ARRAY,0,Te,ie.width,ie.height,ie.depth,0,le,Re,ie.data);else if(v.isData3DTexture)I?(de&&t.texStorage3D(r.TEXTURE_3D,Ae,Te,ie.width,ie.height,ie.depth),ce&&t.texSubImage3D(r.TEXTURE_3D,0,0,0,0,ie.width,ie.height,ie.depth,le,Re,ie.data)):t.texImage3D(r.TEXTURE_3D,0,Te,ie.width,ie.height,ie.depth,0,le,Re,ie.data);else if(v.isFramebufferTexture){if(de)if(I)t.texStorage2D(r.TEXTURE_2D,Ae,Te,ie.width,ie.height);else{let se=ie.width,$=ie.height;for(let Pe=0;Pe<Ae;Pe++)t.texImage2D(r.TEXTURE_2D,Pe,Te,se,$,0,le,Re,null),se>>=1,$>>=1}}else if(Ye.length>0){if(I&&de){const se=ae(Ye[0]);t.texStorage2D(r.TEXTURE_2D,Ae,Te,se.width,se.height)}for(let se=0,$=Ye.length;se<$;se++)ge=Ye[se],I?ce&&t.texSubImage2D(r.TEXTURE_2D,se,0,0,le,Re,ge):t.texImage2D(r.TEXTURE_2D,se,Te,le,Re,ge);v.generateMipmaps=!1}else if(I){if(de){const se=ae(ie);t.texStorage2D(r.TEXTURE_2D,Ae,Te,se.width,se.height)}ce&&t.texSubImage2D(r.TEXTURE_2D,0,0,0,le,Re,ie)}else t.texImage2D(r.TEXTURE_2D,0,Te,le,Re,ie);d(v)&&p(Z),Ce.__version=j.version,v.onUpdate&&v.onUpdate(v)}w.__version=v.version}function he(w,v,L){if(v.image.length!==6)return;const Z=Ge(w,v),te=v.source;t.bindTexture(r.TEXTURE_CUBE_MAP,w.__webglTexture,r.TEXTURE0+L);const j=n.get(te);if(te.version!==j.__version||Z===!0){t.activeTexture(r.TEXTURE0+L);const Ce=ct.getPrimaries(ct.workingColorSpace),fe=v.colorSpace===Hi?null:ct.getPrimaries(v.colorSpace),we=v.colorSpace===Hi||Ce===fe?r.NONE:r.BROWSER_DEFAULT_WEBGL;r.pixelStorei(r.UNPACK_FLIP_Y_WEBGL,v.flipY),r.pixelStorei(r.UNPACK_PREMULTIPLY_ALPHA_WEBGL,v.premultiplyAlpha),r.pixelStorei(r.UNPACK_ALIGNMENT,v.unpackAlignment),r.pixelStorei(r.UNPACK_COLORSPACE_CONVERSION_WEBGL,we);const Fe=v.isCompressedTexture||v.image[0].isCompressedTexture,ie=v.image[0]&&v.image[0].isDataTexture,le=[];for(let $=0;$<6;$++)!Fe&&!ie?le[$]=g(v.image[$],!0,i.maxCubemapSize):le[$]=ie?v.image[$].image:v.image[$],le[$]=ue(v,le[$]);const Re=le[0],Te=s.convert(v.format,v.colorSpace),ge=s.convert(v.type),Ye=y(v.internalFormat,Te,ge,v.colorSpace),I=v.isVideoTexture!==!0,de=j.__version===void 0||Z===!0,ce=te.dataReady;let Ae=T(v,Re);xe(r.TEXTURE_CUBE_MAP,v);let se;if(Fe){I&&de&&t.texStorage2D(r.TEXTURE_CUBE_MAP,Ae,Ye,Re.width,Re.height);for(let $=0;$<6;$++){se=le[$].mipmaps;for(let Pe=0;Pe<se.length;Pe++){const Oe=se[Pe];v.format!==Kn?Te!==null?I?ce&&t.compressedTexSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe,0,0,Oe.width,Oe.height,Te,Oe.data):t.compressedTexImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe,Ye,Oe.width,Oe.height,0,Oe.data):Xe("WebGLRenderer: Attempt to load unsupported compressed texture format in .setTextureCube()"):I?ce&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe,0,0,Oe.width,Oe.height,Te,ge,Oe.data):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe,Ye,Oe.width,Oe.height,0,Te,ge,Oe.data)}}}else{if(se=v.mipmaps,I&&de){se.length>0&&Ae++;const $=ae(le[0]);t.texStorage2D(r.TEXTURE_CUBE_MAP,Ae,Ye,$.width,$.height)}for(let $=0;$<6;$++)if(ie){I?ce&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,0,0,le[$].width,le[$].height,Te,ge,le[$].data):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,Ye,le[$].width,le[$].height,0,Te,ge,le[$].data);for(let Pe=0;Pe<se.length;Pe++){const ht=se[Pe].image[$].image;I?ce&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe+1,0,0,ht.width,ht.height,Te,ge,ht.data):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe+1,Ye,ht.width,ht.height,0,Te,ge,ht.data)}}else{I?ce&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,0,0,Te,ge,le[$]):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,0,Ye,Te,ge,le[$]);for(let Pe=0;Pe<se.length;Pe++){const Oe=se[Pe];I?ce&&t.texSubImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe+1,0,0,Te,ge,Oe.image[$]):t.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+$,Pe+1,Ye,Te,ge,Oe.image[$])}}}d(v)&&p(r.TEXTURE_CUBE_MAP),j.__version=te.version,v.onUpdate&&v.onUpdate(v)}w.__version=v.version}function pe(w,v,L,Z,te,j){const Ce=s.convert(L.format,L.colorSpace),fe=s.convert(L.type),we=y(L.internalFormat,Ce,fe,L.colorSpace),Fe=n.get(v),ie=n.get(L);if(ie.__renderTarget=v,!Fe.__hasExternalTextures){const le=Math.max(1,v.width>>j),Re=Math.max(1,v.height>>j);te===r.TEXTURE_3D||te===r.TEXTURE_2D_ARRAY?t.texImage3D(te,j,we,le,Re,v.depth,0,Ce,fe,null):t.texImage2D(te,j,we,le,Re,0,Ce,fe,null)}t.bindFramebuffer(r.FRAMEBUFFER,w),Ee(v)?o.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,Z,te,ie.__webglTexture,0,C(v)):(te===r.TEXTURE_2D||te>=r.TEXTURE_CUBE_MAP_POSITIVE_X&&te<=r.TEXTURE_CUBE_MAP_NEGATIVE_Z)&&r.framebufferTexture2D(r.FRAMEBUFFER,Z,te,ie.__webglTexture,j),t.bindFramebuffer(r.FRAMEBUFFER,null)}function He(w,v,L){if(r.bindRenderbuffer(r.RENDERBUFFER,w),v.depthBuffer){const Z=v.depthTexture,te=Z&&Z.isDepthTexture?Z.type:null,j=b(v.stencilBuffer,te),Ce=v.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;Ee(v)?o.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,C(v),j,v.width,v.height):L?r.renderbufferStorageMultisample(r.RENDERBUFFER,C(v),j,v.width,v.height):r.renderbufferStorage(r.RENDERBUFFER,j,v.width,v.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,Ce,r.RENDERBUFFER,w)}else{const Z=v.textures;for(let te=0;te<Z.length;te++){const j=Z[te],Ce=s.convert(j.format,j.colorSpace),fe=s.convert(j.type),we=y(j.internalFormat,Ce,fe,j.colorSpace);Ee(v)?o.renderbufferStorageMultisampleEXT(r.RENDERBUFFER,C(v),we,v.width,v.height):L?r.renderbufferStorageMultisample(r.RENDERBUFFER,C(v),we,v.width,v.height):r.renderbufferStorage(r.RENDERBUFFER,we,v.width,v.height)}}r.bindRenderbuffer(r.RENDERBUFFER,null)}function ke(w,v,L){const Z=v.isWebGLCubeRenderTarget===!0;if(t.bindFramebuffer(r.FRAMEBUFFER,w),!(v.depthTexture&&v.depthTexture.isDepthTexture))throw new Error("renderTarget.depthTexture must be an instance of THREE.DepthTexture");const te=n.get(v.depthTexture);if(te.__renderTarget=v,(!te.__webglTexture||v.depthTexture.image.width!==v.width||v.depthTexture.image.height!==v.height)&&(v.depthTexture.image.width=v.width,v.depthTexture.image.height=v.height,v.depthTexture.needsUpdate=!0),Z){if(te.__webglInit===void 0&&(te.__webglInit=!0,v.depthTexture.addEventListener("dispose",A)),te.__webglTexture===void 0){te.__webglTexture=r.createTexture(),t.bindTexture(r.TEXTURE_CUBE_MAP,te.__webglTexture),xe(r.TEXTURE_CUBE_MAP,v.depthTexture);const Fe=s.convert(v.depthTexture.format),ie=s.convert(v.depthTexture.type);let le;v.depthTexture.format===Ri?le=r.DEPTH_COMPONENT24:v.depthTexture.format===vr&&(le=r.DEPTH24_STENCIL8);for(let Re=0;Re<6;Re++)r.texImage2D(r.TEXTURE_CUBE_MAP_POSITIVE_X+Re,0,le,v.width,v.height,0,Fe,ie,null)}}else O(v.depthTexture,0);const j=te.__webglTexture,Ce=C(v),fe=Z?r.TEXTURE_CUBE_MAP_POSITIVE_X+L:r.TEXTURE_2D,we=v.depthTexture.format===vr?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;if(v.depthTexture.format===Ri)Ee(v)?o.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,we,fe,j,0,Ce):r.framebufferTexture2D(r.FRAMEBUFFER,we,fe,j,0);else if(v.depthTexture.format===vr)Ee(v)?o.framebufferTexture2DMultisampleEXT(r.FRAMEBUFFER,we,fe,j,0,Ce):r.framebufferTexture2D(r.FRAMEBUFFER,we,fe,j,0);else throw new Error("Unknown depthTexture format")}function ze(w){const v=n.get(w),L=w.isWebGLCubeRenderTarget===!0;if(v.__boundDepthTexture!==w.depthTexture){const Z=w.depthTexture;if(v.__depthDisposeCallback&&v.__depthDisposeCallback(),Z){const te=()=>{delete v.__boundDepthTexture,delete v.__depthDisposeCallback,Z.removeEventListener("dispose",te)};Z.addEventListener("dispose",te),v.__depthDisposeCallback=te}v.__boundDepthTexture=Z}if(w.depthTexture&&!v.__autoAllocateDepthBuffer)if(L)for(let Z=0;Z<6;Z++)ke(v.__webglFramebuffer[Z],w,Z);else{const Z=w.texture.mipmaps;Z&&Z.length>0?ke(v.__webglFramebuffer[0],w,0):ke(v.__webglFramebuffer,w,0)}else if(L){v.__webglDepthbuffer=[];for(let Z=0;Z<6;Z++)if(t.bindFramebuffer(r.FRAMEBUFFER,v.__webglFramebuffer[Z]),v.__webglDepthbuffer[Z]===void 0)v.__webglDepthbuffer[Z]=r.createRenderbuffer(),He(v.__webglDepthbuffer[Z],w,!1);else{const te=w.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,j=v.__webglDepthbuffer[Z];r.bindRenderbuffer(r.RENDERBUFFER,j),r.framebufferRenderbuffer(r.FRAMEBUFFER,te,r.RENDERBUFFER,j)}}else{const Z=w.texture.mipmaps;if(Z&&Z.length>0?t.bindFramebuffer(r.FRAMEBUFFER,v.__webglFramebuffer[0]):t.bindFramebuffer(r.FRAMEBUFFER,v.__webglFramebuffer),v.__webglDepthbuffer===void 0)v.__webglDepthbuffer=r.createRenderbuffer(),He(v.__webglDepthbuffer,w,!1);else{const te=w.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,j=v.__webglDepthbuffer;r.bindRenderbuffer(r.RENDERBUFFER,j),r.framebufferRenderbuffer(r.FRAMEBUFFER,te,r.RENDERBUFFER,j)}}t.bindFramebuffer(r.FRAMEBUFFER,null)}function Et(w,v,L){const Z=n.get(w);v!==void 0&&pe(Z.__webglFramebuffer,w,w.texture,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,0),L!==void 0&&ze(w)}function Je(w){const v=w.texture,L=n.get(w),Z=n.get(v);w.addEventListener("dispose",R);const te=w.textures,j=w.isWebGLCubeRenderTarget===!0,Ce=te.length>1;if(Ce||(Z.__webglTexture===void 0&&(Z.__webglTexture=r.createTexture()),Z.__version=v.version,a.memory.textures++),j){L.__webglFramebuffer=[];for(let fe=0;fe<6;fe++)if(v.mipmaps&&v.mipmaps.length>0){L.__webglFramebuffer[fe]=[];for(let we=0;we<v.mipmaps.length;we++)L.__webglFramebuffer[fe][we]=r.createFramebuffer()}else L.__webglFramebuffer[fe]=r.createFramebuffer()}else{if(v.mipmaps&&v.mipmaps.length>0){L.__webglFramebuffer=[];for(let fe=0;fe<v.mipmaps.length;fe++)L.__webglFramebuffer[fe]=r.createFramebuffer()}else L.__webglFramebuffer=r.createFramebuffer();if(Ce)for(let fe=0,we=te.length;fe<we;fe++){const Fe=n.get(te[fe]);Fe.__webglTexture===void 0&&(Fe.__webglTexture=r.createTexture(),a.memory.textures++)}if(w.samples>0&&Ee(w)===!1){L.__webglMultisampledFramebuffer=r.createFramebuffer(),L.__webglColorRenderbuffer=[],t.bindFramebuffer(r.FRAMEBUFFER,L.__webglMultisampledFramebuffer);for(let fe=0;fe<te.length;fe++){const we=te[fe];L.__webglColorRenderbuffer[fe]=r.createRenderbuffer(),r.bindRenderbuffer(r.RENDERBUFFER,L.__webglColorRenderbuffer[fe]);const Fe=s.convert(we.format,we.colorSpace),ie=s.convert(we.type),le=y(we.internalFormat,Fe,ie,we.colorSpace,w.isXRRenderTarget===!0),Re=C(w);r.renderbufferStorageMultisample(r.RENDERBUFFER,Re,le,w.width,w.height),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+fe,r.RENDERBUFFER,L.__webglColorRenderbuffer[fe])}r.bindRenderbuffer(r.RENDERBUFFER,null),w.depthBuffer&&(L.__webglDepthRenderbuffer=r.createRenderbuffer(),He(L.__webglDepthRenderbuffer,w,!0)),t.bindFramebuffer(r.FRAMEBUFFER,null)}}if(j){t.bindTexture(r.TEXTURE_CUBE_MAP,Z.__webglTexture),xe(r.TEXTURE_CUBE_MAP,v);for(let fe=0;fe<6;fe++)if(v.mipmaps&&v.mipmaps.length>0)for(let we=0;we<v.mipmaps.length;we++)pe(L.__webglFramebuffer[fe][we],w,v,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+fe,we);else pe(L.__webglFramebuffer[fe],w,v,r.COLOR_ATTACHMENT0,r.TEXTURE_CUBE_MAP_POSITIVE_X+fe,0);d(v)&&p(r.TEXTURE_CUBE_MAP),t.unbindTexture()}else if(Ce){for(let fe=0,we=te.length;fe<we;fe++){const Fe=te[fe],ie=n.get(Fe);let le=r.TEXTURE_2D;(w.isWebGL3DRenderTarget||w.isWebGLArrayRenderTarget)&&(le=w.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),t.bindTexture(le,ie.__webglTexture),xe(le,Fe),pe(L.__webglFramebuffer,w,Fe,r.COLOR_ATTACHMENT0+fe,le,0),d(Fe)&&p(le)}t.unbindTexture()}else{let fe=r.TEXTURE_2D;if((w.isWebGL3DRenderTarget||w.isWebGLArrayRenderTarget)&&(fe=w.isWebGL3DRenderTarget?r.TEXTURE_3D:r.TEXTURE_2D_ARRAY),t.bindTexture(fe,Z.__webglTexture),xe(fe,v),v.mipmaps&&v.mipmaps.length>0)for(let we=0;we<v.mipmaps.length;we++)pe(L.__webglFramebuffer[we],w,v,r.COLOR_ATTACHMENT0,fe,we);else pe(L.__webglFramebuffer,w,v,r.COLOR_ATTACHMENT0,fe,0);d(v)&&p(fe),t.unbindTexture()}w.depthBuffer&&ze(w)}function nt(w){const v=w.textures;for(let L=0,Z=v.length;L<Z;L++){const te=v[L];if(d(te)){const j=M(w),Ce=n.get(te).__webglTexture;t.bindTexture(j,Ce),p(j),t.unbindTexture()}}}const st=[],K=[];function re(w){if(w.samples>0){if(Ee(w)===!1){const v=w.textures,L=w.width,Z=w.height;let te=r.COLOR_BUFFER_BIT;const j=w.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT,Ce=n.get(w),fe=v.length>1;if(fe)for(let Fe=0;Fe<v.length;Fe++)t.bindFramebuffer(r.FRAMEBUFFER,Ce.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+Fe,r.RENDERBUFFER,null),t.bindFramebuffer(r.FRAMEBUFFER,Ce.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+Fe,r.TEXTURE_2D,null,0);t.bindFramebuffer(r.READ_FRAMEBUFFER,Ce.__webglMultisampledFramebuffer);const we=w.texture.mipmaps;we&&we.length>0?t.bindFramebuffer(r.DRAW_FRAMEBUFFER,Ce.__webglFramebuffer[0]):t.bindFramebuffer(r.DRAW_FRAMEBUFFER,Ce.__webglFramebuffer);for(let Fe=0;Fe<v.length;Fe++){if(w.resolveDepthBuffer&&(w.depthBuffer&&(te|=r.DEPTH_BUFFER_BIT),w.stencilBuffer&&w.resolveStencilBuffer&&(te|=r.STENCIL_BUFFER_BIT)),fe){r.framebufferRenderbuffer(r.READ_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.RENDERBUFFER,Ce.__webglColorRenderbuffer[Fe]);const ie=n.get(v[Fe]).__webglTexture;r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0,r.TEXTURE_2D,ie,0)}r.blitFramebuffer(0,0,L,Z,0,0,L,Z,te,r.NEAREST),c===!0&&(st.length=0,K.length=0,st.push(r.COLOR_ATTACHMENT0+Fe),w.depthBuffer&&w.resolveDepthBuffer===!1&&(st.push(j),K.push(j),r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,K)),r.invalidateFramebuffer(r.READ_FRAMEBUFFER,st))}if(t.bindFramebuffer(r.READ_FRAMEBUFFER,null),t.bindFramebuffer(r.DRAW_FRAMEBUFFER,null),fe)for(let Fe=0;Fe<v.length;Fe++){t.bindFramebuffer(r.FRAMEBUFFER,Ce.__webglMultisampledFramebuffer),r.framebufferRenderbuffer(r.FRAMEBUFFER,r.COLOR_ATTACHMENT0+Fe,r.RENDERBUFFER,Ce.__webglColorRenderbuffer[Fe]);const ie=n.get(v[Fe]).__webglTexture;t.bindFramebuffer(r.FRAMEBUFFER,Ce.__webglFramebuffer),r.framebufferTexture2D(r.DRAW_FRAMEBUFFER,r.COLOR_ATTACHMENT0+Fe,r.TEXTURE_2D,ie,0)}t.bindFramebuffer(r.DRAW_FRAMEBUFFER,Ce.__webglMultisampledFramebuffer)}else if(w.depthBuffer&&w.resolveDepthBuffer===!1&&c){const v=w.stencilBuffer?r.DEPTH_STENCIL_ATTACHMENT:r.DEPTH_ATTACHMENT;r.invalidateFramebuffer(r.DRAW_FRAMEBUFFER,[v])}}}function C(w){return Math.min(i.maxSamples,w.samples)}function Ee(w){const v=n.get(w);return w.samples>0&&e.has("WEBGL_multisampled_render_to_texture")===!0&&v.__useRenderToTexture!==!1}function oe(w){const v=a.render.frame;u.get(w)!==v&&(u.set(w,v),w.update())}function ue(w,v){const L=w.colorSpace,Z=w.format,te=w.type;return w.isCompressedTexture===!0||w.isVideoTexture===!0||L!==ls&&L!==Hi&&(ct.getTransfer(L)===mt?(Z!==Kn||te!==Cn)&&Xe("WebGLTextures: sRGB encoded textures have to use RGBAFormat and UnsignedByteType."):lt("WebGLTextures: Unsupported texture color space:",L)),v}function ae(w){return typeof HTMLImageElement<"u"&&w instanceof HTMLImageElement?(l.width=w.naturalWidth||w.width,l.height=w.naturalHeight||w.height):typeof VideoFrame<"u"&&w instanceof VideoFrame?(l.width=w.displayWidth,l.height=w.displayHeight):(l.width=w.width,l.height=w.height),l}this.allocateTextureUnit=F,this.resetTextureUnits=z,this.setTexture2D=O,this.setTexture2DArray=V,this.setTexture3D=G,this.setTextureCube=ne,this.rebindTextures=Et,this.setupRenderTarget=Je,this.updateRenderTargetMipmap=nt,this.updateMultisampleRenderTarget=re,this.setupDepthRenderbuffer=ze,this.setupFrameBufferTexture=pe,this.useMultisampledRTT=Ee,this.isReversedDepthBuffer=function(){return t.buffers.depth.getReversed()}}function Av(r,e){function t(n,i=Hi){let s;const a=ct.getTransfer(i);if(n===Cn)return r.UNSIGNED_BYTE;if(n===hc)return r.UNSIGNED_SHORT_4_4_4_4;if(n===fc)return r.UNSIGNED_SHORT_5_5_5_1;if(n===Bh)return r.UNSIGNED_INT_5_9_9_9_REV;if(n===kh)return r.UNSIGNED_INT_10F_11F_11F_REV;if(n===Fh)return r.BYTE;if(n===Oh)return r.SHORT;if(n===ks)return r.UNSIGNED_SHORT;if(n===uc)return r.INT;if(n===hi)return r.UNSIGNED_INT;if(n===ai)return r.FLOAT;if(n===wi)return r.HALF_FLOAT;if(n===zh)return r.ALPHA;if(n===Vh)return r.RGB;if(n===Kn)return r.RGBA;if(n===Ri)return r.DEPTH_COMPONENT;if(n===vr)return r.DEPTH_STENCIL;if(n===Gh)return r.RED;if(n===dc)return r.RED_INTEGER;if(n===os)return r.RG;if(n===pc)return r.RG_INTEGER;if(n===mc)return r.RGBA_INTEGER;if(n===Ua||n===Na||n===Fa||n===Oa)if(a===mt)if(s=e.get("WEBGL_compressed_texture_s3tc_srgb"),s!==null){if(n===Ua)return s.COMPRESSED_SRGB_S3TC_DXT1_EXT;if(n===Na)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT;if(n===Fa)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT;if(n===Oa)return s.COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT}else return null;else if(s=e.get("WEBGL_compressed_texture_s3tc"),s!==null){if(n===Ua)return s.COMPRESSED_RGB_S3TC_DXT1_EXT;if(n===Na)return s.COMPRESSED_RGBA_S3TC_DXT1_EXT;if(n===Fa)return s.COMPRESSED_RGBA_S3TC_DXT3_EXT;if(n===Oa)return s.COMPRESSED_RGBA_S3TC_DXT5_EXT}else return null;if(n===hl||n===fl||n===dl||n===pl)if(s=e.get("WEBGL_compressed_texture_pvrtc"),s!==null){if(n===hl)return s.COMPRESSED_RGB_PVRTC_4BPPV1_IMG;if(n===fl)return s.COMPRESSED_RGB_PVRTC_2BPPV1_IMG;if(n===dl)return s.COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;if(n===pl)return s.COMPRESSED_RGBA_PVRTC_2BPPV1_IMG}else return null;if(n===ml||n===_l||n===gl||n===vl||n===xl||n===Ml||n===Sl)if(s=e.get("WEBGL_compressed_texture_etc"),s!==null){if(n===ml||n===_l)return a===mt?s.COMPRESSED_SRGB8_ETC2:s.COMPRESSED_RGB8_ETC2;if(n===gl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ETC2_EAC:s.COMPRESSED_RGBA8_ETC2_EAC;if(n===vl)return s.COMPRESSED_R11_EAC;if(n===xl)return s.COMPRESSED_SIGNED_R11_EAC;if(n===Ml)return s.COMPRESSED_RG11_EAC;if(n===Sl)return s.COMPRESSED_SIGNED_RG11_EAC}else return null;if(n===yl||n===bl||n===El||n===Tl||n===Al||n===wl||n===Rl||n===Cl||n===Pl||n===Dl||n===Ll||n===Il||n===Ul||n===Nl)if(s=e.get("WEBGL_compressed_texture_astc"),s!==null){if(n===yl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR:s.COMPRESSED_RGBA_ASTC_4x4_KHR;if(n===bl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR:s.COMPRESSED_RGBA_ASTC_5x4_KHR;if(n===El)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR:s.COMPRESSED_RGBA_ASTC_5x5_KHR;if(n===Tl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR:s.COMPRESSED_RGBA_ASTC_6x5_KHR;if(n===Al)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR:s.COMPRESSED_RGBA_ASTC_6x6_KHR;if(n===wl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR:s.COMPRESSED_RGBA_ASTC_8x5_KHR;if(n===Rl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR:s.COMPRESSED_RGBA_ASTC_8x6_KHR;if(n===Cl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR:s.COMPRESSED_RGBA_ASTC_8x8_KHR;if(n===Pl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR:s.COMPRESSED_RGBA_ASTC_10x5_KHR;if(n===Dl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR:s.COMPRESSED_RGBA_ASTC_10x6_KHR;if(n===Ll)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR:s.COMPRESSED_RGBA_ASTC_10x8_KHR;if(n===Il)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR:s.COMPRESSED_RGBA_ASTC_10x10_KHR;if(n===Ul)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR:s.COMPRESSED_RGBA_ASTC_12x10_KHR;if(n===Nl)return a===mt?s.COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR:s.COMPRESSED_RGBA_ASTC_12x12_KHR}else return null;if(n===Fl||n===Ol||n===Bl)if(s=e.get("EXT_texture_compression_bptc"),s!==null){if(n===Fl)return a===mt?s.COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT:s.COMPRESSED_RGBA_BPTC_UNORM_EXT;if(n===Ol)return s.COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT;if(n===Bl)return s.COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT}else return null;if(n===kl||n===zl||n===Vl||n===Gl)if(s=e.get("EXT_texture_compression_rgtc"),s!==null){if(n===kl)return s.COMPRESSED_RED_RGTC1_EXT;if(n===zl)return s.COMPRESSED_SIGNED_RED_RGTC1_EXT;if(n===Vl)return s.COMPRESSED_RED_GREEN_RGTC2_EXT;if(n===Gl)return s.COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT}else return null;return n===zs?r.UNSIGNED_INT_24_8:r[n]!==void 0?r[n]:null}return{convert:t}}const wv=`
void main() {

	gl_Position = vec4( position, 1.0 );

}`,Rv=`
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

}`;class Cv{constructor(){this.texture=null,this.mesh=null,this.depthNear=0,this.depthFar=0}init(e,t){if(this.texture===null){const n=new jh(e.texture);(e.depthNear!==t.depthNear||e.depthFar!==t.depthFar)&&(this.depthNear=e.depthNear,this.depthFar=e.depthFar),this.texture=n}}getMesh(e){if(this.texture!==null&&this.mesh===null){const t=e.cameras[0].viewport,n=new di({vertexShader:wv,fragmentShader:Rv,uniforms:{depthColor:{value:this.texture},depthWidth:{value:t.z},depthHeight:{value:t.w}}});this.mesh=new Yt(new Qs(20,20),n)}return this.mesh}reset(){this.texture=null,this.mesh=null}getDepthTexture(){return this.texture}}class Pv extends Tr{constructor(e,t){super();const n=this;let i=null,s=1,a=null,o="local-floor",c=1,l=null,u=null,f=null,h=null,m=null,_=null;const g=typeof XRWebGLBinding<"u",d=new Cv,p={},M=t.getContextAttributes();let y=null,b=null;const T=[],A=[],R=new $e;let x=null;const S=new Rn;S.viewport=new Dt;const W=new Rn;W.viewport=new Dt;const P=[S,W],z=new kp;let F=null,B=null;this.cameraAutoUpdate=!0,this.enabled=!1,this.isPresenting=!1,this.getController=function(ee){let he=T[ee];return he===void 0&&(he=new vo,T[ee]=he),he.getTargetRaySpace()},this.getControllerGrip=function(ee){let he=T[ee];return he===void 0&&(he=new vo,T[ee]=he),he.getGripSpace()},this.getHand=function(ee){let he=T[ee];return he===void 0&&(he=new vo,T[ee]=he),he.getHandSpace()};function O(ee){const he=A.indexOf(ee.inputSource);if(he===-1)return;const pe=T[he];pe!==void 0&&(pe.update(ee.inputSource,ee.frame,l||a),pe.dispatchEvent({type:ee.type,data:ee.inputSource}))}function V(){i.removeEventListener("select",O),i.removeEventListener("selectstart",O),i.removeEventListener("selectend",O),i.removeEventListener("squeeze",O),i.removeEventListener("squeezestart",O),i.removeEventListener("squeezeend",O),i.removeEventListener("end",V),i.removeEventListener("inputsourceschange",G);for(let ee=0;ee<T.length;ee++){const he=A[ee];he!==null&&(A[ee]=null,T[ee].disconnect(he))}F=null,B=null,d.reset();for(const ee in p)delete p[ee];e.setRenderTarget(y),m=null,h=null,f=null,i=null,b=null,je.stop(),n.isPresenting=!1,e.setPixelRatio(x),e.setSize(R.width,R.height,!1),n.dispatchEvent({type:"sessionend"})}this.setFramebufferScaleFactor=function(ee){s=ee,n.isPresenting===!0&&Xe("WebXRManager: Cannot change framebuffer scale while presenting.")},this.setReferenceSpaceType=function(ee){o=ee,n.isPresenting===!0&&Xe("WebXRManager: Cannot change reference space type while presenting.")},this.getReferenceSpace=function(){return l||a},this.setReferenceSpace=function(ee){l=ee},this.getBaseLayer=function(){return h!==null?h:m},this.getBinding=function(){return f===null&&g&&(f=new XRWebGLBinding(i,t)),f},this.getFrame=function(){return _},this.getSession=function(){return i},this.setSession=async function(ee){if(i=ee,i!==null){if(y=e.getRenderTarget(),i.addEventListener("select",O),i.addEventListener("selectstart",O),i.addEventListener("selectend",O),i.addEventListener("squeeze",O),i.addEventListener("squeezestart",O),i.addEventListener("squeezeend",O),i.addEventListener("end",V),i.addEventListener("inputsourceschange",G),M.xrCompatible!==!0&&await t.makeXRCompatible(),x=e.getPixelRatio(),e.getSize(R),g&&"createProjectionLayer"in XRWebGLBinding.prototype){let pe=null,He=null,ke=null;M.depth&&(ke=M.stencil?t.DEPTH24_STENCIL8:t.DEPTH_COMPONENT24,pe=M.stencil?vr:Ri,He=M.stencil?zs:hi);const ze={colorFormat:t.RGBA8,depthFormat:ke,scaleFactor:s};f=this.getBinding(),h=f.createProjectionLayer(ze),i.updateRenderState({layers:[h]}),e.setPixelRatio(1),e.setSize(h.textureWidth,h.textureHeight,!1),b=new ui(h.textureWidth,h.textureHeight,{format:Kn,type:Cn,depthTexture:new Gs(h.textureWidth,h.textureHeight,He,void 0,void 0,void 0,void 0,void 0,void 0,pe),stencilBuffer:M.stencil,colorSpace:e.outputColorSpace,samples:M.antialias?4:0,resolveDepthBuffer:h.ignoreDepthValues===!1,resolveStencilBuffer:h.ignoreDepthValues===!1})}else{const pe={antialias:M.antialias,alpha:!0,depth:M.depth,stencil:M.stencil,framebufferScaleFactor:s};m=new XRWebGLLayer(i,t,pe),i.updateRenderState({baseLayer:m}),e.setPixelRatio(1),e.setSize(m.framebufferWidth,m.framebufferHeight,!1),b=new ui(m.framebufferWidth,m.framebufferHeight,{format:Kn,type:Cn,colorSpace:e.outputColorSpace,stencilBuffer:M.stencil,resolveDepthBuffer:m.ignoreDepthValues===!1,resolveStencilBuffer:m.ignoreDepthValues===!1})}b.isXRRenderTarget=!0,this.setFoveation(c),l=null,a=await i.requestReferenceSpace(o),je.setContext(i),je.start(),n.isPresenting=!0,n.dispatchEvent({type:"sessionstart"})}},this.getEnvironmentBlendMode=function(){if(i!==null)return i.environmentBlendMode},this.getDepthTexture=function(){return d.getDepthTexture()};function G(ee){for(let he=0;he<ee.removed.length;he++){const pe=ee.removed[he],He=A.indexOf(pe);He>=0&&(A[He]=null,T[He].disconnect(pe))}for(let he=0;he<ee.added.length;he++){const pe=ee.added[he];let He=A.indexOf(pe);if(He===-1){for(let ze=0;ze<T.length;ze++)if(ze>=A.length){A.push(pe),He=ze;break}else if(A[ze]===null){A[ze]=pe,He=ze;break}if(He===-1)break}const ke=T[He];ke&&ke.connect(pe)}}const ne=new k,Q=new k;function me(ee,he,pe){ne.setFromMatrixPosition(he.matrixWorld),Q.setFromMatrixPosition(pe.matrixWorld);const He=ne.distanceTo(Q),ke=he.projectionMatrix.elements,ze=pe.projectionMatrix.elements,Et=ke[14]/(ke[10]-1),Je=ke[14]/(ke[10]+1),nt=(ke[9]+1)/ke[5],st=(ke[9]-1)/ke[5],K=(ke[8]-1)/ke[0],re=(ze[8]+1)/ze[0],C=Et*K,Ee=Et*re,oe=He/(-K+re),ue=oe*-K;if(he.matrixWorld.decompose(ee.position,ee.quaternion,ee.scale),ee.translateX(ue),ee.translateZ(oe),ee.matrixWorld.compose(ee.position,ee.quaternion,ee.scale),ee.matrixWorldInverse.copy(ee.matrixWorld).invert(),ke[10]===-1)ee.projectionMatrix.copy(he.projectionMatrix),ee.projectionMatrixInverse.copy(he.projectionMatrixInverse);else{const ae=Et+oe,w=Je+oe,v=C-ue,L=Ee+(He-ue),Z=nt*Je/w*ae,te=st*Je/w*ae;ee.projectionMatrix.makePerspective(v,L,Z,te,ae,w),ee.projectionMatrixInverse.copy(ee.projectionMatrix).invert()}}function be(ee,he){he===null?ee.matrixWorld.copy(ee.matrix):ee.matrixWorld.multiplyMatrices(he.matrixWorld,ee.matrix),ee.matrixWorldInverse.copy(ee.matrixWorld).invert()}this.updateCamera=function(ee){if(i===null)return;let he=ee.near,pe=ee.far;d.texture!==null&&(d.depthNear>0&&(he=d.depthNear),d.depthFar>0&&(pe=d.depthFar)),z.near=W.near=S.near=he,z.far=W.far=S.far=pe,(F!==z.near||B!==z.far)&&(i.updateRenderState({depthNear:z.near,depthFar:z.far}),F=z.near,B=z.far),z.layers.mask=ee.layers.mask|6,S.layers.mask=z.layers.mask&-5,W.layers.mask=z.layers.mask&-3;const He=ee.parent,ke=z.cameras;be(z,He);for(let ze=0;ze<ke.length;ze++)be(ke[ze],He);ke.length===2?me(z,S,W):z.projectionMatrix.copy(S.projectionMatrix),xe(ee,z,He)};function xe(ee,he,pe){pe===null?ee.matrix.copy(he.matrixWorld):(ee.matrix.copy(pe.matrixWorld),ee.matrix.invert(),ee.matrix.multiply(he.matrixWorld)),ee.matrix.decompose(ee.position,ee.quaternion,ee.scale),ee.updateMatrixWorld(!0),ee.projectionMatrix.copy(he.projectionMatrix),ee.projectionMatrixInverse.copy(he.projectionMatrixInverse),ee.isPerspectiveCamera&&(ee.fov=Hl*2*Math.atan(1/ee.projectionMatrix.elements[5]),ee.zoom=1)}this.getCamera=function(){return z},this.getFoveation=function(){if(!(h===null&&m===null))return c},this.setFoveation=function(ee){c=ee,h!==null&&(h.fixedFoveation=ee),m!==null&&m.fixedFoveation!==void 0&&(m.fixedFoveation=ee)},this.hasDepthSensing=function(){return d.texture!==null},this.getDepthSensingMesh=function(){return d.getMesh(z)},this.getCameraTexture=function(ee){return p[ee]};let Ge=null;function qe(ee,he){if(u=he.getViewerPose(l||a),_=he,u!==null){const pe=u.views;m!==null&&(e.setRenderTargetFramebuffer(b,m.framebuffer),e.setRenderTarget(b));let He=!1;pe.length!==z.cameras.length&&(z.cameras.length=0,He=!0);for(let Je=0;Je<pe.length;Je++){const nt=pe[Je];let st=null;if(m!==null)st=m.getViewport(nt);else{const re=f.getViewSubImage(h,nt);st=re.viewport,Je===0&&(e.setRenderTargetTextures(b,re.colorTexture,re.depthStencilTexture),e.setRenderTarget(b))}let K=P[Je];K===void 0&&(K=new Rn,K.layers.enable(Je),K.viewport=new Dt,P[Je]=K),K.matrix.fromArray(nt.transform.matrix),K.matrix.decompose(K.position,K.quaternion,K.scale),K.projectionMatrix.fromArray(nt.projectionMatrix),K.projectionMatrixInverse.copy(K.projectionMatrix).invert(),K.viewport.set(st.x,st.y,st.width,st.height),Je===0&&(z.matrix.copy(K.matrix),z.matrix.decompose(z.position,z.quaternion,z.scale)),He===!0&&z.cameras.push(K)}const ke=i.enabledFeatures;if(ke&&ke.includes("depth-sensing")&&i.depthUsage=="gpu-optimized"&&g){f=n.getBinding();const Je=f.getDepthInformation(pe[0]);Je&&Je.isValid&&Je.texture&&d.init(Je,i.renderState)}if(ke&&ke.includes("camera-access")&&g){e.state.unbindTexture(),f=n.getBinding();for(let Je=0;Je<pe.length;Je++){const nt=pe[Je].camera;if(nt){let st=p[nt];st||(st=new jh,p[nt]=st);const K=f.getCameraImage(nt);st.sourceTexture=K}}}}for(let pe=0;pe<T.length;pe++){const He=A[pe],ke=T[pe];He!==null&&ke!==void 0&&ke.update(He,he,l||a)}Ge&&Ge(ee,he),he.detectedPlanes&&n.dispatchEvent({type:"planesdetected",data:he}),_=null}const je=new tf;je.setAnimationLoop(qe),this.setAnimationLoop=function(ee){Ge=ee},this.dispose=function(){}}}const ur=new fi,Dv=new yt;function Lv(r,e){function t(d,p){d.matrixAutoUpdate===!0&&d.updateMatrix(),p.value.copy(d.matrix)}function n(d,p){p.color.getRGB(d.fogColor.value,Jh(r)),p.isFog?(d.fogNear.value=p.near,d.fogFar.value=p.far):p.isFogExp2&&(d.fogDensity.value=p.density)}function i(d,p,M,y,b){p.isMeshBasicMaterial?s(d,p):p.isMeshLambertMaterial?(s(d,p),p.envMap&&(d.envMapIntensity.value=p.envMapIntensity)):p.isMeshToonMaterial?(s(d,p),f(d,p)):p.isMeshPhongMaterial?(s(d,p),u(d,p),p.envMap&&(d.envMapIntensity.value=p.envMapIntensity)):p.isMeshStandardMaterial?(s(d,p),h(d,p),p.isMeshPhysicalMaterial&&m(d,p,b)):p.isMeshMatcapMaterial?(s(d,p),_(d,p)):p.isMeshDepthMaterial?s(d,p):p.isMeshDistanceMaterial?(s(d,p),g(d,p)):p.isMeshNormalMaterial?s(d,p):p.isLineBasicMaterial?(a(d,p),p.isLineDashedMaterial&&o(d,p)):p.isPointsMaterial?c(d,p,M,y):p.isSpriteMaterial?l(d,p):p.isShadowMaterial?(d.color.value.copy(p.color),d.opacity.value=p.opacity):p.isShaderMaterial&&(p.uniformsNeedUpdate=!1)}function s(d,p){d.opacity.value=p.opacity,p.color&&d.diffuse.value.copy(p.color),p.emissive&&d.emissive.value.copy(p.emissive).multiplyScalar(p.emissiveIntensity),p.map&&(d.map.value=p.map,t(p.map,d.mapTransform)),p.alphaMap&&(d.alphaMap.value=p.alphaMap,t(p.alphaMap,d.alphaMapTransform)),p.bumpMap&&(d.bumpMap.value=p.bumpMap,t(p.bumpMap,d.bumpMapTransform),d.bumpScale.value=p.bumpScale,p.side===pn&&(d.bumpScale.value*=-1)),p.normalMap&&(d.normalMap.value=p.normalMap,t(p.normalMap,d.normalMapTransform),d.normalScale.value.copy(p.normalScale),p.side===pn&&d.normalScale.value.negate()),p.displacementMap&&(d.displacementMap.value=p.displacementMap,t(p.displacementMap,d.displacementMapTransform),d.displacementScale.value=p.displacementScale,d.displacementBias.value=p.displacementBias),p.emissiveMap&&(d.emissiveMap.value=p.emissiveMap,t(p.emissiveMap,d.emissiveMapTransform)),p.specularMap&&(d.specularMap.value=p.specularMap,t(p.specularMap,d.specularMapTransform)),p.alphaTest>0&&(d.alphaTest.value=p.alphaTest);const M=e.get(p),y=M.envMap,b=M.envMapRotation;y&&(d.envMap.value=y,ur.copy(b),ur.x*=-1,ur.y*=-1,ur.z*=-1,y.isCubeTexture&&y.isRenderTargetTexture===!1&&(ur.y*=-1,ur.z*=-1),d.envMapRotation.value.setFromMatrix4(Dv.makeRotationFromEuler(ur)),d.flipEnvMap.value=y.isCubeTexture&&y.isRenderTargetTexture===!1?-1:1,d.reflectivity.value=p.reflectivity,d.ior.value=p.ior,d.refractionRatio.value=p.refractionRatio),p.lightMap&&(d.lightMap.value=p.lightMap,d.lightMapIntensity.value=p.lightMapIntensity,t(p.lightMap,d.lightMapTransform)),p.aoMap&&(d.aoMap.value=p.aoMap,d.aoMapIntensity.value=p.aoMapIntensity,t(p.aoMap,d.aoMapTransform))}function a(d,p){d.diffuse.value.copy(p.color),d.opacity.value=p.opacity,p.map&&(d.map.value=p.map,t(p.map,d.mapTransform))}function o(d,p){d.dashSize.value=p.dashSize,d.totalSize.value=p.dashSize+p.gapSize,d.scale.value=p.scale}function c(d,p,M,y){d.diffuse.value.copy(p.color),d.opacity.value=p.opacity,d.size.value=p.size*M,d.scale.value=y*.5,p.map&&(d.map.value=p.map,t(p.map,d.uvTransform)),p.alphaMap&&(d.alphaMap.value=p.alphaMap,t(p.alphaMap,d.alphaMapTransform)),p.alphaTest>0&&(d.alphaTest.value=p.alphaTest)}function l(d,p){d.diffuse.value.copy(p.color),d.opacity.value=p.opacity,d.rotation.value=p.rotation,p.map&&(d.map.value=p.map,t(p.map,d.mapTransform)),p.alphaMap&&(d.alphaMap.value=p.alphaMap,t(p.alphaMap,d.alphaMapTransform)),p.alphaTest>0&&(d.alphaTest.value=p.alphaTest)}function u(d,p){d.specular.value.copy(p.specular),d.shininess.value=Math.max(p.shininess,1e-4)}function f(d,p){p.gradientMap&&(d.gradientMap.value=p.gradientMap)}function h(d,p){d.metalness.value=p.metalness,p.metalnessMap&&(d.metalnessMap.value=p.metalnessMap,t(p.metalnessMap,d.metalnessMapTransform)),d.roughness.value=p.roughness,p.roughnessMap&&(d.roughnessMap.value=p.roughnessMap,t(p.roughnessMap,d.roughnessMapTransform)),p.envMap&&(d.envMapIntensity.value=p.envMapIntensity)}function m(d,p,M){d.ior.value=p.ior,p.sheen>0&&(d.sheenColor.value.copy(p.sheenColor).multiplyScalar(p.sheen),d.sheenRoughness.value=p.sheenRoughness,p.sheenColorMap&&(d.sheenColorMap.value=p.sheenColorMap,t(p.sheenColorMap,d.sheenColorMapTransform)),p.sheenRoughnessMap&&(d.sheenRoughnessMap.value=p.sheenRoughnessMap,t(p.sheenRoughnessMap,d.sheenRoughnessMapTransform))),p.clearcoat>0&&(d.clearcoat.value=p.clearcoat,d.clearcoatRoughness.value=p.clearcoatRoughness,p.clearcoatMap&&(d.clearcoatMap.value=p.clearcoatMap,t(p.clearcoatMap,d.clearcoatMapTransform)),p.clearcoatRoughnessMap&&(d.clearcoatRoughnessMap.value=p.clearcoatRoughnessMap,t(p.clearcoatRoughnessMap,d.clearcoatRoughnessMapTransform)),p.clearcoatNormalMap&&(d.clearcoatNormalMap.value=p.clearcoatNormalMap,t(p.clearcoatNormalMap,d.clearcoatNormalMapTransform),d.clearcoatNormalScale.value.copy(p.clearcoatNormalScale),p.side===pn&&d.clearcoatNormalScale.value.negate())),p.dispersion>0&&(d.dispersion.value=p.dispersion),p.iridescence>0&&(d.iridescence.value=p.iridescence,d.iridescenceIOR.value=p.iridescenceIOR,d.iridescenceThicknessMinimum.value=p.iridescenceThicknessRange[0],d.iridescenceThicknessMaximum.value=p.iridescenceThicknessRange[1],p.iridescenceMap&&(d.iridescenceMap.value=p.iridescenceMap,t(p.iridescenceMap,d.iridescenceMapTransform)),p.iridescenceThicknessMap&&(d.iridescenceThicknessMap.value=p.iridescenceThicknessMap,t(p.iridescenceThicknessMap,d.iridescenceThicknessMapTransform))),p.transmission>0&&(d.transmission.value=p.transmission,d.transmissionSamplerMap.value=M.texture,d.transmissionSamplerSize.value.set(M.width,M.height),p.transmissionMap&&(d.transmissionMap.value=p.transmissionMap,t(p.transmissionMap,d.transmissionMapTransform)),d.thickness.value=p.thickness,p.thicknessMap&&(d.thicknessMap.value=p.thicknessMap,t(p.thicknessMap,d.thicknessMapTransform)),d.attenuationDistance.value=p.attenuationDistance,d.attenuationColor.value.copy(p.attenuationColor)),p.anisotropy>0&&(d.anisotropyVector.value.set(p.anisotropy*Math.cos(p.anisotropyRotation),p.anisotropy*Math.sin(p.anisotropyRotation)),p.anisotropyMap&&(d.anisotropyMap.value=p.anisotropyMap,t(p.anisotropyMap,d.anisotropyMapTransform))),d.specularIntensity.value=p.specularIntensity,d.specularColor.value.copy(p.specularColor),p.specularColorMap&&(d.specularColorMap.value=p.specularColorMap,t(p.specularColorMap,d.specularColorMapTransform)),p.specularIntensityMap&&(d.specularIntensityMap.value=p.specularIntensityMap,t(p.specularIntensityMap,d.specularIntensityMapTransform))}function _(d,p){p.matcap&&(d.matcap.value=p.matcap)}function g(d,p){const M=e.get(p).light;d.referencePosition.value.setFromMatrixPosition(M.matrixWorld),d.nearDistance.value=M.shadow.camera.near,d.farDistance.value=M.shadow.camera.far}return{refreshFogUniforms:n,refreshMaterialUniforms:i}}function Iv(r,e,t,n){let i={},s={},a=[];const o=r.getParameter(r.MAX_UNIFORM_BUFFER_BINDINGS);function c(M,y){const b=y.program;n.uniformBlockBinding(M,b)}function l(M,y){let b=i[M.id];b===void 0&&(_(M),b=u(M),i[M.id]=b,M.addEventListener("dispose",d));const T=y.program;n.updateUBOMapping(M,T);const A=e.render.frame;s[M.id]!==A&&(h(M),s[M.id]=A)}function u(M){const y=f();M.__bindingPointIndex=y;const b=r.createBuffer(),T=M.__size,A=M.usage;return r.bindBuffer(r.UNIFORM_BUFFER,b),r.bufferData(r.UNIFORM_BUFFER,T,A),r.bindBuffer(r.UNIFORM_BUFFER,null),r.bindBufferBase(r.UNIFORM_BUFFER,y,b),b}function f(){for(let M=0;M<o;M++)if(a.indexOf(M)===-1)return a.push(M),M;return lt("WebGLRenderer: Maximum number of simultaneously usable uniforms groups reached."),0}function h(M){const y=i[M.id],b=M.uniforms,T=M.__cache;r.bindBuffer(r.UNIFORM_BUFFER,y);for(let A=0,R=b.length;A<R;A++){const x=Array.isArray(b[A])?b[A]:[b[A]];for(let S=0,W=x.length;S<W;S++){const P=x[S];if(m(P,A,S,T)===!0){const z=P.__offset,F=Array.isArray(P.value)?P.value:[P.value];let B=0;for(let O=0;O<F.length;O++){const V=F[O],G=g(V);typeof V=="number"||typeof V=="boolean"?(P.__data[0]=V,r.bufferSubData(r.UNIFORM_BUFFER,z+B,P.__data)):V.isMatrix3?(P.__data[0]=V.elements[0],P.__data[1]=V.elements[1],P.__data[2]=V.elements[2],P.__data[3]=0,P.__data[4]=V.elements[3],P.__data[5]=V.elements[4],P.__data[6]=V.elements[5],P.__data[7]=0,P.__data[8]=V.elements[6],P.__data[9]=V.elements[7],P.__data[10]=V.elements[8],P.__data[11]=0):(V.toArray(P.__data,B),B+=G.storage/Float32Array.BYTES_PER_ELEMENT)}r.bufferSubData(r.UNIFORM_BUFFER,z,P.__data)}}}r.bindBuffer(r.UNIFORM_BUFFER,null)}function m(M,y,b,T){const A=M.value,R=y+"_"+b;if(T[R]===void 0)return typeof A=="number"||typeof A=="boolean"?T[R]=A:T[R]=A.clone(),!0;{const x=T[R];if(typeof A=="number"||typeof A=="boolean"){if(x!==A)return T[R]=A,!0}else if(x.equals(A)===!1)return x.copy(A),!0}return!1}function _(M){const y=M.uniforms;let b=0;const T=16;for(let R=0,x=y.length;R<x;R++){const S=Array.isArray(y[R])?y[R]:[y[R]];for(let W=0,P=S.length;W<P;W++){const z=S[W],F=Array.isArray(z.value)?z.value:[z.value];for(let B=0,O=F.length;B<O;B++){const V=F[B],G=g(V),ne=b%T,Q=ne%G.boundary,me=ne+Q;b+=Q,me!==0&&T-me<G.storage&&(b+=T-me),z.__data=new Float32Array(G.storage/Float32Array.BYTES_PER_ELEMENT),z.__offset=b,b+=G.storage}}}const A=b%T;return A>0&&(b+=T-A),M.__size=b,M.__cache={},this}function g(M){const y={boundary:0,storage:0};return typeof M=="number"||typeof M=="boolean"?(y.boundary=4,y.storage=4):M.isVector2?(y.boundary=8,y.storage=8):M.isVector3||M.isColor?(y.boundary=16,y.storage=12):M.isVector4?(y.boundary=16,y.storage=16):M.isMatrix3?(y.boundary=48,y.storage=48):M.isMatrix4?(y.boundary=64,y.storage=64):M.isTexture?Xe("WebGLRenderer: Texture samplers can not be part of an uniforms group."):Xe("WebGLRenderer: Unsupported uniform value type.",M),y}function d(M){const y=M.target;y.removeEventListener("dispose",d);const b=a.indexOf(y.__bindingPointIndex);a.splice(b,1),r.deleteBuffer(i[y.id]),delete i[y.id],delete s[y.id]}function p(){for(const M in i)r.deleteBuffer(i[M]);a=[],i={},s={}}return{bind:c,update:l,dispose:p}}const Uv=new Uint16Array([12469,15057,12620,14925,13266,14620,13807,14376,14323,13990,14545,13625,14713,13328,14840,12882,14931,12528,14996,12233,15039,11829,15066,11525,15080,11295,15085,10976,15082,10705,15073,10495,13880,14564,13898,14542,13977,14430,14158,14124,14393,13732,14556,13410,14702,12996,14814,12596,14891,12291,14937,11834,14957,11489,14958,11194,14943,10803,14921,10506,14893,10278,14858,9960,14484,14039,14487,14025,14499,13941,14524,13740,14574,13468,14654,13106,14743,12678,14818,12344,14867,11893,14889,11509,14893,11180,14881,10751,14852,10428,14812,10128,14765,9754,14712,9466,14764,13480,14764,13475,14766,13440,14766,13347,14769,13070,14786,12713,14816,12387,14844,11957,14860,11549,14868,11215,14855,10751,14825,10403,14782,10044,14729,9651,14666,9352,14599,9029,14967,12835,14966,12831,14963,12804,14954,12723,14936,12564,14917,12347,14900,11958,14886,11569,14878,11247,14859,10765,14828,10401,14784,10011,14727,9600,14660,9289,14586,8893,14508,8533,15111,12234,15110,12234,15104,12216,15092,12156,15067,12010,15028,11776,14981,11500,14942,11205,14902,10752,14861,10393,14812,9991,14752,9570,14682,9252,14603,8808,14519,8445,14431,8145,15209,11449,15208,11451,15202,11451,15190,11438,15163,11384,15117,11274,15055,10979,14994,10648,14932,10343,14871,9936,14803,9532,14729,9218,14645,8742,14556,8381,14461,8020,14365,7603,15273,10603,15272,10607,15267,10619,15256,10631,15231,10614,15182,10535,15118,10389,15042,10167,14963,9787,14883,9447,14800,9115,14710,8665,14615,8318,14514,7911,14411,7507,14279,7198,15314,9675,15313,9683,15309,9712,15298,9759,15277,9797,15229,9773,15166,9668,15084,9487,14995,9274,14898,8910,14800,8539,14697,8234,14590,7790,14479,7409,14367,7067,14178,6621,15337,8619,15337,8631,15333,8677,15325,8769,15305,8871,15264,8940,15202,8909,15119,8775,15022,8565,14916,8328,14804,8009,14688,7614,14569,7287,14448,6888,14321,6483,14088,6171,15350,7402,15350,7419,15347,7480,15340,7613,15322,7804,15287,7973,15229,8057,15148,8012,15046,7846,14933,7611,14810,7357,14682,7069,14552,6656,14421,6316,14251,5948,14007,5528,15356,5942,15356,5977,15353,6119,15348,6294,15332,6551,15302,6824,15249,7044,15171,7122,15070,7050,14949,6861,14818,6611,14679,6349,14538,6067,14398,5651,14189,5311,13935,4958,15359,4123,15359,4153,15356,4296,15353,4646,15338,5160,15311,5508,15263,5829,15188,6042,15088,6094,14966,6001,14826,5796,14678,5543,14527,5287,14377,4985,14133,4586,13869,4257,15360,1563,15360,1642,15358,2076,15354,2636,15341,3350,15317,4019,15273,4429,15203,4732,15105,4911,14981,4932,14836,4818,14679,4621,14517,4386,14359,4156,14083,3795,13808,3437,15360,122,15360,137,15358,285,15355,636,15344,1274,15322,2177,15281,2765,15215,3223,15120,3451,14995,3569,14846,3567,14681,3466,14511,3305,14344,3121,14037,2800,13753,2467,15360,0,15360,1,15359,21,15355,89,15346,253,15325,479,15287,796,15225,1148,15133,1492,15008,1749,14856,1882,14685,1886,14506,1783,14324,1608,13996,1398,13702,1183]);let ni=null;function Nv(){return ni===null&&(ni=new Mp(Uv,16,16,os,wi),ni.name="DFG_LUT",ni.minFilter=nn,ni.magFilter=nn,ni.wrapS=Ei,ni.wrapT=Ei,ni.generateMipmaps=!1,ni.needsUpdate=!0),ni}class Fv{constructor(e={}){const{canvas:t=jd(),context:n=null,depth:i=!0,stencil:s=!1,alpha:a=!1,antialias:o=!1,premultipliedAlpha:c=!0,preserveDrawingBuffer:l=!1,powerPreference:u="default",failIfMajorPerformanceCaveat:f=!1,reversedDepthBuffer:h=!1,outputBufferType:m=Cn}=e;this.isWebGLRenderer=!0;let _;if(n!==null){if(typeof WebGLRenderingContext<"u"&&n instanceof WebGLRenderingContext)throw new Error("THREE.WebGLRenderer: WebGL 1 is not supported since r163.");_=n.getContextAttributes().alpha}else _=a;const g=m,d=new Set([mc,pc,dc]),p=new Set([Cn,hi,ks,zs,hc,fc]),M=new Uint32Array(4),y=new Int32Array(4);let b=null,T=null;const A=[],R=[];let x=null;this.domElement=t,this.debug={checkShaderErrors:!0,onShaderError:null},this.autoClear=!0,this.autoClearColor=!0,this.autoClearDepth=!0,this.autoClearStencil=!0,this.sortObjects=!0,this.clippingPlanes=[],this.localClippingEnabled=!1,this.toneMapping=ci,this.toneMappingExposure=1,this.transmissionResolutionScale=1;const S=this;let W=!1;this._outputColorSpace=An;let P=0,z=0,F=null,B=-1,O=null;const V=new Dt,G=new Dt;let ne=null;const Q=new rt(0);let me=0,be=t.width,xe=t.height,Ge=1,qe=null,je=null;const ee=new Dt(0,0,be,xe),he=new Dt(0,0,be,xe);let pe=!1;const He=new Sc;let ke=!1,ze=!1;const Et=new yt,Je=new k,nt=new Dt,st={background:null,fog:null,environment:null,overrideMaterial:null,isScene:!0};let K=!1;function re(){return F===null?Ge:1}let C=n;function Ee(E,N){return t.getContext(E,N)}try{const E={alpha:!0,depth:i,stencil:s,antialias:o,premultipliedAlpha:c,preserveDrawingBuffer:l,powerPreference:u,failIfMajorPerformanceCaveat:f};if("setAttribute"in t&&t.setAttribute("data-engine",`three.js r${lc}`),t.addEventListener("webglcontextlost",Pe,!1),t.addEventListener("webglcontextrestored",Oe,!1),t.addEventListener("webglcontextcreationerror",ht,!1),C===null){const N="webgl2";if(C=Ee(N,E),C===null)throw Ee(N)?new Error("Error creating WebGL context with your selected attributes."):new Error("Error creating WebGL context.")}}catch(E){throw lt("WebGLRenderer: "+E.message),E}let oe,ue,ae,w,v,L,Z,te,j,Ce,fe,we,Fe,ie,le,Re,Te,ge,Ye,I,de,ce,Ae;function se(){oe=new Fg(C),oe.init(),de=new Av(C,oe),ue=new Rg(C,oe,e,de),ae=new Ev(C,oe),ue.reversedDepthBuffer&&h&&ae.buffers.depth.setReversed(!0),w=new kg(C),v=new uv,L=new Tv(C,oe,ae,v,ue,de,w),Z=new Ng(S),te=new Wp(C),ce=new Ag(C,te),j=new Og(C,te,w,ce),Ce=new Vg(C,j,te,ce,w),ge=new zg(C,ue,L),le=new Cg(v),fe=new cv(S,Z,oe,ue,ce,le),we=new Lv(S,v),Fe=new fv,ie=new vv(oe),Te=new Tg(S,Z,ae,Ce,_,c),Re=new bv(S,Ce,ue),Ae=new Iv(C,w,ue,ae),Ye=new wg(C,oe,w),I=new Bg(C,oe,w),w.programs=fe.programs,S.capabilities=ue,S.extensions=oe,S.properties=v,S.renderLists=Fe,S.shadowMap=Re,S.state=ae,S.info=w}se(),g!==Cn&&(x=new Hg(g,t.width,t.height,i,s));const $=new Pv(S,C);this.xr=$,this.getContext=function(){return C},this.getContextAttributes=function(){return C.getContextAttributes()},this.forceContextLoss=function(){const E=oe.get("WEBGL_lose_context");E&&E.loseContext()},this.forceContextRestore=function(){const E=oe.get("WEBGL_lose_context");E&&E.restoreContext()},this.getPixelRatio=function(){return Ge},this.setPixelRatio=function(E){E!==void 0&&(Ge=E,this.setSize(be,xe,!1))},this.getSize=function(E){return E.set(be,xe)},this.setSize=function(E,N,Y=!0){if($.isPresenting){Xe("WebGLRenderer: Can't change size while VR device is presenting.");return}be=E,xe=N,t.width=Math.floor(E*Ge),t.height=Math.floor(N*Ge),Y===!0&&(t.style.width=E+"px",t.style.height=N+"px"),x!==null&&x.setSize(t.width,t.height),this.setViewport(0,0,E,N)},this.getDrawingBufferSize=function(E){return E.set(be*Ge,xe*Ge).floor()},this.setDrawingBufferSize=function(E,N,Y){be=E,xe=N,Ge=Y,t.width=Math.floor(E*Y),t.height=Math.floor(N*Y),this.setViewport(0,0,E,N)},this.setEffects=function(E){if(g===Cn){console.error("THREE.WebGLRenderer: setEffects() requires outputBufferType set to HalfFloatType or FloatType.");return}if(E){for(let N=0;N<E.length;N++)if(E[N].isOutputPass===!0){console.warn("THREE.WebGLRenderer: OutputPass is not needed in setEffects(). Tone mapping and color space conversion are applied automatically.");break}}x.setEffects(E||[])},this.getCurrentViewport=function(E){return E.copy(V)},this.getViewport=function(E){return E.copy(ee)},this.setViewport=function(E,N,Y,X){E.isVector4?ee.set(E.x,E.y,E.z,E.w):ee.set(E,N,Y,X),ae.viewport(V.copy(ee).multiplyScalar(Ge).round())},this.getScissor=function(E){return E.copy(he)},this.setScissor=function(E,N,Y,X){E.isVector4?he.set(E.x,E.y,E.z,E.w):he.set(E,N,Y,X),ae.scissor(G.copy(he).multiplyScalar(Ge).round())},this.getScissorTest=function(){return pe},this.setScissorTest=function(E){ae.setScissorTest(pe=E)},this.setOpaqueSort=function(E){qe=E},this.setTransparentSort=function(E){je=E},this.getClearColor=function(E){return E.copy(Te.getClearColor())},this.setClearColor=function(){Te.setClearColor(...arguments)},this.getClearAlpha=function(){return Te.getClearAlpha()},this.setClearAlpha=function(){Te.setClearAlpha(...arguments)},this.clear=function(E=!0,N=!0,Y=!0){let X=0;if(E){let H=!1;if(F!==null){const Me=F.texture.format;H=d.has(Me)}if(H){const Me=F.texture.type,Se=p.has(Me),_e=Te.getClearColor(),De=Te.getClearAlpha(),J=_e.r,We=_e.g,Qe=_e.b;Se?(M[0]=J,M[1]=We,M[2]=Qe,M[3]=De,C.clearBufferuiv(C.COLOR,0,M)):(y[0]=J,y[1]=We,y[2]=Qe,y[3]=De,C.clearBufferiv(C.COLOR,0,y))}else X|=C.COLOR_BUFFER_BIT}N&&(X|=C.DEPTH_BUFFER_BIT),Y&&(X|=C.STENCIL_BUFFER_BIT,this.state.buffers.stencil.setMask(4294967295)),X!==0&&C.clear(X)},this.clearColor=function(){this.clear(!0,!1,!1)},this.clearDepth=function(){this.clear(!1,!0,!1)},this.clearStencil=function(){this.clear(!1,!1,!0)},this.dispose=function(){t.removeEventListener("webglcontextlost",Pe,!1),t.removeEventListener("webglcontextrestored",Oe,!1),t.removeEventListener("webglcontextcreationerror",ht,!1),Te.dispose(),Fe.dispose(),ie.dispose(),v.dispose(),Z.dispose(),Ce.dispose(),ce.dispose(),Ae.dispose(),fe.dispose(),$.dispose(),$.removeEventListener("sessionstart",Di),$.removeEventListener("sessionend",nr),Gn.stop()};function Pe(E){E.preventDefault(),Qc("WebGLRenderer: Context Lost."),W=!0}function Oe(){Qc("WebGLRenderer: Context Restored."),W=!1;const E=w.autoReset,N=Re.enabled,Y=Re.autoUpdate,X=Re.needsUpdate,H=Re.type;se(),w.autoReset=E,Re.enabled=N,Re.autoUpdate=Y,Re.needsUpdate=X,Re.type=H}function ht(E){lt("WebGLRenderer: A WebGL context could not be created. Reason: ",E.statusMessage)}function ot(E){const N=E.target;N.removeEventListener("dispose",ot),Wt(N)}function Wt(E){sn(E),v.remove(E)}function sn(E){const N=v.get(E).programs;N!==void 0&&(N.forEach(function(Y){fe.releaseProgram(Y)}),E.isShaderMaterial&&fe.releaseShaderCache(E))}this.renderBufferDirect=function(E,N,Y,X,H,Me){N===null&&(N=st);const Se=H.isMesh&&H.matrixWorld.determinant()<0,_e=Mn(E,N,Y,X,H);ae.setMaterial(X,Se);let De=Y.index,J=1;if(X.wireframe===!0){if(De=j.getWireframeAttribute(Y),De===void 0)return;J=2}const We=Y.drawRange,Qe=Y.attributes.position;let Ne=We.start*J,_t=(We.start+We.count)*J;Me!==null&&(Ne=Math.max(Ne,Me.start*J),_t=Math.min(_t,(Me.start+Me.count)*J)),De!==null?(Ne=Math.max(Ne,0),_t=Math.min(_t,De.count)):Qe!=null&&(Ne=Math.max(Ne,0),_t=Math.min(_t,Qe.count));const It=_t-Ne;if(It<0||It===1/0)return;ce.setup(H,X,_e,Y,De);let Pt,gt=Ye;if(De!==null&&(Pt=te.get(De),gt=I,gt.setIndex(Pt)),H.isMesh)X.wireframe===!0?(ae.setLineWidth(X.wireframeLinewidth*re()),gt.setMode(C.LINES)):gt.setMode(C.TRIANGLES);else if(H.isLine){let jt=X.linewidth;jt===void 0&&(jt=1),ae.setLineWidth(jt*re()),H.isLineSegments?gt.setMode(C.LINES):H.isLineLoop?gt.setMode(C.LINE_LOOP):gt.setMode(C.LINE_STRIP)}else H.isPoints?gt.setMode(C.POINTS):H.isSprite&&gt.setMode(C.TRIANGLES);if(H.isBatchedMesh)if(H._multiDrawInstances!==null)Xa("WebGLRenderer: renderMultiDrawInstances has been deprecated and will be removed in r184. Append to renderMultiDraw arguments and use indirection."),gt.renderMultiDrawInstances(H._multiDrawStarts,H._multiDrawCounts,H._multiDrawCount,H._multiDrawInstances);else if(oe.get("WEBGL_multi_draw"))gt.renderMultiDraw(H._multiDrawStarts,H._multiDrawCounts,H._multiDrawCount);else{const jt=H._multiDrawStarts,Le=H._multiDrawCounts,Sn=H._multiDrawCount,ut=De?te.get(De).bytesPerElement:1,Wn=v.get(X).currentProgram.getUniforms();for(let ei=0;ei<Sn;ei++)Wn.setValue(C,"_gl_DrawID",ei),gt.render(jt[ei]/ut,Le[ei])}else if(H.isInstancedMesh)gt.renderInstances(Ne,It,H.count);else if(Y.isInstancedBufferGeometry){const jt=Y._maxInstanceCount!==void 0?Y._maxInstanceCount:1/0,Le=Math.min(Y.instanceCount,jt);gt.renderInstances(Ne,It,Le)}else gt.render(Ne,It)};function jn(E,N,Y){E.transparent===!0&&E.side===$n&&E.forceSinglePass===!1?(E.side=pn,E.needsUpdate=!0,ve(E,N,Y),E.side=Zi,E.needsUpdate=!0,ve(E,N,Y),E.side=$n):ve(E,N,Y)}this.compile=function(E,N,Y=null){Y===null&&(Y=E),T=ie.get(Y),T.init(N),R.push(T),Y.traverseVisible(function(H){H.isLight&&H.layers.test(N.layers)&&(T.pushLight(H),H.castShadow&&T.pushShadow(H))}),E!==Y&&E.traverseVisible(function(H){H.isLight&&H.layers.test(N.layers)&&(T.pushLight(H),H.castShadow&&T.pushShadow(H))}),T.setupLights();const X=new Set;return E.traverse(function(H){if(!(H.isMesh||H.isPoints||H.isLine||H.isSprite))return;const Me=H.material;if(Me)if(Array.isArray(Me))for(let Se=0;Se<Me.length;Se++){const _e=Me[Se];jn(_e,Y,H),X.add(_e)}else jn(Me,Y,H),X.add(Me)}),T=R.pop(),X},this.compileAsync=function(E,N,Y=null){const X=this.compile(E,N,Y);return new Promise(H=>{function Me(){if(X.forEach(function(Se){v.get(Se).currentProgram.isReady()&&X.delete(Se)}),X.size===0){H(E);return}setTimeout(Me,10)}oe.get("KHR_parallel_shader_compile")!==null?Me():setTimeout(Me,10)})};let Jn=null;function Vn(E){Jn&&Jn(E)}function Di(){Gn.stop()}function nr(){Gn.start()}const Gn=new tf;Gn.setAnimationLoop(Vn),typeof self<"u"&&Gn.setContext(self),this.setAnimationLoop=function(E){Jn=E,$.setAnimationLoop(E),E===null?Gn.stop():Gn.start()},$.addEventListener("sessionstart",Di),$.addEventListener("sessionend",nr),this.render=function(E,N){if(N!==void 0&&N.isCamera!==!0){lt("WebGLRenderer.render: camera is not an instance of THREE.Camera.");return}if(W===!0)return;const Y=$.enabled===!0&&$.isPresenting===!0,X=x!==null&&(F===null||Y)&&x.begin(S,F);if(E.matrixWorldAutoUpdate===!0&&E.updateMatrixWorld(),N.parent===null&&N.matrixWorldAutoUpdate===!0&&N.updateMatrixWorld(),$.enabled===!0&&$.isPresenting===!0&&(x===null||x.isCompositing()===!1)&&($.cameraAutoUpdate===!0&&$.updateCamera(N),N=$.getCamera()),E.isScene===!0&&E.onBeforeRender(S,E,N,F),T=ie.get(E,R.length),T.init(N),R.push(T),Et.multiplyMatrices(N.projectionMatrix,N.matrixWorldInverse),He.setFromProjectionMatrix(Et,oi,N.reversedDepth),ze=this.localClippingEnabled,ke=le.init(this.clippingPlanes,ze),b=Fe.get(E,A.length),b.init(),A.push(b),$.enabled===!0&&$.isPresenting===!0){const Se=S.xr.getDepthSensingMesh();Se!==null&&Qn(Se,N,-1/0,S.sortObjects)}Qn(E,N,0,S.sortObjects),b.finish(),S.sortObjects===!0&&b.sort(qe,je),K=$.enabled===!1||$.isPresenting===!1||$.hasDepthSensing()===!1,K&&Te.addToRenderList(b,E),this.info.render.frame++,ke===!0&&le.beginShadows();const H=T.state.shadowsArray;if(Re.render(H,E,N),ke===!0&&le.endShadows(),this.info.autoReset===!0&&this.info.reset(),(X&&x.hasRenderPass())===!1){const Se=b.opaque,_e=b.transmissive;if(T.setupLights(),N.isArrayCamera){const De=N.cameras;if(_e.length>0)for(let J=0,We=De.length;J<We;J++){const Qe=De[J];ir(Se,_e,E,Qe)}K&&Te.render(E);for(let J=0,We=De.length;J<We;J++){const Qe=De[J];mi(b,E,Qe,Qe.viewport)}}else _e.length>0&&ir(Se,_e,E,N),K&&Te.render(E),mi(b,E,N)}F!==null&&z===0&&(L.updateMultisampleRenderTarget(F),L.updateRenderTargetMipmap(F)),X&&x.end(S),E.isScene===!0&&E.onAfterRender(S,E,N),ce.resetDefaultState(),B=-1,O=null,R.pop(),R.length>0?(T=R[R.length-1],ke===!0&&le.setGlobalState(S.clippingPlanes,T.state.camera)):T=null,A.pop(),A.length>0?b=A[A.length-1]:b=null};function Qn(E,N,Y,X){if(E.visible===!1)return;if(E.layers.test(N.layers)){if(E.isGroup)Y=E.renderOrder;else if(E.isLOD)E.autoUpdate===!0&&E.update(N);else if(E.isLight)T.pushLight(E),E.castShadow&&T.pushShadow(E);else if(E.isSprite){if(!E.frustumCulled||He.intersectsSprite(E)){X&&nt.setFromMatrixPosition(E.matrixWorld).applyMatrix4(Et);const Se=Ce.update(E),_e=E.material;_e.visible&&b.push(E,Se,_e,Y,nt.z,null)}}else if((E.isMesh||E.isLine||E.isPoints)&&(!E.frustumCulled||He.intersectsObject(E))){const Se=Ce.update(E),_e=E.material;if(X&&(E.boundingSphere!==void 0?(E.boundingSphere===null&&E.computeBoundingSphere(),nt.copy(E.boundingSphere.center)):(Se.boundingSphere===null&&Se.computeBoundingSphere(),nt.copy(Se.boundingSphere.center)),nt.applyMatrix4(E.matrixWorld).applyMatrix4(Et)),Array.isArray(_e)){const De=Se.groups;for(let J=0,We=De.length;J<We;J++){const Qe=De[J],Ne=_e[Qe.materialIndex];Ne&&Ne.visible&&b.push(E,Se,Ne,Y,nt.z,Qe)}}else _e.visible&&b.push(E,Se,_e,Y,nt.z,null)}}const Me=E.children;for(let Se=0,_e=Me.length;Se<_e;Se++)Qn(Me[Se],N,Y,X)}function mi(E,N,Y,X){const{opaque:H,transmissive:Me,transparent:Se}=E;T.setupLightsView(Y),ke===!0&&le.setGlobalState(S.clippingPlanes,Y),X&&ae.viewport(V.copy(X)),H.length>0&&U(H,N,Y),Me.length>0&&U(Me,N,Y),Se.length>0&&U(Se,N,Y),ae.buffers.depth.setTest(!0),ae.buffers.depth.setMask(!0),ae.buffers.color.setMask(!0),ae.setPolygonOffset(!1)}function ir(E,N,Y,X){if((Y.isScene===!0?Y.overrideMaterial:null)!==null)return;if(T.state.transmissionRenderTarget[X.id]===void 0){const Ne=oe.has("EXT_color_buffer_half_float")||oe.has("EXT_color_buffer_float");T.state.transmissionRenderTarget[X.id]=new ui(1,1,{generateMipmaps:!0,type:Ne?wi:Cn,minFilter:gr,samples:Math.max(4,ue.samples),stencilBuffer:s,resolveDepthBuffer:!1,resolveStencilBuffer:!1,colorSpace:ct.workingColorSpace})}const Me=T.state.transmissionRenderTarget[X.id],Se=X.viewport||V;Me.setSize(Se.z*S.transmissionResolutionScale,Se.w*S.transmissionResolutionScale);const _e=S.getRenderTarget(),De=S.getActiveCubeFace(),J=S.getActiveMipmapLevel();S.setRenderTarget(Me),S.getClearColor(Q),me=S.getClearAlpha(),me<1&&S.setClearColor(16777215,.5),S.clear(),K&&Te.render(Y);const We=S.toneMapping;S.toneMapping=ci;const Qe=X.viewport;if(X.viewport!==void 0&&(X.viewport=void 0),T.setupLightsView(X),ke===!0&&le.setGlobalState(S.clippingPlanes,X),U(E,Y,X),L.updateMultisampleRenderTarget(Me),L.updateRenderTargetMipmap(Me),oe.has("WEBGL_multisampled_render_to_texture")===!1){let Ne=!1;for(let _t=0,It=N.length;_t<It;_t++){const Pt=N[_t],{object:gt,geometry:jt,material:Le,group:Sn}=Pt;if(Le.side===$n&&gt.layers.test(X.layers)){const ut=Le.side;Le.side=pn,Le.needsUpdate=!0,D(gt,Y,X,jt,Le,Sn),Le.side=ut,Le.needsUpdate=!0,Ne=!0}}Ne===!0&&(L.updateMultisampleRenderTarget(Me),L.updateRenderTargetMipmap(Me))}S.setRenderTarget(_e,De,J),S.setClearColor(Q,me),Qe!==void 0&&(X.viewport=Qe),S.toneMapping=We}function U(E,N,Y){const X=N.isScene===!0?N.overrideMaterial:null;for(let H=0,Me=E.length;H<Me;H++){const Se=E[H],{object:_e,geometry:De,group:J}=Se;let We=Se.material;We.allowOverride===!0&&X!==null&&(We=X),_e.layers.test(Y.layers)&&D(_e,N,Y,De,We,J)}}function D(E,N,Y,X,H,Me){E.onBeforeRender(S,N,Y,X,H,Me),E.modelViewMatrix.multiplyMatrices(Y.matrixWorldInverse,E.matrixWorld),E.normalMatrix.getNormalMatrix(E.modelViewMatrix),H.onBeforeRender(S,N,Y,X,E,Me),H.transparent===!0&&H.side===$n&&H.forceSinglePass===!1?(H.side=pn,H.needsUpdate=!0,S.renderBufferDirect(Y,N,X,H,E,Me),H.side=Zi,H.needsUpdate=!0,S.renderBufferDirect(Y,N,X,H,E,Me),H.side=$n):S.renderBufferDirect(Y,N,X,H,E,Me),E.onAfterRender(S,N,Y,X,H,Me)}function ve(E,N,Y){N.isScene!==!0&&(N=st);const X=v.get(E),H=T.state.lights,Me=T.state.shadowsArray,Se=H.state.version,_e=fe.getParameters(E,H.state,Me,N,Y),De=fe.getProgramCacheKey(_e);let J=X.programs;X.environment=E.isMeshStandardMaterial||E.isMeshLambertMaterial||E.isMeshPhongMaterial?N.environment:null,X.fog=N.fog;const We=E.isMeshStandardMaterial||E.isMeshLambertMaterial&&!E.envMap||E.isMeshPhongMaterial&&!E.envMap;X.envMap=Z.get(E.envMap||X.environment,We),X.envMapRotation=X.environment!==null&&E.envMap===null?N.environmentRotation:E.envMapRotation,J===void 0&&(E.addEventListener("dispose",ot),J=new Map,X.programs=J);let Qe=J.get(De);if(Qe!==void 0){if(X.currentProgram===Qe&&X.lightsStateVersion===Se)return tt(E,_e),Qe}else _e.uniforms=fe.getUniforms(E),E.onBeforeCompile(_e,S),Qe=fe.acquireProgram(_e,De),J.set(De,Qe),X.uniforms=_e.uniforms;const Ne=X.uniforms;return(!E.isShaderMaterial&&!E.isRawShaderMaterial||E.clipping===!0)&&(Ne.clippingPlanes=le.uniform),tt(E,_e),X.needsLights=Hn(E),X.lightsStateVersion=Se,X.needsLights&&(Ne.ambientLightColor.value=H.state.ambient,Ne.lightProbe.value=H.state.probe,Ne.directionalLights.value=H.state.directional,Ne.directionalLightShadows.value=H.state.directionalShadow,Ne.spotLights.value=H.state.spot,Ne.spotLightShadows.value=H.state.spotShadow,Ne.rectAreaLights.value=H.state.rectArea,Ne.ltc_1.value=H.state.rectAreaLTC1,Ne.ltc_2.value=H.state.rectAreaLTC2,Ne.pointLights.value=H.state.point,Ne.pointLightShadows.value=H.state.pointShadow,Ne.hemisphereLights.value=H.state.hemi,Ne.directionalShadowMatrix.value=H.state.directionalShadowMatrix,Ne.spotLightMatrix.value=H.state.spotLightMatrix,Ne.spotLightMap.value=H.state.spotLightMap,Ne.pointShadowMatrix.value=H.state.pointShadowMatrix),X.currentProgram=Qe,X.uniformsList=null,Qe}function Ue(E){if(E.uniformsList===null){const N=E.currentProgram.getUniforms();E.uniformsList=ka.seqWithValue(N.seq,E.uniforms)}return E.uniformsList}function tt(E,N){const Y=v.get(E);Y.outputColorSpace=N.outputColorSpace,Y.batching=N.batching,Y.batchingColor=N.batchingColor,Y.instancing=N.instancing,Y.instancingColor=N.instancingColor,Y.instancingMorph=N.instancingMorph,Y.skinning=N.skinning,Y.morphTargets=N.morphTargets,Y.morphNormals=N.morphNormals,Y.morphColors=N.morphColors,Y.morphTargetsCount=N.morphTargetsCount,Y.numClippingPlanes=N.numClippingPlanes,Y.numIntersection=N.numClipIntersection,Y.vertexAlphas=N.vertexAlphas,Y.vertexTangents=N.vertexTangents,Y.toneMapping=N.toneMapping}function Mn(E,N,Y,X,H){N.isScene!==!0&&(N=st),L.resetTextureUnits();const Me=N.fog,Se=X.isMeshStandardMaterial||X.isMeshLambertMaterial||X.isMeshPhongMaterial?N.environment:null,_e=F===null?S.outputColorSpace:F.isXRRenderTarget===!0?F.texture.colorSpace:ls,De=X.isMeshStandardMaterial||X.isMeshLambertMaterial&&!X.envMap||X.isMeshPhongMaterial&&!X.envMap,J=Z.get(X.envMap||Se,De),We=X.vertexColors===!0&&!!Y.attributes.color&&Y.attributes.color.itemSize===4,Qe=!!Y.attributes.tangent&&(!!X.normalMap||X.anisotropy>0),Ne=!!Y.morphAttributes.position,_t=!!Y.morphAttributes.normal,It=!!Y.morphAttributes.color;let Pt=ci;X.toneMapped&&(F===null||F.isXRRenderTarget===!0)&&(Pt=S.toneMapping);const gt=Y.morphAttributes.position||Y.morphAttributes.normal||Y.morphAttributes.color,jt=gt!==void 0?gt.length:0,Le=v.get(X),Sn=T.state.lights;if(ke===!0&&(ze===!0||E!==O)){const Vt=E===O&&X.id===B;le.setState(X,E,Vt)}let ut=!1;X.version===Le.__version?(Le.needsLights&&Le.lightsStateVersion!==Sn.state.version||Le.outputColorSpace!==_e||H.isBatchedMesh&&Le.batching===!1||!H.isBatchedMesh&&Le.batching===!0||H.isBatchedMesh&&Le.batchingColor===!0&&H.colorTexture===null||H.isBatchedMesh&&Le.batchingColor===!1&&H.colorTexture!==null||H.isInstancedMesh&&Le.instancing===!1||!H.isInstancedMesh&&Le.instancing===!0||H.isSkinnedMesh&&Le.skinning===!1||!H.isSkinnedMesh&&Le.skinning===!0||H.isInstancedMesh&&Le.instancingColor===!0&&H.instanceColor===null||H.isInstancedMesh&&Le.instancingColor===!1&&H.instanceColor!==null||H.isInstancedMesh&&Le.instancingMorph===!0&&H.morphTexture===null||H.isInstancedMesh&&Le.instancingMorph===!1&&H.morphTexture!==null||Le.envMap!==J||X.fog===!0&&Le.fog!==Me||Le.numClippingPlanes!==void 0&&(Le.numClippingPlanes!==le.numPlanes||Le.numIntersection!==le.numIntersection)||Le.vertexAlphas!==We||Le.vertexTangents!==Qe||Le.morphTargets!==Ne||Le.morphNormals!==_t||Le.morphColors!==It||Le.toneMapping!==Pt||Le.morphTargetsCount!==jt)&&(ut=!0):(ut=!0,Le.__version=X.version);let Wn=Le.currentProgram;ut===!0&&(Wn=ve(X,N,H));let ei=!1,rr=!1,Cr=!1;const xt=Wn.getUniforms(),Xt=Le.uniforms;if(ae.useProgram(Wn.program)&&(ei=!0,rr=!0,Cr=!0),X.id!==B&&(B=X.id,rr=!0),ei||O!==E){ae.buffers.depth.getReversed()&&E.reversedDepth!==!0&&(E._reversedDepth=!0,E.updateProjectionMatrix()),xt.setValue(C,"projectionMatrix",E.projectionMatrix),xt.setValue(C,"viewMatrix",E.matrixWorldInverse);const Ui=xt.map.cameraPosition;Ui!==void 0&&Ui.setValue(C,Je.setFromMatrixPosition(E.matrixWorld)),ue.logarithmicDepthBuffer&&xt.setValue(C,"logDepthBufFC",2/(Math.log(E.far+1)/Math.LN2)),(X.isMeshPhongMaterial||X.isMeshToonMaterial||X.isMeshLambertMaterial||X.isMeshBasicMaterial||X.isMeshStandardMaterial||X.isShaderMaterial)&&xt.setValue(C,"isOrthographic",E.isOrthographicCamera===!0),O!==E&&(O=E,rr=!0,Cr=!0)}if(Le.needsLights&&(Sn.state.directionalShadowMap.length>0&&xt.setValue(C,"directionalShadowMap",Sn.state.directionalShadowMap,L),Sn.state.spotShadowMap.length>0&&xt.setValue(C,"spotShadowMap",Sn.state.spotShadowMap,L),Sn.state.pointShadowMap.length>0&&xt.setValue(C,"pointShadowMap",Sn.state.pointShadowMap,L)),H.isSkinnedMesh){xt.setOptional(C,H,"bindMatrix"),xt.setOptional(C,H,"bindMatrixInverse");const Vt=H.skeleton;Vt&&(Vt.boneTexture===null&&Vt.computeBoneTexture(),xt.setValue(C,"boneTexture",Vt.boneTexture,L))}H.isBatchedMesh&&(xt.setOptional(C,H,"batchingTexture"),xt.setValue(C,"batchingTexture",H._matricesTexture,L),xt.setOptional(C,H,"batchingIdTexture"),xt.setValue(C,"batchingIdTexture",H._indirectTexture,L),xt.setOptional(C,H,"batchingColorTexture"),H._colorsTexture!==null&&xt.setValue(C,"batchingColorTexture",H._colorsTexture,L));const Ii=Y.morphAttributes;if((Ii.position!==void 0||Ii.normal!==void 0||Ii.color!==void 0)&&ge.update(H,Y,Wn),(rr||Le.receiveShadow!==H.receiveShadow)&&(Le.receiveShadow=H.receiveShadow,xt.setValue(C,"receiveShadow",H.receiveShadow)),(X.isMeshStandardMaterial||X.isMeshLambertMaterial||X.isMeshPhongMaterial)&&X.envMap===null&&N.environment!==null&&(Xt.envMapIntensity.value=N.environmentIntensity),Xt.dfgLUT!==void 0&&(Xt.dfgLUT.value=Nv()),rr&&(xt.setValue(C,"toneMappingExposure",S.toneMappingExposure),Le.needsLights&&bt(Xt,Cr),Me&&X.fog===!0&&we.refreshFogUniforms(Xt,Me),we.refreshMaterialUniforms(Xt,X,Ge,xe,T.state.transmissionRenderTarget[E.id]),ka.upload(C,Ue(Le),Xt,L)),X.isShaderMaterial&&X.uniformsNeedUpdate===!0&&(ka.upload(C,Ue(Le),Xt,L),X.uniformsNeedUpdate=!1),X.isSpriteMaterial&&xt.setValue(C,"center",H.center),xt.setValue(C,"modelViewMatrix",H.modelViewMatrix),xt.setValue(C,"normalMatrix",H.normalMatrix),xt.setValue(C,"modelMatrix",H.matrixWorld),X.isShaderMaterial||X.isRawShaderMaterial){const Vt=X.uniformsGroups;for(let Ui=0,Pr=Vt.length;Ui<Pr;Ui++){const Wc=Vt[Ui];Ae.update(Wc,Wn),Ae.bind(Wc,Wn)}}return Wn}function bt(E,N){E.ambientLightColor.needsUpdate=N,E.lightProbe.needsUpdate=N,E.directionalLights.needsUpdate=N,E.directionalLightShadows.needsUpdate=N,E.pointLights.needsUpdate=N,E.pointLightShadows.needsUpdate=N,E.spotLights.needsUpdate=N,E.spotLightShadows.needsUpdate=N,E.rectAreaLights.needsUpdate=N,E.hemisphereLights.needsUpdate=N}function Hn(E){return E.isMeshLambertMaterial||E.isMeshToonMaterial||E.isMeshPhongMaterial||E.isMeshStandardMaterial||E.isShadowMaterial||E.isShaderMaterial&&E.lights===!0}this.getActiveCubeFace=function(){return P},this.getActiveMipmapLevel=function(){return z},this.getRenderTarget=function(){return F},this.setRenderTargetTextures=function(E,N,Y){const X=v.get(E);X.__autoAllocateDepthBuffer=E.resolveDepthBuffer===!1,X.__autoAllocateDepthBuffer===!1&&(X.__useRenderToTexture=!1),v.get(E.texture).__webglTexture=N,v.get(E.depthTexture).__webglTexture=X.__autoAllocateDepthBuffer?void 0:Y,X.__hasExternalTextures=!0},this.setRenderTargetFramebuffer=function(E,N){const Y=v.get(E);Y.__webglFramebuffer=N,Y.__useDefaultFramebuffer=N===void 0};const Rr=C.createFramebuffer();this.setRenderTarget=function(E,N=0,Y=0){F=E,P=N,z=Y;let X=null,H=!1,Me=!1;if(E){const _e=v.get(E);if(_e.__useDefaultFramebuffer!==void 0){ae.bindFramebuffer(C.FRAMEBUFFER,_e.__webglFramebuffer),V.copy(E.viewport),G.copy(E.scissor),ne=E.scissorTest,ae.viewport(V),ae.scissor(G),ae.setScissorTest(ne),B=-1;return}else if(_e.__webglFramebuffer===void 0)L.setupRenderTarget(E);else if(_e.__hasExternalTextures)L.rebindTextures(E,v.get(E.texture).__webglTexture,v.get(E.depthTexture).__webglTexture);else if(E.depthBuffer){const We=E.depthTexture;if(_e.__boundDepthTexture!==We){if(We!==null&&v.has(We)&&(E.width!==We.image.width||E.height!==We.image.height))throw new Error("WebGLRenderTarget: Attached DepthTexture is initialized to the incorrect size.");L.setupDepthRenderbuffer(E)}}const De=E.texture;(De.isData3DTexture||De.isDataArrayTexture||De.isCompressedArrayTexture)&&(Me=!0);const J=v.get(E).__webglFramebuffer;E.isWebGLCubeRenderTarget?(Array.isArray(J[N])?X=J[N][Y]:X=J[N],H=!0):E.samples>0&&L.useMultisampledRTT(E)===!1?X=v.get(E).__webglMultisampledFramebuffer:Array.isArray(J)?X=J[Y]:X=J,V.copy(E.viewport),G.copy(E.scissor),ne=E.scissorTest}else V.copy(ee).multiplyScalar(Ge).floor(),G.copy(he).multiplyScalar(Ge).floor(),ne=pe;if(Y!==0&&(X=Rr),ae.bindFramebuffer(C.FRAMEBUFFER,X)&&ae.drawBuffers(E,X),ae.viewport(V),ae.scissor(G),ae.setScissorTest(ne),H){const _e=v.get(E.texture);C.framebufferTexture2D(C.FRAMEBUFFER,C.COLOR_ATTACHMENT0,C.TEXTURE_CUBE_MAP_POSITIVE_X+N,_e.__webglTexture,Y)}else if(Me){const _e=N;for(let De=0;De<E.textures.length;De++){const J=v.get(E.textures[De]);C.framebufferTextureLayer(C.FRAMEBUFFER,C.COLOR_ATTACHMENT0+De,J.__webglTexture,Y,_e)}}else if(E!==null&&Y!==0){const _e=v.get(E.texture);C.framebufferTexture2D(C.FRAMEBUFFER,C.COLOR_ATTACHMENT0,C.TEXTURE_2D,_e.__webglTexture,Y)}B=-1},this.readRenderTargetPixels=function(E,N,Y,X,H,Me,Se,_e=0){if(!(E&&E.isWebGLRenderTarget)){lt("WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");return}let De=v.get(E).__webglFramebuffer;if(E.isWebGLCubeRenderTarget&&Se!==void 0&&(De=De[Se]),De){ae.bindFramebuffer(C.FRAMEBUFFER,De);try{const J=E.textures[_e],We=J.format,Qe=J.type;if(E.textures.length>1&&C.readBuffer(C.COLOR_ATTACHMENT0+_e),!ue.textureFormatReadable(We)){lt("WebGLRenderer.readRenderTargetPixels: renderTarget is not in RGBA or implementation defined format.");return}if(!ue.textureTypeReadable(Qe)){lt("WebGLRenderer.readRenderTargetPixels: renderTarget is not in UnsignedByteType or implementation defined type.");return}N>=0&&N<=E.width-X&&Y>=0&&Y<=E.height-H&&C.readPixels(N,Y,X,H,de.convert(We),de.convert(Qe),Me)}finally{const J=F!==null?v.get(F).__webglFramebuffer:null;ae.bindFramebuffer(C.FRAMEBUFFER,J)}}},this.readRenderTargetPixelsAsync=async function(E,N,Y,X,H,Me,Se,_e=0){if(!(E&&E.isWebGLRenderTarget))throw new Error("THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not THREE.WebGLRenderTarget.");let De=v.get(E).__webglFramebuffer;if(E.isWebGLCubeRenderTarget&&Se!==void 0&&(De=De[Se]),De)if(N>=0&&N<=E.width-X&&Y>=0&&Y<=E.height-H){ae.bindFramebuffer(C.FRAMEBUFFER,De);const J=E.textures[_e],We=J.format,Qe=J.type;if(E.textures.length>1&&C.readBuffer(C.COLOR_ATTACHMENT0+_e),!ue.textureFormatReadable(We))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in RGBA or implementation defined format.");if(!ue.textureTypeReadable(Qe))throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: renderTarget is not in UnsignedByteType or implementation defined type.");const Ne=C.createBuffer();C.bindBuffer(C.PIXEL_PACK_BUFFER,Ne),C.bufferData(C.PIXEL_PACK_BUFFER,Me.byteLength,C.STREAM_READ),C.readPixels(N,Y,X,H,de.convert(We),de.convert(Qe),0);const _t=F!==null?v.get(F).__webglFramebuffer:null;ae.bindFramebuffer(C.FRAMEBUFFER,_t);const It=C.fenceSync(C.SYNC_GPU_COMMANDS_COMPLETE,0);return C.flush(),await Jd(C,It,4),C.bindBuffer(C.PIXEL_PACK_BUFFER,Ne),C.getBufferSubData(C.PIXEL_PACK_BUFFER,0,Me),C.deleteBuffer(Ne),C.deleteSync(It),Me}else throw new Error("THREE.WebGLRenderer.readRenderTargetPixelsAsync: requested read bounds are out of range.")},this.copyFramebufferToTexture=function(E,N=null,Y=0){const X=Math.pow(2,-Y),H=Math.floor(E.image.width*X),Me=Math.floor(E.image.height*X),Se=N!==null?N.x:0,_e=N!==null?N.y:0;L.setTexture2D(E,0),C.copyTexSubImage2D(C.TEXTURE_2D,Y,0,0,Se,_e,H,Me),ae.unbindTexture()};const Li=C.createFramebuffer(),Ct=C.createFramebuffer();this.copyTextureToTexture=function(E,N,Y=null,X=null,H=0,Me=0){let Se,_e,De,J,We,Qe,Ne,_t,It;const Pt=E.isCompressedTexture?E.mipmaps[Me]:E.image;if(Y!==null)Se=Y.max.x-Y.min.x,_e=Y.max.y-Y.min.y,De=Y.isBox3?Y.max.z-Y.min.z:1,J=Y.min.x,We=Y.min.y,Qe=Y.isBox3?Y.min.z:0;else{const Xt=Math.pow(2,-H);Se=Math.floor(Pt.width*Xt),_e=Math.floor(Pt.height*Xt),E.isDataArrayTexture?De=Pt.depth:E.isData3DTexture?De=Math.floor(Pt.depth*Xt):De=1,J=0,We=0,Qe=0}X!==null?(Ne=X.x,_t=X.y,It=X.z):(Ne=0,_t=0,It=0);const gt=de.convert(N.format),jt=de.convert(N.type);let Le;N.isData3DTexture?(L.setTexture3D(N,0),Le=C.TEXTURE_3D):N.isDataArrayTexture||N.isCompressedArrayTexture?(L.setTexture2DArray(N,0),Le=C.TEXTURE_2D_ARRAY):(L.setTexture2D(N,0),Le=C.TEXTURE_2D),C.pixelStorei(C.UNPACK_FLIP_Y_WEBGL,N.flipY),C.pixelStorei(C.UNPACK_PREMULTIPLY_ALPHA_WEBGL,N.premultiplyAlpha),C.pixelStorei(C.UNPACK_ALIGNMENT,N.unpackAlignment);const Sn=C.getParameter(C.UNPACK_ROW_LENGTH),ut=C.getParameter(C.UNPACK_IMAGE_HEIGHT),Wn=C.getParameter(C.UNPACK_SKIP_PIXELS),ei=C.getParameter(C.UNPACK_SKIP_ROWS),rr=C.getParameter(C.UNPACK_SKIP_IMAGES);C.pixelStorei(C.UNPACK_ROW_LENGTH,Pt.width),C.pixelStorei(C.UNPACK_IMAGE_HEIGHT,Pt.height),C.pixelStorei(C.UNPACK_SKIP_PIXELS,J),C.pixelStorei(C.UNPACK_SKIP_ROWS,We),C.pixelStorei(C.UNPACK_SKIP_IMAGES,Qe);const Cr=E.isDataArrayTexture||E.isData3DTexture,xt=N.isDataArrayTexture||N.isData3DTexture;if(E.isDepthTexture){const Xt=v.get(E),Ii=v.get(N),Vt=v.get(Xt.__renderTarget),Ui=v.get(Ii.__renderTarget);ae.bindFramebuffer(C.READ_FRAMEBUFFER,Vt.__webglFramebuffer),ae.bindFramebuffer(C.DRAW_FRAMEBUFFER,Ui.__webglFramebuffer);for(let Pr=0;Pr<De;Pr++)Cr&&(C.framebufferTextureLayer(C.READ_FRAMEBUFFER,C.COLOR_ATTACHMENT0,v.get(E).__webglTexture,H,Qe+Pr),C.framebufferTextureLayer(C.DRAW_FRAMEBUFFER,C.COLOR_ATTACHMENT0,v.get(N).__webglTexture,Me,It+Pr)),C.blitFramebuffer(J,We,Se,_e,Ne,_t,Se,_e,C.DEPTH_BUFFER_BIT,C.NEAREST);ae.bindFramebuffer(C.READ_FRAMEBUFFER,null),ae.bindFramebuffer(C.DRAW_FRAMEBUFFER,null)}else if(H!==0||E.isRenderTargetTexture||v.has(E)){const Xt=v.get(E),Ii=v.get(N);ae.bindFramebuffer(C.READ_FRAMEBUFFER,Li),ae.bindFramebuffer(C.DRAW_FRAMEBUFFER,Ct);for(let Vt=0;Vt<De;Vt++)Cr?C.framebufferTextureLayer(C.READ_FRAMEBUFFER,C.COLOR_ATTACHMENT0,Xt.__webglTexture,H,Qe+Vt):C.framebufferTexture2D(C.READ_FRAMEBUFFER,C.COLOR_ATTACHMENT0,C.TEXTURE_2D,Xt.__webglTexture,H),xt?C.framebufferTextureLayer(C.DRAW_FRAMEBUFFER,C.COLOR_ATTACHMENT0,Ii.__webglTexture,Me,It+Vt):C.framebufferTexture2D(C.DRAW_FRAMEBUFFER,C.COLOR_ATTACHMENT0,C.TEXTURE_2D,Ii.__webglTexture,Me),H!==0?C.blitFramebuffer(J,We,Se,_e,Ne,_t,Se,_e,C.COLOR_BUFFER_BIT,C.NEAREST):xt?C.copyTexSubImage3D(Le,Me,Ne,_t,It+Vt,J,We,Se,_e):C.copyTexSubImage2D(Le,Me,Ne,_t,J,We,Se,_e);ae.bindFramebuffer(C.READ_FRAMEBUFFER,null),ae.bindFramebuffer(C.DRAW_FRAMEBUFFER,null)}else xt?E.isDataTexture||E.isData3DTexture?C.texSubImage3D(Le,Me,Ne,_t,It,Se,_e,De,gt,jt,Pt.data):N.isCompressedArrayTexture?C.compressedTexSubImage3D(Le,Me,Ne,_t,It,Se,_e,De,gt,Pt.data):C.texSubImage3D(Le,Me,Ne,_t,It,Se,_e,De,gt,jt,Pt):E.isDataTexture?C.texSubImage2D(C.TEXTURE_2D,Me,Ne,_t,Se,_e,gt,jt,Pt.data):E.isCompressedTexture?C.compressedTexSubImage2D(C.TEXTURE_2D,Me,Ne,_t,Pt.width,Pt.height,gt,Pt.data):C.texSubImage2D(C.TEXTURE_2D,Me,Ne,_t,Se,_e,gt,jt,Pt);C.pixelStorei(C.UNPACK_ROW_LENGTH,Sn),C.pixelStorei(C.UNPACK_IMAGE_HEIGHT,ut),C.pixelStorei(C.UNPACK_SKIP_PIXELS,Wn),C.pixelStorei(C.UNPACK_SKIP_ROWS,ei),C.pixelStorei(C.UNPACK_SKIP_IMAGES,rr),Me===0&&N.generateMipmaps&&C.generateMipmap(Le),ae.unbindTexture()},this.initRenderTarget=function(E){v.get(E).__webglFramebuffer===void 0&&L.setupRenderTarget(E)},this.initTexture=function(E){E.isCubeTexture?L.setTextureCube(E,0):E.isData3DTexture?L.setTexture3D(E,0):E.isDataArrayTexture||E.isCompressedArrayTexture?L.setTexture2DArray(E,0):L.setTexture2D(E,0),ae.unbindTexture()},this.resetState=function(){P=0,z=0,F=null,ae.reset(),ce.reset()},typeof __THREE_DEVTOOLS__<"u"&&__THREE_DEVTOOLS__.dispatchEvent(new CustomEvent("observe",{detail:this}))}get coordinateSystem(){return oi}get outputColorSpace(){return this._outputColorSpace}set outputColorSpace(e){this._outputColorSpace=e;const t=this.getContext();t.drawingBufferColorSpace=ct._getDrawingBufferColorSpace(e),t.unpackColorSpace=ct._getUnpackColorSpace()}}const Qu={type:"change"},Tc={type:"start"},lf={type:"end"},Da=new Js,eh=new Gi,Ov=Math.cos(70*tp.DEG2RAD),Ot=new k,hn=2*Math.PI,vt={NONE:-1,ROTATE:0,DOLLY:1,PAN:2,TOUCH_ROTATE:3,TOUCH_PAN:4,TOUCH_DOLLY_PAN:5,TOUCH_DOLLY_ROTATE:6},Xo=1e-6;class Bv extends Gp{constructor(e,t=null){super(e,t),this.state=vt.NONE,this.target=new k,this.cursor=new k,this.minDistance=0,this.maxDistance=1/0,this.minZoom=0,this.maxZoom=1/0,this.minTargetRadius=0,this.maxTargetRadius=1/0,this.minPolarAngle=0,this.maxPolarAngle=Math.PI,this.minAzimuthAngle=-1/0,this.maxAzimuthAngle=1/0,this.enableDamping=!1,this.dampingFactor=.05,this.enableZoom=!0,this.zoomSpeed=1,this.enableRotate=!0,this.rotateSpeed=1,this.keyRotateSpeed=1,this.enablePan=!0,this.panSpeed=1,this.screenSpacePanning=!0,this.keyPanSpeed=7,this.zoomToCursor=!1,this.autoRotate=!1,this.autoRotateSpeed=2,this.keys={LEFT:"ArrowLeft",UP:"ArrowUp",RIGHT:"ArrowRight",BOTTOM:"ArrowDown"},this.mouseButtons={LEFT:Qr.ROTATE,MIDDLE:Qr.DOLLY,RIGHT:Qr.PAN},this.touches={ONE:Zr.ROTATE,TWO:Zr.DOLLY_PAN},this.target0=this.target.clone(),this.position0=this.object.position.clone(),this.zoom0=this.object.zoom,this._cursorStyle="auto",this._domElementKeyEvents=null,this._lastPosition=new k,this._lastQuaternion=new ji,this._lastTargetPosition=new k,this._quat=new ji().setFromUnitVectors(e.up,new k(0,1,0)),this._quatInverse=this._quat.clone().invert(),this._spherical=new Ru,this._sphericalDelta=new Ru,this._scale=1,this._panOffset=new k,this._rotateStart=new $e,this._rotateEnd=new $e,this._rotateDelta=new $e,this._panStart=new $e,this._panEnd=new $e,this._panDelta=new $e,this._dollyStart=new $e,this._dollyEnd=new $e,this._dollyDelta=new $e,this._dollyDirection=new k,this._mouse=new $e,this._performCursorZoom=!1,this._pointers=[],this._pointerPositions={},this._controlActive=!1,this._onPointerMove=zv.bind(this),this._onPointerDown=kv.bind(this),this._onPointerUp=Vv.bind(this),this._onContextMenu=$v.bind(this),this._onMouseWheel=Wv.bind(this),this._onKeyDown=Xv.bind(this),this._onTouchStart=qv.bind(this),this._onTouchMove=Yv.bind(this),this._onMouseDown=Gv.bind(this),this._onMouseMove=Hv.bind(this),this._interceptControlDown=Kv.bind(this),this._interceptControlUp=Zv.bind(this),this.domElement!==null&&this.connect(this.domElement),this.update()}set cursorStyle(e){this._cursorStyle=e,e==="grab"?this.domElement.style.cursor="grab":this.domElement.style.cursor="auto"}get cursorStyle(){return this._cursorStyle}connect(e){super.connect(e),this.domElement.addEventListener("pointerdown",this._onPointerDown),this.domElement.addEventListener("pointercancel",this._onPointerUp),this.domElement.addEventListener("contextmenu",this._onContextMenu),this.domElement.addEventListener("wheel",this._onMouseWheel,{passive:!1}),this.domElement.getRootNode().addEventListener("keydown",this._interceptControlDown,{passive:!0,capture:!0}),this.domElement.style.touchAction="none"}disconnect(){this.domElement.removeEventListener("pointerdown",this._onPointerDown),this.domElement.ownerDocument.removeEventListener("pointermove",this._onPointerMove),this.domElement.ownerDocument.removeEventListener("pointerup",this._onPointerUp),this.domElement.removeEventListener("pointercancel",this._onPointerUp),this.domElement.removeEventListener("wheel",this._onMouseWheel),this.domElement.removeEventListener("contextmenu",this._onContextMenu),this.stopListenToKeyEvents(),this.domElement.getRootNode().removeEventListener("keydown",this._interceptControlDown,{capture:!0}),this.domElement.style.touchAction="auto"}dispose(){this.disconnect()}getPolarAngle(){return this._spherical.phi}getAzimuthalAngle(){return this._spherical.theta}getDistance(){return this.object.position.distanceTo(this.target)}listenToKeyEvents(e){e.addEventListener("keydown",this._onKeyDown),this._domElementKeyEvents=e}stopListenToKeyEvents(){this._domElementKeyEvents!==null&&(this._domElementKeyEvents.removeEventListener("keydown",this._onKeyDown),this._domElementKeyEvents=null)}saveState(){this.target0.copy(this.target),this.position0.copy(this.object.position),this.zoom0=this.object.zoom}reset(){this.target.copy(this.target0),this.object.position.copy(this.position0),this.object.zoom=this.zoom0,this.object.updateProjectionMatrix(),this.dispatchEvent(Qu),this.update(),this.state=vt.NONE}pan(e,t){this._pan(e,t),this.update()}dollyIn(e){this._dollyIn(e),this.update()}dollyOut(e){this._dollyOut(e),this.update()}rotateLeft(e){this._rotateLeft(e),this.update()}rotateUp(e){this._rotateUp(e),this.update()}update(e=null){const t=this.object.position;Ot.copy(t).sub(this.target),Ot.applyQuaternion(this._quat),this._spherical.setFromVector3(Ot),this.autoRotate&&this.state===vt.NONE&&this._rotateLeft(this._getAutoRotationAngle(e)),this.enableDamping?(this._spherical.theta+=this._sphericalDelta.theta*this.dampingFactor,this._spherical.phi+=this._sphericalDelta.phi*this.dampingFactor):(this._spherical.theta+=this._sphericalDelta.theta,this._spherical.phi+=this._sphericalDelta.phi);let n=this.minAzimuthAngle,i=this.maxAzimuthAngle;isFinite(n)&&isFinite(i)&&(n<-Math.PI?n+=hn:n>Math.PI&&(n-=hn),i<-Math.PI?i+=hn:i>Math.PI&&(i-=hn),n<=i?this._spherical.theta=Math.max(n,Math.min(i,this._spherical.theta)):this._spherical.theta=this._spherical.theta>(n+i)/2?Math.max(n,this._spherical.theta):Math.min(i,this._spherical.theta)),this._spherical.phi=Math.max(this.minPolarAngle,Math.min(this.maxPolarAngle,this._spherical.phi)),this._spherical.makeSafe(),this.enableDamping===!0?this.target.addScaledVector(this._panOffset,this.dampingFactor):this.target.add(this._panOffset),this.target.sub(this.cursor),this.target.clampLength(this.minTargetRadius,this.maxTargetRadius),this.target.add(this.cursor);let s=!1;if(this.zoomToCursor&&this._performCursorZoom||this.object.isOrthographicCamera)this._spherical.radius=this._clampDistance(this._spherical.radius);else{const a=this._spherical.radius;this._spherical.radius=this._clampDistance(this._spherical.radius*this._scale),s=a!=this._spherical.radius}if(Ot.setFromSpherical(this._spherical),Ot.applyQuaternion(this._quatInverse),t.copy(this.target).add(Ot),this.object.lookAt(this.target),this.enableDamping===!0?(this._sphericalDelta.theta*=1-this.dampingFactor,this._sphericalDelta.phi*=1-this.dampingFactor,this._panOffset.multiplyScalar(1-this.dampingFactor)):(this._sphericalDelta.set(0,0,0),this._panOffset.set(0,0,0)),this.zoomToCursor&&this._performCursorZoom){let a=null;if(this.object.isPerspectiveCamera){const o=Ot.length();a=this._clampDistance(o*this._scale);const c=o-a;this.object.position.addScaledVector(this._dollyDirection,c),this.object.updateMatrixWorld(),s=!!c}else if(this.object.isOrthographicCamera){const o=new k(this._mouse.x,this._mouse.y,0);o.unproject(this.object);const c=this.object.zoom;this.object.zoom=Math.max(this.minZoom,Math.min(this.maxZoom,this.object.zoom/this._scale)),this.object.updateProjectionMatrix(),s=c!==this.object.zoom;const l=new k(this._mouse.x,this._mouse.y,0);l.unproject(this.object),this.object.position.sub(l).add(o),this.object.updateMatrixWorld(),a=Ot.length()}else console.warn("WARNING: OrbitControls.js encountered an unknown camera type - zoom to cursor disabled."),this.zoomToCursor=!1;a!==null&&(this.screenSpacePanning?this.target.set(0,0,-1).transformDirection(this.object.matrix).multiplyScalar(a).add(this.object.position):(Da.origin.copy(this.object.position),Da.direction.set(0,0,-1).transformDirection(this.object.matrix),Math.abs(this.object.up.dot(Da.direction))<Ov?this.object.lookAt(this.target):(eh.setFromNormalAndCoplanarPoint(this.object.up,this.target),Da.intersectPlane(eh,this.target))))}else if(this.object.isOrthographicCamera){const a=this.object.zoom;this.object.zoom=Math.max(this.minZoom,Math.min(this.maxZoom,this.object.zoom/this._scale)),a!==this.object.zoom&&(this.object.updateProjectionMatrix(),s=!0)}return this._scale=1,this._performCursorZoom=!1,s||this._lastPosition.distanceToSquared(this.object.position)>Xo||8*(1-this._lastQuaternion.dot(this.object.quaternion))>Xo||this._lastTargetPosition.distanceToSquared(this.target)>Xo?(this.dispatchEvent(Qu),this._lastPosition.copy(this.object.position),this._lastQuaternion.copy(this.object.quaternion),this._lastTargetPosition.copy(this.target),!0):!1}_getAutoRotationAngle(e){return e!==null?hn/60*this.autoRotateSpeed*e:hn/60/60*this.autoRotateSpeed}_getZoomScale(e){const t=Math.abs(e*.01);return Math.pow(.95,this.zoomSpeed*t)}_rotateLeft(e){this._sphericalDelta.theta-=e}_rotateUp(e){this._sphericalDelta.phi-=e}_panLeft(e,t){Ot.setFromMatrixColumn(t,0),Ot.multiplyScalar(-e),this._panOffset.add(Ot)}_panUp(e,t){this.screenSpacePanning===!0?Ot.setFromMatrixColumn(t,1):(Ot.setFromMatrixColumn(t,0),Ot.crossVectors(this.object.up,Ot)),Ot.multiplyScalar(e),this._panOffset.add(Ot)}_pan(e,t){const n=this.domElement;if(this.object.isPerspectiveCamera){const i=this.object.position;Ot.copy(i).sub(this.target);let s=Ot.length();s*=Math.tan(this.object.fov/2*Math.PI/180),this._panLeft(2*e*s/n.clientHeight,this.object.matrix),this._panUp(2*t*s/n.clientHeight,this.object.matrix)}else this.object.isOrthographicCamera?(this._panLeft(e*(this.object.right-this.object.left)/this.object.zoom/n.clientWidth,this.object.matrix),this._panUp(t*(this.object.top-this.object.bottom)/this.object.zoom/n.clientHeight,this.object.matrix)):(console.warn("WARNING: OrbitControls.js encountered an unknown camera type - pan disabled."),this.enablePan=!1)}_dollyOut(e){this.object.isPerspectiveCamera||this.object.isOrthographicCamera?this._scale/=e:(console.warn("WARNING: OrbitControls.js encountered an unknown camera type - dolly/zoom disabled."),this.enableZoom=!1)}_dollyIn(e){this.object.isPerspectiveCamera||this.object.isOrthographicCamera?this._scale*=e:(console.warn("WARNING: OrbitControls.js encountered an unknown camera type - dolly/zoom disabled."),this.enableZoom=!1)}_updateZoomParameters(e,t){if(!this.zoomToCursor)return;this._performCursorZoom=!0;const n=this.domElement.getBoundingClientRect(),i=e-n.left,s=t-n.top,a=n.width,o=n.height;this._mouse.x=i/a*2-1,this._mouse.y=-(s/o)*2+1,this._dollyDirection.set(this._mouse.x,this._mouse.y,1).unproject(this.object).sub(this.object.position).normalize()}_clampDistance(e){return Math.max(this.minDistance,Math.min(this.maxDistance,e))}_handleMouseDownRotate(e){this._rotateStart.set(e.clientX,e.clientY)}_handleMouseDownDolly(e){this._updateZoomParameters(e.clientX,e.clientX),this._dollyStart.set(e.clientX,e.clientY)}_handleMouseDownPan(e){this._panStart.set(e.clientX,e.clientY)}_handleMouseMoveRotate(e){this._rotateEnd.set(e.clientX,e.clientY),this._rotateDelta.subVectors(this._rotateEnd,this._rotateStart).multiplyScalar(this.rotateSpeed);const t=this.domElement;this._rotateLeft(hn*this._rotateDelta.x/t.clientHeight),this._rotateUp(hn*this._rotateDelta.y/t.clientHeight),this._rotateStart.copy(this._rotateEnd),this.update()}_handleMouseMoveDolly(e){this._dollyEnd.set(e.clientX,e.clientY),this._dollyDelta.subVectors(this._dollyEnd,this._dollyStart),this._dollyDelta.y>0?this._dollyOut(this._getZoomScale(this._dollyDelta.y)):this._dollyDelta.y<0&&this._dollyIn(this._getZoomScale(this._dollyDelta.y)),this._dollyStart.copy(this._dollyEnd),this.update()}_handleMouseMovePan(e){this._panEnd.set(e.clientX,e.clientY),this._panDelta.subVectors(this._panEnd,this._panStart).multiplyScalar(this.panSpeed),this._pan(this._panDelta.x,this._panDelta.y),this._panStart.copy(this._panEnd),this.update()}_handleMouseWheel(e){this._updateZoomParameters(e.clientX,e.clientY),e.deltaY<0?this._dollyIn(this._getZoomScale(e.deltaY)):e.deltaY>0&&this._dollyOut(this._getZoomScale(e.deltaY)),this.update()}_handleKeyDown(e){let t=!1;switch(e.code){case this.keys.UP:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateUp(hn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(0,this.keyPanSpeed),t=!0;break;case this.keys.BOTTOM:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateUp(-hn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(0,-this.keyPanSpeed),t=!0;break;case this.keys.LEFT:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateLeft(hn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(this.keyPanSpeed,0),t=!0;break;case this.keys.RIGHT:e.ctrlKey||e.metaKey||e.shiftKey?this.enableRotate&&this._rotateLeft(-hn*this.keyRotateSpeed/this.domElement.clientHeight):this.enablePan&&this._pan(-this.keyPanSpeed,0),t=!0;break}t&&(e.preventDefault(),this.update())}_handleTouchStartRotate(e){if(this._pointers.length===1)this._rotateStart.set(e.pageX,e.pageY);else{const t=this._getSecondPointerPosition(e),n=.5*(e.pageX+t.x),i=.5*(e.pageY+t.y);this._rotateStart.set(n,i)}}_handleTouchStartPan(e){if(this._pointers.length===1)this._panStart.set(e.pageX,e.pageY);else{const t=this._getSecondPointerPosition(e),n=.5*(e.pageX+t.x),i=.5*(e.pageY+t.y);this._panStart.set(n,i)}}_handleTouchStartDolly(e){const t=this._getSecondPointerPosition(e),n=e.pageX-t.x,i=e.pageY-t.y,s=Math.sqrt(n*n+i*i);this._dollyStart.set(0,s)}_handleTouchStartDollyPan(e){this.enableZoom&&this._handleTouchStartDolly(e),this.enablePan&&this._handleTouchStartPan(e)}_handleTouchStartDollyRotate(e){this.enableZoom&&this._handleTouchStartDolly(e),this.enableRotate&&this._handleTouchStartRotate(e)}_handleTouchMoveRotate(e){if(this._pointers.length==1)this._rotateEnd.set(e.pageX,e.pageY);else{const n=this._getSecondPointerPosition(e),i=.5*(e.pageX+n.x),s=.5*(e.pageY+n.y);this._rotateEnd.set(i,s)}this._rotateDelta.subVectors(this._rotateEnd,this._rotateStart).multiplyScalar(this.rotateSpeed);const t=this.domElement;this._rotateLeft(hn*this._rotateDelta.x/t.clientHeight),this._rotateUp(hn*this._rotateDelta.y/t.clientHeight),this._rotateStart.copy(this._rotateEnd)}_handleTouchMovePan(e){if(this._pointers.length===1)this._panEnd.set(e.pageX,e.pageY);else{const t=this._getSecondPointerPosition(e),n=.5*(e.pageX+t.x),i=.5*(e.pageY+t.y);this._panEnd.set(n,i)}this._panDelta.subVectors(this._panEnd,this._panStart).multiplyScalar(this.panSpeed),this._pan(this._panDelta.x,this._panDelta.y),this._panStart.copy(this._panEnd)}_handleTouchMoveDolly(e){const t=this._getSecondPointerPosition(e),n=e.pageX-t.x,i=e.pageY-t.y,s=Math.sqrt(n*n+i*i);this._dollyEnd.set(0,s),this._dollyDelta.set(0,Math.pow(this._dollyEnd.y/this._dollyStart.y,this.zoomSpeed)),this._dollyOut(this._dollyDelta.y),this._dollyStart.copy(this._dollyEnd);const a=(e.pageX+t.x)*.5,o=(e.pageY+t.y)*.5;this._updateZoomParameters(a,o)}_handleTouchMoveDollyPan(e){this.enableZoom&&this._handleTouchMoveDolly(e),this.enablePan&&this._handleTouchMovePan(e)}_handleTouchMoveDollyRotate(e){this.enableZoom&&this._handleTouchMoveDolly(e),this.enableRotate&&this._handleTouchMoveRotate(e)}_addPointer(e){this._pointers.push(e.pointerId)}_removePointer(e){delete this._pointerPositions[e.pointerId];for(let t=0;t<this._pointers.length;t++)if(this._pointers[t]==e.pointerId){this._pointers.splice(t,1);return}}_isTrackingPointer(e){for(let t=0;t<this._pointers.length;t++)if(this._pointers[t]==e.pointerId)return!0;return!1}_trackPointer(e){let t=this._pointerPositions[e.pointerId];t===void 0&&(t=new $e,this._pointerPositions[e.pointerId]=t),t.set(e.pageX,e.pageY)}_getSecondPointerPosition(e){const t=e.pointerId===this._pointers[0]?this._pointers[1]:this._pointers[0];return this._pointerPositions[t]}_customWheelEvent(e){const t=e.deltaMode,n={clientX:e.clientX,clientY:e.clientY,deltaY:e.deltaY};switch(t){case 1:n.deltaY*=16;break;case 2:n.deltaY*=100;break}return e.ctrlKey&&!this._controlActive&&(n.deltaY*=10),n}}function kv(r){this.enabled!==!1&&(this._pointers.length===0&&(this.domElement.setPointerCapture(r.pointerId),this.domElement.ownerDocument.addEventListener("pointermove",this._onPointerMove),this.domElement.ownerDocument.addEventListener("pointerup",this._onPointerUp)),!this._isTrackingPointer(r)&&(this._addPointer(r),r.pointerType==="touch"?this._onTouchStart(r):this._onMouseDown(r),this._cursorStyle==="grab"&&(this.domElement.style.cursor="grabbing")))}function zv(r){this.enabled!==!1&&(r.pointerType==="touch"?this._onTouchMove(r):this._onMouseMove(r))}function Vv(r){switch(this._removePointer(r),this._pointers.length){case 0:this.domElement.releasePointerCapture(r.pointerId),this.domElement.ownerDocument.removeEventListener("pointermove",this._onPointerMove),this.domElement.ownerDocument.removeEventListener("pointerup",this._onPointerUp),this.dispatchEvent(lf),this.state=vt.NONE,this._cursorStyle==="grab"&&(this.domElement.style.cursor="grab");break;case 1:const e=this._pointers[0],t=this._pointerPositions[e];this._onTouchStart({pointerId:e,pageX:t.x,pageY:t.y});break}}function Gv(r){let e;switch(r.button){case 0:e=this.mouseButtons.LEFT;break;case 1:e=this.mouseButtons.MIDDLE;break;case 2:e=this.mouseButtons.RIGHT;break;default:e=-1}switch(e){case Qr.DOLLY:if(this.enableZoom===!1)return;this._handleMouseDownDolly(r),this.state=vt.DOLLY;break;case Qr.ROTATE:if(r.ctrlKey||r.metaKey||r.shiftKey){if(this.enablePan===!1)return;this._handleMouseDownPan(r),this.state=vt.PAN}else{if(this.enableRotate===!1)return;this._handleMouseDownRotate(r),this.state=vt.ROTATE}break;case Qr.PAN:if(r.ctrlKey||r.metaKey||r.shiftKey){if(this.enableRotate===!1)return;this._handleMouseDownRotate(r),this.state=vt.ROTATE}else{if(this.enablePan===!1)return;this._handleMouseDownPan(r),this.state=vt.PAN}break;default:this.state=vt.NONE}this.state!==vt.NONE&&this.dispatchEvent(Tc)}function Hv(r){switch(this.state){case vt.ROTATE:if(this.enableRotate===!1)return;this._handleMouseMoveRotate(r);break;case vt.DOLLY:if(this.enableZoom===!1)return;this._handleMouseMoveDolly(r);break;case vt.PAN:if(this.enablePan===!1)return;this._handleMouseMovePan(r);break}}function Wv(r){this.enabled===!1||this.enableZoom===!1||this.state!==vt.NONE||(r.preventDefault(),this.dispatchEvent(Tc),this._handleMouseWheel(this._customWheelEvent(r)),this.dispatchEvent(lf))}function Xv(r){this.enabled!==!1&&this._handleKeyDown(r)}function qv(r){switch(this._trackPointer(r),this._pointers.length){case 1:switch(this.touches.ONE){case Zr.ROTATE:if(this.enableRotate===!1)return;this._handleTouchStartRotate(r),this.state=vt.TOUCH_ROTATE;break;case Zr.PAN:if(this.enablePan===!1)return;this._handleTouchStartPan(r),this.state=vt.TOUCH_PAN;break;default:this.state=vt.NONE}break;case 2:switch(this.touches.TWO){case Zr.DOLLY_PAN:if(this.enableZoom===!1&&this.enablePan===!1)return;this._handleTouchStartDollyPan(r),this.state=vt.TOUCH_DOLLY_PAN;break;case Zr.DOLLY_ROTATE:if(this.enableZoom===!1&&this.enableRotate===!1)return;this._handleTouchStartDollyRotate(r),this.state=vt.TOUCH_DOLLY_ROTATE;break;default:this.state=vt.NONE}break;default:this.state=vt.NONE}this.state!==vt.NONE&&this.dispatchEvent(Tc)}function Yv(r){switch(this._trackPointer(r),this.state){case vt.TOUCH_ROTATE:if(this.enableRotate===!1)return;this._handleTouchMoveRotate(r),this.update();break;case vt.TOUCH_PAN:if(this.enablePan===!1)return;this._handleTouchMovePan(r),this.update();break;case vt.TOUCH_DOLLY_PAN:if(this.enableZoom===!1&&this.enablePan===!1)return;this._handleTouchMoveDollyPan(r),this.update();break;case vt.TOUCH_DOLLY_ROTATE:if(this.enableZoom===!1&&this.enableRotate===!1)return;this._handleTouchMoveDollyRotate(r),this.update();break;default:this.state=vt.NONE}}function $v(r){this.enabled!==!1&&r.preventDefault()}function Kv(r){r.key==="Control"&&(this._controlActive=!0,this.domElement.getRootNode().addEventListener("keyup",this._interceptControlUp,{passive:!0,capture:!0}))}function Zv(r){r.key==="Control"&&(this._controlActive=!1,this.domElement.getRootNode().removeEventListener("keyup",this._interceptControlUp,{passive:!0,capture:!0}))}class Yr extends Bt{constructor(e=document.createElement("div")){super(),this.isCSS2DObject=!0,this.element=e,this.element.style.position="absolute",this.element.style.userSelect="none",this.element.setAttribute("draggable",!1),this.center=new $e(.5,.5),this.addEventListener("removed",function(){this.traverse(function(t){t.element&&t.element instanceof t.element.ownerDocument.defaultView.Element&&t.element.parentNode!==null&&t.element.remove()})})}copy(e,t){return super.copy(e,t),this.element=e.element.cloneNode(!0),this.center=e.center,this}}const $r=new k,th=new yt,nh=new yt,ih=new k,rh=new k;class jv{constructor(e={}){const t=this;let n,i,s,a;const o={objects:new WeakMap},c=e.element!==void 0?e.element:document.createElement("div");c.style.overflow="hidden",this.domElement=c,this.sortObjects=!0,this.getSize=function(){return{width:n,height:i}},this.render=function(_,g){_.matrixWorldAutoUpdate===!0&&_.updateMatrixWorld(),g.parent===null&&g.matrixWorldAutoUpdate===!0&&g.updateMatrixWorld(),th.copy(g.matrixWorldInverse),nh.multiplyMatrices(g.projectionMatrix,th),u(_,_,g),this.sortObjects&&m(_)},this.setSize=function(_,g){n=_,i=g,s=n/2,a=i/2,c.style.width=_+"px",c.style.height=g+"px"};function l(_){_.isCSS2DObject&&(_.element.style.display="none");for(let g=0,d=_.children.length;g<d;g++)l(_.children[g])}function u(_,g,d){if(_.visible===!1){l(_);return}if(_.isCSS2DObject){$r.setFromMatrixPosition(_.matrixWorld),$r.applyMatrix4(nh);const p=$r.z>=-1&&$r.z<=1&&_.layers.test(d.layers)===!0,M=_.element;M.style.display=p===!0?"":"none",p===!0&&(_.onBeforeRender(t,g,d),M.style.transform="translate("+-100*_.center.x+"%,"+-100*_.center.y+"%)translate("+($r.x*s+s)+"px,"+(-$r.y*a+a)+"px)",M.parentNode!==c&&c.appendChild(M),_.onAfterRender(t,g,d));const y={distanceToCameraSquared:f(d,_)};o.objects.set(_,y)}for(let p=0,M=_.children.length;p<M;p++)u(_.children[p],g,d)}function f(_,g){return ih.setFromMatrixPosition(_.matrixWorld),rh.setFromMatrixPosition(g.matrixWorld),ih.distanceToSquared(rh)}function h(_){const g=[];return _.traverseVisible(function(d){d.isCSS2DObject&&g.push(d)}),g}function m(_){const g=h(_).sort(function(p,M){if(p.renderOrder!==M.renderOrder)return M.renderOrder-p.renderOrder;const y=o.objects.get(p).distanceToCameraSquared,b=o.objects.get(M).distanceToCameraSquared;return y-b}),d=g.length;for(let p=0,M=g.length;p<M;p++)g[p].element.style.zIndex=d-p}}}function Si(r){if(r===void 0)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return r}function cf(r,e){r.prototype=Object.create(e.prototype),r.prototype.constructor=r,r.__proto__=e}/*!
 * GSAP 3.14.2
 * https://gsap.com
 *
 * @license Copyright 2008-2025, GreenSock. All rights reserved.
 * Subject to the terms at https://gsap.com/standard-license
 * @author: Jack Doyle, jack@greensock.com
*/var In={autoSleep:120,force3D:"auto",nullTargetWarn:1,units:{lineHeight:""}},us={duration:.5,overwrite:!1,delay:0},Ac,Zt,Tt,kn=1e8,St=1/kn,$l=Math.PI*2,Jv=$l/4,Qv=0,uf=Math.sqrt,ex=Math.cos,tx=Math.sin,Ht=function(e){return typeof e=="string"},Lt=function(e){return typeof e=="function"},Ci=function(e){return typeof e=="number"},wc=function(e){return typeof e>"u"},pi=function(e){return typeof e=="object"},mn=function(e){return e!==!1},Rc=function(){return typeof window<"u"},La=function(e){return Lt(e)||Ht(e)},hf=typeof ArrayBuffer=="function"&&ArrayBuffer.isView||function(){},rn=Array.isArray,nx=/random\([^)]+\)/g,ix=/,\s*/g,sh=/(?:-?\.?\d|\.)+/gi,ff=/[-+=.]*\d+[.e\-+]*\d*[e\-+]*\d*/g,jr=/[-+=.]*\d+[.e-]*\d*[a-z%]*/g,qo=/[-+=.]*\d+\.?\d*(?:e-|e\+)?\d*/gi,df=/[+-]=-?[.\d]+/,rx=/[^,'"\[\]\s]+/gi,sx=/^[+\-=e\s\d]*\d+[.\d]*([a-z]*|%)\s*$/i,wt,ii,Kl,Cc,Un={},Ka={},pf,mf=function(e){return(Ka=hs(e,Un))&&xn},Pc=function(e,t){return console.warn("Invalid property",e,"set to",t,"Missing plugin? gsap.registerPlugin()")},Hs=function(e,t){return!t&&console.warn(e)},_f=function(e,t){return e&&(Un[e]=t)&&Ka&&(Ka[e]=t)||Un},Ws=function(){return 0},ax={suppressEvents:!0,isStart:!0,kill:!1},za={suppressEvents:!0,kill:!1},ox={suppressEvents:!0},Dc={},$i=[],Zl={},gf,wn={},Yo={},ah=30,Va=[],Lc="",Ic=function(e){var t=e[0],n,i;if(pi(t)||Lt(t)||(e=[e]),!(n=(t._gsap||{}).harness)){for(i=Va.length;i--&&!Va[i].targetTest(t););n=Va[i]}for(i=e.length;i--;)e[i]&&(e[i]._gsap||(e[i]._gsap=new Vf(e[i],n)))||e.splice(i,1);return e},Mr=function(e){return e._gsap||Ic(zn(e))[0]._gsap},vf=function(e,t,n){return(n=e[t])&&Lt(n)?e[t]():wc(n)&&e.getAttribute&&e.getAttribute(t)||n},_n=function(e,t){return(e=e.split(",")).forEach(t)||e},Ut=function(e){return Math.round(e*1e5)/1e5||0},At=function(e){return Math.round(e*1e7)/1e7||0},ns=function(e,t){var n=t.charAt(0),i=parseFloat(t.substr(2));return e=parseFloat(e),n==="+"?e+i:n==="-"?e-i:n==="*"?e*i:e/i},lx=function(e,t){for(var n=t.length,i=0;e.indexOf(t[i])<0&&++i<n;);return i<n},Za=function(){var e=$i.length,t=$i.slice(0),n,i;for(Zl={},$i.length=0,n=0;n<e;n++)i=t[n],i&&i._lazy&&(i.render(i._lazy[0],i._lazy[1],!0)._lazy=0)},Uc=function(e){return!!(e._initted||e._startAt||e.add)},xf=function(e,t,n,i){$i.length&&!Zt&&Za(),e.render(t,n,!!(Zt&&t<0&&Uc(e))),$i.length&&!Zt&&Za()},Mf=function(e){var t=parseFloat(e);return(t||t===0)&&(e+"").match(rx).length<2?t:Ht(e)?e.trim():e},Sf=function(e){return e},Nn=function(e,t){for(var n in t)n in e||(e[n]=t[n]);return e},cx=function(e){return function(t,n){for(var i in n)i in t||i==="duration"&&e||i==="ease"||(t[i]=n[i])}},hs=function(e,t){for(var n in t)e[n]=t[n];return e},oh=function r(e,t){for(var n in t)n!=="__proto__"&&n!=="constructor"&&n!=="prototype"&&(e[n]=pi(t[n])?r(e[n]||(e[n]={}),t[n]):t[n]);return e},ja=function(e,t){var n={},i;for(i in e)i in t||(n[i]=e[i]);return n},Fs=function(e){var t=e.parent||wt,n=e.keyframes?cx(rn(e.keyframes)):Nn;if(mn(e.inherit))for(;t;)n(e,t.vars.defaults),t=t.parent||t._dp;return e},ux=function(e,t){for(var n=e.length,i=n===t.length;i&&n--&&e[n]===t[n];);return n<0},yf=function(e,t,n,i,s){var a=e[i],o;if(s)for(o=t[s];a&&a[s]>o;)a=a._prev;return a?(t._next=a._next,a._next=t):(t._next=e[n],e[n]=t),t._next?t._next._prev=t:e[i]=t,t._prev=a,t.parent=t._dp=e,t},so=function(e,t,n,i){n===void 0&&(n="_first"),i===void 0&&(i="_last");var s=t._prev,a=t._next;s?s._next=a:e[n]===t&&(e[n]=a),a?a._prev=s:e[i]===t&&(e[i]=s),t._next=t._prev=t.parent=null},Ji=function(e,t){e.parent&&(!t||e.parent.autoRemoveChildren)&&e.parent.remove&&e.parent.remove(e),e._act=0},Sr=function(e,t){if(e&&(!t||t._end>e._dur||t._start<0))for(var n=e;n;)n._dirty=1,n=n.parent;return e},hx=function(e){for(var t=e.parent;t&&t.parent;)t._dirty=1,t.totalDuration(),t=t.parent;return e},jl=function(e,t,n,i){return e._startAt&&(Zt?e._startAt.revert(za):e.vars.immediateRender&&!e.vars.autoRevert||e._startAt.render(t,!0,i))},fx=function r(e){return!e||e._ts&&r(e.parent)},lh=function(e){return e._repeat?fs(e._tTime,e=e.duration()+e._rDelay)*e:0},fs=function(e,t){var n=Math.floor(e=At(e/t));return e&&n===e?n-1:n},Ja=function(e,t){return(e-t._start)*t._ts+(t._ts>=0?0:t._dirty?t.totalDuration():t._tDur)},ao=function(e){return e._end=At(e._start+(e._tDur/Math.abs(e._ts||e._rts||St)||0))},oo=function(e,t){var n=e._dp;return n&&n.smoothChildTiming&&e._ts&&(e._start=At(n._time-(e._ts>0?t/e._ts:((e._dirty?e.totalDuration():e._tDur)-t)/-e._ts)),ao(e),n._dirty||Sr(n,e)),e},bf=function(e,t){var n;if((t._time||!t._dur&&t._initted||t._start<e._time&&(t._dur||!t.add))&&(n=Ja(e.rawTime(),t),(!t._dur||ea(0,t.totalDuration(),n)-t._tTime>St)&&t.render(n,!0)),Sr(e,t)._dp&&e._initted&&e._time>=e._dur&&e._ts){if(e._dur<e.duration())for(n=e;n._dp;)n.rawTime()>=0&&n.totalTime(n._tTime),n=n._dp;e._zTime=-St}},si=function(e,t,n,i){return t.parent&&Ji(t),t._start=At((Ci(n)?n:n||e!==wt?On(e,n,t):e._time)+t._delay),t._end=At(t._start+(t.totalDuration()/Math.abs(t.timeScale())||0)),yf(e,t,"_first","_last",e._sort?"_start":0),Jl(t)||(e._recent=t),i||bf(e,t),e._ts<0&&oo(e,e._tTime),e},Ef=function(e,t){return(Un.ScrollTrigger||Pc("scrollTrigger",t))&&Un.ScrollTrigger.create(t,e)},Tf=function(e,t,n,i,s){if(Fc(e,t,s),!e._initted)return 1;if(!n&&e._pt&&!Zt&&(e._dur&&e.vars.lazy!==!1||!e._dur&&e.vars.lazy)&&gf!==Pn.frame)return $i.push(e),e._lazy=[s,i],1},dx=function r(e){var t=e.parent;return t&&t._ts&&t._initted&&!t._lock&&(t.rawTime()<0||r(t))},Jl=function(e){var t=e.data;return t==="isFromStart"||t==="isStart"},px=function(e,t,n,i){var s=e.ratio,a=t<0||!t&&(!e._start&&dx(e)&&!(!e._initted&&Jl(e))||(e._ts<0||e._dp._ts<0)&&!Jl(e))?0:1,o=e._rDelay,c=0,l,u,f;if(o&&e._repeat&&(c=ea(0,e._tDur,t),u=fs(c,o),e._yoyo&&u&1&&(a=1-a),u!==fs(e._tTime,o)&&(s=1-a,e.vars.repeatRefresh&&e._initted&&e.invalidate())),a!==s||Zt||i||e._zTime===St||!t&&e._zTime){if(!e._initted&&Tf(e,t,i,n,c))return;for(f=e._zTime,e._zTime=t||(n?St:0),n||(n=t&&!f),e.ratio=a,e._from&&(a=1-a),e._time=0,e._tTime=c,l=e._pt;l;)l.r(a,l.d),l=l._next;t<0&&jl(e,t,n,!0),e._onUpdate&&!n&&Dn(e,"onUpdate"),c&&e._repeat&&!n&&e.parent&&Dn(e,"onRepeat"),(t>=e._tDur||t<0)&&e.ratio===a&&(a&&Ji(e,1),!n&&!Zt&&(Dn(e,a?"onComplete":"onReverseComplete",!0),e._prom&&e._prom()))}else e._zTime||(e._zTime=t)},mx=function(e,t,n){var i;if(n>t)for(i=e._first;i&&i._start<=n;){if(i.data==="isPause"&&i._start>t)return i;i=i._next}else for(i=e._last;i&&i._start>=n;){if(i.data==="isPause"&&i._start<t)return i;i=i._prev}},ds=function(e,t,n,i){var s=e._repeat,a=At(t)||0,o=e._tTime/e._tDur;return o&&!i&&(e._time*=a/e._dur),e._dur=a,e._tDur=s?s<0?1e10:At(a*(s+1)+e._rDelay*s):a,o>0&&!i&&oo(e,e._tTime=e._tDur*o),e.parent&&ao(e),n||Sr(e.parent,e),e},ch=function(e){return e instanceof on?Sr(e):ds(e,e._dur)},_x={_start:0,endTime:Ws,totalDuration:Ws},On=function r(e,t,n){var i=e.labels,s=e._recent||_x,a=e.duration()>=kn?s.endTime(!1):e._dur,o,c,l;return Ht(t)&&(isNaN(t)||t in i)?(c=t.charAt(0),l=t.substr(-1)==="%",o=t.indexOf("="),c==="<"||c===">"?(o>=0&&(t=t.replace(/=/,"")),(c==="<"?s._start:s.endTime(s._repeat>=0))+(parseFloat(t.substr(1))||0)*(l?(o<0?s:n).totalDuration()/100:1)):o<0?(t in i||(i[t]=a),i[t]):(c=parseFloat(t.charAt(o-1)+t.substr(o+1)),l&&n&&(c=c/100*(rn(n)?n[0]:n).totalDuration()),o>1?r(e,t.substr(0,o-1),n)+c:a+c)):t==null?a:+t},Os=function(e,t,n){var i=Ci(t[1]),s=(i?2:1)+(e<2?0:1),a=t[s],o,c;if(i&&(a.duration=t[1]),a.parent=n,e){for(o=a,c=n;c&&!("immediateRender"in o);)o=c.vars.defaults||{},c=mn(c.vars.inherit)&&c.parent;a.immediateRender=mn(o.immediateRender),e<2?a.runBackwards=1:a.startAt=t[s-1]}return new Ft(t[0],a,t[s+1])},tr=function(e,t){return e||e===0?t(e):t},ea=function(e,t,n){return n<e?e:n>t?t:n},tn=function(e,t){return!Ht(e)||!(t=sx.exec(e))?"":t[1]},gx=function(e,t,n){return tr(n,function(i){return ea(e,t,i)})},Ql=[].slice,Af=function(e,t){return e&&pi(e)&&"length"in e&&(!t&&!e.length||e.length-1 in e&&pi(e[0]))&&!e.nodeType&&e!==ii},vx=function(e,t,n){return n===void 0&&(n=[]),e.forEach(function(i){var s;return Ht(i)&&!t||Af(i,1)?(s=n).push.apply(s,zn(i)):n.push(i)})||n},zn=function(e,t,n){return Tt&&!t&&Tt.selector?Tt.selector(e):Ht(e)&&!n&&(Kl||!ps())?Ql.call((t||Cc).querySelectorAll(e),0):rn(e)?vx(e,n):Af(e)?Ql.call(e,0):e?[e]:[]},ec=function(e){return e=zn(e)[0]||Hs("Invalid scope")||{},function(t){var n=e.current||e.nativeElement||e;return zn(t,n.querySelectorAll?n:n===e?Hs("Invalid scope")||Cc.createElement("div"):e)}},wf=function(e){return e.sort(function(){return .5-Math.random()})},Rf=function(e){if(Lt(e))return e;var t=pi(e)?e:{each:e},n=yr(t.ease),i=t.from||0,s=parseFloat(t.base)||0,a={},o=i>0&&i<1,c=isNaN(i)||o,l=t.axis,u=i,f=i;return Ht(i)?u=f={center:.5,edges:.5,end:1}[i]||0:!o&&c&&(u=i[0],f=i[1]),function(h,m,_){var g=(_||t).length,d=a[g],p,M,y,b,T,A,R,x,S;if(!d){if(S=t.grid==="auto"?0:(t.grid||[1,kn])[1],!S){for(R=-kn;R<(R=_[S++].getBoundingClientRect().left)&&S<g;);S<g&&S--}for(d=a[g]=[],p=c?Math.min(S,g)*u-.5:i%S,M=S===kn?0:c?g*f/S-.5:i/S|0,R=0,x=kn,A=0;A<g;A++)y=A%S-p,b=M-(A/S|0),d[A]=T=l?Math.abs(l==="y"?b:y):uf(y*y+b*b),T>R&&(R=T),T<x&&(x=T);i==="random"&&wf(d),d.max=R-x,d.min=x,d.v=g=(parseFloat(t.amount)||parseFloat(t.each)*(S>g?g-1:l?l==="y"?g/S:S:Math.max(S,g/S))||0)*(i==="edges"?-1:1),d.b=g<0?s-g:s,d.u=tn(t.amount||t.each)||0,n=n&&g<0?Bf(n):n}return g=(d[h]-d.min)/d.max||0,At(d.b+(n?n(g):g)*d.v)+d.u}},tc=function(e){var t=Math.pow(10,((e+"").split(".")[1]||"").length);return function(n){var i=At(Math.round(parseFloat(n)/e)*e*t);return(i-i%1)/t+(Ci(n)?0:tn(n))}},Cf=function(e,t){var n=rn(e),i,s;return!n&&pi(e)&&(i=n=e.radius||kn,e.values?(e=zn(e.values),(s=!Ci(e[0]))&&(i*=i)):e=tc(e.increment)),tr(t,n?Lt(e)?function(a){return s=e(a),Math.abs(s-a)<=i?s:a}:function(a){for(var o=parseFloat(s?a.x:a),c=parseFloat(s?a.y:0),l=kn,u=0,f=e.length,h,m;f--;)s?(h=e[f].x-o,m=e[f].y-c,h=h*h+m*m):h=Math.abs(e[f]-o),h<l&&(l=h,u=f);return u=!i||l<=i?e[u]:a,s||u===a||Ci(a)?u:u+tn(a)}:tc(e))},Pf=function(e,t,n,i){return tr(rn(e)?!t:n===!0?!!(n=0):!i,function(){return rn(e)?e[~~(Math.random()*e.length)]:(n=n||1e-5)&&(i=n<1?Math.pow(10,(n+"").length-2):1)&&Math.floor(Math.round((e-n/2+Math.random()*(t-e+n*.99))/n)*n*i)/i})},xx=function(){for(var e=arguments.length,t=new Array(e),n=0;n<e;n++)t[n]=arguments[n];return function(i){return t.reduce(function(s,a){return a(s)},i)}},Mx=function(e,t){return function(n){return e(parseFloat(n))+(t||tn(n))}},Sx=function(e,t,n){return Lf(e,t,0,1,n)},Df=function(e,t,n){return tr(n,function(i){return e[~~t(i)]})},yx=function r(e,t,n){var i=t-e;return rn(e)?Df(e,r(0,e.length),t):tr(n,function(s){return(i+(s-e)%i)%i+e})},bx=function r(e,t,n){var i=t-e,s=i*2;return rn(e)?Df(e,r(0,e.length-1),t):tr(n,function(a){return a=(s+(a-e)%s)%s||0,e+(a>i?s-a:a)})},Xs=function(e){return e.replace(nx,function(t){var n=t.indexOf("[")+1,i=t.substring(n||7,n?t.indexOf("]"):t.length-1).split(ix);return Pf(n?i:+i[0],n?0:+i[1],+i[2]||1e-5)})},Lf=function(e,t,n,i,s){var a=t-e,o=i-n;return tr(s,function(c){return n+((c-e)/a*o||0)})},Ex=function r(e,t,n,i){var s=isNaN(e+t)?0:function(m){return(1-m)*e+m*t};if(!s){var a=Ht(e),o={},c,l,u,f,h;if(n===!0&&(i=1)&&(n=null),a)e={p:e},t={p:t};else if(rn(e)&&!rn(t)){for(u=[],f=e.length,h=f-2,l=1;l<f;l++)u.push(r(e[l-1],e[l]));f--,s=function(_){_*=f;var g=Math.min(h,~~_);return u[g](_-g)},n=t}else i||(e=hs(rn(e)?[]:{},e));if(!u){for(c in t)Nc.call(o,e,c,"get",t[c]);s=function(_){return kc(_,o)||(a?e.p:e)}}}return tr(n,s)},uh=function(e,t,n){var i=e.labels,s=kn,a,o,c;for(a in i)o=i[a]-t,o<0==!!n&&o&&s>(o=Math.abs(o))&&(c=a,s=o);return c},Dn=function(e,t,n){var i=e.vars,s=i[t],a=Tt,o=e._ctx,c,l,u;if(s)return c=i[t+"Params"],l=i.callbackScope||e,n&&$i.length&&Za(),o&&(Tt=o),u=c?s.apply(l,c):s.call(l),Tt=a,u},Ls=function(e){return Ji(e),e.scrollTrigger&&e.scrollTrigger.kill(!!Zt),e.progress()<1&&Dn(e,"onInterrupt"),e},Jr,If=[],Uf=function(e){if(e)if(e=!e.name&&e.default||e,Rc()||e.headless){var t=e.name,n=Lt(e),i=t&&!n&&e.init?function(){this._props=[]}:e,s={init:Ws,render:kc,add:Nc,kill:zx,modifier:kx,rawVars:0},a={targetTest:0,get:0,getSetter:Bc,aliases:{},register:0};if(ps(),e!==i){if(wn[t])return;Nn(i,Nn(ja(e,s),a)),hs(i.prototype,hs(s,ja(e,a))),wn[i.prop=t]=i,e.targetTest&&(Va.push(i),Dc[t]=1),t=(t==="css"?"CSS":t.charAt(0).toUpperCase()+t.substr(1))+"Plugin"}_f(t,i),e.register&&e.register(xn,i,gn)}else If.push(e)},Mt=255,Is={aqua:[0,Mt,Mt],lime:[0,Mt,0],silver:[192,192,192],black:[0,0,0],maroon:[128,0,0],teal:[0,128,128],blue:[0,0,Mt],navy:[0,0,128],white:[Mt,Mt,Mt],olive:[128,128,0],yellow:[Mt,Mt,0],orange:[Mt,165,0],gray:[128,128,128],purple:[128,0,128],green:[0,128,0],red:[Mt,0,0],pink:[Mt,192,203],cyan:[0,Mt,Mt],transparent:[Mt,Mt,Mt,0]},$o=function(e,t,n){return e+=e<0?1:e>1?-1:0,(e*6<1?t+(n-t)*e*6:e<.5?n:e*3<2?t+(n-t)*(2/3-e)*6:t)*Mt+.5|0},Nf=function(e,t,n){var i=e?Ci(e)?[e>>16,e>>8&Mt,e&Mt]:0:Is.black,s,a,o,c,l,u,f,h,m,_;if(!i){if(e.substr(-1)===","&&(e=e.substr(0,e.length-1)),Is[e])i=Is[e];else if(e.charAt(0)==="#"){if(e.length<6&&(s=e.charAt(1),a=e.charAt(2),o=e.charAt(3),e="#"+s+s+a+a+o+o+(e.length===5?e.charAt(4)+e.charAt(4):"")),e.length===9)return i=parseInt(e.substr(1,6),16),[i>>16,i>>8&Mt,i&Mt,parseInt(e.substr(7),16)/255];e=parseInt(e.substr(1),16),i=[e>>16,e>>8&Mt,e&Mt]}else if(e.substr(0,3)==="hsl"){if(i=_=e.match(sh),!t)c=+i[0]%360/360,l=+i[1]/100,u=+i[2]/100,a=u<=.5?u*(l+1):u+l-u*l,s=u*2-a,i.length>3&&(i[3]*=1),i[0]=$o(c+1/3,s,a),i[1]=$o(c,s,a),i[2]=$o(c-1/3,s,a);else if(~e.indexOf("="))return i=e.match(ff),n&&i.length<4&&(i[3]=1),i}else i=e.match(sh)||Is.transparent;i=i.map(Number)}return t&&!_&&(s=i[0]/Mt,a=i[1]/Mt,o=i[2]/Mt,f=Math.max(s,a,o),h=Math.min(s,a,o),u=(f+h)/2,f===h?c=l=0:(m=f-h,l=u>.5?m/(2-f-h):m/(f+h),c=f===s?(a-o)/m+(a<o?6:0):f===a?(o-s)/m+2:(s-a)/m+4,c*=60),i[0]=~~(c+.5),i[1]=~~(l*100+.5),i[2]=~~(u*100+.5)),n&&i.length<4&&(i[3]=1),i},Ff=function(e){var t=[],n=[],i=-1;return e.split(Ki).forEach(function(s){var a=s.match(jr)||[];t.push.apply(t,a),n.push(i+=a.length+1)}),t.c=n,t},hh=function(e,t,n){var i="",s=(e+i).match(Ki),a=t?"hsla(":"rgba(",o=0,c,l,u,f;if(!s)return e;if(s=s.map(function(h){return(h=Nf(h,t,1))&&a+(t?h[0]+","+h[1]+"%,"+h[2]+"%,"+h[3]:h.join(","))+")"}),n&&(u=Ff(e),c=n.c,c.join(i)!==u.c.join(i)))for(l=e.replace(Ki,"1").split(jr),f=l.length-1;o<f;o++)i+=l[o]+(~c.indexOf(o)?s.shift()||a+"0,0,0,0)":(u.length?u:s.length?s:n).shift());if(!l)for(l=e.split(Ki),f=l.length-1;o<f;o++)i+=l[o]+s[o];return i+l[f]},Ki=function(){var r="(?:\\b(?:(?:rgb|rgba|hsl|hsla)\\(.+?\\))|\\B#(?:[0-9a-f]{3,4}){1,2}\\b",e;for(e in Is)r+="|"+e+"\\b";return new RegExp(r+")","gi")}(),Tx=/hsl[a]?\(/,Of=function(e){var t=e.join(" "),n;if(Ki.lastIndex=0,Ki.test(t))return n=Tx.test(t),e[1]=hh(e[1],n),e[0]=hh(e[0],n,Ff(e[1])),!0},qs,Pn=function(){var r=Date.now,e=500,t=33,n=r(),i=n,s=1e3/240,a=s,o=[],c,l,u,f,h,m,_=function g(d){var p=r()-i,M=d===!0,y,b,T,A;if((p>e||p<0)&&(n+=p-t),i+=p,T=i-n,y=T-a,(y>0||M)&&(A=++f.frame,h=T-f.time*1e3,f.time=T=T/1e3,a+=y+(y>=s?4:s-y),b=1),M||(c=l(g)),b)for(m=0;m<o.length;m++)o[m](T,h,A,d)};return f={time:0,frame:0,tick:function(){_(!0)},deltaRatio:function(d){return h/(1e3/(d||60))},wake:function(){pf&&(!Kl&&Rc()&&(ii=Kl=window,Cc=ii.document||{},Un.gsap=xn,(ii.gsapVersions||(ii.gsapVersions=[])).push(xn.version),mf(Ka||ii.GreenSockGlobals||!ii.gsap&&ii||{}),If.forEach(Uf)),u=typeof requestAnimationFrame<"u"&&requestAnimationFrame,c&&f.sleep(),l=u||function(d){return setTimeout(d,a-f.time*1e3+1|0)},qs=1,_(2))},sleep:function(){(u?cancelAnimationFrame:clearTimeout)(c),qs=0,l=Ws},lagSmoothing:function(d,p){e=d||1/0,t=Math.min(p||33,e)},fps:function(d){s=1e3/(d||240),a=f.time*1e3+s},add:function(d,p,M){var y=p?function(b,T,A,R){d(b,T,A,R),f.remove(y)}:d;return f.remove(d),o[M?"unshift":"push"](y),ps(),y},remove:function(d,p){~(p=o.indexOf(d))&&o.splice(p,1)&&m>=p&&m--},_listeners:o},f}(),ps=function(){return!qs&&Pn.wake()},at={},Ax=/^[\d.\-M][\d.\-,\s]/,wx=/["']/g,Rx=function(e){for(var t={},n=e.substr(1,e.length-3).split(":"),i=n[0],s=1,a=n.length,o,c,l;s<a;s++)c=n[s],o=s!==a-1?c.lastIndexOf(","):c.length,l=c.substr(0,o),t[i]=isNaN(l)?l.replace(wx,"").trim():+l,i=c.substr(o+1).trim();return t},Cx=function(e){var t=e.indexOf("(")+1,n=e.indexOf(")"),i=e.indexOf("(",t);return e.substring(t,~i&&i<n?e.indexOf(")",n+1):n)},Px=function(e){var t=(e+"").split("("),n=at[t[0]];return n&&t.length>1&&n.config?n.config.apply(null,~e.indexOf("{")?[Rx(t[1])]:Cx(e).split(",").map(Mf)):at._CE&&Ax.test(e)?at._CE("",e):n},Bf=function(e){return function(t){return 1-e(1-t)}},kf=function r(e,t){for(var n=e._first,i;n;)n instanceof on?r(n,t):n.vars.yoyoEase&&(!n._yoyo||!n._repeat)&&n._yoyo!==t&&(n.timeline?r(n.timeline,t):(i=n._ease,n._ease=n._yEase,n._yEase=i,n._yoyo=t)),n=n._next},yr=function(e,t){return e&&(Lt(e)?e:at[e]||Px(e))||t},wr=function(e,t,n,i){n===void 0&&(n=function(c){return 1-t(1-c)}),i===void 0&&(i=function(c){return c<.5?t(c*2)/2:1-t((1-c)*2)/2});var s={easeIn:t,easeOut:n,easeInOut:i},a;return _n(e,function(o){at[o]=Un[o]=s,at[a=o.toLowerCase()]=n;for(var c in s)at[a+(c==="easeIn"?".in":c==="easeOut"?".out":".inOut")]=at[o+"."+c]=s[c]}),s},zf=function(e){return function(t){return t<.5?(1-e(1-t*2))/2:.5+e((t-.5)*2)/2}},Ko=function r(e,t,n){var i=t>=1?t:1,s=(n||(e?.3:.45))/(t<1?t:1),a=s/$l*(Math.asin(1/i)||0),o=function(u){return u===1?1:i*Math.pow(2,-10*u)*tx((u-a)*s)+1},c=e==="out"?o:e==="in"?function(l){return 1-o(1-l)}:zf(o);return s=$l/s,c.config=function(l,u){return r(e,l,u)},c},Zo=function r(e,t){t===void 0&&(t=1.70158);var n=function(a){return a?--a*a*((t+1)*a+t)+1:0},i=e==="out"?n:e==="in"?function(s){return 1-n(1-s)}:zf(n);return i.config=function(s){return r(e,s)},i};_n("Linear,Quad,Cubic,Quart,Quint,Strong",function(r,e){var t=e<5?e+1:e;wr(r+",Power"+(t-1),e?function(n){return Math.pow(n,t)}:function(n){return n},function(n){return 1-Math.pow(1-n,t)},function(n){return n<.5?Math.pow(n*2,t)/2:1-Math.pow((1-n)*2,t)/2})});at.Linear.easeNone=at.none=at.Linear.easeIn;wr("Elastic",Ko("in"),Ko("out"),Ko());(function(r,e){var t=1/e,n=2*t,i=2.5*t,s=function(o){return o<t?r*o*o:o<n?r*Math.pow(o-1.5/e,2)+.75:o<i?r*(o-=2.25/e)*o+.9375:r*Math.pow(o-2.625/e,2)+.984375};wr("Bounce",function(a){return 1-s(1-a)},s)})(7.5625,2.75);wr("Expo",function(r){return Math.pow(2,10*(r-1))*r+r*r*r*r*r*r*(1-r)});wr("Circ",function(r){return-(uf(1-r*r)-1)});wr("Sine",function(r){return r===1?1:-ex(r*Jv)+1});wr("Back",Zo("in"),Zo("out"),Zo());at.SteppedEase=at.steps=Un.SteppedEase={config:function(e,t){e===void 0&&(e=1);var n=1/e,i=e+(t?0:1),s=t?1:0,a=1-St;return function(o){return((i*ea(0,a,o)|0)+s)*n}}};us.ease=at["quad.out"];_n("onComplete,onUpdate,onStart,onRepeat,onReverseComplete,onInterrupt",function(r){return Lc+=r+","+r+"Params,"});var Vf=function(e,t){this.id=Qv++,e._gsap=this,this.target=e,this.harness=t,this.get=t?t.get:vf,this.set=t?t.getSetter:Bc},Ys=function(){function r(t){this.vars=t,this._delay=+t.delay||0,(this._repeat=t.repeat===1/0?-2:t.repeat||0)&&(this._rDelay=t.repeatDelay||0,this._yoyo=!!t.yoyo||!!t.yoyoEase),this._ts=1,ds(this,+t.duration,1,1),this.data=t.data,Tt&&(this._ctx=Tt,Tt.data.push(this)),qs||Pn.wake()}var e=r.prototype;return e.delay=function(n){return n||n===0?(this.parent&&this.parent.smoothChildTiming&&this.startTime(this._start+n-this._delay),this._delay=n,this):this._delay},e.duration=function(n){return arguments.length?this.totalDuration(this._repeat>0?n+(n+this._rDelay)*this._repeat:n):this.totalDuration()&&this._dur},e.totalDuration=function(n){return arguments.length?(this._dirty=0,ds(this,this._repeat<0?n:(n-this._repeat*this._rDelay)/(this._repeat+1))):this._tDur},e.totalTime=function(n,i){if(ps(),!arguments.length)return this._tTime;var s=this._dp;if(s&&s.smoothChildTiming&&this._ts){for(oo(this,n),!s._dp||s.parent||bf(s,this);s&&s.parent;)s.parent._time!==s._start+(s._ts>=0?s._tTime/s._ts:(s.totalDuration()-s._tTime)/-s._ts)&&s.totalTime(s._tTime,!0),s=s.parent;!this.parent&&this._dp.autoRemoveChildren&&(this._ts>0&&n<this._tDur||this._ts<0&&n>0||!this._tDur&&!n)&&si(this._dp,this,this._start-this._delay)}return(this._tTime!==n||!this._dur&&!i||this._initted&&Math.abs(this._zTime)===St||!this._initted&&this._dur&&n||!n&&!this._initted&&(this.add||this._ptLookup))&&(this._ts||(this._pTime=n),xf(this,n,i)),this},e.time=function(n,i){return arguments.length?this.totalTime(Math.min(this.totalDuration(),n+lh(this))%(this._dur+this._rDelay)||(n?this._dur:0),i):this._time},e.totalProgress=function(n,i){return arguments.length?this.totalTime(this.totalDuration()*n,i):this.totalDuration()?Math.min(1,this._tTime/this._tDur):this.rawTime()>=0&&this._initted?1:0},e.progress=function(n,i){return arguments.length?this.totalTime(this.duration()*(this._yoyo&&!(this.iteration()&1)?1-n:n)+lh(this),i):this.duration()?Math.min(1,this._time/this._dur):this.rawTime()>0?1:0},e.iteration=function(n,i){var s=this.duration()+this._rDelay;return arguments.length?this.totalTime(this._time+(n-1)*s,i):this._repeat?fs(this._tTime,s)+1:1},e.timeScale=function(n,i){if(!arguments.length)return this._rts===-St?0:this._rts;if(this._rts===n)return this;var s=this.parent&&this._ts?Ja(this.parent._time,this):this._tTime;return this._rts=+n||0,this._ts=this._ps||n===-St?0:this._rts,this.totalTime(ea(-Math.abs(this._delay),this.totalDuration(),s),i!==!1),ao(this),hx(this)},e.paused=function(n){return arguments.length?(this._ps!==n&&(this._ps=n,n?(this._pTime=this._tTime||Math.max(-this._delay,this.rawTime()),this._ts=this._act=0):(ps(),this._ts=this._rts,this.totalTime(this.parent&&!this.parent.smoothChildTiming?this.rawTime():this._tTime||this._pTime,this.progress()===1&&Math.abs(this._zTime)!==St&&(this._tTime-=St)))),this):this._ps},e.startTime=function(n){if(arguments.length){this._start=At(n);var i=this.parent||this._dp;return i&&(i._sort||!this.parent)&&si(i,this,this._start-this._delay),this}return this._start},e.endTime=function(n){return this._start+(mn(n)?this.totalDuration():this.duration())/Math.abs(this._ts||1)},e.rawTime=function(n){var i=this.parent||this._dp;return i?n&&(!this._ts||this._repeat&&this._time&&this.totalProgress()<1)?this._tTime%(this._dur+this._rDelay):this._ts?Ja(i.rawTime(n),this):this._tTime:this._tTime},e.revert=function(n){n===void 0&&(n=ox);var i=Zt;return Zt=n,Uc(this)&&(this.timeline&&this.timeline.revert(n),this.totalTime(-.01,n.suppressEvents)),this.data!=="nested"&&n.kill!==!1&&this.kill(),Zt=i,this},e.globalTime=function(n){for(var i=this,s=arguments.length?n:i.rawTime();i;)s=i._start+s/(Math.abs(i._ts)||1),i=i._dp;return!this.parent&&this._sat?this._sat.globalTime(n):s},e.repeat=function(n){return arguments.length?(this._repeat=n===1/0?-2:n,ch(this)):this._repeat===-2?1/0:this._repeat},e.repeatDelay=function(n){if(arguments.length){var i=this._time;return this._rDelay=n,ch(this),i?this.time(i):this}return this._rDelay},e.yoyo=function(n){return arguments.length?(this._yoyo=n,this):this._yoyo},e.seek=function(n,i){return this.totalTime(On(this,n),mn(i))},e.restart=function(n,i){return this.play().totalTime(n?-this._delay:0,mn(i)),this._dur||(this._zTime=-St),this},e.play=function(n,i){return n!=null&&this.seek(n,i),this.reversed(!1).paused(!1)},e.reverse=function(n,i){return n!=null&&this.seek(n||this.totalDuration(),i),this.reversed(!0).paused(!1)},e.pause=function(n,i){return n!=null&&this.seek(n,i),this.paused(!0)},e.resume=function(){return this.paused(!1)},e.reversed=function(n){return arguments.length?(!!n!==this.reversed()&&this.timeScale(-this._rts||(n?-St:0)),this):this._rts<0},e.invalidate=function(){return this._initted=this._act=0,this._zTime=-St,this},e.isActive=function(){var n=this.parent||this._dp,i=this._start,s;return!!(!n||this._ts&&this._initted&&n.isActive()&&(s=n.rawTime(!0))>=i&&s<this.endTime(!0)-St)},e.eventCallback=function(n,i,s){var a=this.vars;return arguments.length>1?(i?(a[n]=i,s&&(a[n+"Params"]=s),n==="onUpdate"&&(this._onUpdate=i)):delete a[n],this):a[n]},e.then=function(n){var i=this,s=i._prom;return new Promise(function(a){var o=Lt(n)?n:Sf,c=function(){var u=i.then;i.then=null,s&&s(),Lt(o)&&(o=o(i))&&(o.then||o===i)&&(i.then=u),a(o),i.then=u};i._initted&&i.totalProgress()===1&&i._ts>=0||!i._tTime&&i._ts<0?c():i._prom=c})},e.kill=function(){Ls(this)},r}();Nn(Ys.prototype,{_time:0,_start:0,_end:0,_tTime:0,_tDur:0,_dirty:0,_repeat:0,_yoyo:!1,parent:null,_initted:!1,_rDelay:0,_ts:1,_dp:0,ratio:0,_zTime:-St,_prom:0,_ps:!1,_rts:1});var on=function(r){cf(e,r);function e(n,i){var s;return n===void 0&&(n={}),s=r.call(this,n)||this,s.labels={},s.smoothChildTiming=!!n.smoothChildTiming,s.autoRemoveChildren=!!n.autoRemoveChildren,s._sort=mn(n.sortChildren),wt&&si(n.parent||wt,Si(s),i),n.reversed&&s.reverse(),n.paused&&s.paused(!0),n.scrollTrigger&&Ef(Si(s),n.scrollTrigger),s}var t=e.prototype;return t.to=function(i,s,a){return Os(0,arguments,this),this},t.from=function(i,s,a){return Os(1,arguments,this),this},t.fromTo=function(i,s,a,o){return Os(2,arguments,this),this},t.set=function(i,s,a){return s.duration=0,s.parent=this,Fs(s).repeatDelay||(s.repeat=0),s.immediateRender=!!s.immediateRender,new Ft(i,s,On(this,a),1),this},t.call=function(i,s,a){return si(this,Ft.delayedCall(0,i,s),a)},t.staggerTo=function(i,s,a,o,c,l,u){return a.duration=s,a.stagger=a.stagger||o,a.onComplete=l,a.onCompleteParams=u,a.parent=this,new Ft(i,a,On(this,c)),this},t.staggerFrom=function(i,s,a,o,c,l,u){return a.runBackwards=1,Fs(a).immediateRender=mn(a.immediateRender),this.staggerTo(i,s,a,o,c,l,u)},t.staggerFromTo=function(i,s,a,o,c,l,u,f){return o.startAt=a,Fs(o).immediateRender=mn(o.immediateRender),this.staggerTo(i,s,o,c,l,u,f)},t.render=function(i,s,a){var o=this._time,c=this._dirty?this.totalDuration():this._tDur,l=this._dur,u=i<=0?0:At(i),f=this._zTime<0!=i<0&&(this._initted||!l),h,m,_,g,d,p,M,y,b,T,A,R;if(this!==wt&&u>c&&i>=0&&(u=c),u!==this._tTime||a||f){if(o!==this._time&&l&&(u+=this._time-o,i+=this._time-o),h=u,b=this._start,y=this._ts,p=!y,f&&(l||(o=this._zTime),(i||!s)&&(this._zTime=i)),this._repeat){if(A=this._yoyo,d=l+this._rDelay,this._repeat<-1&&i<0)return this.totalTime(d*100+i,s,a);if(h=At(u%d),u===c?(g=this._repeat,h=l):(T=At(u/d),g=~~T,g&&g===T&&(h=l,g--),h>l&&(h=l)),T=fs(this._tTime,d),!o&&this._tTime&&T!==g&&this._tTime-T*d-this._dur<=0&&(T=g),A&&g&1&&(h=l-h,R=1),g!==T&&!this._lock){var x=A&&T&1,S=x===(A&&g&1);if(g<T&&(x=!x),o=x?0:u%l?l:u,this._lock=1,this.render(o||(R?0:At(g*d)),s,!l)._lock=0,this._tTime=u,!s&&this.parent&&Dn(this,"onRepeat"),this.vars.repeatRefresh&&!R&&(this.invalidate()._lock=1,T=g),o&&o!==this._time||p!==!this._ts||this.vars.onRepeat&&!this.parent&&!this._act)return this;if(l=this._dur,c=this._tDur,S&&(this._lock=2,o=x?l:-1e-4,this.render(o,!0),this.vars.repeatRefresh&&!R&&this.invalidate()),this._lock=0,!this._ts&&!p)return this;kf(this,R)}}if(this._hasPause&&!this._forcing&&this._lock<2&&(M=mx(this,At(o),At(h)),M&&(u-=h-(h=M._start))),this._tTime=u,this._time=h,this._act=!y,this._initted||(this._onUpdate=this.vars.onUpdate,this._initted=1,this._zTime=i,o=0),!o&&u&&l&&!s&&!T&&(Dn(this,"onStart"),this._tTime!==u))return this;if(h>=o&&i>=0)for(m=this._first;m;){if(_=m._next,(m._act||h>=m._start)&&m._ts&&M!==m){if(m.parent!==this)return this.render(i,s,a);if(m.render(m._ts>0?(h-m._start)*m._ts:(m._dirty?m.totalDuration():m._tDur)+(h-m._start)*m._ts,s,a),h!==this._time||!this._ts&&!p){M=0,_&&(u+=this._zTime=-St);break}}m=_}else{m=this._last;for(var W=i<0?i:h;m;){if(_=m._prev,(m._act||W<=m._end)&&m._ts&&M!==m){if(m.parent!==this)return this.render(i,s,a);if(m.render(m._ts>0?(W-m._start)*m._ts:(m._dirty?m.totalDuration():m._tDur)+(W-m._start)*m._ts,s,a||Zt&&Uc(m)),h!==this._time||!this._ts&&!p){M=0,_&&(u+=this._zTime=W?-St:St);break}}m=_}}if(M&&!s&&(this.pause(),M.render(h>=o?0:-St)._zTime=h>=o?1:-1,this._ts))return this._start=b,ao(this),this.render(i,s,a);this._onUpdate&&!s&&Dn(this,"onUpdate",!0),(u===c&&this._tTime>=this.totalDuration()||!u&&o)&&(b===this._start||Math.abs(y)!==Math.abs(this._ts))&&(this._lock||((i||!l)&&(u===c&&this._ts>0||!u&&this._ts<0)&&Ji(this,1),!s&&!(i<0&&!o)&&(u||o||!c)&&(Dn(this,u===c&&i>=0?"onComplete":"onReverseComplete",!0),this._prom&&!(u<c&&this.timeScale()>0)&&this._prom())))}return this},t.add=function(i,s){var a=this;if(Ci(s)||(s=On(this,s,i)),!(i instanceof Ys)){if(rn(i))return i.forEach(function(o){return a.add(o,s)}),this;if(Ht(i))return this.addLabel(i,s);if(Lt(i))i=Ft.delayedCall(0,i);else return this}return this!==i?si(this,i,s):this},t.getChildren=function(i,s,a,o){i===void 0&&(i=!0),s===void 0&&(s=!0),a===void 0&&(a=!0),o===void 0&&(o=-kn);for(var c=[],l=this._first;l;)l._start>=o&&(l instanceof Ft?s&&c.push(l):(a&&c.push(l),i&&c.push.apply(c,l.getChildren(!0,s,a)))),l=l._next;return c},t.getById=function(i){for(var s=this.getChildren(1,1,1),a=s.length;a--;)if(s[a].vars.id===i)return s[a]},t.remove=function(i){return Ht(i)?this.removeLabel(i):Lt(i)?this.killTweensOf(i):(i.parent===this&&so(this,i),i===this._recent&&(this._recent=this._last),Sr(this))},t.totalTime=function(i,s){return arguments.length?(this._forcing=1,!this._dp&&this._ts&&(this._start=At(Pn.time-(this._ts>0?i/this._ts:(this.totalDuration()-i)/-this._ts))),r.prototype.totalTime.call(this,i,s),this._forcing=0,this):this._tTime},t.addLabel=function(i,s){return this.labels[i]=On(this,s),this},t.removeLabel=function(i){return delete this.labels[i],this},t.addPause=function(i,s,a){var o=Ft.delayedCall(0,s||Ws,a);return o.data="isPause",this._hasPause=1,si(this,o,On(this,i))},t.removePause=function(i){var s=this._first;for(i=On(this,i);s;)s._start===i&&s.data==="isPause"&&Ji(s),s=s._next},t.killTweensOf=function(i,s,a){for(var o=this.getTweensOf(i,a),c=o.length;c--;)Xi!==o[c]&&o[c].kill(i,s);return this},t.getTweensOf=function(i,s){for(var a=[],o=zn(i),c=this._first,l=Ci(s),u;c;)c instanceof Ft?lx(c._targets,o)&&(l?(!Xi||c._initted&&c._ts)&&c.globalTime(0)<=s&&c.globalTime(c.totalDuration())>s:!s||c.isActive())&&a.push(c):(u=c.getTweensOf(o,s)).length&&a.push.apply(a,u),c=c._next;return a},t.tweenTo=function(i,s){s=s||{};var a=this,o=On(a,i),c=s,l=c.startAt,u=c.onStart,f=c.onStartParams,h=c.immediateRender,m,_=Ft.to(a,Nn({ease:s.ease||"none",lazy:!1,immediateRender:!1,time:o,overwrite:"auto",duration:s.duration||Math.abs((o-(l&&"time"in l?l.time:a._time))/a.timeScale())||St,onStart:function(){if(a.pause(),!m){var d=s.duration||Math.abs((o-(l&&"time"in l?l.time:a._time))/a.timeScale());_._dur!==d&&ds(_,d,0,1).render(_._time,!0,!0),m=1}u&&u.apply(_,f||[])}},s));return h?_.render(0):_},t.tweenFromTo=function(i,s,a){return this.tweenTo(s,Nn({startAt:{time:On(this,i)}},a))},t.recent=function(){return this._recent},t.nextLabel=function(i){return i===void 0&&(i=this._time),uh(this,On(this,i))},t.previousLabel=function(i){return i===void 0&&(i=this._time),uh(this,On(this,i),1)},t.currentLabel=function(i){return arguments.length?this.seek(i,!0):this.previousLabel(this._time+St)},t.shiftChildren=function(i,s,a){a===void 0&&(a=0);var o=this._first,c=this.labels,l;for(i=At(i);o;)o._start>=a&&(o._start+=i,o._end+=i),o=o._next;if(s)for(l in c)c[l]>=a&&(c[l]+=i);return Sr(this)},t.invalidate=function(i){var s=this._first;for(this._lock=0;s;)s.invalidate(i),s=s._next;return r.prototype.invalidate.call(this,i)},t.clear=function(i){i===void 0&&(i=!0);for(var s=this._first,a;s;)a=s._next,this.remove(s),s=a;return this._dp&&(this._time=this._tTime=this._pTime=0),i&&(this.labels={}),Sr(this)},t.totalDuration=function(i){var s=0,a=this,o=a._last,c=kn,l,u,f;if(arguments.length)return a.timeScale((a._repeat<0?a.duration():a.totalDuration())/(a.reversed()?-i:i));if(a._dirty){for(f=a.parent;o;)l=o._prev,o._dirty&&o.totalDuration(),u=o._start,u>c&&a._sort&&o._ts&&!a._lock?(a._lock=1,si(a,o,u-o._delay,1)._lock=0):c=u,u<0&&o._ts&&(s-=u,(!f&&!a._dp||f&&f.smoothChildTiming)&&(a._start+=At(u/a._ts),a._time-=u,a._tTime-=u),a.shiftChildren(-u,!1,-1/0),c=0),o._end>s&&o._ts&&(s=o._end),o=l;ds(a,a===wt&&a._time>s?a._time:s,1,1),a._dirty=0}return a._tDur},e.updateRoot=function(i){if(wt._ts&&(xf(wt,Ja(i,wt)),gf=Pn.frame),Pn.frame>=ah){ah+=In.autoSleep||120;var s=wt._first;if((!s||!s._ts)&&In.autoSleep&&Pn._listeners.length<2){for(;s&&!s._ts;)s=s._next;s||Pn.sleep()}}},e}(Ys);Nn(on.prototype,{_lock:0,_hasPause:0,_forcing:0});var Dx=function(e,t,n,i,s,a,o){var c=new gn(this._pt,e,t,0,1,Yf,null,s),l=0,u=0,f,h,m,_,g,d,p,M;for(c.b=n,c.e=i,n+="",i+="",(p=~i.indexOf("random("))&&(i=Xs(i)),a&&(M=[n,i],a(M,e,t),n=M[0],i=M[1]),h=n.match(qo)||[];f=qo.exec(i);)_=f[0],g=i.substring(l,f.index),m?m=(m+1)%5:g.substr(-5)==="rgba("&&(m=1),_!==h[u++]&&(d=parseFloat(h[u-1])||0,c._pt={_next:c._pt,p:g||u===1?g:",",s:d,c:_.charAt(1)==="="?ns(d,_)-d:parseFloat(_)-d,m:m&&m<4?Math.round:0},l=qo.lastIndex);return c.c=l<i.length?i.substring(l,i.length):"",c.fp=o,(df.test(i)||p)&&(c.e=0),this._pt=c,c},Nc=function(e,t,n,i,s,a,o,c,l,u){Lt(i)&&(i=i(s||0,e,a));var f=e[t],h=n!=="get"?n:Lt(f)?l?e[t.indexOf("set")||!Lt(e["get"+t.substr(3)])?t:"get"+t.substr(3)](l):e[t]():f,m=Lt(f)?l?Fx:Xf:Oc,_;if(Ht(i)&&(~i.indexOf("random(")&&(i=Xs(i)),i.charAt(1)==="="&&(_=ns(h,i)+(tn(h)||0),(_||_===0)&&(i=_))),!u||h!==i||nc)return!isNaN(h*i)&&i!==""?(_=new gn(this._pt,e,t,+h||0,i-(h||0),typeof f=="boolean"?Bx:qf,0,m),l&&(_.fp=l),o&&_.modifier(o,this,e),this._pt=_):(!f&&!(t in e)&&Pc(t,i),Dx.call(this,e,t,h,i,m,c||In.stringFilter,l))},Lx=function(e,t,n,i,s){if(Lt(e)&&(e=Bs(e,s,t,n,i)),!pi(e)||e.style&&e.nodeType||rn(e)||hf(e))return Ht(e)?Bs(e,s,t,n,i):e;var a={},o;for(o in e)a[o]=Bs(e[o],s,t,n,i);return a},Gf=function(e,t,n,i,s,a){var o,c,l,u;if(wn[e]&&(o=new wn[e]).init(s,o.rawVars?t[e]:Lx(t[e],i,s,a,n),n,i,a)!==!1&&(n._pt=c=new gn(n._pt,s,e,0,1,o.render,o,0,o.priority),n!==Jr))for(l=n._ptLookup[n._targets.indexOf(s)],u=o._props.length;u--;)l[o._props[u]]=c;return o},Xi,nc,Fc=function r(e,t,n){var i=e.vars,s=i.ease,a=i.startAt,o=i.immediateRender,c=i.lazy,l=i.onUpdate,u=i.runBackwards,f=i.yoyoEase,h=i.keyframes,m=i.autoRevert,_=e._dur,g=e._startAt,d=e._targets,p=e.parent,M=p&&p.data==="nested"?p.vars.targets:d,y=e._overwrite==="auto"&&!Ac,b=e.timeline,T,A,R,x,S,W,P,z,F,B,O,V,G;if(b&&(!h||!s)&&(s="none"),e._ease=yr(s,us.ease),e._yEase=f?Bf(yr(f===!0?s:f,us.ease)):0,f&&e._yoyo&&!e._repeat&&(f=e._yEase,e._yEase=e._ease,e._ease=f),e._from=!b&&!!i.runBackwards,!b||h&&!i.stagger){if(z=d[0]?Mr(d[0]).harness:0,V=z&&i[z.prop],T=ja(i,Dc),g&&(g._zTime<0&&g.progress(1),t<0&&u&&o&&!m?g.render(-1,!0):g.revert(u&&_?za:ax),g._lazy=0),a){if(Ji(e._startAt=Ft.set(d,Nn({data:"isStart",overwrite:!1,parent:p,immediateRender:!0,lazy:!g&&mn(c),startAt:null,delay:0,onUpdate:l&&function(){return Dn(e,"onUpdate")},stagger:0},a))),e._startAt._dp=0,e._startAt._sat=e,t<0&&(Zt||!o&&!m)&&e._startAt.revert(za),o&&_&&t<=0&&n<=0){t&&(e._zTime=t);return}}else if(u&&_&&!g){if(t&&(o=!1),R=Nn({overwrite:!1,data:"isFromStart",lazy:o&&!g&&mn(c),immediateRender:o,stagger:0,parent:p},T),V&&(R[z.prop]=V),Ji(e._startAt=Ft.set(d,R)),e._startAt._dp=0,e._startAt._sat=e,t<0&&(Zt?e._startAt.revert(za):e._startAt.render(-1,!0)),e._zTime=t,!o)r(e._startAt,St,St);else if(!t)return}for(e._pt=e._ptCache=0,c=_&&mn(c)||c&&!_,A=0;A<d.length;A++){if(S=d[A],P=S._gsap||Ic(d)[A]._gsap,e._ptLookup[A]=B={},Zl[P.id]&&$i.length&&Za(),O=M===d?A:M.indexOf(S),z&&(F=new z).init(S,V||T,e,O,M)!==!1&&(e._pt=x=new gn(e._pt,S,F.name,0,1,F.render,F,0,F.priority),F._props.forEach(function(ne){B[ne]=x}),F.priority&&(W=1)),!z||V)for(R in T)wn[R]&&(F=Gf(R,T,e,O,S,M))?F.priority&&(W=1):B[R]=x=Nc.call(e,S,R,"get",T[R],O,M,0,i.stringFilter);e._op&&e._op[A]&&e.kill(S,e._op[A]),y&&e._pt&&(Xi=e,wt.killTweensOf(S,B,e.globalTime(t)),G=!e.parent,Xi=0),e._pt&&c&&(Zl[P.id]=1)}W&&$f(e),e._onInit&&e._onInit(e)}e._onUpdate=l,e._initted=(!e._op||e._pt)&&!G,h&&t<=0&&b.render(kn,!0,!0)},Ix=function(e,t,n,i,s,a,o,c){var l=(e._pt&&e._ptCache||(e._ptCache={}))[t],u,f,h,m;if(!l)for(l=e._ptCache[t]=[],h=e._ptLookup,m=e._targets.length;m--;){if(u=h[m][t],u&&u.d&&u.d._pt)for(u=u.d._pt;u&&u.p!==t&&u.fp!==t;)u=u._next;if(!u)return nc=1,e.vars[t]="+=0",Fc(e,o),nc=0,c?Hs(t+" not eligible for reset"):1;l.push(u)}for(m=l.length;m--;)f=l[m],u=f._pt||f,u.s=(i||i===0)&&!s?i:u.s+(i||0)+a*u.c,u.c=n-u.s,f.e&&(f.e=Ut(n)+tn(f.e)),f.b&&(f.b=u.s+tn(f.b))},Ux=function(e,t){var n=e[0]?Mr(e[0]).harness:0,i=n&&n.aliases,s,a,o,c;if(!i)return t;s=hs({},t);for(a in i)if(a in s)for(c=i[a].split(","),o=c.length;o--;)s[c[o]]=s[a];return s},Nx=function(e,t,n,i){var s=t.ease||i||"power1.inOut",a,o;if(rn(t))o=n[e]||(n[e]=[]),t.forEach(function(c,l){return o.push({t:l/(t.length-1)*100,v:c,e:s})});else for(a in t)o=n[a]||(n[a]=[]),a==="ease"||o.push({t:parseFloat(e),v:t[a],e:s})},Bs=function(e,t,n,i,s){return Lt(e)?e.call(t,n,i,s):Ht(e)&&~e.indexOf("random(")?Xs(e):e},Hf=Lc+"repeat,repeatDelay,yoyo,repeatRefresh,yoyoEase,autoRevert",Wf={};_n(Hf+",id,stagger,delay,duration,paused,scrollTrigger",function(r){return Wf[r]=1});var Ft=function(r){cf(e,r);function e(n,i,s,a){var o;typeof i=="number"&&(s.duration=i,i=s,s=null),o=r.call(this,a?i:Fs(i))||this;var c=o.vars,l=c.duration,u=c.delay,f=c.immediateRender,h=c.stagger,m=c.overwrite,_=c.keyframes,g=c.defaults,d=c.scrollTrigger,p=c.yoyoEase,M=i.parent||wt,y=(rn(n)||hf(n)?Ci(n[0]):"length"in i)?[n]:zn(n),b,T,A,R,x,S,W,P;if(o._targets=y.length?Ic(y):Hs("GSAP target "+n+" not found. https://gsap.com",!In.nullTargetWarn)||[],o._ptLookup=[],o._overwrite=m,_||h||La(l)||La(u)){if(i=o.vars,b=o.timeline=new on({data:"nested",defaults:g||{},targets:M&&M.data==="nested"?M.vars.targets:y}),b.kill(),b.parent=b._dp=Si(o),b._start=0,h||La(l)||La(u)){if(R=y.length,W=h&&Rf(h),pi(h))for(x in h)~Hf.indexOf(x)&&(P||(P={}),P[x]=h[x]);for(T=0;T<R;T++)A=ja(i,Wf),A.stagger=0,p&&(A.yoyoEase=p),P&&hs(A,P),S=y[T],A.duration=+Bs(l,Si(o),T,S,y),A.delay=(+Bs(u,Si(o),T,S,y)||0)-o._delay,!h&&R===1&&A.delay&&(o._delay=u=A.delay,o._start+=u,A.delay=0),b.to(S,A,W?W(T,S,y):0),b._ease=at.none;b.duration()?l=u=0:o.timeline=0}else if(_){Fs(Nn(b.vars.defaults,{ease:"none"})),b._ease=yr(_.ease||i.ease||"none");var z=0,F,B,O;if(rn(_))_.forEach(function(V){return b.to(y,V,">")}),b.duration();else{A={};for(x in _)x==="ease"||x==="easeEach"||Nx(x,_[x],A,_.easeEach);for(x in A)for(F=A[x].sort(function(V,G){return V.t-G.t}),z=0,T=0;T<F.length;T++)B=F[T],O={ease:B.e,duration:(B.t-(T?F[T-1].t:0))/100*l},O[x]=B.v,b.to(y,O,z),z+=O.duration;b.duration()<l&&b.to({},{duration:l-b.duration()})}}l||o.duration(l=b.duration())}else o.timeline=0;return m===!0&&!Ac&&(Xi=Si(o),wt.killTweensOf(y),Xi=0),si(M,Si(o),s),i.reversed&&o.reverse(),i.paused&&o.paused(!0),(f||!l&&!_&&o._start===At(M._time)&&mn(f)&&fx(Si(o))&&M.data!=="nested")&&(o._tTime=-St,o.render(Math.max(0,-u)||0)),d&&Ef(Si(o),d),o}var t=e.prototype;return t.render=function(i,s,a){var o=this._time,c=this._tDur,l=this._dur,u=i<0,f=i>c-St&&!u?c:i<St?0:i,h,m,_,g,d,p,M,y,b;if(!l)px(this,i,s,a);else if(f!==this._tTime||!i||a||!this._initted&&this._tTime||this._startAt&&this._zTime<0!==u||this._lazy){if(h=f,y=this.timeline,this._repeat){if(g=l+this._rDelay,this._repeat<-1&&u)return this.totalTime(g*100+i,s,a);if(h=At(f%g),f===c?(_=this._repeat,h=l):(d=At(f/g),_=~~d,_&&_===d?(h=l,_--):h>l&&(h=l)),p=this._yoyo&&_&1,p&&(b=this._yEase,h=l-h),d=fs(this._tTime,g),h===o&&!a&&this._initted&&_===d)return this._tTime=f,this;_!==d&&(y&&this._yEase&&kf(y,p),this.vars.repeatRefresh&&!p&&!this._lock&&h!==g&&this._initted&&(this._lock=a=1,this.render(At(g*_),!0).invalidate()._lock=0))}if(!this._initted){if(Tf(this,u?i:h,a,s,f))return this._tTime=0,this;if(o!==this._time&&!(a&&this.vars.repeatRefresh&&_!==d))return this;if(l!==this._dur)return this.render(i,s,a)}if(this._tTime=f,this._time=h,!this._act&&this._ts&&(this._act=1,this._lazy=0),this.ratio=M=(b||this._ease)(h/l),this._from&&(this.ratio=M=1-M),!o&&f&&!s&&!d&&(Dn(this,"onStart"),this._tTime!==f))return this;for(m=this._pt;m;)m.r(M,m.d),m=m._next;y&&y.render(i<0?i:y._dur*y._ease(h/this._dur),s,a)||this._startAt&&(this._zTime=i),this._onUpdate&&!s&&(u&&jl(this,i,s,a),Dn(this,"onUpdate")),this._repeat&&_!==d&&this.vars.onRepeat&&!s&&this.parent&&Dn(this,"onRepeat"),(f===this._tDur||!f)&&this._tTime===f&&(u&&!this._onUpdate&&jl(this,i,!0,!0),(i||!l)&&(f===this._tDur&&this._ts>0||!f&&this._ts<0)&&Ji(this,1),!s&&!(u&&!o)&&(f||o||p)&&(Dn(this,f===c?"onComplete":"onReverseComplete",!0),this._prom&&!(f<c&&this.timeScale()>0)&&this._prom()))}return this},t.targets=function(){return this._targets},t.invalidate=function(i){return(!i||!this.vars.runBackwards)&&(this._startAt=0),this._pt=this._op=this._onUpdate=this._lazy=this.ratio=0,this._ptLookup=[],this.timeline&&this.timeline.invalidate(i),r.prototype.invalidate.call(this,i)},t.resetTo=function(i,s,a,o,c){qs||Pn.wake(),this._ts||this.play();var l=Math.min(this._dur,(this._dp._time-this._start)*this._ts),u;return this._initted||Fc(this,l),u=this._ease(l/this._dur),Ix(this,i,s,a,o,u,l,c)?this.resetTo(i,s,a,o,1):(oo(this,0),this.parent||yf(this._dp,this,"_first","_last",this._dp._sort?"_start":0),this.render(0))},t.kill=function(i,s){if(s===void 0&&(s="all"),!i&&(!s||s==="all"))return this._lazy=this._pt=0,this.parent?Ls(this):this.scrollTrigger&&this.scrollTrigger.kill(!!Zt),this;if(this.timeline){var a=this.timeline.totalDuration();return this.timeline.killTweensOf(i,s,Xi&&Xi.vars.overwrite!==!0)._first||Ls(this),this.parent&&a!==this.timeline.totalDuration()&&ds(this,this._dur*this.timeline._tDur/a,0,1),this}var o=this._targets,c=i?zn(i):o,l=this._ptLookup,u=this._pt,f,h,m,_,g,d,p;if((!s||s==="all")&&ux(o,c))return s==="all"&&(this._pt=0),Ls(this);for(f=this._op=this._op||[],s!=="all"&&(Ht(s)&&(g={},_n(s,function(M){return g[M]=1}),s=g),s=Ux(o,s)),p=o.length;p--;)if(~c.indexOf(o[p])){h=l[p],s==="all"?(f[p]=s,_=h,m={}):(m=f[p]=f[p]||{},_=s);for(g in _)d=h&&h[g],d&&((!("kill"in d.d)||d.d.kill(g)===!0)&&so(this,d,"_pt"),delete h[g]),m!=="all"&&(m[g]=1)}return this._initted&&!this._pt&&u&&Ls(this),this},e.to=function(i,s){return new e(i,s,arguments[2])},e.from=function(i,s){return Os(1,arguments)},e.delayedCall=function(i,s,a,o){return new e(s,0,{immediateRender:!1,lazy:!1,overwrite:!1,delay:i,onComplete:s,onReverseComplete:s,onCompleteParams:a,onReverseCompleteParams:a,callbackScope:o})},e.fromTo=function(i,s,a){return Os(2,arguments)},e.set=function(i,s){return s.duration=0,s.repeatDelay||(s.repeat=0),new e(i,s)},e.killTweensOf=function(i,s,a){return wt.killTweensOf(i,s,a)},e}(Ys);Nn(Ft.prototype,{_targets:[],_lazy:0,_startAt:0,_op:0,_onInit:0});_n("staggerTo,staggerFrom,staggerFromTo",function(r){Ft[r]=function(){var e=new on,t=Ql.call(arguments,0);return t.splice(r==="staggerFromTo"?5:4,0,0),e[r].apply(e,t)}});var Oc=function(e,t,n){return e[t]=n},Xf=function(e,t,n){return e[t](n)},Fx=function(e,t,n,i){return e[t](i.fp,n)},Ox=function(e,t,n){return e.setAttribute(t,n)},Bc=function(e,t){return Lt(e[t])?Xf:wc(e[t])&&e.setAttribute?Ox:Oc},qf=function(e,t){return t.set(t.t,t.p,Math.round((t.s+t.c*e)*1e6)/1e6,t)},Bx=function(e,t){return t.set(t.t,t.p,!!(t.s+t.c*e),t)},Yf=function(e,t){var n=t._pt,i="";if(!e&&t.b)i=t.b;else if(e===1&&t.e)i=t.e;else{for(;n;)i=n.p+(n.m?n.m(n.s+n.c*e):Math.round((n.s+n.c*e)*1e4)/1e4)+i,n=n._next;i+=t.c}t.set(t.t,t.p,i,t)},kc=function(e,t){for(var n=t._pt;n;)n.r(e,n.d),n=n._next},kx=function(e,t,n,i){for(var s=this._pt,a;s;)a=s._next,s.p===i&&s.modifier(e,t,n),s=a},zx=function(e){for(var t=this._pt,n,i;t;)i=t._next,t.p===e&&!t.op||t.op===e?so(this,t,"_pt"):t.dep||(n=1),t=i;return!n},Vx=function(e,t,n,i){i.mSet(e,t,i.m.call(i.tween,n,i.mt),i)},$f=function(e){for(var t=e._pt,n,i,s,a;t;){for(n=t._next,i=s;i&&i.pr>t.pr;)i=i._next;(t._prev=i?i._prev:a)?t._prev._next=t:s=t,(t._next=i)?i._prev=t:a=t,t=n}e._pt=s},gn=function(){function r(t,n,i,s,a,o,c,l,u){this.t=n,this.s=s,this.c=a,this.p=i,this.r=o||qf,this.d=c||this,this.set=l||Oc,this.pr=u||0,this._next=t,t&&(t._prev=this)}var e=r.prototype;return e.modifier=function(n,i,s){this.mSet=this.mSet||this.set,this.set=Vx,this.m=n,this.mt=s,this.tween=i},r}();_n(Lc+"parent,duration,ease,delay,overwrite,runBackwards,startAt,yoyo,immediateRender,repeat,repeatDelay,data,paused,reversed,lazy,callbackScope,stringFilter,id,yoyoEase,stagger,inherit,repeatRefresh,keyframes,autoRevert,scrollTrigger",function(r){return Dc[r]=1});Un.TweenMax=Un.TweenLite=Ft;Un.TimelineLite=Un.TimelineMax=on;wt=new on({sortChildren:!1,defaults:us,autoRemoveChildren:!0,id:"root",smoothChildTiming:!0});In.stringFilter=Of;var br=[],Ga={},Gx=[],fh=0,Hx=0,jo=function(e){return(Ga[e]||Gx).map(function(t){return t()})},ic=function(){var e=Date.now(),t=[];e-fh>2&&(jo("matchMediaInit"),br.forEach(function(n){var i=n.queries,s=n.conditions,a,o,c,l;for(o in i)a=ii.matchMedia(i[o]).matches,a&&(c=1),a!==s[o]&&(s[o]=a,l=1);l&&(n.revert(),c&&t.push(n))}),jo("matchMediaRevert"),t.forEach(function(n){return n.onMatch(n,function(i){return n.add(null,i)})}),fh=e,jo("matchMedia"))},Kf=function(){function r(t,n){this.selector=n&&ec(n),this.data=[],this._r=[],this.isReverted=!1,this.id=Hx++,t&&this.add(t)}var e=r.prototype;return e.add=function(n,i,s){Lt(n)&&(s=i,i=n,n=Lt);var a=this,o=function(){var l=Tt,u=a.selector,f;return l&&l!==a&&l.data.push(a),s&&(a.selector=ec(s)),Tt=a,f=i.apply(a,arguments),Lt(f)&&a._r.push(f),Tt=l,a.selector=u,a.isReverted=!1,f};return a.last=o,n===Lt?o(a,function(c){return a.add(null,c)}):n?a[n]=o:o},e.ignore=function(n){var i=Tt;Tt=null,n(this),Tt=i},e.getTweens=function(){var n=[];return this.data.forEach(function(i){return i instanceof r?n.push.apply(n,i.getTweens()):i instanceof Ft&&!(i.parent&&i.parent.data==="nested")&&n.push(i)}),n},e.clear=function(){this._r.length=this.data.length=0},e.kill=function(n,i){var s=this;if(n?function(){for(var o=s.getTweens(),c=s.data.length,l;c--;)l=s.data[c],l.data==="isFlip"&&(l.revert(),l.getChildren(!0,!0,!1).forEach(function(u){return o.splice(o.indexOf(u),1)}));for(o.map(function(u){return{g:u._dur||u._delay||u._sat&&!u._sat.vars.immediateRender?u.globalTime(0):-1/0,t:u}}).sort(function(u,f){return f.g-u.g||-1/0}).forEach(function(u){return u.t.revert(n)}),c=s.data.length;c--;)l=s.data[c],l instanceof on?l.data!=="nested"&&(l.scrollTrigger&&l.scrollTrigger.revert(),l.kill()):!(l instanceof Ft)&&l.revert&&l.revert(n);s._r.forEach(function(u){return u(n,s)}),s.isReverted=!0}():this.data.forEach(function(o){return o.kill&&o.kill()}),this.clear(),i)for(var a=br.length;a--;)br[a].id===this.id&&br.splice(a,1)},e.revert=function(n){this.kill(n||{})},r}(),Wx=function(){function r(t){this.contexts=[],this.scope=t,Tt&&Tt.data.push(this)}var e=r.prototype;return e.add=function(n,i,s){pi(n)||(n={matches:n});var a=new Kf(0,s||this.scope),o=a.conditions={},c,l,u;Tt&&!a.selector&&(a.selector=Tt.selector),this.contexts.push(a),i=a.add("onMatch",i),a.queries=n;for(l in n)l==="all"?u=1:(c=ii.matchMedia(n[l]),c&&(br.indexOf(a)<0&&br.push(a),(o[l]=c.matches)&&(u=1),c.addListener?c.addListener(ic):c.addEventListener("change",ic)));return u&&i(a,function(f){return a.add(null,f)}),this},e.revert=function(n){this.kill(n||{})},e.kill=function(n){this.contexts.forEach(function(i){return i.kill(n,!0)})},r}(),Qa={registerPlugin:function(){for(var e=arguments.length,t=new Array(e),n=0;n<e;n++)t[n]=arguments[n];t.forEach(function(i){return Uf(i)})},timeline:function(e){return new on(e)},getTweensOf:function(e,t){return wt.getTweensOf(e,t)},getProperty:function(e,t,n,i){Ht(e)&&(e=zn(e)[0]);var s=Mr(e||{}).get,a=n?Sf:Mf;return n==="native"&&(n=""),e&&(t?a((wn[t]&&wn[t].get||s)(e,t,n,i)):function(o,c,l){return a((wn[o]&&wn[o].get||s)(e,o,c,l))})},quickSetter:function(e,t,n){if(e=zn(e),e.length>1){var i=e.map(function(u){return xn.quickSetter(u,t,n)}),s=i.length;return function(u){for(var f=s;f--;)i[f](u)}}e=e[0]||{};var a=wn[t],o=Mr(e),c=o.harness&&(o.harness.aliases||{})[t]||t,l=a?function(u){var f=new a;Jr._pt=0,f.init(e,n?u+n:u,Jr,0,[e]),f.render(1,f),Jr._pt&&kc(1,Jr)}:o.set(e,c);return a?l:function(u){return l(e,c,n?u+n:u,o,1)}},quickTo:function(e,t,n){var i,s=xn.to(e,Nn((i={},i[t]="+=0.1",i.paused=!0,i.stagger=0,i),n||{})),a=function(c,l,u){return s.resetTo(t,c,l,u)};return a.tween=s,a},isTweening:function(e){return wt.getTweensOf(e,!0).length>0},defaults:function(e){return e&&e.ease&&(e.ease=yr(e.ease,us.ease)),oh(us,e||{})},config:function(e){return oh(In,e||{})},registerEffect:function(e){var t=e.name,n=e.effect,i=e.plugins,s=e.defaults,a=e.extendTimeline;(i||"").split(",").forEach(function(o){return o&&!wn[o]&&!Un[o]&&Hs(t+" effect requires "+o+" plugin.")}),Yo[t]=function(o,c,l){return n(zn(o),Nn(c||{},s),l)},a&&(on.prototype[t]=function(o,c,l){return this.add(Yo[t](o,pi(c)?c:(l=c)&&{},this),l)})},registerEase:function(e,t){at[e]=yr(t)},parseEase:function(e,t){return arguments.length?yr(e,t):at},getById:function(e){return wt.getById(e)},exportRoot:function(e,t){e===void 0&&(e={});var n=new on(e),i,s;for(n.smoothChildTiming=mn(e.smoothChildTiming),wt.remove(n),n._dp=0,n._time=n._tTime=wt._time,i=wt._first;i;)s=i._next,(t||!(!i._dur&&i instanceof Ft&&i.vars.onComplete===i._targets[0]))&&si(n,i,i._start-i._delay),i=s;return si(wt,n,0),n},context:function(e,t){return e?new Kf(e,t):Tt},matchMedia:function(e){return new Wx(e)},matchMediaRefresh:function(){return br.forEach(function(e){var t=e.conditions,n,i;for(i in t)t[i]&&(t[i]=!1,n=1);n&&e.revert()})||ic()},addEventListener:function(e,t){var n=Ga[e]||(Ga[e]=[]);~n.indexOf(t)||n.push(t)},removeEventListener:function(e,t){var n=Ga[e],i=n&&n.indexOf(t);i>=0&&n.splice(i,1)},utils:{wrap:yx,wrapYoyo:bx,distribute:Rf,random:Pf,snap:Cf,normalize:Sx,getUnit:tn,clamp:gx,splitColor:Nf,toArray:zn,selector:ec,mapRange:Lf,pipe:xx,unitize:Mx,interpolate:Ex,shuffle:wf},install:mf,effects:Yo,ticker:Pn,updateRoot:on.updateRoot,plugins:wn,globalTimeline:wt,core:{PropTween:gn,globals:_f,Tween:Ft,Timeline:on,Animation:Ys,getCache:Mr,_removeLinkedListItem:so,reverting:function(){return Zt},context:function(e){return e&&Tt&&(Tt.data.push(e),e._ctx=Tt),Tt},suppressOverwrites:function(e){return Ac=e}}};_n("to,from,fromTo,delayedCall,set,killTweensOf",function(r){return Qa[r]=Ft[r]});Pn.add(on.updateRoot);Jr=Qa.to({},{duration:0});var Xx=function(e,t){for(var n=e._pt;n&&n.p!==t&&n.op!==t&&n.fp!==t;)n=n._next;return n},qx=function(e,t){var n=e._targets,i,s,a;for(i in t)for(s=n.length;s--;)a=e._ptLookup[s][i],a&&(a=a.d)&&(a._pt&&(a=Xx(a,i)),a&&a.modifier&&a.modifier(t[i],e,n[s],i))},Jo=function(e,t){return{name:e,headless:1,rawVars:1,init:function(i,s,a){a._onInit=function(o){var c,l;if(Ht(s)&&(c={},_n(s,function(u){return c[u]=1}),s=c),t){c={};for(l in s)c[l]=t(s[l]);s=c}qx(o,s)}}}},xn=Qa.registerPlugin({name:"attr",init:function(e,t,n,i,s){var a,o,c;this.tween=n;for(a in t)c=e.getAttribute(a)||"",o=this.add(e,"setAttribute",(c||0)+"",t[a],i,s,0,0,a),o.op=a,o.b=c,this._props.push(a)},render:function(e,t){for(var n=t._pt;n;)Zt?n.set(n.t,n.p,n.b,n):n.r(e,n.d),n=n._next}},{name:"endArray",headless:1,init:function(e,t){for(var n=t.length;n--;)this.add(e,n,e[n]||0,t[n],0,0,0,0,0,1)}},Jo("roundProps",tc),Jo("modifiers"),Jo("snap",Cf))||Qa;Ft.version=on.version=xn.version="3.14.2";pf=1;Rc()&&ps();at.Power0;at.Power1;at.Power2;at.Power3;at.Power4;at.Linear;at.Quad;at.Cubic;at.Quart;at.Quint;at.Strong;at.Elastic;at.Back;at.SteppedEase;at.Bounce;at.Sine;at.Expo;at.Circ;/*!
 * CSSPlugin 3.14.2
 * https://gsap.com
 *
 * Copyright 2008-2025, GreenSock. All rights reserved.
 * Subject to the terms at https://gsap.com/standard-license
 * @author: Jack Doyle, jack@greensock.com
*/var dh,qi,is,zc,xr,ph,Vc,Yx=function(){return typeof window<"u"},Pi={},dr=180/Math.PI,rs=Math.PI/180,Kr=Math.atan2,mh=1e8,Gc=/([A-Z])/g,$x=/(left|right|width|margin|padding|x)/i,Kx=/[\s,\(]\S/,li={autoAlpha:"opacity,visibility",scale:"scaleX,scaleY",alpha:"opacity"},rc=function(e,t){return t.set(t.t,t.p,Math.round((t.s+t.c*e)*1e4)/1e4+t.u,t)},Zx=function(e,t){return t.set(t.t,t.p,e===1?t.e:Math.round((t.s+t.c*e)*1e4)/1e4+t.u,t)},jx=function(e,t){return t.set(t.t,t.p,e?Math.round((t.s+t.c*e)*1e4)/1e4+t.u:t.b,t)},Jx=function(e,t){return t.set(t.t,t.p,e===1?t.e:e?Math.round((t.s+t.c*e)*1e4)/1e4+t.u:t.b,t)},Qx=function(e,t){var n=t.s+t.c*e;t.set(t.t,t.p,~~(n+(n<0?-.5:.5))+t.u,t)},Zf=function(e,t){return t.set(t.t,t.p,e?t.e:t.b,t)},jf=function(e,t){return t.set(t.t,t.p,e!==1?t.b:t.e,t)},eM=function(e,t,n){return e.style[t]=n},tM=function(e,t,n){return e.style.setProperty(t,n)},nM=function(e,t,n){return e._gsap[t]=n},iM=function(e,t,n){return e._gsap.scaleX=e._gsap.scaleY=n},rM=function(e,t,n,i,s){var a=e._gsap;a.scaleX=a.scaleY=n,a.renderTransform(s,a)},sM=function(e,t,n,i,s){var a=e._gsap;a[t]=n,a.renderTransform(s,a)},Rt="transform",vn=Rt+"Origin",aM=function r(e,t){var n=this,i=this.target,s=i.style,a=i._gsap;if(e in Pi&&s){if(this.tfm=this.tfm||{},e!=="transform")e=li[e]||e,~e.indexOf(",")?e.split(",").forEach(function(o){return n.tfm[o]=yi(i,o)}):this.tfm[e]=a.x?a[e]:yi(i,e),e===vn&&(this.tfm.zOrigin=a.zOrigin);else return li.transform.split(",").forEach(function(o){return r.call(n,o,t)});if(this.props.indexOf(Rt)>=0)return;a.svg&&(this.svgo=i.getAttribute("data-svg-origin"),this.props.push(vn,t,"")),e=Rt}(s||t)&&this.props.push(e,t,s[e])},Jf=function(e){e.translate&&(e.removeProperty("translate"),e.removeProperty("scale"),e.removeProperty("rotate"))},oM=function(){var e=this.props,t=this.target,n=t.style,i=t._gsap,s,a;for(s=0;s<e.length;s+=3)e[s+1]?e[s+1]===2?t[e[s]](e[s+2]):t[e[s]]=e[s+2]:e[s+2]?n[e[s]]=e[s+2]:n.removeProperty(e[s].substr(0,2)==="--"?e[s]:e[s].replace(Gc,"-$1").toLowerCase());if(this.tfm){for(a in this.tfm)i[a]=this.tfm[a];i.svg&&(i.renderTransform(),t.setAttribute("data-svg-origin",this.svgo||"")),s=Vc(),(!s||!s.isStart)&&!n[Rt]&&(Jf(n),i.zOrigin&&n[vn]&&(n[vn]+=" "+i.zOrigin+"px",i.zOrigin=0,i.renderTransform()),i.uncache=1)}},Qf=function(e,t){var n={target:e,props:[],revert:oM,save:aM};return e._gsap||xn.core.getCache(e),t&&e.style&&e.nodeType&&t.split(",").forEach(function(i){return n.save(i)}),n},ed,sc=function(e,t){var n=qi.createElementNS?qi.createElementNS((t||"http://www.w3.org/1999/xhtml").replace(/^https/,"http"),e):qi.createElement(e);return n&&n.style?n:qi.createElement(e)},Ln=function r(e,t,n){var i=getComputedStyle(e);return i[t]||i.getPropertyValue(t.replace(Gc,"-$1").toLowerCase())||i.getPropertyValue(t)||!n&&r(e,ms(t)||t,1)||""},_h="O,Moz,ms,Ms,Webkit".split(","),ms=function(e,t,n){var i=t||xr,s=i.style,a=5;if(e in s&&!n)return e;for(e=e.charAt(0).toUpperCase()+e.substr(1);a--&&!(_h[a]+e in s););return a<0?null:(a===3?"ms":a>=0?_h[a]:"")+e},ac=function(){Yx()&&window.document&&(dh=window,qi=dh.document,is=qi.documentElement,xr=sc("div")||{style:{}},sc("div"),Rt=ms(Rt),vn=Rt+"Origin",xr.style.cssText="border-width:0;line-height:0;position:absolute;padding:0",ed=!!ms("perspective"),Vc=xn.core.reverting,zc=1)},gh=function(e){var t=e.ownerSVGElement,n=sc("svg",t&&t.getAttribute("xmlns")||"http://www.w3.org/2000/svg"),i=e.cloneNode(!0),s;i.style.display="block",n.appendChild(i),is.appendChild(n);try{s=i.getBBox()}catch{}return n.removeChild(i),is.removeChild(n),s},vh=function(e,t){for(var n=t.length;n--;)if(e.hasAttribute(t[n]))return e.getAttribute(t[n])},td=function(e){var t,n;try{t=e.getBBox()}catch{t=gh(e),n=1}return t&&(t.width||t.height)||n||(t=gh(e)),t&&!t.width&&!t.x&&!t.y?{x:+vh(e,["x","cx","x1"])||0,y:+vh(e,["y","cy","y1"])||0,width:0,height:0}:t},nd=function(e){return!!(e.getCTM&&(!e.parentNode||e.ownerSVGElement)&&td(e))},Qi=function(e,t){if(t){var n=e.style,i;t in Pi&&t!==vn&&(t=Rt),n.removeProperty?(i=t.substr(0,2),(i==="ms"||t.substr(0,6)==="webkit")&&(t="-"+t),n.removeProperty(i==="--"?t:t.replace(Gc,"-$1").toLowerCase())):n.removeAttribute(t)}},Yi=function(e,t,n,i,s,a){var o=new gn(e._pt,t,n,0,1,a?jf:Zf);return e._pt=o,o.b=i,o.e=s,e._props.push(n),o},xh={deg:1,rad:1,turn:1},lM={grid:1,flex:1},er=function r(e,t,n,i){var s=parseFloat(n)||0,a=(n+"").trim().substr((s+"").length)||"px",o=xr.style,c=$x.test(t),l=e.tagName.toLowerCase()==="svg",u=(l?"client":"offset")+(c?"Width":"Height"),f=100,h=i==="px",m=i==="%",_,g,d,p;if(i===a||!s||xh[i]||xh[a])return s;if(a!=="px"&&!h&&(s=r(e,t,n,"px")),p=e.getCTM&&nd(e),(m||a==="%")&&(Pi[t]||~t.indexOf("adius")))return _=p?e.getBBox()[c?"width":"height"]:e[u],Ut(m?s/_*f:s/100*_);if(o[c?"width":"height"]=f+(h?a:i),g=i!=="rem"&&~t.indexOf("adius")||i==="em"&&e.appendChild&&!l?e:e.parentNode,p&&(g=(e.ownerSVGElement||{}).parentNode),(!g||g===qi||!g.appendChild)&&(g=qi.body),d=g._gsap,d&&m&&d.width&&c&&d.time===Pn.time&&!d.uncache)return Ut(s/d.width*f);if(m&&(t==="height"||t==="width")){var M=e.style[t];e.style[t]=f+i,_=e[u],M?e.style[t]=M:Qi(e,t)}else(m||a==="%")&&!lM[Ln(g,"display")]&&(o.position=Ln(e,"position")),g===e&&(o.position="static"),g.appendChild(xr),_=xr[u],g.removeChild(xr),o.position="absolute";return c&&m&&(d=Mr(g),d.time=Pn.time,d.width=g[u]),Ut(h?_*s/f:_&&s?f/_*s:0)},yi=function(e,t,n,i){var s;return zc||ac(),t in li&&t!=="transform"&&(t=li[t],~t.indexOf(",")&&(t=t.split(",")[0])),Pi[t]&&t!=="transform"?(s=Ks(e,i),s=t!=="transformOrigin"?s[t]:s.svg?s.origin:to(Ln(e,vn))+" "+s.zOrigin+"px"):(s=e.style[t],(!s||s==="auto"||i||~(s+"").indexOf("calc("))&&(s=eo[t]&&eo[t](e,t,n)||Ln(e,t)||vf(e,t)||(t==="opacity"?1:0))),n&&!~(s+"").trim().indexOf(" ")?er(e,t,s,n)+n:s},cM=function(e,t,n,i){if(!n||n==="none"){var s=ms(t,e,1),a=s&&Ln(e,s,1);a&&a!==n?(t=s,n=a):t==="borderColor"&&(n=Ln(e,"borderTopColor"))}var o=new gn(this._pt,e.style,t,0,1,Yf),c=0,l=0,u,f,h,m,_,g,d,p,M,y,b,T;if(o.b=n,o.e=i,n+="",i+="",i.substring(0,6)==="var(--"&&(i=Ln(e,i.substring(4,i.indexOf(")")))),i==="auto"&&(g=e.style[t],e.style[t]=i,i=Ln(e,t)||i,g?e.style[t]=g:Qi(e,t)),u=[n,i],Of(u),n=u[0],i=u[1],h=n.match(jr)||[],T=i.match(jr)||[],T.length){for(;f=jr.exec(i);)d=f[0],M=i.substring(c,f.index),_?_=(_+1)%5:(M.substr(-5)==="rgba("||M.substr(-5)==="hsla(")&&(_=1),d!==(g=h[l++]||"")&&(m=parseFloat(g)||0,b=g.substr((m+"").length),d.charAt(1)==="="&&(d=ns(m,d)+b),p=parseFloat(d),y=d.substr((p+"").length),c=jr.lastIndex-y.length,y||(y=y||In.units[t]||b,c===i.length&&(i+=y,o.e+=y)),b!==y&&(m=er(e,t,g,y)||0),o._pt={_next:o._pt,p:M||l===1?M:",",s:m,c:p-m,m:_&&_<4||t==="zIndex"?Math.round:0});o.c=c<i.length?i.substring(c,i.length):""}else o.r=t==="display"&&i==="none"?jf:Zf;return df.test(i)&&(o.e=0),this._pt=o,o},Mh={top:"0%",bottom:"100%",left:"0%",right:"100%",center:"50%"},uM=function(e){var t=e.split(" "),n=t[0],i=t[1]||"50%";return(n==="top"||n==="bottom"||i==="left"||i==="right")&&(e=n,n=i,i=e),t[0]=Mh[n]||n,t[1]=Mh[i]||i,t.join(" ")},hM=function(e,t){if(t.tween&&t.tween._time===t.tween._dur){var n=t.t,i=n.style,s=t.u,a=n._gsap,o,c,l;if(s==="all"||s===!0)i.cssText="",c=1;else for(s=s.split(","),l=s.length;--l>-1;)o=s[l],Pi[o]&&(c=1,o=o==="transformOrigin"?vn:Rt),Qi(n,o);c&&(Qi(n,Rt),a&&(a.svg&&n.removeAttribute("transform"),i.scale=i.rotate=i.translate="none",Ks(n,1),a.uncache=1,Jf(i)))}},eo={clearProps:function(e,t,n,i,s){if(s.data!=="isFromStart"){var a=e._pt=new gn(e._pt,t,n,0,0,hM);return a.u=i,a.pr=-10,a.tween=s,e._props.push(n),1}}},$s=[1,0,0,1,0,0],id={},rd=function(e){return e==="matrix(1, 0, 0, 1, 0, 0)"||e==="none"||!e},Sh=function(e){var t=Ln(e,Rt);return rd(t)?$s:t.substr(7).match(ff).map(Ut)},Hc=function(e,t){var n=e._gsap||Mr(e),i=e.style,s=Sh(e),a,o,c,l;return n.svg&&e.getAttribute("transform")?(c=e.transform.baseVal.consolidate().matrix,s=[c.a,c.b,c.c,c.d,c.e,c.f],s.join(",")==="1,0,0,1,0,0"?$s:s):(s===$s&&!e.offsetParent&&e!==is&&!n.svg&&(c=i.display,i.display="block",a=e.parentNode,(!a||!e.offsetParent&&!e.getBoundingClientRect().width)&&(l=1,o=e.nextElementSibling,is.appendChild(e)),s=Sh(e),c?i.display=c:Qi(e,"display"),l&&(o?a.insertBefore(e,o):a?a.appendChild(e):is.removeChild(e))),t&&s.length>6?[s[0],s[1],s[4],s[5],s[12],s[13]]:s)},oc=function(e,t,n,i,s,a){var o=e._gsap,c=s||Hc(e,!0),l=o.xOrigin||0,u=o.yOrigin||0,f=o.xOffset||0,h=o.yOffset||0,m=c[0],_=c[1],g=c[2],d=c[3],p=c[4],M=c[5],y=t.split(" "),b=parseFloat(y[0])||0,T=parseFloat(y[1])||0,A,R,x,S;n?c!==$s&&(R=m*d-_*g)&&(x=b*(d/R)+T*(-g/R)+(g*M-d*p)/R,S=b*(-_/R)+T*(m/R)-(m*M-_*p)/R,b=x,T=S):(A=td(e),b=A.x+(~y[0].indexOf("%")?b/100*A.width:b),T=A.y+(~(y[1]||y[0]).indexOf("%")?T/100*A.height:T)),i||i!==!1&&o.smooth?(p=b-l,M=T-u,o.xOffset=f+(p*m+M*g)-p,o.yOffset=h+(p*_+M*d)-M):o.xOffset=o.yOffset=0,o.xOrigin=b,o.yOrigin=T,o.smooth=!!i,o.origin=t,o.originIsAbsolute=!!n,e.style[vn]="0px 0px",a&&(Yi(a,o,"xOrigin",l,b),Yi(a,o,"yOrigin",u,T),Yi(a,o,"xOffset",f,o.xOffset),Yi(a,o,"yOffset",h,o.yOffset)),e.setAttribute("data-svg-origin",b+" "+T)},Ks=function(e,t){var n=e._gsap||new Vf(e);if("x"in n&&!t&&!n.uncache)return n;var i=e.style,s=n.scaleX<0,a="px",o="deg",c=getComputedStyle(e),l=Ln(e,vn)||"0",u,f,h,m,_,g,d,p,M,y,b,T,A,R,x,S,W,P,z,F,B,O,V,G,ne,Q,me,be,xe,Ge,qe,je;return u=f=h=g=d=p=M=y=b=0,m=_=1,n.svg=!!(e.getCTM&&nd(e)),c.translate&&((c.translate!=="none"||c.scale!=="none"||c.rotate!=="none")&&(i[Rt]=(c.translate!=="none"?"translate3d("+(c.translate+" 0 0").split(" ").slice(0,3).join(", ")+") ":"")+(c.rotate!=="none"?"rotate("+c.rotate+") ":"")+(c.scale!=="none"?"scale("+c.scale.split(" ").join(",")+") ":"")+(c[Rt]!=="none"?c[Rt]:"")),i.scale=i.rotate=i.translate="none"),R=Hc(e,n.svg),n.svg&&(n.uncache?(ne=e.getBBox(),l=n.xOrigin-ne.x+"px "+(n.yOrigin-ne.y)+"px",G=""):G=!t&&e.getAttribute("data-svg-origin"),oc(e,G||l,!!G||n.originIsAbsolute,n.smooth!==!1,R)),T=n.xOrigin||0,A=n.yOrigin||0,R!==$s&&(P=R[0],z=R[1],F=R[2],B=R[3],u=O=R[4],f=V=R[5],R.length===6?(m=Math.sqrt(P*P+z*z),_=Math.sqrt(B*B+F*F),g=P||z?Kr(z,P)*dr:0,M=F||B?Kr(F,B)*dr+g:0,M&&(_*=Math.abs(Math.cos(M*rs))),n.svg&&(u-=T-(T*P+A*F),f-=A-(T*z+A*B))):(je=R[6],Ge=R[7],me=R[8],be=R[9],xe=R[10],qe=R[11],u=R[12],f=R[13],h=R[14],x=Kr(je,xe),d=x*dr,x&&(S=Math.cos(-x),W=Math.sin(-x),G=O*S+me*W,ne=V*S+be*W,Q=je*S+xe*W,me=O*-W+me*S,be=V*-W+be*S,xe=je*-W+xe*S,qe=Ge*-W+qe*S,O=G,V=ne,je=Q),x=Kr(-F,xe),p=x*dr,x&&(S=Math.cos(-x),W=Math.sin(-x),G=P*S-me*W,ne=z*S-be*W,Q=F*S-xe*W,qe=B*W+qe*S,P=G,z=ne,F=Q),x=Kr(z,P),g=x*dr,x&&(S=Math.cos(x),W=Math.sin(x),G=P*S+z*W,ne=O*S+V*W,z=z*S-P*W,V=V*S-O*W,P=G,O=ne),d&&Math.abs(d)+Math.abs(g)>359.9&&(d=g=0,p=180-p),m=Ut(Math.sqrt(P*P+z*z+F*F)),_=Ut(Math.sqrt(V*V+je*je)),x=Kr(O,V),M=Math.abs(x)>2e-4?x*dr:0,b=qe?1/(qe<0?-qe:qe):0),n.svg&&(G=e.getAttribute("transform"),n.forceCSS=e.setAttribute("transform","")||!rd(Ln(e,Rt)),G&&e.setAttribute("transform",G))),Math.abs(M)>90&&Math.abs(M)<270&&(s?(m*=-1,M+=g<=0?180:-180,g+=g<=0?180:-180):(_*=-1,M+=M<=0?180:-180)),t=t||n.uncache,n.x=u-((n.xPercent=u&&(!t&&n.xPercent||(Math.round(e.offsetWidth/2)===Math.round(-u)?-50:0)))?e.offsetWidth*n.xPercent/100:0)+a,n.y=f-((n.yPercent=f&&(!t&&n.yPercent||(Math.round(e.offsetHeight/2)===Math.round(-f)?-50:0)))?e.offsetHeight*n.yPercent/100:0)+a,n.z=h+a,n.scaleX=Ut(m),n.scaleY=Ut(_),n.rotation=Ut(g)+o,n.rotationX=Ut(d)+o,n.rotationY=Ut(p)+o,n.skewX=M+o,n.skewY=y+o,n.transformPerspective=b+a,(n.zOrigin=parseFloat(l.split(" ")[2])||!t&&n.zOrigin||0)&&(i[vn]=to(l)),n.xOffset=n.yOffset=0,n.force3D=In.force3D,n.renderTransform=n.svg?dM:ed?sd:fM,n.uncache=0,n},to=function(e){return(e=e.split(" "))[0]+" "+e[1]},Qo=function(e,t,n){var i=tn(t);return Ut(parseFloat(t)+parseFloat(er(e,"x",n+"px",i)))+i},fM=function(e,t){t.z="0px",t.rotationY=t.rotationX="0deg",t.force3D=0,sd(e,t)},hr="0deg",Cs="0px",fr=") ",sd=function(e,t){var n=t||this,i=n.xPercent,s=n.yPercent,a=n.x,o=n.y,c=n.z,l=n.rotation,u=n.rotationY,f=n.rotationX,h=n.skewX,m=n.skewY,_=n.scaleX,g=n.scaleY,d=n.transformPerspective,p=n.force3D,M=n.target,y=n.zOrigin,b="",T=p==="auto"&&e&&e!==1||p===!0;if(y&&(f!==hr||u!==hr)){var A=parseFloat(u)*rs,R=Math.sin(A),x=Math.cos(A),S;A=parseFloat(f)*rs,S=Math.cos(A),a=Qo(M,a,R*S*-y),o=Qo(M,o,-Math.sin(A)*-y),c=Qo(M,c,x*S*-y+y)}d!==Cs&&(b+="perspective("+d+fr),(i||s)&&(b+="translate("+i+"%, "+s+"%) "),(T||a!==Cs||o!==Cs||c!==Cs)&&(b+=c!==Cs||T?"translate3d("+a+", "+o+", "+c+") ":"translate("+a+", "+o+fr),l!==hr&&(b+="rotate("+l+fr),u!==hr&&(b+="rotateY("+u+fr),f!==hr&&(b+="rotateX("+f+fr),(h!==hr||m!==hr)&&(b+="skew("+h+", "+m+fr),(_!==1||g!==1)&&(b+="scale("+_+", "+g+fr),M.style[Rt]=b||"translate(0, 0)"},dM=function(e,t){var n=t||this,i=n.xPercent,s=n.yPercent,a=n.x,o=n.y,c=n.rotation,l=n.skewX,u=n.skewY,f=n.scaleX,h=n.scaleY,m=n.target,_=n.xOrigin,g=n.yOrigin,d=n.xOffset,p=n.yOffset,M=n.forceCSS,y=parseFloat(a),b=parseFloat(o),T,A,R,x,S;c=parseFloat(c),l=parseFloat(l),u=parseFloat(u),u&&(u=parseFloat(u),l+=u,c+=u),c||l?(c*=rs,l*=rs,T=Math.cos(c)*f,A=Math.sin(c)*f,R=Math.sin(c-l)*-h,x=Math.cos(c-l)*h,l&&(u*=rs,S=Math.tan(l-u),S=Math.sqrt(1+S*S),R*=S,x*=S,u&&(S=Math.tan(u),S=Math.sqrt(1+S*S),T*=S,A*=S)),T=Ut(T),A=Ut(A),R=Ut(R),x=Ut(x)):(T=f,x=h,A=R=0),(y&&!~(a+"").indexOf("px")||b&&!~(o+"").indexOf("px"))&&(y=er(m,"x",a,"px"),b=er(m,"y",o,"px")),(_||g||d||p)&&(y=Ut(y+_-(_*T+g*R)+d),b=Ut(b+g-(_*A+g*x)+p)),(i||s)&&(S=m.getBBox(),y=Ut(y+i/100*S.width),b=Ut(b+s/100*S.height)),S="matrix("+T+","+A+","+R+","+x+","+y+","+b+")",m.setAttribute("transform",S),M&&(m.style[Rt]=S)},pM=function(e,t,n,i,s){var a=360,o=Ht(s),c=parseFloat(s)*(o&&~s.indexOf("rad")?dr:1),l=c-i,u=i+l+"deg",f,h;return o&&(f=s.split("_")[1],f==="short"&&(l%=a,l!==l%(a/2)&&(l+=l<0?a:-a)),f==="cw"&&l<0?l=(l+a*mh)%a-~~(l/a)*a:f==="ccw"&&l>0&&(l=(l-a*mh)%a-~~(l/a)*a)),e._pt=h=new gn(e._pt,t,n,i,l,Zx),h.e=u,h.u="deg",e._props.push(n),h},yh=function(e,t){for(var n in t)e[n]=t[n];return e},mM=function(e,t,n){var i=yh({},n._gsap),s="perspective,force3D,transformOrigin,svgOrigin",a=n.style,o,c,l,u,f,h,m,_;i.svg?(l=n.getAttribute("transform"),n.setAttribute("transform",""),a[Rt]=t,o=Ks(n,1),Qi(n,Rt),n.setAttribute("transform",l)):(l=getComputedStyle(n)[Rt],a[Rt]=t,o=Ks(n,1),a[Rt]=l);for(c in Pi)l=i[c],u=o[c],l!==u&&s.indexOf(c)<0&&(m=tn(l),_=tn(u),f=m!==_?er(n,c,l,_):parseFloat(l),h=parseFloat(u),e._pt=new gn(e._pt,o,c,f,h-f,rc),e._pt.u=_||0,e._props.push(c));yh(o,i)};_n("padding,margin,Width,Radius",function(r,e){var t="Top",n="Right",i="Bottom",s="Left",a=(e<3?[t,n,i,s]:[t+s,t+n,i+n,i+s]).map(function(o){return e<2?r+o:"border"+o+r});eo[e>1?"border"+r:r]=function(o,c,l,u,f){var h,m;if(arguments.length<4)return h=a.map(function(_){return yi(o,_,l)}),m=h.join(" "),m.split(h[0]).length===5?h[0]:m;h=(u+"").split(" "),m={},a.forEach(function(_,g){return m[_]=h[g]=h[g]||h[(g-1)/2|0]}),o.init(c,m,f)}});var ad={name:"css",register:ac,targetTest:function(e){return e.style&&e.nodeType},init:function(e,t,n,i,s){var a=this._props,o=e.style,c=n.vars.startAt,l,u,f,h,m,_,g,d,p,M,y,b,T,A,R,x,S;zc||ac(),this.styles=this.styles||Qf(e),x=this.styles.props,this.tween=n;for(g in t)if(g!=="autoRound"&&(u=t[g],!(wn[g]&&Gf(g,t,n,i,e,s)))){if(m=typeof u,_=eo[g],m==="function"&&(u=u.call(n,i,e,s),m=typeof u),m==="string"&&~u.indexOf("random(")&&(u=Xs(u)),_)_(this,e,g,u,n)&&(R=1);else if(g.substr(0,2)==="--")l=(getComputedStyle(e).getPropertyValue(g)+"").trim(),u+="",Ki.lastIndex=0,Ki.test(l)||(d=tn(l),p=tn(u),p?d!==p&&(l=er(e,g,l,p)+p):d&&(u+=d)),this.add(o,"setProperty",l,u,i,s,0,0,g),a.push(g),x.push(g,0,o[g]);else if(m!=="undefined"){if(c&&g in c?(l=typeof c[g]=="function"?c[g].call(n,i,e,s):c[g],Ht(l)&&~l.indexOf("random(")&&(l=Xs(l)),tn(l+"")||l==="auto"||(l+=In.units[g]||tn(yi(e,g))||""),(l+"").charAt(1)==="="&&(l=yi(e,g))):l=yi(e,g),h=parseFloat(l),M=m==="string"&&u.charAt(1)==="="&&u.substr(0,2),M&&(u=u.substr(2)),f=parseFloat(u),g in li&&(g==="autoAlpha"&&(h===1&&yi(e,"visibility")==="hidden"&&f&&(h=0),x.push("visibility",0,o.visibility),Yi(this,o,"visibility",h?"inherit":"hidden",f?"inherit":"hidden",!f)),g!=="scale"&&g!=="transform"&&(g=li[g],~g.indexOf(",")&&(g=g.split(",")[0]))),y=g in Pi,y){if(this.styles.save(g),S=u,m==="string"&&u.substring(0,6)==="var(--"){if(u=Ln(e,u.substring(4,u.indexOf(")"))),u.substring(0,5)==="calc("){var W=e.style.perspective;e.style.perspective=u,u=Ln(e,"perspective"),W?e.style.perspective=W:Qi(e,"perspective")}f=parseFloat(u)}if(b||(T=e._gsap,T.renderTransform&&!t.parseTransform||Ks(e,t.parseTransform),A=t.smoothOrigin!==!1&&T.smooth,b=this._pt=new gn(this._pt,o,Rt,0,1,T.renderTransform,T,0,-1),b.dep=1),g==="scale")this._pt=new gn(this._pt,T,"scaleY",T.scaleY,(M?ns(T.scaleY,M+f):f)-T.scaleY||0,rc),this._pt.u=0,a.push("scaleY",g),g+="X";else if(g==="transformOrigin"){x.push(vn,0,o[vn]),u=uM(u),T.svg?oc(e,u,0,A,0,this):(p=parseFloat(u.split(" ")[2])||0,p!==T.zOrigin&&Yi(this,T,"zOrigin",T.zOrigin,p),Yi(this,o,g,to(l),to(u)));continue}else if(g==="svgOrigin"){oc(e,u,1,A,0,this);continue}else if(g in id){pM(this,T,g,h,M?ns(h,M+u):u);continue}else if(g==="smoothOrigin"){Yi(this,T,"smooth",T.smooth,u);continue}else if(g==="force3D"){T[g]=u;continue}else if(g==="transform"){mM(this,u,e);continue}}else g in o||(g=ms(g)||g);if(y||(f||f===0)&&(h||h===0)&&!Kx.test(u)&&g in o)d=(l+"").substr((h+"").length),f||(f=0),p=tn(u)||(g in In.units?In.units[g]:d),d!==p&&(h=er(e,g,l,p)),this._pt=new gn(this._pt,y?T:o,g,h,(M?ns(h,M+f):f)-h,!y&&(p==="px"||g==="zIndex")&&t.autoRound!==!1?Qx:rc),this._pt.u=p||0,y&&S!==u?(this._pt.b=l,this._pt.e=S,this._pt.r=Jx):d!==p&&p!=="%"&&(this._pt.b=l,this._pt.r=jx);else if(g in o)cM.call(this,e,g,l,M?M+u:u);else if(g in e)this.add(e,g,l||e[g],M?M+u:u,i,s);else if(g!=="parseTransform"){Pc(g,u);continue}y||(g in o?x.push(g,0,o[g]):typeof e[g]=="function"?x.push(g,2,e[g]()):x.push(g,1,l||e[g])),a.push(g)}}R&&$f(this)},render:function(e,t){if(t.tween._time||!Vc())for(var n=t._pt;n;)n.r(e,n.d),n=n._next;else t.styles.revert()},get:yi,aliases:li,getSetter:function(e,t,n){var i=li[t];return i&&i.indexOf(",")<0&&(t=i),t in Pi&&t!==vn&&(e._gsap.x||yi(e,"x"))?n&&ph===n?t==="scale"?iM:nM:(ph=n||{})&&(t==="scale"?rM:sM):e.style&&!wc(e.style[t])?eM:~t.indexOf("-")?tM:Bc(e,t)},core:{_removeProperty:Qi,_getMatrix:Hc}};xn.utils.checkPrefix=ms;xn.core.getStyleSaver=Qf;(function(r,e,t,n){var i=_n(r+","+e+","+t,function(s){Pi[s]=1});_n(e,function(s){In.units[s]="deg",id[s]=1}),li[i[13]]=r+","+e,_n(n,function(s){var a=s.split(":");li[a[1]]=i[a[0]]})})("x,y,z,scale,scaleX,scaleY,xPercent,yPercent","rotation,rotationX,rotationY,skewX,skewY","transform,transformOrigin,svgOrigin,force3D,smoothOrigin,transformPerspective","0:translateX,1:translateY,2:translateZ,8:rotate,8:rotationZ,8:rotateZ,9:rotateX,10:rotateY");_n("x,y,z,top,right,bottom,left,width,height,fontSize,padding,margin,perspective",function(r){In.units[r]="px"});xn.registerPlugin(ad);var Vi=xn.registerPlugin(ad)||xn;Vi.core.Tween;const _M={class:"scene-toolbar"},gM={class:"toolbar-left"},vM={class:"toolbar-center"},xM={class:"scene-hud"},MM={class:"hud-panel"},SM={class:"hud-panel"},yM={class:"hud-panel"},bM={class:"tt-header"},EM=["innerHTML"],TM=bh({__name:"Panorama3D",props:{buildings:{},floors:{},roomLookup:{}},emits:["click-room","click-bed"],setup(r,{emit:e}){const t=r,n=e,i=pt(null),s=pt(null),a=pt("PARK"),o=pt(""),c=pt(""),l=pt(""),u=pt("园区总览"),f=pt("等待交互"),h=pt({visible:!1,x:0,y:0,title:"",content:""}),m=dt(()=>{let K="园区全景";return o.value&&(K+=` / ${o.value}`),c.value&&(K+=` / ${c.value}`),l.value&&(K+=` / 房间 ${l.value}`),K}),_=dt(()=>a.value==="PARK"?"园区":a.value==="BUILDING"?"楼栋":a.value==="FLOOR"?"楼层":"房间"),g=vs(new uu),d=vs(null),p=vs(null),M=vs(null),y=vs(null),b=new Map,T=[],A=[];let R=null,x=0,S=0,W=0;const P=pt(!0),z=new $e,F=new zp;let B=null;const O={building:new En({color:5756927,transparent:!0,opacity:.1,roughness:.15,metalness:.86,side:$n}),floor:new En({color:530735,transparent:!0,opacity:.95,roughness:.42,metalness:.42}),room:new En({color:1456217,transparent:!0,opacity:.7,roughness:.55,metalness:.2}),roomHover:new En({color:2871295,emissive:2871295,emissiveIntensity:.4,transparent:!0,opacity:.85}),hover:new En({color:6546431,emissive:2871295,emissiveIntensity:.6,transparent:!0,opacity:.85}),bedNormal:new En({color:2871295,emissive:2871295,emissiveIntensity:.36,metalness:.48,roughness:.28}),bedOccupied:new En({color:2411667,emissive:2411667,emissiveIntensity:.42,metalness:.38,roughness:.3}),bedSleep:new En({color:9401599,emissive:9401599,emissiveIntensity:.44,metalness:.36,roughness:.32}),bedWarning:new En({color:16754006,emissive:16754006,emissiveIntensity:.48,metalness:.34,roughness:.3}),bedAlert:new En({color:16735612,emissive:16735612,emissiveIntensity:.76,metalness:.26,roughness:.28}),bedOffline:new En({color:7571616,emissive:7571616,emissiveIntensity:.18,metalness:.2,roughness:.5})};function V(){if(!s.value)return;const K=s.value.clientWidth,re=s.value.clientHeight;g.value=new uu,g.value.background=new rt(199446),g.value.fog=new Mc(133137,.008),d.value=new Rn(42,K/re,.1,1600),d.value.position.set(42,38,64),p.value=new Fv({antialias:!0,alpha:!0}),p.value.setSize(K,re),p.value.setPixelRatio(Math.min(window.devicePixelRatio,2)),p.value.setClearColor(133137,1),p.value.outputColorSpace=An,p.value.toneMapping=cc,p.value.toneMappingExposure=1.16,s.value.appendChild(p.value.domElement),M.value=new jv,M.value.setSize(K,re),M.value.domElement.style.position="absolute",M.value.domElement.style.top="0",M.value.domElement.style.pointerEvents="none",s.value.appendChild(M.value.domElement),y.value=new Bv(d.value,p.value.domElement),y.value.enableDamping=!0,y.value.dampingFactor=.05,y.value.minDistance=8,y.value.maxDistance=180,y.value.maxPolarAngle=Math.PI/2-.04,y.value.autoRotate=!0,y.value.autoRotateSpeed=.18;const C=new Op(14217215,.62);g.value.add(C);const Ee=new Tu(15793151,1.45);Ee.position.set(36,80,40),g.value.add(Ee);const oe=new Tu(4571135,.65);oe.position.set(-40,24,-36),g.value.add(oe);const ue=new Np(9401599,1.2,260,2);ue.position.set(0,20,-24),g.value.add(ue),G(),p.value.domElement.addEventListener("pointermove",he),p.value.domElement.addEventListener("click",pe),window.addEventListener("resize",Et),Ge(),st()}function G(){if(!g.value)return;const K=new Vp(280,42,1456217,660774);K.position.y=-.12,K.material.transparent=!0,K.material.opacity=.38,K.userData.isGenerated=!0,g.value.add(K);const re=new yc(32,34,120),C=new qa({color:2871295,transparent:!0,opacity:.08,side:$n}),Ee=new Yt(re,C);Ee.rotation.x=-Math.PI/2,Ee.position.y=-.08,Ee.userData.isGenerated=!0,g.value.add(Ee);const oe=new cn,ue=320,ae=new Float32Array(ue*3);for(let v=0;v<ue;v+=1)ae[v*3]=(Math.random()-.5)*180,ae[v*3+1]=Math.random()*28+4,ae[v*3+2]=(Math.random()-.5)*180;oe.setAttribute("position",new Zn(ae,3));const w=new Kh({size:.7,color:5756927,transparent:!0,opacity:.4});R=new Tp(oe,w),R.userData.isGenerated=!0,g.value.add(R)}function ne(K,re){const C=document.createElement("div");return C.className=`scene-label ${re}`,C.textContent=K,new Yr(C)}function Q(K){const re=Number(K.abnormalVital24hCount||0);return K.status===0||K.status===2?"offline":K.riskLevel==="HIGH"||re>0?"alert":K.riskLevel==="MEDIUM"||K.status===3||String(K.bedNo||"").endsWith("R")?"warning":K.riskLevel==="LOW"&&K.elderId?"sleep":K.elderId?"occupied":"normal"}function me(K){return K==="occupied"?O.bedOccupied.clone():K==="sleep"?O.bedSleep.clone():K==="warning"?O.bedWarning.clone():K==="alert"?O.bedAlert.clone():K==="offline"?O.bedOffline.clone():O.bedNormal.clone()}function be(K,re,C){const Ee=Q(K),oe=new bi,ue=.22,ae=new Yt(new dn(re,ue,C),new En({color:1058109,metalness:.5,roughness:.42}));ae.position.y=ue/2,oe.add(ae);const w=me(Ee),v=new Yt(new dn(re*.82,.14,C*.74),w);v.position.y=ue+.08,v.userData={type:"bed",bed:K,originalMat:w,state:Ee},oe.add(v),T.push(v);const L=new Yt(new dn(re*.84,.34,.08),new En({color:2048871,metalness:.42,roughness:.26}));L.position.set(0,.34,-C*.36),oe.add(L);const Z=new Yt(new Qs(re*.96,C*.88),new qa({color:w.color,transparent:!0,opacity:Ee==="alert"?.24:.12}));return Z.rotation.x=-Math.PI/2,Z.position.y=.02,oe.add(Z),Ee==="alert"?A.push({mesh:v,type:"pulse",baseIntensity:.76}):Ee==="warning"?A.push({mesh:v,type:"blink",baseIntensity:.48}):Ee==="sleep"&&A.push({mesh:v,type:"sleep",baseIntensity:.44}),oe}function xe(){T.length=0,A.length=0,b.clear(),B=null,h.value.visible=!1,g.value.children.filter(re=>re.userData.isGenerated).forEach(re=>{re.traverse(C=>{var ue,ae,w;const Ee=C;Ee.geometry&&((ae=(ue=Ee.geometry).dispose)==null||ae.call(ue));const oe=Ee.material;Array.isArray(oe)?oe.forEach(v=>{var L;return(L=v.dispose)==null?void 0:L.call(v)}):(w=oe==null?void 0:oe.dispose)==null||w.call(oe)}),g.value.remove(re)}),G()}function Ge(){if(xe(),!t.buildings.length)return;const K=new bi;K.userData.isGenerated=!0,g.value.add(K);const re=Math.ceil(Math.sqrt(t.buildings.length)),C=24;t.buildings.forEach((Ee,oe)=>{const ue=new bi;ue.userData={type:"building",name:Ee};const ae=Math.floor(oe/re),w=oe%re;ue.position.set((w-(re-1)/2)*C,0,(ae-(Math.ceil(t.buildings.length/re)-1)/2)*C);const v=t.floors.filter(we=>t.roomLookup.has(`${Ee}@@${we}`)),L=4,Z=v.length||1;v.forEach((we,Fe)=>{const ie=new bi,le=Z-1-Fe;ie.position.y=le*L,ie.userData={type:"floor",building:Ee,name:we,floorYIndex:le};const Re=t.roomLookup.get(`${Ee}@@${we}`)||[],Te=Math.ceil(Math.sqrt(Math.max(Re.length,1))),ge=4.4,Ye=.8,I=Te*(ge+Ye)+1.2,de=Math.ceil(Math.max(Re.length,1)/Te)*(ge+Ye)+1.2,ce=new Yt(new dn(I,.42,de),O.floor.clone());ce.position.y=.18,ce.userData={type:"floorSlab",building:Ee,floor:we,originalMat:O.floor},ie.add(ce),T.push(ce),b.set(`floor_${Ee}_${we}`,ie);const Ae=new Ba(new Oo(new dn(I,.42,de)),new Ns({color:2871295,transparent:!0,opacity:.28}));Ae.position.y=.18,ie.add(Ae),Re.forEach(($,Pe)=>{const Oe=new bi;Oe.userData={type:"room",building:Ee,floor:we,room:$};const ht=Math.floor(Pe/Te),ot=Pe%Te;Oe.position.set((ot-(Te-1)/2)*(ge+Ye),.42,(ht-(Math.ceil(Math.max(Re.length,1)/Te)-1)/2)*(ge+Ye));const Wt=new Yt(new dn(ge,.22,ge),O.room.clone());Wt.position.y=.11,Wt.userData={type:"roomTile",ref:Oe,data:$,floorName:we,originalMat:O.room},Oe.add(Wt),T.push(Wt);const sn=new Ba(new Oo(new dn(ge,.22,ge)),new Ns({color:2871295,transparent:!0,opacity:.26}));sn.position.y=.11,Oe.add(sn);const jn=$.beds.length;if(jn){const Vn=Math.ceil(Math.sqrt(jn)),Di=(ge-.8)/Vn,nr=Math.min(Di*1.5,(ge-.8)/Math.ceil(jn/Vn));$.beds.forEach((Gn,Qn)=>{const mi=be(Gn,Di*.88,nr*.82),ir=Math.floor(Qn/Vn),U=Qn%Vn;mi.position.set((U-(Vn-1)/2)*Di,.26,(ir-(Math.ceil(jn/Vn)-1)/2)*nr),Oe.add(mi)})}const Jn=ne(`${$.roomNo}  ${$.occupiedBeds}/${$.totalBeds}`,"room-label");Jn.position.set(0,1.9,0),Jn.visible=!1,Oe.add(Jn),ie.add(Oe)});const se=ne(we,"floor-label");se.position.set(I/2+1.6,.2,0),se.visible=!1,ie.add(se),ue.add(ie)});const te=Math.max(Z*L,L),j=new Yt(new dn(12,te,12),O.building.clone());j.position.y=te/2,j.userData={type:"buildingHitbox",name:Ee,ref:ue,totalFloors:Z,totalRooms:v.reduce((we,Fe)=>{var ie;return we+(((ie=t.roomLookup.get(`${Ee}@@${Fe}`))==null?void 0:ie.length)||0)},0)},ue.add(j),T.push(j);const Ce=new Ba(new Oo(new dn(12,te,12)),new Ns({color:5756927,transparent:!0,opacity:.58}));Ce.position.y=te/2,ue.add(Ce);const fe=ne(Ee,"building-label");fe.position.set(0,te+2.2,0),ue.add(fe),K.add(ue),b.set(`building_${Ee}`,ue)}),qe()}function qe(){y.value.autoRotate=a.value==="PARK",b.forEach((K,re)=>{if(!re.startsWith("building_"))return;const C=K.userData.name,Ee=K.children.find(ue=>ue.userData.type==="buildingHitbox"),oe=K.children.find(ue=>ue instanceof Yr&&ue.element.className.includes("building-label"));if(a.value==="PARK"){K.visible=!0,Ee&&(Ee.visible=!0),oe&&(oe.visible=!0),K.children.forEach(ue=>{ue.userData.type==="floor"&&(ue.visible=!1,Vi.to(ue.position,{y:ue.userData.originalY??ue.position.y,duration:.5}))});return}if(C!==o.value){K.visible=!1;return}K.visible=!0,Ee&&(Ee.visible=!1),oe&&(oe.visible=!1),K.children.forEach(ue=>{if(ue.userData.type==="floor"){if(ue.userData.originalY=ue.userData.originalY??ue.position.y,a.value==="BUILDING"){ue.visible=!0,Vi.to(ue.position,{y:ue.userData.floorYIndex*8.5,duration:.85,ease:"power2.out"}),ue.children.forEach(ae=>{if(ae.userData.type==="room"){ae.visible=!0;const w=ae.children.find(v=>v instanceof Yr&&v.element.className.includes("room-label"));w&&(w.visible=!0)}ae instanceof Yr&&ae.element.className.includes("floor-label")&&(ae.visible=!1)});return}ue.userData.name===c.value?(ue.visible=!0,Vi.to(ue.position,{y:0,duration:.85,ease:"power2.out"}),ue.children.forEach(ae=>{if(ae.userData.type==="room"){const w=ae.userData.room.roomNo===l.value;ae.visible=a.value!=="ROOM"||w;const v=ae.children.find(L=>L instanceof Yr&&L.element.className.includes("room-label"));v&&(v.visible=a.value==="FLOOR")}ae instanceof Yr&&ae.element.className.includes("floor-label")&&(ae.visible=!1)})):ue.visible=!1}})})}function je(K,re){if(h.value.visible=!0,h.value.x=K.clientX+18,h.value.y=K.clientY+18,re.userData.type==="buildingHitbox"){h.value.title=re.userData.name,h.value.content=`<div class="tt-row"><span>楼层数</span><span class="tt-val cyan">${re.userData.totalFloors} 层</span></div>
      <div class="tt-row"><span>总房间</span><span class="tt-val">${re.userData.totalRooms} 间</span></div>
      <div class="tt-tip">点击展开楼栋内部结构</div>`,u.value=re.userData.name,f.value="楼栋级观察";return}if(re.userData.type==="floorSlab"){h.value.title=`${re.userData.building} / ${re.userData.floor}`,h.value.content=`<div class="tt-row"><span>透视模式</span><span class="tt-val cyan">楼层平铺</span></div>
      <div class="tt-tip">点击进入楼层鸟瞰视图</div>`,u.value=`${re.userData.building} ${re.userData.floor}`,f.value="楼层级观察";return}if(re.userData.type==="roomTile"){const C=re.userData.data;h.value.title=`房间 ${C.roomNo||"-"}`,h.value.content=`<div class="tt-row"><span>容量</span><span class="tt-val">${C.totalBeds} 床</span></div>
      <div class="tt-row"><span>入住</span><span class="tt-val ${C.occupiedBeds>=C.totalBeds?"red":"green"}">${C.occupiedBeds}/${C.totalBeds}</span></div>
      <div class="tt-tip">${a.value==="ROOM"?"再次点击打开房间详情":"点击进入房间视角"}</div>`,u.value=`房间 ${C.roomNo||"-"}`,f.value=`${C.occupiedBeds}/${C.totalBeds} 床位占用`;return}if(re.userData.type==="bed"){const C=re.userData.bed,Ee=C.elderId?`${C.elderName||"已入住"}`:"空床";h.value.title=`床位 ${C.bedNo||"-"}`,h.value.content=`<div class="tt-row"><span>状态</span><span class="tt-val">${Ee}</span></div>
      <div class="tt-row"><span>风险</span><span class="tt-val ${C.riskLevel==="HIGH"?"red":C.riskLevel==="MEDIUM"?"orange":"cyan"}">${C.riskLabel||"正常"}</span></div>
      <div class="tt-row"><span>24h异常</span><span class="tt-val">${C.abnormalVital24hCount||0} 次</span></div>
      <div class="tt-tip">点击查看业务详情</div>`,u.value=`${C.roomNo||"-"} / ${C.bedNo||"-"}`,f.value=C.riskLabel||(C.elderId?"在住监测中":"床位空闲");return}h.value.visible=!1}function ee(K){(K.userData.type==="buildingHitbox"||K.userData.type==="floorSlab")&&(K.material=K.userData.originalMat||O.building),K.userData.type==="roomTile"&&(K.material=K.userData.originalMat||O.room),K.userData.type==="bed"&&(K.material=K.userData.originalMat,K.scale.set(1,1,1))}function he(K){var ue;if(!s.value||!d.value)return;const re=performance.now();if(re-W<32)return;W=re;const C=s.value.getBoundingClientRect();z.x=(K.clientX-C.left)/C.width*2-1,z.y=-((K.clientY-C.top)/C.height)*2+1,F.setFromCamera(z,d.value);const Ee=F.intersectObjects(T,!1).filter(ae=>ae.object.visible);if(B&&B!==((ue=Ee[0])==null?void 0:ue.object)&&(ee(B),B=null),!Ee.length){h.value.visible=!1,p.value&&(p.value.domElement.style.cursor="default");return}const oe=Ee[0].object;je(K,oe),oe!==B&&((oe.userData.type==="buildingHitbox"||oe.userData.type==="floorSlab")&&(oe.userData.originalMat=oe.material,oe.material=O.hover),oe.userData.type==="roomTile"&&(oe.material=O.roomHover),oe.userData.type==="bed"&&(oe.scale.set(1.14,1.14,1.14),A.some(ae=>ae.mesh===oe)||(oe.material=O.hover)),B=oe),p.value.domElement.style.cursor="pointer"}function pe(){if(!B){ke();return}if(B.userData.type==="buildingHitbox"){o.value=B.userData.name,c.value="",l.value="",a.value="BUILDING",qe(),He(B.userData.ref);return}if(B.userData.type==="floorSlab"){c.value=B.userData.floor,l.value="",a.value="FLOOR",qe();const K=b.get(`floor_${o.value}_${c.value}`);K&&He(K,!0);return}if(B.userData.type==="roomTile"){if(a.value==="ROOM"){n("click-room",B.userData.data);return}l.value=B.userData.ref.userData.room.roomNo,a.value="ROOM",qe(),He(B.userData.ref,!1,18);return}B.userData.type==="bed"&&n("click-bed",B.userData.bed)}function He(K,re=!1,C){if(!d.value||!y.value)return;const Ee=new _s().setFromObject(K),oe=Ee.getCenter(new k),ue=Ee.getSize(new k),ae=Math.max(ue.x,ue.y,ue.z),w=d.value.fov*(Math.PI/180),v=C||Math.abs(ae/2/Math.tan(w/2))*1.8,L=re?0:v*.42,Z=re?0:v;Vi.to(d.value.position,{x:oe.x+L,y:re?oe.y+v:oe.y+ue.y/2+10,z:oe.z+Z,duration:1.24,ease:"power3.inOut"}),Vi.to(y.value.target,{x:oe.x,y:oe.y,z:oe.z,duration:1.24,ease:"power3.inOut"})}function ke(){if(a.value==="ROOM"){a.value="FLOOR",l.value="",qe();const K=b.get(`floor_${o.value}_${c.value}`);K&&He(K,!0);return}if(a.value==="FLOOR"){a.value="BUILDING",c.value="",qe();const K=b.get(`building_${o.value}`);K&&He(K);return}a.value==="BUILDING"&&(a.value="PARK",o.value="",c.value="",qe(),ze())}function ze(){!d.value||!y.value||(a.value="PARK",o.value="",c.value="",l.value="",u.value="园区总览",f.value="等待交互",qe(),Vi.to(d.value.position,{x:42,y:38,z:64,duration:1.1,ease:"power3.out"}),Vi.to(y.value.target,{x:0,y:0,z:0,duration:1.1,ease:"power3.out"}))}function Et(){if(!s.value||!d.value||!p.value||!M.value)return;const K=s.value.clientWidth,re=s.value.clientHeight;d.value.aspect=K/re,d.value.updateProjectionMatrix(),p.value.setSize(K,re),M.value.setSize(K,re)}function Je(){S&&window.clearTimeout(S),S=window.setTimeout(()=>{S=0,Ge()},80)}function nt(){P.value=!document.hidden,P.value&&!x&&st()}function st(){var re,C,Ee;if(x=requestAnimationFrame(st),!P.value)return;const K=Date.now()*.0014;(re=y.value)==null||re.update(),A.forEach(oe=>{const ue=oe.mesh.material;oe.type==="pulse"?ue.emissiveIntensity=oe.baseIntensity+(Math.sin(K*5)+1)*.3:oe.type==="blink"?ue.emissiveIntensity=Math.sin(K*7)>0?oe.baseIntensity+.28:oe.baseIntensity*.36:ue.emissiveIntensity=oe.baseIntensity+(Math.sin(K*2.8)+1)*.12}),R&&(R.rotation.y+=8e-4,R.position.y=Math.sin(K*.8)*.6),(C=p.value)==null||C.render(g.value,d.value),(Ee=M.value)==null||Ee.render(g.value,d.value)}return _r(()=>t.buildings,Je,{deep:!0}),_r(()=>t.floors,Je,{deep:!0}),_r(()=>t.roomLookup,Je,{deep:!0}),Eh(()=>{P.value=!document.hidden,V(),document.addEventListener("visibilitychange",nt)}),Th(()=>{var K,re,C,Ee;cancelAnimationFrame(x),x=0,S&&(window.clearTimeout(S),S=0),(K=p.value)==null||K.domElement.removeEventListener("pointermove",he),(re=p.value)==null||re.domElement.removeEventListener("click",pe),window.removeEventListener("resize",Et),document.removeEventListener("visibilitychange",nt),(C=y.value)==null||C.dispose(),(Ee=p.value)==null||Ee.dispose()}),(K,re)=>{const C=en("a-button");return ft(),qt("div",{class:"panorama-container",ref_key:"containerRef",ref:i},[re[7]||(re[7]=q("div",{class:"scene-atmosphere"},null,-1)),re[8]||(re[8]=q("div",{class:"scene-scanlines"},null,-1)),q("div",_M,[q("div",gM,[a.value!=="PARK"?(ft(),fn(C,{key:0,class:"tech-btn",onClick:ke},{default:Ve(()=>[...re[0]||(re[0]=[et("返回上级",-1)])]),_:1})):Tn("",!0),Be(C,{class:"tech-btn",onClick:ze},{default:Ve(()=>[...re[1]||(re[1]=[et("重置视角",-1)])]),_:1})]),q("div",vM,[re[2]||(re[2]=q("span",{class:"toolbar-eyebrow"},"Current Scope",-1)),q("strong",null,Ie(m.value),1)]),re[3]||(re[3]=od('<div class="toolbar-right" data-v-6c1681e7><div class="legend-chip status-normal" data-v-6c1681e7>正常</div><div class="legend-chip status-occupied" data-v-6c1681e7>在床</div><div class="legend-chip status-sleep" data-v-6c1681e7>睡眠</div><div class="legend-chip status-alert" data-v-6c1681e7>告警</div><div class="legend-chip status-offline" data-v-6c1681e7>离线</div></div>',1))]),q("div",xM,[q("div",MM,[re[4]||(re[4]=q("span",{class:"hud-label"},"当前层级",-1)),q("strong",null,Ie(_.value),1)]),q("div",SM,[re[5]||(re[5]=q("span",{class:"hud-label"},"聚焦对象",-1)),q("strong",null,Ie(u.value),1)]),q("div",yM,[re[6]||(re[6]=q("span",{class:"hud-label"},"运行状态",-1)),q("strong",null,Ie(f.value),1)])]),ld(q("div",{class:"tech-tooltip",style:Ah({left:`${h.value.x}px`,top:`${h.value.y}px`})},[q("div",bM,Ie(h.value.title),1),q("div",{class:"tt-body",innerHTML:h.value.content},null,8,EM)],4),[[cd,h.value.visible]]),q("div",{ref_key:"canvasRef",ref:s,class:"canvas-wrapper"},null,512)],512)}}}),AM=wh(TM,[["__scopeId","data-v-6c1681e7"]]),wM={class:"cockpit-topbar"},RM={class:"topbar-status"},CM={class:"topbar-clock"},PM={class:"clock-date"},DM={class:"clock-time"},LM={class:"hero-metrics"},IM={class:"metric-label"},UM={class:"metric-value"},NM={class:"metric-meta"},FM={class:"bed-cockpit-shell"},OM={class:"cockpit-panel panel-left"},BM={class:"tech-panel"},kM={class:"metric-grid"},zM={class:"distribution-list"},VM={class:"distribution-top"},GM={class:"distribution-track"},HM={class:"tech-panel"},WM={class:"filter-stack"},XM={class:"field-block"},qM={class:"field-block"},YM={class:"field-block"},$M={class:"field-inline"},KM={class:"field-block"},ZM={class:"selection-tags"},jM={key:2,class:"field-tip"},JM={class:"command-buttons"},QM={class:"tech-panel"},eS={class:"focus-bed-list"},tS={class:"cockpit-core"},nS={class:"tech-panel stage-panel"},iS={class:"stage-heading"},rS={class:"stage-kpis"},sS={class:"stage-shell"},aS={class:"stage-overlay-card"},oS={class:"overlay-name"},lS={class:"overlay-meta"},cS={key:0,class:"overlay-tags"},uS={class:"overlay-chip"},hS={key:0,class:"overlay-chip"},fS={class:"overlay-chip"},dS={class:"chart-grid"},pS={class:"tech-panel chart-panel"},mS={class:"tech-panel chart-panel"},_S={class:"tech-panel chart-panel"},gS={class:"tech-panel chart-panel"},vS={class:"cockpit-panel panel-right"},xS={class:"tech-panel"},MS={class:"event-list"},SS={class:"event-top"},yS={class:"event-type"},bS={class:"event-time"},ES={class:"tech-panel"},TS={class:"timeline-list"},AS={class:"tech-panel selected-panel"},wS={key:0,class:"selected-summary"},RS={class:"selected-title"},CS={class:"selected-meta"},PS={class:"selected-badges"},DS={class:"overlay-chip"},LS={key:0,class:"overlay-chip"},IS={class:"overlay-chip"},US={class:"action-grid"},NS=bh({__name:"BedPanorama",setup(r){const e=hd(),t=ud(),n=pt([]),i=pt({}),s=pt({}),a=pt(""),o=pt(null),c=pt(!1),l=pt(!1),u=pt(null),f=pt([]),h=pt(!1),m=pt("ALL"),_=pt(!1),g=pt("ALL"),d=pt(!1),p=pt(!1),M=pt(""),y=pt(""),b=pt(ta()),T=[{label:"全部",value:"ALL"},{label:"仅空床",value:"IDLE"},{label:"仅入住",value:"OCCUPIED"}],A=[{label:"全部风险",value:"ALL"},{label:"高风险",value:"HIGH"},{label:"中风险",value:"MEDIUM"},{label:"低风险",value:"LOW"}],R=["bedBuilding","bedFloor","bedKeyword","bedQuick","bedRiskEnabled","bedRiskLevel"],x=pt(""),S=pt(!1);let W=null;const P=dt(()=>{const U=String(t.query.source||"").trim().toLowerCase(),D=String(t.query.scene||"").trim().toLowerCase(),ve=U==="lifecycle"||D==="status-change";return{active:ve,message:ve?"当前来自入住状态变更联动，可在床态视图快速确认清洁/维修与空床调配。":""}}),z=dt(()=>n.value.filter(U=>{if(a.value){const D=`${U.roomNo||""} ${U.elderName||""}`.toLowerCase(),ve=a.value.toLowerCase();if(!D.includes(ve))return!1}return!0})),F=dt(()=>{const U=z.value.filter(D=>m.value==="IDLE"?Te(D):m.value==="OCCUPIED"?!!D.elderId:!0);return!_.value||!p.value?U:U.filter(D=>D.riskLevel?g.value==="ALL"?!0:D.riskLevel===g.value:!1)}),B=dt(()=>F.value.filter(U=>!(M.value&&String(U.building||"")!==M.value||y.value&&String(U.floorNo||"")!==y.value))),O=dt(()=>{const U=new Set;return B.value.forEach(D=>U.add((D.building||"未分配楼栋").trim()||"未分配楼栋")),Array.from(U).sort((D,ve)=>D.localeCompare(ve,"zh-CN"))}),V=dt(()=>O.value),G=dt(()=>{const U=new Set;return B.value.forEach(D=>U.add((D.floorNo||"未知楼层").trim()||"未知楼层")),Array.from(U).sort((D,ve)=>{const Ue=we(ve)-we(D);return Ue!==0?Ue:String(D).localeCompare(String(ve),"zh-CN")})}),ne=dt(()=>G.value),Q=dt(()=>{const U={idle:0,reserved:0,occupied:0,maintenance:0,cleaning:0,locked:0};return n.value.forEach(D=>{const ve=le(D);ve==="空闲"&&(U.idle+=1),ve==="预定"&&(U.reserved+=1),ve==="在住"&&(U.occupied+=1),ve==="维修"&&(U.maintenance+=1),ve==="清洁中"&&(U.cleaning+=1),ve==="锁定"&&(U.locked+=1)}),U}),me=dt(()=>[...F.value].filter(U=>ge(U)).sort((U,D)=>+(D.riskLevel==="HIGH")-+(U.riskLevel==="HIGH")||Number(D.abnormalVital24hCount||0)-Number(U.abnormalVital24hCount||0))),be=dt(()=>me.value.filter(U=>U.riskLevel==="HIGH"||U.status===0).length),xe=dt(()=>n.value.length?Math.round(Q.value.occupied/n.value.length*100):0),Ge=dt(()=>n.value.length?Math.max(0,Math.round((n.value.length-Q.value.locked-Q.value.maintenance)/n.value.length*100)):100),qe=dt(()=>n.value.filter(U=>U.elderId&&U.riskLevel!=="HIGH"&&Number(U.abnormalVital24hCount||0)===0).length),je=dt(()=>{const U=new Map,D=new Map;return B.value.forEach(ve=>{const Ue=(ve.building||"未分配楼栋").trim()||"未分配楼栋",tt=(ve.floorNo||"未知楼层").trim()||"未知楼层",Mn=(ve.roomNo||`房间-${ve.roomId||"-"}`).trim()||`房间-${ve.roomId||"-"}`,bt=`${Ue}@@${tt}@@${Mn}`;D.has(bt)||D.set(bt,[]),D.get(bt).push(ve)}),D.forEach((ve,Ue)=>{var Me,Se;const[tt,Mn,bt]=Ue.split("@@"),Hn=`${tt}@@${Mn}`;U.has(Hn)||U.set(Hn,[]);const Rr=String(((Me=ve[0])==null?void 0:Me.roomId)||""),Li=[...ve].sort((_e,De)=>{const J=Te(_e)?0:1,We=Te(De)?0:1;return J!==We?J-We:String(_e.bedNo||"").localeCompare(String(De.bedNo||""))}),Ct=ve.length,E=ve.filter(_e=>!!_e.elderId).length,N=ve.filter(_e=>!!_e.elderName).length,Y=Ct-E,X=s.value[Rr]||Ct||1,H=X>=3?2:1;U.get(Hn).push({key:Ue,roomNo:bt,roomType:Re(i.value[Rr]||"标准间"),capacity:X,autoSpan:H,beds:Li,totalBeds:Ct,occupiedBeds:E,elderCount:N,emptyBeds:Y,remark:(Se=ve[0])==null?void 0:Se.roomRemark})}),U.forEach(ve=>{ve.sort((Ue,tt)=>Ue.capacity!==tt.capacity?tt.capacity-Ue.capacity:Ue.emptyBeds!==tt.emptyBeds?tt.emptyBeds-Ue.emptyBeds:String(Ue.roomNo).localeCompare(String(tt.roomNo)))}),U}),ee=dt(()=>b.value.format("YYYY年MM月DD日 dddd")),he=dt(()=>b.value.format("HH:mm:ss")),pe=dt(()=>[{label:"总床位数",value:`${n.value.length}`,meta:`${V.value.length} 栋 ${ne.value.length} 层`,tone:"tone-cyan"},{label:"已占用床位",value:`${Q.value.occupied}`,meta:`占用率 ${xe.value}%`,tone:"tone-green"},{label:"紧急告警数",value:`${be.value}`,meta:`${me.value.length} 个重点关注`,tone:"tone-red"},{label:"设备在线率",value:`${Ge.value}%`,meta:`可监测床位 ${Math.max(0,n.value.length-Q.value.maintenance)}`,tone:"tone-blue"}]),He=dt(()=>[{label:"空床位",value:Q.value.idle,meta:"待分配",tone:"tone-cyan"},{label:"在住床位",value:Q.value.occupied,meta:"实时监测",tone:"tone-green"},{label:"异常床位",value:me.value.length,meta:"需要关注",tone:"tone-orange"},{label:"锁定/离线",value:Q.value.locked,meta:"弱化显示",tone:"tone-gray"},{label:"维修床位",value:Q.value.maintenance,meta:"需恢复",tone:"tone-orange"},{label:"睡眠稳定",value:qe.value,meta:"低异常波动",tone:"tone-purple"}]),ke=dt(()=>{const U=Math.max(1,n.value.length);return[{label:"空闲",value:Q.value.idle,percent:Math.round(Q.value.idle/U*100),tone:"fill-cyan"},{label:"在住",value:Q.value.occupied,percent:Math.round(Q.value.occupied/U*100),tone:"fill-green"},{label:"预定",value:Q.value.reserved,percent:Math.round(Q.value.reserved/U*100),tone:"fill-blue"},{label:"清洁/维修",value:Q.value.cleaning+Q.value.maintenance,percent:Math.round((Q.value.cleaning+Q.value.maintenance)/U*100),tone:"fill-orange"},{label:"锁定",value:Q.value.locked,percent:Math.round(Q.value.locked/U*100),tone:"fill-red"}]}),ze=dt(()=>(me.value.length?me.value:F.value.filter(D=>D.elderId)).slice(0,4)),Et=dt(()=>me.value.slice(0,6).map((U,D)=>({key:`alert-${U.id}`,type:U.riskLevel==="HIGH"?"紧急告警":"实时异常",time:ta().subtract(D*4,"minute").format("HH:mm"),title:`${U.roomNo||"-"} / ${U.bedNo||"-"} ${U.elderName||"空床"}`,description:`${U.riskLabel||"体征波动"}，24h异常 ${U.abnormalVital24hCount||0} 次，当前状态 ${le(U)}`,tone:U.riskLevel==="HIGH"?"tone-red":"tone-orange"}))),Je=dt(()=>{const U=V.value.slice(0,2).map((ve,Ue)=>({key:`building-${ve}`,title:`${ve} 监测刷新`,description:`${ue(ve,ne.value[Ue]||ne.value[0]||"").length} 个房间已完成床态同步`,tone:"dot-cyan"})),D=Array.from(je.value.values()).flat().slice(0,3).map(ve=>({key:ve.key,title:`${ve.roomNo} 房间态势更新`,description:`在住 ${ve.occupiedBeds} / ${ve.totalBeds}，空床 ${ve.emptyBeds}，房型 ${ve.roomType}`,tone:ve.emptyBeds===0?"dot-orange":"dot-green"}));return[...U,...D]}),nt=dt(()=>Array.from({length:7},(U,D)=>ta().subtract(6-D,"day").format("MM/DD")));function st(U,D){return D.map((ve,Ue)=>{const tt=Math.sin((Ue+1)*.9)*Math.max(1,U*.05);return Math.max(0,Math.round(U*ve+tt))})}const K=dt(()=>oe({labels:nt.value,data:st(Q.value.occupied,[.84,.88,.9,.94,.95,.97,1]),color:"#52f3c4",areaColor:"rgba(62, 232, 181, 0.22)"})),re=dt(()=>oe({labels:nt.value,data:st(qe.value,[.76,.8,.82,.85,.88,.92,1]),color:"#9e88ff",areaColor:"rgba(155, 123, 255, 0.22)"})),C=dt(()=>oe({labels:nt.value,data:st(Math.max(1,me.value.length),[1.34,1.2,1.12,.92,.98,1.05,1]),color:"#ff6d89",areaColor:"rgba(255, 93, 124, 0.2)"})),Ee=dt(()=>oe({labels:nt.value,data:st(Ge.value,[.91,.92,.94,.95,.97,.98,1]),color:"#57d7ff",areaColor:"rgba(87, 215, 255, 0.2)",formatter:"{value}%"}));function oe(U){return{animationDuration:900,animationEasing:"cubicOut",grid:{left:14,right:18,top:24,bottom:22,containLabel:!0},tooltip:{trigger:"axis",backgroundColor:"rgba(6, 18, 34, 0.92)",borderColor:"rgba(87, 215, 255, 0.3)",textStyle:{color:"#eaf7ff"}},xAxis:{type:"category",boundaryGap:!1,data:U.labels,axisLine:{lineStyle:{color:"rgba(120, 164, 201, 0.25)"}},axisLabel:{color:"#7fa2bf",fontSize:11}},yAxis:{type:"value",axisLine:{show:!1},splitLine:{lineStyle:{color:"rgba(120, 164, 201, 0.12)"}},axisLabel:{color:"#7fa2bf",fontSize:11,formatter:U.formatter||"{value}"}},series:[{type:"line",smooth:!0,symbol:"circle",symbolSize:7,data:U.data,lineStyle:{width:2,color:U.color,shadowBlur:14,shadowColor:U.color},itemStyle:{color:U.color,borderColor:"#ffffff",borderWidth:1},areaStyle:{color:U.areaColor}}]}}function ue(U,D){return je.value.get(`${U}@@${D}`)||[]}function ae(){M.value="",y.value=""}function w(){a.value="",m.value="ALL",_.value=!1,g.value="ALL",ae()}function v(U){return Array.isArray(U)?v(U[0]):U==null?"":String(U).trim()}function L(U){return Object.entries(U||{}).reduce((D,[ve,Ue])=>{const tt=v(Ue);return tt&&(D[ve]=tt),D},{})}function Z(U){return[...R].sort().map(D=>`${D}:${v(U[D])}`).join("|")}function te(){a.value=v(t.query.bedKeyword);const U=v(t.query.bedQuick).toUpperCase();m.value=U==="IDLE"||U==="OCCUPIED"?U:"ALL",_.value=v(t.query.bedRiskEnabled)==="1";const D=v(t.query.bedRiskLevel).toUpperCase();g.value=D==="HIGH"||D==="MEDIUM"||D==="LOW"?D:"ALL",M.value=v(t.query.bedBuilding),y.value=v(t.query.bedFloor)}function j(){const U={};return Object.entries(L(t.query)).forEach(([D,ve])=>{R.includes(D)||(U[D]=ve)}),a.value.trim()&&(U.bedKeyword=a.value.trim()),U.bedQuick=m.value,_.value&&(U.bedRiskEnabled="1"),_.value&&g.value!=="ALL"&&(U.bedRiskLevel=g.value),M.value&&(U.bedBuilding=M.value),y.value&&(U.bedFloor=y.value),U}function Ce(U){const D=L(t.query),ve=Object.keys(D),Ue=Object.keys(U);return ve.length!==Ue.length?!1:Ue.every(tt=>D[tt]===U[tt])}async function fe(){const U=j();Ce(U)||(S.value=!0,x.value=Z(U),await e.replace({path:t.path,query:U}))}function we(U){const D=String(U||"").trim();if(!D)return-999;const ve=D.replace(/\s+/g,"").toUpperCase();if(/^(ROOF|RF|屋顶|天台)$/.test(ve))return 999;const Ue=ve.match(/(?:地下|负|B)([0-9]+|[一二三四五六七八九十百两]+)/);if(Ue)return-Fe(Ue[1]);const tt=ve.match(/([0-9]+|[一二三四五六七八九十百两]+)(?:F|楼|层)?/);return tt?Fe(tt[1]):-999}function Fe(U){if(/^\d+$/.test(U))return Number(U);const D=ie(U);return D>0?D:0}function ie(U){const D=U.split(""),ve={零:0,一:1,二:2,两:2,三:3,四:4,五:5,六:6,七:7,八:8,九:9};if(!D.length)return 0;if(D.length===1)return ve[D[0]]??0;let Ue=0,tt=0;return D.forEach(Mn=>{if(ve[Mn]!==void 0){tt=ve[Mn];return}if(Mn==="十"){Ue+=(tt||1)*10,tt=0;return}Mn==="百"&&(Ue+=(tt||1)*100,tt=0)}),Ue+tt}function le(U){return U.elderId?"在住":U.status===0?"锁定":U.status===2?"维修":U.status===3?"清洁中":String(U.bedNo||"").endsWith("R")?"预定":"空闲"}function Re(U){const D=String(U||"").trim();if(!D)return"-";const ve=D.toUpperCase();return{1:"单人间",2:"双人间",3:"三人间",SINGLE:"单人间",DOUBLE:"双人间",TRIPLE:"三人间",ONE:"单人间",TWO:"双人间",THREE:"三人间",STANDARD:"标准间",STANDARD_ROOM:"标准间",DELUXE:"豪华间",VIP:"VIP房",SUITE:"套间"}[ve]||D}function Te(U){return!U.elderId&&le(U)==="空闲"}function ge(U){return U.riskLevel==="HIGH"||U.status===0||Number(U.abnormalVital24hCount||0)>0}function Ye(U){return U.riskLabel||""}function I(U){const D=U.latestAssessmentLevel||"-",ve=U.latestAssessmentDate||"-";return D==="-"&&ve==="-"?"-":`${D} / ${ve}`}function de(U){o.value=U,u.value=null,c.value=!0}function ce(U){if(!U)return[];try{const D=JSON.parse(U);return(Array.isArray(D==null?void 0:D.slots)?D.slots:[D==null?void 0:D.remark1,D==null?void 0:D.remark2,D==null?void 0:D.remark3]).map((Ue,tt)=>Ue?typeof Ue=="string"?{text:Ue,visible:!0,index:tt}:{text:String(Ue.text||Ue.value||"").trim(),visible:Ue.visible!==!1,index:tt}:null).filter(Ue=>Ue&&Ue.text)}catch{return[{text:U,visible:!0,index:0}]}}function Ae(U){return ce(U).filter(D=>D.visible).map(D=>D.text).join("；")}function se(U){return ce(U).map(D=>D.text).join("；")}async function $(U){u.value=U,o.value=null,l.value=!0;const D=Array.from(new Set(U.beds.map(ve=>ve.elderId).filter(Boolean)));if(!D.length){f.value=[];return}h.value=!0;try{const ve=await Promise.allSettled(D.map(Ue=>_d(Ue)));f.value=ve.filter(Ue=>Ue.status==="fulfilled").map(Ue=>Ue.value)}finally{h.value=!1}}function Pe(U){l.value=!1,e.push(`/elder/detail/${U}`)}function Oe(U){e.push(`/finance/bills/in-resident?elderId=${U}`)}function ht(){var D;const U=String(((D=o.value)==null?void 0:D.elderId)||"").trim();return U||(xs.warning("当前床位未关联长者"),"")}function ot(){const U=ht();U&&(c.value=!1,e.push(`/elder/assessment/ability/archive?elderId=${encodeURIComponent(U)}`))}function Wt(){const U=ht();U&&(c.value=!1,e.push(`/elder/contracts-invoices?elderId=${encodeURIComponent(U)}`))}function sn(){var D;c.value=!1;const U=String(((D=o.value)==null?void 0:D.elderId)||"").trim();e.push({path:"/elder/status-change",query:U?{residentId:U}:void 0})}function jn(){var U;if(!((U=o.value)!=null&&U.elderId)){xs.warning("当前是空床位，请先分配床位");return}c.value=!1,e.push(`/elder/detail/${o.value.elderId}`)}function Jn(){if(!o.value){xs.warning("请先选择床位");return}c.value=!1,e.push(`/elder/admission-processing?bedId=${o.value.id}`)}function Vn(){if(!o.value){xs.warning("请先选择床位");return}c.value=!1,xs.success("已生成提醒并推送到提醒中心/任务中心"),e.push("/oa/work-execution/task?category=alert")}function Di(){var U;c.value=!1,e.push(`/care/workbench/qr?bedId=${((U=o.value)==null?void 0:U.id)||""}`)}function nr(){e.push("/logistics/assets/room-state-map")}function Gn(){e.push("/logistics/assets/bed-management")}async function Qn(U=!1){n.value=await pd({params:{includeRisk:U}}),U?p.value=!0:p.value=!1}async function mi(){if(!(p.value||d.value)){d.value=!0;try{await Qn(!0)}finally{d.value=!1}}}async function ir(){const U=await md(),D={},ve={};U.forEach(Ue=>{const tt=String(Ue.id);D[tt]=Ue.roomType||"标准间",ve[tt]=Number(Ue.capacity||1)}),i.value=D,s.value=ve}return gd({topics:["elder","bed","lifecycle","finance","care","dining"],refresh:async()=>{await Promise.all([Qn(!1),ir()]),_.value&&await mi()}}),Eh(async()=>{te(),x.value=Z(t.query),W=window.setInterval(()=>{b.value=ta()},1e3),await Promise.all([Qn(!1),ir()]),_.value&&await mi(),await fe().catch(()=>{})}),Th(()=>{W&&window.clearInterval(W)}),_r(_,U=>{U&&mi().catch(()=>{})}),_r(F,()=>{M.value&&!F.value.some(U=>String(U.building||"")===M.value)&&(M.value=""),y.value&&!F.value.some(U=>String(U.floorNo||"")===y.value)&&(y.value="")}),_r([a,m,_,g,M,y],()=>{fe().catch(()=>{})}),_r(()=>t.query,U=>{const D=Z(U);if(S.value){S.value=!1,x.value=D;return}D!==x.value&&(x.value=D,te())},{deep:!0}),(U,D)=>{const ve=en("a-input-search"),Ue=en("a-segmented"),tt=en("a-switch"),Mn=en("a-tag"),bt=en("a-button"),Hn=en("a-empty"),Rr=en("a-alert"),Li=en("v-chart"),Ct=en("a-descriptions-item"),E=en("a-descriptions"),N=en("a-space"),Y=en("a-modal"),X=en("a-avatar"),H=en("a-table-column"),Me=en("a-table");return ft(),fn(fd,{class:"bed-cockpit-page",title:"智慧床态全景指挥中心",subTitle:"3D床态全景 / 智慧养老数字孪生驾驶舱"},{extra:Ve(()=>[q("div",wM,[q("div",RM,[D[6]||(D[6]=q("span",{class:"status-dot"},null,-1)),D[7]||(D[7]=q("span",null,"系统运行正常",-1)),D[8]||(D[8]=q("span",{class:"status-divider"},null,-1)),q("span",null,Ie(P.value.active?"入住联动模式":"床态实时监控"),1)]),q("div",CM,[q("div",PM,Ie(ee.value),1),q("div",DM,Ie(he.value),1)])])]),stats:Ve(()=>[q("div",LM,[(ft(!0),qt(Lr,null,Dr(pe.value,Se=>(ft(),qt("div",{key:Se.label,class:Ms(["hero-metric-card",Se.tone])},[q("span",IM,Ie(Se.label),1),q("strong",UM,Ie(Se.value),1),q("span",NM,Ie(Se.meta),1)],2))),128))])]),default:Ve(()=>{var Se,_e,De;return[q("div",FM,[q("aside",OM,[q("section",BM,[D[9]||(D[9]=q("div",{class:"section-head"},[q("div",null,[q("h3",null,"运行总览"),q("p",null,"床位、告警与设备态势一屏掌握")])],-1)),q("div",kM,[(ft(!0),qt(Lr,null,Dr(He.value,J=>(ft(),qt("div",{key:J.label,class:Ms(["metric-tile",J.tone])},[q("span",null,Ie(J.label),1),q("strong",null,Ie(J.value),1),q("small",null,Ie(J.meta),1)],2))),128))]),q("div",zM,[(ft(!0),qt(Lr,null,Dr(ke.value,J=>(ft(),qt("div",{key:J.label,class:"distribution-item"},[q("div",VM,[q("span",null,Ie(J.label),1),q("strong",null,Ie(J.value),1)]),q("div",GM,[q("span",{class:Ms(["distribution-fill",J.tone]),style:Ah({width:`${J.percent}%`})},null,6)])]))),128))])]),q("section",HM,[D[17]||(D[17]=q("div",{class:"section-head"},[q("div",null,[q("h3",null,"指挥筛选"),q("p",null,"筛选条件仍与原业务参数保持同步")])],-1)),q("div",WM,[q("label",XM,[D[10]||(D[10]=q("span",{class:"field-label"},"搜索目标",-1)),Be(ve,{value:a.value,"onUpdate:value":D[0]||(D[0]=J=>a.value=J),placeholder:"搜索房间号 / 长者名","allow-clear":"",size:"large"},null,8,["value"])]),q("label",qM,[D[11]||(D[11]=q("span",{class:"field-label"},"床位筛选",-1)),Be(Ue,{value:m.value,"onUpdate:value":D[1]||(D[1]=J=>m.value=J),options:T,block:""},null,8,["value"])]),q("div",YM,[q("div",$M,[D[12]||(D[12]=q("span",{class:"field-label"},"风险筛选",-1)),Be(tt,{checked:_.value,"onUpdate:checked":D[2]||(D[2]=J=>_.value=J)},null,8,["checked"])]),_.value?(ft(),fn(Ue,{key:0,value:g.value,"onUpdate:value":D[3]||(D[3]=J=>g.value=J),options:A,block:""},null,8,["value"])):Tn("",!0)]),q("div",KM,[D[13]||(D[13]=q("span",{class:"field-label"},"视角聚焦",-1)),q("div",ZM,[M.value?(ft(),fn(Mn,{key:0,color:"blue"},{default:Ve(()=>[et("楼栋："+Ie(M.value),1)]),_:1})):Tn("",!0),y.value?(ft(),fn(Mn,{key:1,color:"cyan"},{default:Ve(()=>[et("楼层："+Ie(y.value),1)]),_:1})):Tn("",!0),!M.value&&!y.value?(ft(),qt("span",jM,"点击场景中的楼栋、楼层可快速聚焦")):Tn("",!0)])]),q("div",JM,[Be(bt,{onClick:nr},{default:Ve(()=>[...D[14]||(D[14]=[et("房态视图",-1)])]),_:1}),Be(bt,{onClick:Gn},{default:Ve(()=>[...D[15]||(D[15]=[et("床位管理",-1)])]),_:1}),Be(bt,{type:"primary",onClick:w},{default:Ve(()=>[...D[16]||(D[16]=[et("重置",-1)])]),_:1})])])]),q("section",QM,[D[18]||(D[18]=q("div",{class:"section-head"},[q("div",null,[q("h3",null,"重点床位"),q("p",null,"优先关注告警与重点照护对象")])],-1)),q("div",eS,[(ft(!0),qt(Lr,null,Dr(ze.value,J=>(ft(),fn(dd,{key:J.id,bed:J,onClick:We=>de(J)},null,8,["bed","onClick"]))),128)),ze.value.length?Tn("",!0):(ft(),fn(Hn,{key:0,description:"当前没有重点床位"}))])])]),q("main",tS,[q("section",nS,[q("div",iS,[D[22]||(D[22]=q("div",null,[q("div",{class:"eyebrow"},"3D Bed State Panorama"),q("h2",null,"空间透视床态总览"),q("p",null,"中台接口与床位绑定逻辑保持不变，只重构视觉层、结构层与场景表现。")],-1)),q("div",rS,[q("div",null,[D[19]||(D[19]=q("span",null,"监测楼栋",-1)),q("strong",null,Ie(V.value.length),1)]),q("div",null,[D[20]||(D[20]=q("span",null,"监测楼层",-1)),q("strong",null,Ie(ne.value.length),1)]),q("div",null,[D[21]||(D[21]=q("span",null,"实时告警",-1)),q("strong",null,Ie(be.value),1)])])]),P.value.active?(ft(),fn(Rr,{key:0,type:"info","show-icon":"",class:"lifecycle-alert",message:P.value.message},null,8,["message"])):Tn("",!0),q("div",sS,[V.value.length&&ne.value.length?(ft(),fn(AM,{key:0,buildings:V.value,floors:ne.value,"room-lookup":je.value,onClickRoom:$,onClickBed:de},null,8,["buildings","floors","room-lookup"])):(ft(),fn(Hn,{key:1,class:"stage-empty",description:"暂无床位数据"})),q("div",aS,[D[23]||(D[23]=q("div",{class:"overlay-title"},"选中焦点",-1)),q("div",oS,Ie(((Se=o.value)==null?void 0:Se.elderName)||((_e=u.value)==null?void 0:_e.roomNo)||"等待选择床位 / 房间"),1),q("div",lS,Ie(o.value?`${o.value.building||"-"} / ${o.value.floorNo||"-"} / ${o.value.roomNo||"-"} / ${o.value.bedNo||"-"}`:u.value?`${u.value.roomType} · ${u.value.capacity||0}床`:"点击场景中的楼栋、楼层、房间或床位进行联动分析"),1),o.value?(ft(),qt("div",cS,[q("span",uS,Ie(le(o.value)),1),o.value.riskLabel?(ft(),qt("span",hS,Ie(o.value.riskLabel),1)):Tn("",!0),q("span",fS,"异常 "+Ie(o.value.abnormalVital24hCount||0),1)])):Tn("",!0)])])]),q("section",dS,[q("div",pS,[D[24]||(D[24]=q("div",{class:"section-head compact"},[q("div",null,[q("h3",null,"床位占用趋势"),q("p",null,"根据当前床位状态推演近 7 日变化")])],-1)),Be(Li,{class:"chart-view",option:K.value,autoresize:""},null,8,["option"])]),q("div",mS,[D[25]||(D[25]=q("div",{class:"section-head compact"},[q("div",null,[q("h3",null,"睡眠质量趋势"),q("p",null,"基于在住与低风险床位的稳定度估算")])],-1)),Be(Li,{class:"chart-view",option:re.value,autoresize:""},null,8,["option"])]),q("div",_S,[D[26]||(D[26]=q("div",{class:"section-head compact"},[q("div",null,[q("h3",null,"告警趋势"),q("p",null,"高风险、异常体征与锁定状态综合观察")])],-1)),Be(Li,{class:"chart-view",option:C.value,autoresize:""},null,8,["option"])]),q("div",gS,[D[27]||(D[27]=q("div",{class:"section-head compact"},[q("div",null,[q("h3",null,"设备健康趋势"),q("p",null,"根据可监测床位占比生成设备健康视图")])],-1)),Be(Li,{class:"chart-view",option:Ee.value,autoresize:""},null,8,["option"])])])]),q("aside",vS,[q("section",xS,[D[28]||(D[28]=q("div",{class:"section-head"},[q("div",null,[q("h3",null,"实时告警流"),q("p",null,"风险事件、体征异常与设备状态联动")])],-1)),q("div",MS,[(ft(!0),qt(Lr,null,Dr(Et.value,J=>(ft(),qt("div",{key:J.key,class:Ms(["event-card",J.tone])},[q("div",SS,[q("span",yS,Ie(J.type),1),q("span",bS,Ie(J.time),1)]),q("strong",null,Ie(J.title),1),q("p",null,Ie(J.description),1)],2))),128)),Et.value.length?Tn("",!0):(ft(),fn(Hn,{key:0,description:"暂无告警流"}))])]),q("section",ES,[D[29]||(D[29]=q("div",{class:"section-head"},[q("div",null,[q("h3",null,"最新状态变化"),q("p",null,"房间、楼层和床位视角的即时动态")])],-1)),q("div",TS,[(ft(!0),qt(Lr,null,Dr(Je.value,J=>(ft(),qt("div",{key:J.key,class:"timeline-item"},[q("span",{class:Ms(["timeline-dot",J.tone])},null,2),q("div",null,[q("strong",null,Ie(J.title),1),q("p",null,Ie(J.description),1)])]))),128))])]),q("section",AS,[D[36]||(D[36]=q("div",{class:"section-head"},[q("div",null,[q("h3",null,"指挥操作台"),q("p",null,"围绕当前床位执行业务动作")])],-1)),o.value?(ft(),qt("div",wS,[q("div",RS,Ie(o.value.bedNo||"-")+" / "+Ie(o.value.elderName||"空床"),1),q("div",CS,Ie(o.value.building||"-")+" · "+Ie(o.value.floorNo||"-")+" · "+Ie(o.value.roomNo||"-"),1),q("div",PS,[q("span",DS,Ie(le(o.value)),1),o.value.riskLabel?(ft(),qt("span",LS,Ie(o.value.riskLabel),1)):Tn("",!0),q("span",IS,Ie(o.value.careLevel||"未配置护理等级"),1)])])):(ft(),fn(Hn,{key:1,description:"点击床位后显示详情与快捷操作"})),q("div",US,[Be(bt,{block:"",type:"primary",onClick:jn},{default:Ve(()=>[...D[30]||(D[30]=[et("长者档案",-1)])]),_:1}),Be(bt,{block:"",onClick:Jn},{default:Ve(()=>[...D[31]||(D[31]=[et("床位分配",-1)])]),_:1}),Be(bt,{block:"",onClick:ot},{default:Ve(()=>[...D[32]||(D[32]=[et("评估档案",-1)])]),_:1}),Be(bt,{block:"",onClick:Wt},{default:Ve(()=>[...D[33]||(D[33]=[et("合同票据",-1)])]),_:1}),Be(bt,{block:"",onClick:sn},{default:Ve(()=>[...D[34]||(D[34]=[et("状态中心",-1)])]),_:1}),Be(bt,{block:"",danger:"",ghost:"",onClick:Vn},{default:Ve(()=>[...D[35]||(D[35]=[et("生成提醒",-1)])]),_:1})])])])]),Be(Y,{open:c.value,"onUpdate:open":D[4]||(D[4]=J=>c.value=J),title:"床位详情",width:"560px",footer:null,"destroy-on-close":""},{default:Ve(()=>[o.value?(ft(),fn(E,{key:0,column:1,size:"small",bordered:""},{default:Ve(()=>[Be(Ct,{label:"床位"},{default:Ve(()=>[et(Ie(o.value.bedNo||"-"),1)]),_:1}),Be(Ct,{label:"楼栋/楼层/房间"},{default:Ve(()=>[et(Ie(o.value.building||"-")+" / "+Ie(o.value.floorNo||"-")+" / "+Ie(o.value.roomNo||"-"),1)]),_:1}),Be(Ct,{label:"在住长者"},{default:Ve(()=>[et(Ie(o.value.elderName||"-"),1)]),_:1}),Be(Ct,{label:"护理等级"},{default:Ve(()=>[et(Ie(o.value.careLevel||"-"),1)]),_:1}),Be(Ct,{label:"风险级别"},{default:Ve(()=>[et(Ie(Ye(o.value)||"无"),1)]),_:1}),Be(Ct,{label:"风险来源"},{default:Ve(()=>[et(Ie(o.value.riskSource||"-"),1)]),_:1}),Be(Ct,{label:"最近评估"},{default:Ve(()=>[et(Ie(I(o.value)),1)]),_:1}),Be(Ct,{label:"24h异常体征"},{default:Ve(()=>[et(Ie(o.value.abnormalVital24hCount||0)+" 次",1)]),_:1}),Be(Ct,{label:"今日任务"},{default:Ve(()=>[...D[37]||(D[37]=[et("巡视2次、翻身3次、测量生命体征1次",-1)])]),_:1}),Be(Ct,{label:"余额/欠费"},{default:Ve(()=>[...D[38]||(D[38]=[et("余额 1500，欠费 0",-1)])]),_:1})]),_:1})):Tn("",!0),Be(N,{direction:"vertical",style:{"margin-top":"12px",width:"100%"}},{default:Ve(()=>[Be(bt,{block:"",type:"primary",onClick:jn},{default:Ve(()=>[...D[39]||(D[39]=[et("打开长者档案",-1)])]),_:1}),Be(bt,{block:"",onClick:Jn},{default:Ve(()=>[...D[40]||(D[40]=[et("一键分配床位",-1)])]),_:1}),Be(bt,{block:"",onClick:ot},{default:Ve(()=>[...D[41]||(D[41]=[et("查看评估档案",-1)])]),_:1}),Be(bt,{block:"",onClick:Wt},{default:Ve(()=>[...D[42]||(D[42]=[et("合同与票据",-1)])]),_:1}),Be(bt,{block:"",onClick:sn},{default:Ve(()=>[...D[43]||(D[43]=[et("状态变更中心",-1)])]),_:1}),Be(bt,{block:"",danger:"",onClick:Vn},{default:Ve(()=>[...D[44]||(D[44]=[et("生成提醒并进入任务中心",-1)])]),_:1}),Be(bt,{block:"",onClick:Di},{default:Ve(()=>[...D[45]||(D[45]=[et("扫码执行（定位今日任务）",-1)])]),_:1})]),_:1})]),_:1},8,["open"]),Be(Y,{open:l.value,"onUpdate:open":D[5]||(D[5]=J=>l.value=J),title:`房间详情 · ${((De=u.value)==null?void 0:De.roomNo)||"-"}`,width:"760px",footer:null,"destroy-on-close":""},{default:Ve(()=>[Be(E,{bordered:"",size:"small",column:2,style:{"margin-bottom":"12px"}},{default:Ve(()=>[Be(Ct,{label:"房型"},{default:Ve(()=>{var J;return[et(Ie(Re((J=u.value)==null?void 0:J.roomType)),1)]}),_:1}),Be(Ct,{label:"容量"},{default:Ve(()=>{var J;return[et(Ie(((J=u.value)==null?void 0:J.capacity)||0)+" 床",1)]}),_:1}),Be(Ct,{label:"在住人数"},{default:Ve(()=>{var J;return[et(Ie(((J=u.value)==null?void 0:J.elderCount)||0)+" 人",1)]}),_:1}),Be(Ct,{label:"空床"},{default:Ve(()=>{var J;return[et(Ie(((J=u.value)==null?void 0:J.emptyBeds)||0)+" 床",1)]}),_:1}),Be(Ct,{label:"公开备注",span:2},{default:Ve(()=>{var J;return[et(Ie(Ae((J=u.value)==null?void 0:J.remark)||"-"),1)]}),_:1}),Be(Ct,{label:"全部备注",span:2},{default:Ve(()=>{var J;return[et(Ie(se((J=u.value)==null?void 0:J.remark)||"-"),1)]}),_:1})]),_:1}),Be(Me,{loading:h.value,"data-source":f.value,pagination:!1,"row-key":J=>J.id,size:"small"},{default:Ve(()=>[Be(H,{title:"头像",key:"avatar",width:"70"},{default:Ve(({record:J})=>[Be(X,{style:{"background-color":"#1677ff"}},{default:Ve(()=>[et(Ie(String((J==null?void 0:J.fullName)||"?").slice(-1)),1)]),_:2},1024)]),_:1}),Be(H,{title:"姓名","data-index":"fullName",key:"fullName",width:"110"}),Be(H,{title:"生日","data-index":"birthDate",key:"birthDate",width:"120"}),Be(H,{title:"家庭住址","data-index":"homeAddress",key:"homeAddress"}),Be(H,{title:"备注","data-index":"remark",key:"remark"}),Be(H,{title:"操作",key:"action",width:"180"},{default:Ve(({record:J})=>[Be(N,null,{default:Ve(()=>[Be(bt,{type:"link",size:"small",onClick:We=>Pe(J.id)},{default:Ve(()=>[...D[46]||(D[46]=[et("详情",-1)])]),_:1},8,["onClick"]),Be(bt,{type:"link",size:"small",onClick:We=>Oe(J.id)},{default:Ve(()=>[...D[47]||(D[47]=[et("账单",-1)])]),_:1},8,["onClick"])]),_:2},1024)]),_:1})]),_:1},8,["loading","data-source","row-key"]),!h.value&&!f.value.length?(ft(),fn(Hn,{key:0,description:"当前房间暂无入住长者"})):Tn("",!0)]),_:1},8,["open","title"])]}),_:1})}}}),WS=wh(NS,[["__scopeId","data-v-cd00d119"]]);export{WS as default};
