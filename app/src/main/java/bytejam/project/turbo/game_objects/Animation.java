package bytejam.project.turbo.game_objects;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

import bytejam.project.renderer.Texture;

public class Animation {

    private float prevTime, delta;
    private int prevFrame;
    private Texture[] frames;
    private Entity entity;

    public Animation(Entity entity) {
        frames = new Texture[10];
        prevTime = (float)glfwGetTime();
        prevFrame = 0;
        this.entity = entity;
    }

    /* 
    public void addFrames(String[] texturePaths) {
        for (String f : texturePaths) {
            frames[f.indexOf(f)] = new Texture(f);
            frames[f.indexOf(f)].setSize(entity.getSize());
        }
    }
    */

    public void setSpeed(float delta) {
        delta = delta;
    }

    public boolean bind() {
        //frames[prevFrame].setPos(entity.getPos());
        

        if ((float)glfwGetTime() - prevTime > delta) {
            frames[prevFrame].bind();

            prevTime = (float)glfwGetTime();
            prevFrame += 1;
            if (prevFrame == frames.length) {
                prevFrame = 0;
                return true;
            } else {
                return false;
            }
        } else {
            frames[prevFrame].bind();
            return false;
        }
    }

    public void unbind() {
        frames[prevFrame].unbind();
    }
}
