package biomesoplenty.integration.tinkersconstruct;

import java.util.List;

import mods.tinker.tconstruct.library.TConstructRegistry;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BOPCraftingItem extends Item
{
	public String[] textureNames;
	public String[] unlocalizedNames;
	public String folder;
	public Icon[] icons;
	public BOPCraftingItem(int id, String[] names, String[] tex, String folder)
	{
		super(id);
		this.setCreativeTab(TConstructRegistry.materialTab);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.textureNames = tex;
		this.unlocalizedNames = names;
		this.folder = folder;
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int meta)
	{
		return icons[meta];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void registerIcons(IconRegister iconRegister)
    {
		this.icons = new Icon[textureNames.length];

        for (int i = 0; i < this.icons.length; ++i)
        {
            this.icons[i] = iconRegister.registerIcon("BiomesOPlenty:"+folder+textureNames[i]);
        }
    }
	
	public String getUnlocalizedName(ItemStack stack)
	{
		int arr = MathHelper.clamp_int(stack.getItemDamage(), 0, unlocalizedNames.length);
		return getUnlocalizedName() + "." +unlocalizedNames[arr];
	}

	public void getSubItems (int id, CreativeTabs tab, List list)
	{
		for (int i = 0; i < unlocalizedNames.length; i++)
			list.add(new ItemStack(id, 1, i));
	}
}
