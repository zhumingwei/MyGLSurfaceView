package com.tencent.myglsurfaceview;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Square {
    private static String TAG = "xxxxxx Square";
    private final String vertexShaderCode  = "#version 300 es\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "layout (location = 1) in vec2 aTexCoord;\n" +
            "out vec2 TexCoord;\n" +
            "void main()\n" +
            "{\n" +
            "   gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);" +
            "   TexCoord = aTexCoord;" +
            "}\0";
    private final String fragmentShaderCode  = "#version 300 es\n" +
            "out vec4 FragColor;\n" +
            "in vec2 TexCoord;\n" +
            "uniform sampler2D textureId;\n" +
            "void main()\n" +
            "{\n" +
            "   FragColor = vec4(TexCoord.x, TexCoord.y, 0.0f, 1.0f);\n" +
            "   FragColor = vec4(1.0f, 1.0f, 0.0f, 1.0f);\n" +
            "}\n\0";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -0.5f, 0.5f, 0.0f,  // top left
            -0.5f, -0.5f, 0.0f,  // bottom left
            0.5f, -0.5f, 0.0f,  // bottom right
            0.5f, 0.5f, 0.0f }; // top right

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    int[] VAO;
    public Square() {
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // 初始化ByteBuffer，长度为arr数组的长度*2，因为一个short占2个字节
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);


    }

    void init() {
        loadShader();
        loadBuffer();
    }

    void loadBuffer() {
        VAO = new int[1];
        int[] VBO = new int[1];
        int[] EBO = new int[1];

        GLES30.glGenVertexArrays(1, VAO, 0);
        GLES30.glGenBuffers(1, VBO, 0);
        GLES30.glGenBuffers(1, EBO, 0);
        GLES30.glBindVertexArray(VAO[0]);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, squareCoords.length , vertexBuffer, GLES20.GL_STATIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, EBO[0]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, drawOrder.length, drawListBuffer, GLES30.GL_STATIC_DRAW);

        GLES30.glVertexAttribIPointer(0, 3, GLES20.GL_FLOAT, 5 * 4, 0 * 4);
        GLES30.glEnableVertexAttribArray(0);

        GLES30.glVertexAttribIPointer(1, 3, GLES20.GL_FLOAT, 5 * 4, 3 * 4);
        GLES30.glEnableVertexAttribArray(1);

        //unbind
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindVertexArray(0);
    }

    int programId;
    void loadShader() {
        // vertexshader createShader compileshader
        int vertex = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        GLES30.glShaderSource(vertex, vertexShaderCode);
        GLES30.glCompileShader(vertex);
        // fragmentshader createShader compileshader
        int fragment = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(fragment, fragmentShaderCode);
        GLES30.glCompileShader(fragment);
        int[] status = new int[1];
        GLES30.glGetShaderiv(fragment, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != 0) {
            Log.d(TAG, "loadShader:fragment error");
        }
        // create program
        programId = GLES30.glCreateProgram();
        GLES30.glAttachShader(programId, vertex);
        GLES30.glAttachShader(programId, fragment);
        // attach vs
        // attach fs
        // linkprogram
        GLES30.glLinkProgram(programId);
        IntBuffer intBuffer = IntBuffer.allocate(1);
        intBuffer.position(0);
        GLES30.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, intBuffer);
        if (intBuffer.get() != 0) {
            Log.d(TAG, "loadShader: error");
        }
        GLES30.glDeleteShader(vertex);
        GLES30.glDeleteShader(fragment);
    }

    void bind(int tex) {
        // use program
        GLES30.glUseProgram(programId);
        GLES30.glActiveTexture(GLES20.GL_TEXTURE);
        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
        GLES30.glUniform1i(GLES30.glGetUniformLocation(programId, "textureId"), tex);
        //

    }

    void draw (){
        // use program
        GLES30.glUseProgram(programId);
        GLES30.glBindVertexArray(VAO[0]);
        GLES30.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0);
    }
}