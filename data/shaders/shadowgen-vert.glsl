attribute vec3 a_position;
 
varying vec4 v_position;

uniform mat4 u_projTrans;
uniform mat4 u_model;

void main(void) 
{   
   gl_Position =  u_projTrans * u_model * vec4(a_position,1.0) ;
   v_position = gl_Position;   
}