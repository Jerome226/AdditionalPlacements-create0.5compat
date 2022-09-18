package com.firemerald.dvsas.client;

import java.util.ArrayList;
import java.util.List;

import com.firemerald.dvsas.DVSaSMod;
import com.firemerald.dvsas.block.VerticalBlock;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class VerticalBlockModelUtils
{
	public static final ModelProperty<BlockState> MODEL_STATE = new ModelProperty<>();

	public static BlockState getModeledState(BlockState state)
	{
		if (state.getBlock() instanceof VerticalBlock) return ((VerticalBlock<?>) state.getBlock()).getModelState(state);
		else return null;
	}

	public static final BakedModel getBakedModel(BlockState state)
	{
		return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
	}

	public static final ModelData getModelData(BlockState blockState, ModelData defaultData)
	{
		return blockState.hasBlockEntity() ? ((EntityBlock) blockState.getBlock()).newBlockEntity(new BlockPos(0, 0, 0), blockState).getModelData() : defaultData;
	}

	public static final List<BakedQuad> getBakedQuads(BlockState referredState, Direction side, RandomSource rand, ModelData modelData, RenderType renderType)
	{
		BakedModel referredBakedModel = getBakedModel(referredState);
		ModelData referredModelData = getModelData(referredState, modelData);
		List<BakedQuad> referredBakedQuads = new ArrayList<>(referredBakedModel.getQuads(referredState, side, rand, referredModelData, renderType));
		for (BakedQuad referredBakedQuad : referredBakedModel.getQuads(referredState, null, rand, referredModelData, renderType))
		{
			if (referredBakedQuad.getDirection() == side)
			{
				referredBakedQuads.add(referredBakedQuad);
			}
		}
		if (referredBakedQuads.size() == 0)
		{
			DVSaSMod.LOGGER.warn("Block has no texture for " + side + " face. No texture will be generated for that face.");
		}
		return referredBakedQuads;
	}

	public static final BakedQuad getNewBakedQuad(BakedQuad jsonBakedQuad, BakedQuad referredBakedQuad, Direction orientation)
	{
		return new BakedQuad(
				updateVertices(
						jsonBakedQuad.getVertices(),
						referredBakedQuad.getVertices(),
						jsonBakedQuad.getSprite(),
						referredBakedQuad.getSprite()
						),
				referredBakedQuad.getTintIndex(),
				orientation,
				referredBakedQuad.getSprite(),
				jsonBakedQuad.isShade()
				);
	}

	public static final short X_OFFSET = 0;
	public static final short Y_OFFSET = 1;
	public static final short Z_OFFSET = 2;
	public static final short U_OFFSET = 4;
	public static final short V_OFFSET = 5;
	public static final int VERTEX_COUNT = DefaultVertexFormat.BLOCK.getVertexSize();
	public static final int VERTEX_SIZE = DefaultVertexFormat.BLOCK.getIntegerSize();

	public static final boolean isInternalFace(int[] vertices)
	{
		for (int vertexIndex = 0; vertexIndex < VERTEX_COUNT; vertexIndex += VERTEX_SIZE)
		{
			float x = Float.intBitsToFloat(vertices[vertexIndex + X_OFFSET]);
			if (x != 0 && x != 0.5 && x != 1) return true;
			float y = Float.intBitsToFloat(vertices[vertexIndex + Y_OFFSET]);
			if (y != 0 && y != 0.5 && y != 1) return true;
			float z = Float.intBitsToFloat(vertices[vertexIndex + Z_OFFSET]);
			if (z != 0 && z != 0.5 && z != 1) return true;
		}
		return false;
	}

	public static final int[] updateVertices(int[] vertices, int[] referredVertices, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite)
	{
		int[] updatedVertices = vertices.clone();
		for (int vertexIndex = 0; vertexIndex < VERTEX_COUNT; vertexIndex += VERTEX_SIZE)
		{
			updatedVertices[vertexIndex + U_OFFSET] = changeUVertexElementSprite(oldSprite, newSprite, updatedVertices[vertexIndex + U_OFFSET]);
			updatedVertices[vertexIndex + V_OFFSET] = changeVVertexElementSprite(oldSprite, newSprite, updatedVertices[vertexIndex + V_OFFSET]);
	    }
		return updatedVertices;
	}

	private static final int changeUVertexElementSprite(TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite, int vertex)
	{
		return Float.floatToRawIntBits(newSprite.getU(getUV(Float.intBitsToFloat(vertex), oldSprite.getU0(), oldSprite.getU1())));
	}

	private static final int changeVVertexElementSprite(TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite, int vertex)
	{
		return Float.floatToRawIntBits(newSprite.getV(getUV(Float.intBitsToFloat(vertex), oldSprite.getV0(), oldSprite.getV1())));
	}

	private static final double getUV(float uv, float uv0, float uv1)
	{
		return (double) (uv - uv0) * 16.0F / (uv1 - uv0);
	}
}