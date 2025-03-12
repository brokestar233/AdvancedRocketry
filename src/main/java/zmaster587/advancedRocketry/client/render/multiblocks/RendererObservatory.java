package zmaster587.advancedRocketry.client.render.multiblocks;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import zmaster587.advancedRocketry.tile.multiblock.TileObservatory;
import zmaster587.advancedRocketry.util.Debugger;
import zmaster587.libVulpes.block.RotatableBlock;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;

public class RendererObservatory extends TileEntitySpecialRenderer {

    private final IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation("advancedrocketry:models/observatory.obj"));

    @NotNull
    final ResourceLocation texture = new ResourceLocation("advancedrocketry:textures/models/T1Observatory.png");

    // 改为非静态变量并通过volatile保证线程可见性
    private volatile int bodyList = -1; 
    private boolean isDisplayListGenerated = false;

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {
        TileObservatory multiBlockTile = (TileObservatory) tile;

        if (!multiBlockTile.canRender())
            return;

        // 延迟初始化显示列表
        if (!isDisplayListGenerated) {
            generateDisplayList();
            isDisplayListGenerated = true;
        }

        GL11.glPushMatrix();

        // 光照计算
        int bright = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord + 2, tile.zCoord, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, bright % 65536, bright / 65536);

        // 坐标变换
        ForgeDirection front = RotatableBlock.getFront(tile.getBlockMetadata());
        GL11.glTranslated(x + .5, y, z + .5);
        GL11.glRotatef((front.offsetX == 1 ? 180 : 0) + front.offsetZ * 90f, 0, 1, 0);
        GL11.glTranslated(2, -1, 0);

        bindTexture(texture);

        float offset = multiBlockTile.getOpenProgress();

        // 动态渲染逻辑
        if (offset != 0f) {
            renderDynamicParts(offset);
        } else {
            renderStaticParts();
        }

        GL11.glPopMatrix();
    }

    private void generateDisplayList() {
        // 保障线程安全
        if (GL11.glGetCapabilities() == null) return;

        // 生成新的显示列表
        bodyList = GL11.glGenLists(1);
        
        // 手动平衡OpenGL状态
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        GL11.glNewList(bodyList, GL11.GL_COMPILE);
        model.renderOnly("body");
        GL11.glEndList();

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private void renderDynamicParts(float offset) {
        if (Debugger.renderList && bodyList != -1) {
            GL11.glCallList(bodyList);
        } else {
            model.renderOnly("Base");
        }

        model.renderPart("Scope");
        model.renderPart("Axis");

        // 动态部件渲染
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, -offset);
        model.renderOnly("CasingXMinus");
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, offset);
        model.renderOnly("CasingXPlus");
        GL11.glPopMatrix();
    }

    private void renderStaticParts() {
        if (Debugger.renderList && bodyList != -1) {
            GL11.glCallList(bodyList);
        } else {
            model.renderOnly("Base");
        }
        model.renderOnly("CasingXMinus");
        model.renderOnly("CasingXPlus");
    }

    @Override
    public void onRenderWorldUnload() {
        // 安全释放显示列表
        if (bodyList != -1) {
            GL11.glDeleteLists(bodyList, 1);
            bodyList = -1;
            isDisplayListGenerated = false;
        }
    }
}		TileObservatory multiBlockTile = (TileObservatory)tile;

		if(!multiBlockTile.canRender())
			return;

		GL11.glPushMatrix();

		//Initial setup
		int bright = tile.getWorldObj().getLightBrightnessForSkyBlocks(tile.xCoord, tile.yCoord + 2, tile.zCoord,0);
		int brightX = bright % 65536;
		int brightY = bright / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, brightX, brightY);

		//Rotate and move the model into position
		ForgeDirection front = RotatableBlock.getFront(tile.getBlockMetadata());//tile.getWorldObj().getBlockMetadata(tile.xCoord, tile.yCoord, tile.zCoord));
		GL11.glTranslated(x + .5, y, z + .5);
		GL11.glRotatef((front.offsetX == 1 ? 180 : 0) + front.offsetZ*90f, 0, 1, 0);

		GL11.glTranslated(2, -1, 0);

		bindTexture(texture);

		float offset = multiBlockTile.getOpenProgress();

		if(offset != 0f) {
			if(Debugger.renderList)
				GL11.glCallList(bodyList);
			else
				model.renderOnly("Base");

			model.renderPart("Scope");
			model.renderPart("Axis");

			GL11.glPushMatrix();
			GL11.glTranslatef(0, 0, -offset);
			model.renderOnly("CasingXMinus");
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			GL11.glTranslatef(0,0,offset);
			model.renderOnly("CasingXPlus");
			GL11.glPopMatrix();

		}
		else {
			if(Debugger.renderList)
				GL11.glCallList(bodyList);
			else
				model.renderOnly("Base");
			model.renderOnly("CasingXMinus");
			model.renderOnly("CasingXPlus");
		}
		GL11.glPopMatrix();
	}
}
