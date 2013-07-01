package mods.tinker.tconstruct.library.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mods.tinker.tconstruct.library.ActiveToolMod;
import mods.tinker.tconstruct.library.TConstructRegistry;
import mods.tinker.tconstruct.library.crafting.ToolBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** NBTTags
 * Main tag - InfiTool
 * @see ToolBuilder
 * 
 * Required:
 * Head: Base and render tag, above the handle
 * Handle: Base and render tag, bottom layer
 * 
 * Damage: Replacement for metadata
 * MaxDamage: ItemStacks only read setMaxDamage()
 * Broken: Represents whether the tool is broken (boolean)
 * Attack: How much damage a mob will take
 * MiningSpeed: The speed at which a tool mines
 * 
 * Others: 
 * Accessory: Base and tag, above head. Sword guards, binding, etc
 * Effects: Render tag, top layer. Fancy effects like moss or diamond edge.
 * Render order: Handle > Head > Accessory > Effect1 > Effect2 > Effect3 > etc
 * Unbreaking: Reinforced in-game, 10% chance to not use durability per level
 * Stonebound: Mines faster as the tool takes damage, but has less attack
 * Spiny: Opposite of stonebound
 * 
 * Modifiers have their own tags.
 * @see ToolMod
 */

public abstract class ToolCore extends Item
{
    protected Random random = new Random();
    protected int damageVsEntity;
    public static Icon blankSprite;
    public static Icon emptyIcon;

    public ToolCore(int id, int baseDamage)
    {
        super(id);
        this.maxStackSize = 1;
        this.setMaxDamage(100);
        this.setUnlocalizedName("InfiTool");
        this.setCreativeTab(TConstructRegistry.toolTab);
        damageVsEntity = baseDamage;
        TConstructRegistry.addToolMapping(this);
        setNoRepair();
        canRepair = false;
    }

    @Deprecated
    public int getHeadType ()
    {
        return 0;
    }

    /** Determines crafting behavior with regards to durability
     * 0: None
     * 1: Adds handle modifier
     * 2: Averages part with the rest of the tool
     * @return type
     */

    public int durabilityTypeHandle ()
    {
        return 1;
    }

    public int durabilityTypeAccessory ()
    {
        return 0;
    }

    public int durabilityTypeExtra ()
    {
        return 0;
    }

    public int getModifierAmount ()
    {
        return 3;
    }

    public String getToolName ()
    {
        return this.getClass().getSimpleName();
    }

    /* Rendering */

    public HashMap<Integer, Icon> headIcons = new HashMap<Integer, Icon>();
    public HashMap<Integer, Icon> brokenIcons = new HashMap<Integer, Icon>();
    public HashMap<Integer, Icon> handleIcons = new HashMap<Integer, Icon>();
    public HashMap<Integer, Icon> accessoryIcons = new HashMap<Integer, Icon>();
    public HashMap<Integer, Icon> effectIcons = new HashMap<Integer, Icon>();
    public HashMap<Integer, Icon> extraIcons = new HashMap<Integer, Icon>();

    //Not liking this
    public HashMap<Integer, String> headStrings = new HashMap<Integer, String>();
    public HashMap<Integer, String> brokenHeadStrings = new HashMap<Integer, String>();
    public HashMap<Integer, String> handleStrings = new HashMap<Integer, String>();
    public HashMap<Integer, String> accessoryStrings = new HashMap<Integer, String>();
    public HashMap<Integer, String> effectStrings = new HashMap<Integer, String>();
    public HashMap<Integer, String> extraStrings = new HashMap<Integer, String>();

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses ()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses (int metadata)
    {
        return 9;
    }

    //Override me please!
    public int getPartAmount ()
    {
        return 3;
    }

    public abstract String getIconSuffix (int partType);

    public abstract String getEffectSuffix ();

    public abstract String getDefaultFolder ();

    public void registerPartPaths (int index, String[] location)
    {
        headStrings.put(index, location[0]);
        brokenHeadStrings.put(index, location[1]);
        handleStrings.put(index, location[2]);
        if (location.length > 3)
            accessoryStrings.put(index, location[3]);
        if (location.length > 4)
            extraStrings.put(index, location[4]);
    }

    public void registerEffectPath (int index, String location)
    {
        effectStrings.put(index, location);
    }

    @Override
    public void registerIcons (IconRegister iconRegister)
    {
        headIcons.clear();
        brokenIcons.clear();
        handleIcons.clear();
        accessoryIcons.clear();
        effectIcons.clear();
        Iterator iter = headStrings.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry pairs = (Map.Entry) iter.next();
            headIcons.put((Integer) pairs.getKey(), iconRegister.registerIcon((String) pairs.getValue()));
        }

        iter = brokenHeadStrings.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry pairs = (Map.Entry) iter.next();
            brokenIcons.put((Integer) pairs.getKey(), iconRegister.registerIcon((String) pairs.getValue()));
        }

        iter = handleStrings.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry pairs = (Map.Entry) iter.next();
            handleIcons.put((Integer) pairs.getKey(), iconRegister.registerIcon((String) pairs.getValue()));
        }

        if (getPartAmount() > 2)
        {
            iter = accessoryStrings.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry pairs = (Map.Entry) iter.next();
                accessoryIcons.put((Integer) pairs.getKey(), iconRegister.registerIcon((String) pairs.getValue()));
            }
        }

        if (getPartAmount() > 3)
        {
            iter = extraStrings.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry pairs = (Map.Entry) iter.next();
                extraIcons.put((Integer) pairs.getKey(), iconRegister.registerIcon((String) pairs.getValue()));
            }
        }

        iter = effectStrings.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry pairs = (Map.Entry) iter.next();
            effectIcons.put((Integer) pairs.getKey(), iconRegister.registerIcon((String) pairs.getValue()));
        }

        emptyIcon = iconRegister.registerIcon("tinker:blankface");
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIconFromDamage(int meta)
    {
        return blankSprite;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon (ItemStack stack, int renderPass)
    {
        NBTTagCompound tags = stack.getTagCompound();

        if (tags != null)
        {
            tags = stack.getTagCompound().getCompoundTag("InfiTool");
            if (renderPass < getPartAmount())
            {
                if (renderPass == 0) // Handle
                {
                    return handleIcons.get(tags.getInteger("RenderHandle"));
                }

                else if (renderPass == 1) // Head
                {
                    if (tags.getBoolean("Broken"))
                        return (brokenIcons.get(tags.getInteger("RenderHead")));
                    else
                        return (headIcons.get(tags.getInteger("RenderHead")));
                }

                else if (renderPass == 2) // Accessory
                {
                    return (accessoryIcons.get(tags.getInteger("RenderAccessory")));
                }

                else if (renderPass == 3) // Extra
                {
                    return (extraIcons.get(tags.getInteger("RenderExtra")));
                }
            }

            else
            {
                if (renderPass == getPartAmount())
                {
                    if (tags.hasKey("Effect1"))
                        return (effectIcons.get(tags.getInteger("Effect1")));
                }

                else if (renderPass == getPartAmount() + 1)
                {
                    if (tags.hasKey("Effect2"))
                        return (effectIcons.get(tags.getInteger("Effect2")));
                }

                else if (renderPass == getPartAmount() + 2)
                {
                    if (tags.hasKey("Effect3"))
                        return (effectIcons.get(tags.getInteger("Effect3")));
                }

                else if (renderPass == getPartAmount() + 3)
                {
                    if (tags.hasKey("Effect4"))
                        return (effectIcons.get(tags.getInteger("Effect4")));
                }

                else if (renderPass == getPartAmount() + 4)
                {
                    if (tags.hasKey("Effect5"))
                        return (effectIcons.get(tags.getInteger("Effect5")));
                }

                else if (renderPass == getPartAmount() + 5)
                {
                    if (tags.hasKey("Effect6"))
                        return (effectIcons.get(tags.getInteger("Effect6")));
                }
            }
            return blankSprite;
        }
        return emptyIcon;
    }

    /* Tags and information about the tool */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation (ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        if (!stack.hasTagCompound())
            return;

        NBTTagCompound tags = stack.getTagCompound();
        if (tags.hasKey("InfiTool"))
        {
            boolean broken = tags.getCompoundTag("InfiTool").getBoolean("Broken");
            if (broken)
                list.add("\u00A7oBroken");
            else
            {
                int head = tags.getCompoundTag("InfiTool").getInteger("Head");
                int handle = tags.getCompoundTag("InfiTool").getInteger("Handle");
                int binding = tags.getCompoundTag("InfiTool").getInteger("Accessory");
                int extra = tags.getCompoundTag("InfiTool").getInteger("Extra");

                String headName = getAbilityNameForType(head);
                if (!headName.equals(""))
                    list.add(getStyleForType(head) + headName);

                String handleName = getAbilityNameForType(handle);
                if (!handleName.equals("") && handle != head)
                    list.add(getStyleForType(handle) + handleName);

                if (getPartAmount() >= 3)
                {
                    String bindingName = getAbilityNameForType(binding);
                    if (!bindingName.equals("") && binding != head && binding != handle)
                        list.add(getStyleForType(binding) + bindingName);
                }

                if (getPartAmount() >= 4)
                {
                    String extraName = getAbilityNameForType(extra);
                    if (!extraName.equals("") && extra != head && extra != handle && extra != binding)
                        list.add(getStyleForType(extra) + extraName);
                }

                int unbreaking = tags.getCompoundTag("InfiTool").getInteger("Unbreaking");
                String reinforced = getReinforcedName(head, handle, binding, extra, unbreaking);
                if (!reinforced.equals(""))
                    list.add(reinforced);

                boolean displayToolTips = true;
                int tipNum = 0;
                while (displayToolTips)
                {
                    tipNum++;
                    String tooltip = "Tooltip" + tipNum;
                    if (tags.getCompoundTag("InfiTool").hasKey(tooltip))
                    {
                        String tipName = tags.getCompoundTag("InfiTool").getString(tooltip);
                        if (!tipName.equals(""))
                            list.add(tipName);
                    }
                    else
                        displayToolTips = false;
                }
            }
        }
    }

    public static String getStyleForType (int type)
    {
        return TConstructRegistry.getMaterial(type).style();
    }

    public String getAbilityNameForType (int type)
    {
        return TConstructRegistry.getMaterial(type).ability();
    }

    public String getReinforcedName (int head, int handle, int accessory, int extra, int unbreaking)
    {
        ToolMaterial headMat = TConstructRegistry.getMaterial(head);
        ToolMaterial handleMat = TConstructRegistry.getMaterial(handle);
        ToolMaterial accessoryMat = TConstructRegistry.getMaterial(accessory);
        ToolMaterial extraMat = TConstructRegistry.getMaterial(extra);

        int reinforced = 0;
        String style = "";
        int current = headMat.reinforced();
        if (current > 0)
        {
            style = headMat.style();
            reinforced = current;
        }
        current = handleMat.reinforced();
        if (current > 0 && current > reinforced)
        {
            style = handleMat.style();
            reinforced = current;
        }
        if (getPartAmount() >= 3)
        {
            current = accessoryMat.reinforced();
            if (current > 0 && current > reinforced)
            {
                style = accessoryMat.style();
                reinforced = current;
            }
        }
        if (getPartAmount() >= 4)
        {
            current = extraMat.reinforced();
            if (current > 0 && current > reinforced)
            {
                style = extraMat.style();
                reinforced = current;
            }
        }
        
        reinforced += unbreaking - reinforced;

        if (reinforced > 0)
        {
            return style + getReinforcedString(reinforced);
        }
        return "";
    }

    String getReinforcedString (int reinforced)
    {
        if (reinforced > 9)
            return "Unbreakable";
        String ret = "Reinforced ";
        switch (reinforced)
        {
        case 1:
            ret += "I";
            break;
        case 2:
            ret += "II";
            break;
        case 3:
            ret += "III";
            break;
        case 4:
            ret += "IV";
            break;
        case 5:
            ret += "V";
            break;
        case 6:
            ret += "VI";
            break;
        case 7:
            ret += "VII";
            break;
        case 8:
            ret += "VIII";
            break;
        case 9:
            ret += "IX";
            break;
        default:
            ret += "X";
            break;
        }
        return ret;
    }

    //Used for sounds and the like
    public void onEntityDamaged (World world, EntityPlayer player, Entity entity)
    {

    }

    /* Creative mode tools */
    static String[] toolMaterialNames = { "Wooden ", "Stone ", "Iron ", "Flint ", "Cactus ", "Bone ", "Obsidian ", "Netherrack ", "Slime ", "Paper ", "Cobalt ", "Ardite ", "Manyullyn ", "Copper ",
            "Bronze ", "Alumite ", "Steel ", "Slime " };

    @Override
	public void getSubItems (int id, CreativeTabs tab, List list)
    {
        for (int i = 0; i < 18; i++)
        {
            Item accessory = getAccessoryItem();
            ItemStack accessoryStack = accessory != null ? new ItemStack(getAccessoryItem(), 1, i) : null;
            Item extra = getExtraItem();
            ItemStack extraStack = extra != null ? new ItemStack(getExtraItem(), 1, i) : null;
            ItemStack tool = ToolBuilder.instance.buildTool(new ItemStack(getHeadItem(), 1, i), new ItemStack(getHandleItem(), 1, i), accessoryStack, extraStack, toolMaterialNames[i] + getToolName());
            if (tool == null)
            {
                System.out.println("Creative builder failed tool for "+toolMaterialNames[i] + this.getToolName());
                System.out.println("Make sure you do not have item ID conflicts");
            }
            else
            {
                tool.getTagCompound().getCompoundTag("InfiTool").setBoolean("Built", true);
                list.add(tool);
            }
        }
    }

    public abstract Item getHeadItem ();

    public abstract Item getAccessoryItem ();

    public Item getExtraItem ()
    {
        return null;
    }

    public Item getHandleItem ()
    {
        return TConstructRegistry.getItem("toolRod");//TContent.toolRod;
    }

    /* Updating */

    @Override
	public void onUpdate (ItemStack stack, World world, Entity entity, int par4, boolean par5)
    {
        for (ActiveToolMod mod : TConstructRegistry.activeModifiers)
        {
            mod.updateTool(this, stack, world, entity);
        }
    }

    /* Tool uses */

    //Types
    public abstract String[] toolCategories ();

    //Mining
    @Override
    public boolean onBlockStartBreak (ItemStack stack, int x, int y, int z, EntityPlayer player)
    {
        boolean cancelHarvest = false;
        for (ActiveToolMod mod : TConstructRegistry.activeModifiers)
        {
            if (mod.beforeBlockBreak(this, stack, x, y, z, player))
                cancelHarvest = true;
        }

        return cancelHarvest;
    }

    @Override
    public boolean onBlockDestroyed (ItemStack itemstack, World world, int blockID, int x, int y, int z, EntityLiving player)
    {
        return AbilityHelper.onBlockChanged(itemstack, world, blockID, x, y, z, player, random);
    }

    @Override
    public float getStrVsBlock (ItemStack stack, Block block, int meta)
    {
        NBTTagCompound tags = stack.getTagCompound();
        if (tags.getCompoundTag("InfiTool").getBoolean("Broken"))
            return 0.1f;
        return 1f;
    }

    // Attacking
    @Override
    public boolean onLeftClickEntity (ItemStack stack, EntityPlayer player, Entity entity)
    {
        AbilityHelper.onLeftClickEntity(stack, player, entity, this);
        return true;
    }

    @Override
    public boolean hitEntity (ItemStack stack, EntityLiving mob, EntityLiving player)
    {
        return true;
    }

    public boolean pierceArmor ()
    {
        return false;
    }

    public float chargeAttack ()
    {
        return 1f;
    }

    @Override
	public int getDamageVsEntity (Entity par1Entity)
    {
        return this.damageVsEntity;
    }

    //Changes how much durability the base tool has
    public float getDurabilityModifier ()
    {
        return 1f;
    }

    public float getRepairCost ()
    {
        return getDurabilityModifier();
    }

    public float getDamageModifier ()
    {
        return 1.0f;
    }

    //Right-click
    @Override
	public boolean onItemUse (ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float clickX, float clickY, float clickZ)
    {
        /*if (world.isRemote)
            return true;*/

        int posX = x;
        int posY = y;
        int posZ = z;
        int playerPosX = (int) Math.floor(player.posX);
        int playerPosY = (int) Math.floor(player.posY);
        int playerPosZ = (int) Math.floor(player.posZ);
        if (side == 0)
        {
            --posY;
        }

        if (side == 1)
        {
            ++posY;
        }

        if (side == 2)
        {
            --posZ;
        }

        if (side == 3)
        {
            ++posZ;
        }

        if (side == 4)
        {
            --posX;
        }

        if (side == 5)
        {
            ++posX;
        }
        if (posX == playerPosX && (posY == playerPosY || posY == playerPosY + 1 || posY == playerPosY - 1) && posZ == playerPosZ)
        {
            return false;
        }

        boolean used = false;
        int hotbarSlot = player.inventory.currentItem;
        int itemSlot = hotbarSlot == 0 ? 8 : hotbarSlot + 1;
        ItemStack nearbyStack = null;
        
        if (hotbarSlot < 8)
        {
            nearbyStack = player.inventory.getStackInSlot(itemSlot);
            if (nearbyStack != null && nearbyStack.getItem() instanceof ItemBlock)
            {
                used = nearbyStack.getItem().onItemUse(nearbyStack, player, world, x, y, z, side, clickX, clickY, clickZ);
                if (nearbyStack.stackSize < 1)
                {
                    nearbyStack = null;
                    player.inventory.setInventorySlotContents(itemSlot, null);
                }
            }
        }
        
        /*if (used) //Update client
        {
            Packet103SetSlot packet = new Packet103SetSlot(player.openContainer.windowId, itemSlot, nearbyStack);
            ((EntityPlayerMP)player).playerNetServerHandler.sendPacketToPlayer(packet);
        }*/
        return used;
    }

    //Vanilla overrides
    @Override
	public boolean isItemTool (ItemStack par1ItemStack)
    {
        return false;
    }

    @Override
    public boolean getIsRepairable (ItemStack par1ItemStack, ItemStack par2ItemStack)
    {
        return false;
    }

    @Override
	public boolean isRepairable ()
    {
        return false;
    }

    @Override
	public int getItemEnchantability ()
    {
        return 0;
    }

    @Override
	public boolean isFull3D ()
    {
        return true;
    }

    /* Proper stack damage */
    @Override
	public int getItemDamageFromStack (ItemStack stack)
    {
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null)
        {
            //System.out.println("Tool item is uninitalized! This method should never be called with a default item");
            //Exception e = new NullPointerException();
            //e.printStackTrace();
            return 0;
        }

        return tags.getCompoundTag("InfiTool").getInteger("Damage");
    }

    @Override
	public int getItemDamageFromStackForDisplay (ItemStack stack)
    {
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null)
        {
            //System.out.println("Tool item is uninitalized! This method should never be called with a default item");
            //Exception e = new NullPointerException();
            //e.printStackTrace();
            return 0;
        }

        return tags.getCompoundTag("InfiTool").getInteger("Damage");
    }

    @Override
	public int getItemMaxDamageFromStack (ItemStack stack)
    {
        NBTTagCompound tags = stack.getTagCompound();
        if (tags == null)
        {
            //System.out.println("Tool item is uninitalized! This method should never be called with a default item");
            //Exception e = new NullPointerException();
            //e.printStackTrace();
            return 0;
        }
        return tags.getCompoundTag("InfiTool").getInteger("TotalDurability");
    }

}
