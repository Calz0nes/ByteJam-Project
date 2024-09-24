package bytejam.project.renderer;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Shader {
    
    private int vertexID, fragmentID, shaderProgramID;
    private int vaoID, vboID, eboID;
    private String vertexSource, fragmentSource, filepath;
    
    /* Creates shader class. */
    public Shader(String filepath) {

        this.filepath = filepath;
        try {
            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString = source.split("(#type)()+([a-zA-Z]+)");

            // Find the first pattern after #type 'pattern'.
            int index = source.indexOf("#type") + 6;
            int eol = source.indexOf("\r\n", index);
            String firstPattern = source.substring(index, eol).trim();

            // Find the first pattern after #type 'pattern'.
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index, eol).trim();

            // Sort first pattern.
            if (firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException("Unexpected token '" + firstPattern + "'in'" + filepath + "'");
            }

            // Sort second pattern.
            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException("Unexpected token '" + secondPattern + "'in'" + filepath + "'");
            }

        } catch(IOException e) {
            e.printStackTrace();
            assert false : "Error: Could not open file for shader: '" + filepath + "'";
        }

        System.out.println(vertexSource);
        System.out.println(fragmentSource);
    }


    public void compile() {
        //======================================================
        // Compile and link shaders.
        //======================================================

        // First load and compile the vertex shader.
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        
        // Pass the shader source to the GPU.
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // Check for errors in compilation.
        int success_Vertex = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success_Vertex == GL_FALSE) {
            int lenVert = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: 'defaultShader.glsl'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, lenVert));
            assert false : "";
        }

        // Now load and compile the fragment shader.
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        // Pass the shader source to the GPU.
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        // Check for errors in compilation.
        int success_Fragment = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success_Fragment == GL_FALSE) {
            int lenFrag = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: '" + filepath + "'\n\tFragment shader comilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, lenFrag));
            assert false : "";
        }

        // Link shader and check for errors.
        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        glLinkProgram(shaderProgramID);

        // Check for linking errors.
        int success_Program = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if (success_Program == GL_FALSE) {
            int lenPrgm = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filepath + "'\n\tLinking of shaders failed.");
            System.out.println(glGetShaderInfoLog(shaderProgramID, lenPrgm));
        }
    }

    // Bind program.
    public void use() {
        glUseProgram(shaderProgramID);
    }

    // Unbind program.
    public void detach() {
        glUseProgram(0);
    }
}
