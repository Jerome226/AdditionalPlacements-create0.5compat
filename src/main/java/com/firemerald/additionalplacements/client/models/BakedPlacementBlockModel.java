package com.firemerald.additionalplacements.client.models;

import java.util.*;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.firemerald.additionalplacements.AdditionalPlacementsMod;
import com.firemerald.additionalplacements.block.interfaces.IPlacementBlock;
import com.firemerald.additionalplacements.client.BlockModelUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

public class BakedPlacementBlockModel implements IDynamicBakedModel
{
	public final BakedModel model;
	private final Map<ModelKey, List<BakedQuad>> bakedQuadsCache = new HashMap<>();

	public BakedPlacementBlockModel(BakedModel model)
	{
		this.model = model;
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean usesBlockLight()
	{
		return true;
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
	}

	@Override
	public TextureAtlasSprite getParticleIcon(IModelData extraData)
	{
		BlockState modelState = extraData.getData(BlockModelUtils.MODEL_STATE);
		if (modelState != null) return BlockModelUtils.getBakedModel(modelState).getParticleIcon(BlockModelUtils.getModelData(modelState, extraData));
		else return getParticleIcon();
	}


	@Override
	public @Nonnull IModelData getModelData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData modelData)
    {
		return new ModelDataMap.Builder().withInitial(BlockModelUtils.MODEL_STATE, BlockModelUtils.getModeledState(state)).build();
    }

	@Override
	public ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData)
	{
	    BlockState modelState = extraData.getData(BlockModelUtils.MODEL_STATE);
	    if (modelState != null)
	    {
	    	ModelKey modelKey = new ModelKey(modelState, side);
	    	if (!bakedQuadsCache.containsKey(modelKey))
	    	{
	    		Function<Direction, Direction> transformSide;
	    		if (side != null && state.getBlock() instanceof IPlacementBlock) transformSide = ((IPlacementBlock<?>) state.getBlock()).getModelDirectionFunction(state, rand, extraData);
	    		else transformSide = Function.identity();
	    		IModelData modelData = BlockModelUtils.getModelData(modelState, extraData);
    			List<BakedQuad> bakedQuads = new ArrayList<>();
    			for (BakedQuad jsonBakedQuad : model.getQuads(state, side, rand, modelData)) //finds sprite-tint pair that occurs over the highest area in this direction and applies it to the quad
    			{
    				Direction orientation = jsonBakedQuad.getDirection();
    	    		Direction modelSide = orientation == null ? null : transformSide.apply(orientation);
    		    	ModelKey reorientedModelKey = new ModelKey(modelState, modelSide);
    				Pair<TextureAtlasSprite, Integer> texture;
    				if (PlacementBlockModelLoader.TEXTURE_CACHE.containsKey(reorientedModelKey)) texture = PlacementBlockModelLoader.TEXTURE_CACHE.get(reorientedModelKey);
    				else
    				{
    					Map<Pair<TextureAtlasSprite, Integer>, Double> weights = new HashMap<>();
        				for (BakedQuad referredBakedQuad : BlockModelUtils.getBakedQuads(modelState, modelSide, rand, modelData))
        				{
        					Pair<TextureAtlasSprite, Integer> tex = Pair.of(referredBakedQuad.getSprite(), referredBakedQuad.getTintIndex());
        					weights.put(tex, (weights.containsKey(tex) ? weights.get(tex) : 0) + BlockModelUtils.getFaceSize(referredBakedQuad.getVertices()));
        				}
    					texture = weights.entrySet().stream().max((e1, e2) -> (int) Math.signum(e2.getValue() - e1.getValue())).map(Map.Entry::getKey).orElse(null);
    					PlacementBlockModelLoader.TEXTURE_CACHE.put(reorientedModelKey, texture);
    				}
    				if (texture != null) bakedQuads.add(BlockModelUtils.getNewBakedQuad(jsonBakedQuad, texture.getLeft(), texture.getRight(), orientation));
    				else AdditionalPlacementsMod.LOGGER.warn(modelState + " has no texture for " + modelSide + ". No faces will be generated for " + orientation + ".");
    			}
    			bakedQuadsCache.put(modelKey, bakedQuads);
	    	}
	    	return bakedQuadsCache.get(modelKey);
	    }
	    return model.getQuads(state, side, rand, extraData);
	}

}
