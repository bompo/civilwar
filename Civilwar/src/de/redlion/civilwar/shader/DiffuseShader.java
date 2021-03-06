package de.redlion.civilwar.shader;

public class DiffuseShader {

    public static final String mVertexShader =
    	"attribute vec4 a_position;\n" +
    	"attribute vec2 a_texCoord0;\n" +
    	"uniform mat4 VPMatrix;\n"+
    	"uniform mat4 MMatrix;\n"+
    	"varying vec2 vTextureCoord;\n"+
    	"void main() {\n"+
    	"	gl_Position = ((VPMatrix * MMatrix) * a_position);\n"+
    	"   vTextureCoord = a_texCoord0;\n"+
    	"}\n";

    public static final String mFragmentShader =   
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "uniform vec4 a_color;\n"+
        "uniform float alpha;\n"+
        "varying vec2 vTextureCoord;\n"+
        "uniform sampler2D uSampler;\n"+
    	"void main() {\n"+
    	"vec4 color = texture2D(uSampler, vec2(vTextureCoord.s, vTextureCoord.t));\n"+
        "gl_FragColor = vec4(color.rgb, color.a + alpha);\n"+
        "}\n"; 
}