#ifdef GL_ES
precision highp float; 
#endif

attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;

uniform vec3 u_color;
uniform mat4 u_projTrans;
uniform mat4 u_lightProjTrans;
uniform float u_time;
uniform float u_waterOn;

varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec4 v_lightSpacePosition;

vec3 wobble(vec3 pos){
    pos.x = pos.x + cos(pos.x+u_time)*0.1;
    return pos;
}

void main(void) {
	v_texCoord = a_texCoord0;
	vec3 pos = mix(a_position ,wobble(a_position), u_waterOn);
	
	v_normal = (u_projTrans * vec4(a_normal, 0.0)).xyz;
	gl_Position = u_projTrans * vec4(pos,1.0) ;
	v_lightSpacePosition  = u_lightProjTrans * vec4(pos,1.0) ;
}
