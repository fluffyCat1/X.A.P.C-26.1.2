package com.xapc.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class TracerRenderer {
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final float COLOR_R = 255f / 255f;
    private static final float COLOR_G = 230f / 255f;
    private static final float COLOR_B = 161f / 255f;

    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;

    // Снимок трассеров, сделанный в extraction-фазе — используется в drawing-фазе
    private static List<ActiveTracers.Tracer> extractedTracers = List.of();

    private TracerRenderer() {}

    public static void register() {
        LevelRenderEvents.END_EXTRACTION.register(TracerRenderer::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(TracerRenderer::renderAndDraw);
    }

    // --- Extraction phase: только собираем данные, ничего не рисуем ---
    private static void extract(LevelExtractionContext context) {
        extractedTracers = List.copyOf(ActiveTracers.getActive());
    }

    // --- Drawing phase ---
    private static void renderAndDraw(LevelRenderContext context) {
        if (extractedTracers.isEmpty()) return;

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, RenderPipelines.LINES.getVertexFormatMode(), RenderPipelines.LINES.getVertexFormat());
        }

        Matrix4fc matrix = matrices.last().pose();
        long now = System.currentTimeMillis();

        for (ActiveTracers.Tracer tracer : extractedTracers) {
            long age = now - tracer.spawnTime();
            float lifeProgress = Math.min(1f, age / (float) ActiveTracers.LIFETIME_MS);

            float fadeOut = 1f - lifeProgress;
            float fadeIn = Math.min(1f, lifeProgress / 0.15f);
            float alpha = fadeOut * fadeIn;

            addLine(buffer, matrix, tracer.start(), tracer.end(), COLOR_R, COLOR_G, COLOR_B, alpha);
        }

        matrices.popPose();

        drawLines(Minecraft.getInstance(), RenderPipelines.LINES);
    }

    private static void addLine(BufferBuilder buffer, Matrix4fc matrix, Vec3 start, Vec3 end,
                                float r, float g, float b, float alpha) {
        Vec3 direction = end.subtract(start).normalize();
        Vec3 mid = start.add(end.subtract(start).scale(0.5));
        float nx = (float) direction.x, ny = (float) direction.y, nz = (float) direction.z;
        float lineWidth = 2.0f;

        // первый сегмент: start (прозрачный) -> mid (полная альфа)
        buffer.addVertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .setColor(r, g, b, 0f)
                .setNormal(nx, ny, nz)
                .setLineWidth(lineWidth);
        buffer.addVertex(matrix, (float) mid.x, (float) mid.y, (float) mid.z)
                .setColor(r, g, b, alpha)
                .setNormal(nx, ny, nz)
                .setLineWidth(lineWidth);

        // второй сегмент: mid (полная альфа) -> end (прозрачный)
        buffer.addVertex(matrix, (float) mid.x, (float) mid.y, (float) mid.z)
                .setColor(r, g, b, alpha)
                .setNormal(nx, ny, nz)
                .setLineWidth(lineWidth);
        buffer.addVertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .setColor(r, g, b, 0f)
                .setNormal(nx, ny, nz)
                .setLineWidth(lineWidth);
    }

    private static void drawLines(Minecraft client, RenderPipeline pipeline) {
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);
        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        vertexBuffer.rotate();
        buffer = null;
    }

    private static GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBuffer = new MappableRingBuffer(() -> "xapc tracer render pipeline",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(
                vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer,
                             MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "xapc tracer render pass",
                        client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(),
                        client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public static Vec3 getMuzzleOffset(Player player) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = look.cross(up).normalize();

        // подберите под свою модель оружия: вперёд, вправо, вниз
        return look.scale(0.6)          // чуть вперёд
                .add(right.scale(0.35))  // вправо (от третьего лица оружие обычно у правого плеча)
                .add(new Vec3(0, -0.3, 0)); // немного вниз от глаз
    }

    public static void close() {
        ALLOCATOR.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}