package me.Azz_9.flex_hud.client.utils;

import net.minecraft.client.MinecraftClient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MotionBlurUniforms {
    private static Field postProcessorField;
    private static Field passesField;

    static {
        try {
            for (Field f : net.minecraft.client.render.GameRenderer.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals("PostEffectProcessor")
                        || f.getType().getSimpleName().equals("ShaderEffect")) {
                    f.setAccessible(true);
                    postProcessorField = f;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[FlexHUD] Failed to find postProcessor field: " + e.getMessage());
        }
    }

    public static void setUniformOnProcessor(Object processor, String programName, String uniformName, float value) {
        try {
            if (passesField == null) {
                for (Field f : processor.getClass().getDeclaredFields()) {
                    if (java.util.List.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        java.util.List<?> list = (java.util.List<?>) f.get(processor);
                        if (list != null && !list.isEmpty()) {
                            Object first = list.get(0);
                            if (first.getClass().getSimpleName().contains("Pass")) {
                                passesField = f;
                                break;
                            }
                        }
                    }
                }
            }

            if (passesField != null) {
                java.util.List<?> passes = (java.util.List<?>) passesField.get(processor);
                if (passes != null) {
                    for (Object pass : passes) {
                        Object program = null;
                        for (Method m : pass.getClass().getMethods()) {
                            if (m.getParameterCount() == 0 &&
                                    (m.getReturnType().getSimpleName().contains("Program") ||
                                            m.getReturnType().getSimpleName().contains("Shader"))) {
                                program = m.invoke(pass);
                                if (program != null)
                                    break;
                            }
                        }

                        if (program != null) {
                            String actualName = null;
                            try {
                                Method getName = program.getClass().getMethod("getName");
                                actualName = (String) getName.invoke(program);
                            } catch (Exception ignored) {
                                for (Method m : pass.getClass().getMethods()) {
                                    if (m.getReturnType() == String.class && m.getParameterCount() == 0) {
                                        String candidate = (String) m.invoke(pass);
                                        if (candidate != null && candidate.contains("motion_blur")) {
                                            actualName = candidate;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (actualName != null && actualName.contains("motion_blur")) {
                                setUniformOnProgram(program, uniformName, value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[FlexHUD] Error in setUniformOnProcessor: " + e.getMessage());
        }
    }

    private static void setUniformOnProgram(Object program, String uniformName, float value) {
        try {
            Method getUniform = null;
            for (Method m : program.getClass().getMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class &&
                        (m.getReturnType().getSimpleName().contains("Uniform") ||
                                m.getReturnType().getSimpleName().contains("ShaderStage"))) {
                    getUniform = m;
                    break;
                }
            }

            if (getUniform != null) {
                Object uniform = getUniform.invoke(program, uniformName);
                if (uniform != null) {
                    for (Method m : uniform.getClass().getMethods()) {
                        if ((m.getName().equals("set") || m.getName().startsWith("method_")) &&
                                m.getParameterCount() == 1 &&
                                (m.getParameterTypes()[0] == float.class || m.getParameterTypes()[0] == Float.class)) {
                            m.invoke(uniform, value);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static void setUniform(MinecraftClient client, String programName, String uniformName, float value) {
        if (postProcessorField == null)
            return;
        try {
            Object processor = postProcessorField.get(client.gameRenderer);
            if (processor != null)
                setUniformOnProcessor(processor, programName, uniformName, value);
        } catch (Exception e) {
        }
    }
}
