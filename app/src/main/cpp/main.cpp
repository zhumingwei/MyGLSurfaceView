#include <jni.h>

//
// Created by zhumingwei on 2022/4/21.
//
#include <android/log.h>
#include <jni.h>
#include <iostream>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include <GLES2/gl2ext.h>
#define LOG_TAG "[OPENGL_LESSON]"
#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, fmt, ##args)

const char *vertexShaderSource = "#version 300 es\n"
                                 "layout (location = 0) in vec3 aPos;\n"
                                 "layout (location = 1) in vec2 aTexCoord;\n"
                                 "out vec2 TexCoord;\n"
                                 "void main()\n"
                                 "{\n"
                                 "   gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);\n"
                                 " TexCoord = aTexCoord;\n"
                                 "}\0";
const char *fragmentShaderSource ="#version 300 es\n"
                                   "#extension GL_OES_EGL_image_external_essl3 : require\n"
                                   "out vec4 FragColor;\n"
                                   "in vec2 TexCoord;\n"
                                   "uniform samplerExternalOES textureId;\n"
                                   "void main()\n"
                                   "{\n"
//                                   "   FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n"
//                                   "   FragColor = vec4(TexCoord.x, TexCoord.y, 0.0f, 1.0f);\n"
//                                   "   FragColor = vec4(TexCoord.x, TexCoord.x, TexCoord.x, 1.0f);\n"
//                                   "   FragColor = vec4(TexCoord.y, TexCoord.y, TexCoord.y, 1.0f);\n"
                                   "   FragColor = texture( textureId, TexCoord);\n"
                                   "}\n\0";
void checkCompileErrors(GLuint shader, std::string message, int isProgram)
{
    GLint success;
    GLchar infoLog[1024];
    if (isProgram){
        glGetProgramiv(shader, GL_LINK_STATUS, &success);
        if (!success) {
            glGetProgramInfoLog(shader, 1024, NULL, infoLog);
            LOGE("ERROR::SHADER_COMPILATION_ERROR of type: Program message:%s \n %s", message.c_str(), infoLog);
            exit(0);
        }
    } else {
        glGetShaderiv(shader, GL_COMPILE_STATUS, &success);
        if (!success) {
            glGetShaderInfoLog(shader, 1024, NULL, infoLog);
            LOGE("ERROR::SHADER_COMPILATION_ERROR of type: Shader message:%s \n %s", message.c_str(), infoLog);
            exit(0);
        }
    }
}

// 参数
GLuint programID;

float vertices[] = {
        1.0f,  1.0f, 0.0f,  1.0f, 0.0f, // top right
        1.0f, -1.0f, 0.0f,   1.0f, 1.0f, // bottom right
        -1.0f, -1.0f, 0.0f,   0.0f, 1.0f, // bottom left
        -1.0f,  1.0f, 0.0f,   0.0f, 0.0f  // top left
};

//float vertices[] = {
//        0.5f,  0.5f, 0.0f,  1.0f, 0.0f, // top right
//        0.5f, -0.5f, 0.0f,   1.0f, 1.0f, // bottom right
//        -0.5f, -0.5f, 0.0f,   0.0f, 1.0f, // bottom left
//        -0.5f,  0.5f, 0.0f,   0.0f, 0.0f  // top left
//};
unsigned int elementIndices[] = {
        0, 1, 3, // first triangle
        1, 2, 3  // second triangle
};
unsigned int VBO, VAO, EBO;
GLuint textureid = 0;
// 参数

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_myglsurfaceview_MainActivity_nativeBindTexture(JNIEnv
* env,
jobject thiz, jint
id) {
    LOGI("nativeBindTexture");
    textureid = id;
//    glActiveTexture(GL_TEXTURE0);
//    glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureid);
//    GLuint location = glGetUniformLocation(programID, "textureId");
//    glUniform1i(location, 0);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_myglsurfaceview_MainActivity_nativeSurfaceCreate(JNIEnv
* env,
jobject thiz

) {
    LOGI("nativeSurfaceCreate");
    glClearColor(0, 0, 0, 0);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CW);


    // compile shaders
    GLuint vertex, fragment;
    // vertex shader
    vertex = glCreateShader(GL_VERTEX_SHADER);
    glShaderSource(vertex, 1 ,&vertexShaderSource, nullptr);
    glCompileShader(vertex);
    checkCompileErrors(vertex, "VERTEX", 0);
    // Fragment Shader
    fragment = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(fragment, 1, &fragmentShaderSource, nullptr);
    glCompileShader(fragment);
    checkCompileErrors(vertex, "FRAGMENT", 0);
    // program
    programID = glCreateProgram();
    glAttachShader(programID, vertex);
    glAttachShader(programID, fragment);
    glLinkProgram(programID);
    checkCompileErrors(programID, "PROGRAM", 1);
    glDeleteShader(vertex);
    glDeleteShader(fragment);

    glGenVertexArrays(1, &VAO);
    glGenBuffers(1, &VBO);
    glGenBuffers(1, &EBO);

    glBindVertexArray(VAO);

    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(elementIndices), elementIndices, GL_STATIC_DRAW);

    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)0);
    glEnableVertexAttribArray(0); // 启用index 0 一般至少为16个

    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5 * sizeof(float), (void*)(3 * sizeof(float)));
    glEnableVertexAttribArray(1); // 启用index 0 一般至少为16个

    glBindBuffer(GL_ARRAY_BUFFER, 0);//unbind
    glBindVertexArray(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_myglsurfaceview_MainActivity_nativeSurfaceChange(JNIEnv
* env,
jobject thiz,
jint width,
jint height
) {
LOGI("nativeSurfaceChange");
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tencent_myglsurfaceview_MainActivity_nativeDrwaFrame(JNIEnv
* env,
jobject thiz
) {
//LOGI("nativeDrwaFrame");
    glClear(GL_COLOR_BUFFER_BIT| GL_DEPTH_BUFFER_BIT);
    glClearColor(0.0, 0.0, 0.0, 0.0);

    glUseProgram(programID);
    glBindVertexArray(VAO);
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
}