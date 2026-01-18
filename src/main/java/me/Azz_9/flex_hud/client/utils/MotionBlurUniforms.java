package me.Azz_9.flex_hud.client.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.PostEffectProcessor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MotionBlurUniforms {
    private static Field postProcessorField;
    private static Method getProgramMethod;
    private static Method getUniformMethod;

    static {
        try {
            // Cache gameRenderer.postProcessor field
            for (Field f : net.minecraft.client.render.GameRenderer.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals("PostEffectProcessor")
                        || f.getType().getSimpleName().equals("ShaderEffect")) {
                    f.setAccessible(true);
                    postProcessorField = f;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setUniform(MinecraftClient client, String programName, String uniformName, float value) {
        if (postProcessorField == null)
            return;

        try {
            Object processor = postProcessorField.get(client.gameRenderer);
            if (processor == null)
                return;

            // Cache getProgram method from the processor instance class
            if (getProgramMethod == null) {
                for (Method m : processor.getClass().getMethods()) {
                    if ((m.getName().equals("getProgram") || m.getName().equals("method_1234")) && // method_1234 is
                                                                                                   // placeholder, rely
                                                                                                   // on sig
                            m.getParameterCount() == 1 &&
                            m.getParameterTypes()[0] == String.class &&
                            m.getReturnType().getSimpleName().contains("Program")) {
                        getProgramMethod = m;
                        break;
                    }
                }
            }

            if (getProgramMethod != null) {
                Object program = getProgramMethod.invoke(processor, programName);
                if (program != null) {
                    // Cache getUniform method logic if possible, but getUniform is on the Program
                    // object
                    // We can just iterate swiftly or cache it too if we assume Program class is
                    // constant
                    Method getUniform = null;
                    for (Method m : program.getClass().getMethods()) {
                        if (m.getName().equals("getUniform")
                                || (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class)) {
                            getUniform = m;
                            break;
                        }
                    }

                    if (getUniform != null) {
                        Object uniform = getUniform.invoke(program, uniformName);
                        if (uniform != null) {
                            // Reflection set
                            for (Method m : uniform.getClass().getMethods()) {
                                if (m.getName().equals("set") && m.getParameterCount() == 1
                                        && (m.getParameterTypes()[0] == float.class
                                                || m.getParameterTypes()[0] == Float.class)) {
                                    m.invoke(uniform, value);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Squelch for per-frame safety
        }
    }
}
