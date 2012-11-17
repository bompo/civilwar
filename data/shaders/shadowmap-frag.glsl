#ifdef GL_ES
precision highp float; 
#endif

varying vec4 v_lightSpacePosition;
varying vec2 v_texCoord;
varying vec3 v_normal;

uniform vec3 u_viewerPosition;
uniform vec3 u_color;
uniform float u_selected;
uniform float u_time;
uniform sampler2D s_IrradianceMap;

uniform sampler2D s_shadowMap;
uniform sampler2D s_diffMap;

float unpack(vec4 packedZValue) {	
	const vec4 unpackFactors = vec4( 1.0 / (256.0 * 256.0 * 256.0), 1.0 / (256.0 * 256.0), 1.0 / 256.0, 1.0 );
	return dot(packedZValue,unpackFactors);
}

float getShadowFactor(vec4 lightZ) {
	vec4 packedZValue = texture2D(s_shadowMap, lightZ.st);
	float unpackedZValue = unpack(packedZValue);
	return float(unpackedZValue > lightZ.z);
}

void main(void) {	
	float shadowFactor=1.0;				
	vec4 lightZ = v_lightSpacePosition / v_lightSpacePosition.w;
	lightZ = (lightZ + 1.0) / 2.0;
	shadowFactor = getShadowFactor(lightZ);	
    
    vec3 eyeDirection = normalize(v_lightSpacePosition.xyz - u_viewerPosition);

    vec3 normal = normalize(v_normal);
    vec2 sphereUV = vec2(atan(normal.z, normal.x)/(3.14159265*2.0)+0.5, asin(normal.y)/(3.14159265)+0.5);

    vec3 specularNormal = reflect(eyeDirection, normal);
    vec2 sphereSpecUV = vec2(atan(specularNormal.z, specularNormal.x)/(3.14159265*2.0)+0.5, asin(specularNormal.y)/(3.14159265)+0.5);

    vec4 specularColor = texture2D(s_IrradianceMap, sphereSpecUV); 
    
    vec4 textureColor = texture2D(s_diffMap, v_texCoord);
    textureColor = vec4(u_color,1.0)*textureColor*3.5;
    
    vec4 color = texture2D(s_IrradianceMap, sphereUV) + specularColor;
    color = mix(color,vec4(0.1,0.1,0.15,1.0), -0.7 * shadowFactor);
    
    gl_FragColor = textureColor;
    gl_FragColor.rgb = gl_FragColor.rgb * color.rgb;
    gl_FragColor.rgb /= 0.4+dot(gl_FragColor.rgb ,vec3(0.4,0.5,0.1));
    
    gl_FragColor.rgb =  mix(gl_FragColor.rgb, vec3(0.4,0.5,0.1), u_selected * sin(u_time));
}
