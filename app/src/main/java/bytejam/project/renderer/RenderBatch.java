package bytejam.project.renderer;

import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import bytejam.project.turbo.Window;
import bytejam.project.turbo.goc.Background;
import bytejam.project.turbo.goc.Entity;

public class RenderBatch {
    // Vertex
    // ======
    // Pos               Clor
    // float, float,     float, float, float, float
    
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = (POS_OFFSET + POS_SIZE) * Float.BYTES;
    private final int VERTEX_SIZE = 6;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private Entity[] entities;
    private Background background;
    private int numEntities;
    private int numBackground;
    private boolean hasRoom;
    private float[] vertices;

    private int vaoID, vboID;
    private int maxBatchSize;
    private Shader shader;

    public RenderBatch(int maxBatchSize) {
        this.shader = new Shader("assets/shader/default.glsl");
        shader.compile();

        // Max amount of entities minus one for the background.
        this.entities = new Entity[maxBatchSize - numBackground];
        this.maxBatchSize = maxBatchSize;

        // 4 vertices quads.
        this.vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];
        this.numEntities = 0;
        this.numBackground = 0;
        this.hasRoom = true;
        
    }

    public void start() {

        // ===========================================================
        // Generate VAQ, VBO, and EBO buffer objects and send to GPU
        // ===========================================================

        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Allocate space for vertices.
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);
        
        // Create and upload indices buffer.
        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers.
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);
    }

    public void addEntity(Entity entity) {
        // Get index and add renderObject.
        int index = this.numEntities;
        this.entities[index] = entity;
        this.numEntities++;

        // Add properties to local vertices array.
        loadVertexProperties(index);

        if (numEntities >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    public void addBackground(Background background) {
        // Get index and add renderObject.
    }

    public void render() {
        // Set to rebuffer all data every frame.
        glBindBuffer(GL_ARRAY_BUFFER, vaoID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        // Bind.
        shader.bind();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // Render.
        glDrawElements(GL_TRIANGLES, (this.numEntities + this.numBackground) * 6, GL_UNSIGNED_INT, 0);

        // Unbind.
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        shader.unbind();

    }

    private void loadVertexProperties(int index) {
        Entity entity = this.entities[index];

        // Find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;
        
        Vector4f color = entity.getColor();

        // Add vertice with the appropriate properties.
        float xAdd = 1.0f;
        float yAdd = 1.0f;
        for (int i=0; i < 4; i++) {
            if (i ==1) {
                yAdd = 0.0f;
            } else if (i == 2) {
                xAdd = 0.0f;
            } else if (i == 3) {
                yAdd = 1.0f;
            }

            // Load position.
            vertices[offset] = entity.getPos().x + (xAdd * entity.getSize().x);
            vertices[offset + 1] = entity.getPos().y + (yAdd * entity.getSize().y);

            // Load color.
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            offset += VERTEX_SIZE;
        }
    }   

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle)
        int[] elements = new int[6 * maxBatchSize];
        for (int i=0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // Tri 1                    Tri 2
        // 3, 2, 0, 0, 2, 1         7, 6, 4, 4, 6, 5

        // Tri 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset + 0;

        // Tri 2
        elements[offsetArrayIndex + 3] = offset + 0;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;

    }

    public  boolean hasRoom() {
        return this.hasRoom;
    }

}
