/*
 * Copyright (C) 2018  C4
 *
 * This file is part of Champions, a mod made for Minecraft.
 *
 * Champions is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Champions is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Champions.  If not, see <https://www.gnu.org/licenses/>.
 */

package c4.champions.common.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityArcticSpark extends Entity {

    private EntityLivingBase owner;
    private Entity target;
    private EnumFacing direction;
    private int steps;
    private double targetDeltaX;
    private double targetDeltaY;
    private double targetDeltaZ;
    private UUID ownerUniqueId;
    private BlockPos ownerBlockPos;
    private UUID targetUniqueId;
    private BlockPos targetBlockPos;

    public EntityArcticSpark(World worldIn) {
        super(worldIn);
        this.setSize(0.3125F, 0.3125F);
        this.noClip = true;
    }

    @Override
    @Nonnull
    public SoundCategory getSoundCategory()
    {
        return SoundCategory.HOSTILE;
    }

    @SideOnly(Side.CLIENT)
    public EntityArcticSpark(World worldIn, double x, double y, double z, double motionXIn, double motionYIn, double motionZIn) {
        this(worldIn);
        this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
        this.motionX = motionXIn;
        this.motionY = motionYIn;
        this.motionZ = motionZIn;
    }

    public EntityArcticSpark(World worldIn, EntityLivingBase ownerIn, Entity targetIn, EnumFacing.Axis directionIn) {
        this(worldIn);
        this.owner = ownerIn;
        BlockPos blockpos = new BlockPos(ownerIn);
        double d0 = (double)blockpos.getX() + 0.5D;
        double d1 = (double)blockpos.getY() + 0.5D;
        double d2 = (double)blockpos.getZ() + 0.5D;
        this.setLocationAndAngles(d0, d1, d2, this.rotationYaw, this.rotationPitch);
        this.target = targetIn;
        this.direction = EnumFacing.UP;
        this.selectNextMoveDirection(directionIn);
    }

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound compound) {

        if (this.owner != null) {
            BlockPos blockpos = new BlockPos(this.owner);
            NBTTagCompound nbttagcompound = NBTUtil.createUUIDTag(this.owner.getUniqueID());
            nbttagcompound.setInteger("X", blockpos.getX());
            nbttagcompound.setInteger("Y", blockpos.getY());
            nbttagcompound.setInteger("Z", blockpos.getZ());
            compound.setTag("Owner", nbttagcompound);
        }

        if (this.target != null) {
            BlockPos blockpos1 = new BlockPos(this.target);
            NBTTagCompound nbttagcompound1 = NBTUtil.createUUIDTag(this.target.getUniqueID());
            nbttagcompound1.setInteger("X", blockpos1.getX());
            nbttagcompound1.setInteger("Y", blockpos1.getY());
            nbttagcompound1.setInteger("Z", blockpos1.getZ());
            compound.setTag("Target", nbttagcompound1);
        }

        if (this.direction != null) {
            compound.setInteger("Dir", this.direction.getIndex());
        }
        compound.setInteger("Steps", this.steps);
        compound.setDouble("TXD", this.targetDeltaX);
        compound.setDouble("TYD", this.targetDeltaY);
        compound.setDouble("TZD", this.targetDeltaZ);
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound compound) {
        this.steps = compound.getInteger("Steps");
        this.targetDeltaX = compound.getDouble("TXD");
        this.targetDeltaY = compound.getDouble("TYD");
        this.targetDeltaZ = compound.getDouble("TZD");

        if (compound.hasKey("Dir", 99)) {
            this.direction = EnumFacing.byIndex(compound.getInteger("Dir"));
        }

        if (compound.hasKey("Owner", 10)) {
            NBTTagCompound nbttagcompound = compound.getCompoundTag("Owner");
            this.ownerUniqueId = NBTUtil.getUUIDFromTag(nbttagcompound);
            this.ownerBlockPos = new BlockPos(nbttagcompound.getInteger("X"), nbttagcompound.getInteger("Y"), nbttagcompound.getInteger("Z"));
        }

        if (compound.hasKey("Target", 10)) {
            NBTTagCompound nbttagcompound1 = compound.getCompoundTag("Target");
            this.targetUniqueId = NBTUtil.getUUIDFromTag(nbttagcompound1);
            this.targetBlockPos = new BlockPos(nbttagcompound1.getInteger("X"), nbttagcompound1.getInteger("Y"), nbttagcompound1.getInteger("Z"));
        }
    }

    @Override
    protected void entityInit()
    {
    }

    private void setDirection(@Nullable EnumFacing directionIn)
    {
        this.direction = directionIn;
    }

    private void selectNextMoveDirection(@Nullable EnumFacing.Axis direction) {
        double d0 = 0.5D;
        BlockPos blockpos;

        if (this.target == null) {
            blockpos = (new BlockPos(this)).down();
        } else {
            d0 = (double)this.target.height * 0.5D;
            blockpos = new BlockPos(this.target.posX, this.target.posY + d0, this.target.posZ);
        }
        double d1 = (double)blockpos.getX() + 0.5D;
        double d2 = (double)blockpos.getY() + d0;
        double d3 = (double)blockpos.getZ() + 0.5D;
        EnumFacing enumfacing = null;

        if (blockpos.distanceSqToCenter(this.posX, this.posY, this.posZ) >= 4.0D) {
            BlockPos blockpos1 = new BlockPos(this);
            List<EnumFacing> list = Lists.<EnumFacing>newArrayList();

            if (direction != EnumFacing.Axis.X) {

                if (blockpos1.getX() < blockpos.getX() && this.world.isAirBlock(blockpos1.east())) {
                    list.add(EnumFacing.EAST);
                } else if (blockpos1.getX() > blockpos.getX() && this.world.isAirBlock(blockpos1.west())) {
                    list.add(EnumFacing.WEST);
                }
            }

            if (direction != EnumFacing.Axis.Y) {

                if (blockpos1.getY() < blockpos.getY() && this.world.isAirBlock(blockpos1.up())) {
                    list.add(EnumFacing.UP);
                } else if (blockpos1.getY() > blockpos.getY() && this.world.isAirBlock(blockpos1.down())) {
                    list.add(EnumFacing.DOWN);
                }
            }

            if (direction != EnumFacing.Axis.Z) {
                if (blockpos1.getZ() < blockpos.getZ() && this.world.isAirBlock(blockpos1.south())) {
                    list.add(EnumFacing.SOUTH);
                } else if (blockpos1.getZ() > blockpos.getZ() && this.world.isAirBlock(blockpos1.north())) {
                    list.add(EnumFacing.NORTH);
                }
            }
            enumfacing = EnumFacing.random(this.rand);

            if (list.isEmpty()) {

                for (int i = 5; !this.world.isAirBlock(blockpos1.offset(enumfacing)) && i > 0; --i) {
                    enumfacing = EnumFacing.random(this.rand);
                }
            } else {
                enumfacing = list.get(this.rand.nextInt(list.size()));
            }
            d1 = this.posX + (double)enumfacing.getXOffset();
            d2 = this.posY + (double)enumfacing.getYOffset();
            d3 = this.posZ + (double)enumfacing.getZOffset();
        }

        this.setDirection(enumfacing);
        double d6 = d1 - this.posX;
        double d7 = d2 - this.posY;
        double d4 = d3 - this.posZ;
        double d5 = (double) MathHelper.sqrt(d6 * d6 + d7 * d7 + d4 * d4);

        if (d5 == 0.0D) {
            this.targetDeltaX = 0.0D;
            this.targetDeltaY = 0.0D;
            this.targetDeltaZ = 0.0D;
        } else {
            this.targetDeltaX = d6 / d5 * 0.15D;
            this.targetDeltaY = d7 / d5 * 0.15D;
            this.targetDeltaZ = d4 / d5 * 0.15D;
        }
        this.isAirBorne = true;
        this.steps = 10 + this.rand.nextInt(5) * 10;
    }

    @Override
    public void onUpdate() {

        if (!this.world.isRemote && this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
            this.setDead();
        } else {
            super.onUpdate();

            if (!this.world.isRemote) {

                if (this.target == null && this.targetUniqueId != null) {

                    for (EntityLivingBase entitylivingbase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.targetBlockPos.add(-2, -2, -2), this.targetBlockPos.add(2, 2, 2)))) {

                        if (entitylivingbase.getUniqueID().equals(this.targetUniqueId)) {
                            this.target = entitylivingbase;
                            break;
                        }
                    }
                    this.targetUniqueId = null;
                }

                if (this.owner == null && this.ownerUniqueId != null) {

                    for (EntityLivingBase entitylivingbase1 : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.ownerBlockPos.add(-2, -2, -2), this.ownerBlockPos.add(2, 2, 2)))) {

                        if (entitylivingbase1.getUniqueID().equals(this.ownerUniqueId)) {
                            this.owner = entitylivingbase1;
                            break;
                        }
                    }

                    this.ownerUniqueId = null;
                }

                if (this.target == null || !this.target.isEntityAlive() || this.target instanceof EntityPlayer && ((EntityPlayer)this.target).isSpectator()) {

                    if (!this.hasNoGravity()) {
                        this.motionY -= 0.04D;
                    }
                } else {
                    this.targetDeltaX = MathHelper.clamp(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
                    this.targetDeltaY = MathHelper.clamp(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
                    this.targetDeltaZ = MathHelper.clamp(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);
                    this.motionX += (this.targetDeltaX - this.motionX) * 0.2D;
                    this.motionY += (this.targetDeltaY - this.motionY) * 0.2D;
                    this.motionZ += (this.targetDeltaZ - this.motionZ) * 0.2D;
                }
                RayTraceResult raytraceresult = ProjectileHelper.forwardsRaycast(this, true, false, this.owner);

                if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.bulletHit(raytraceresult);
                }
            }
            this.setPosition(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            ProjectileHelper.rotateTowardsMovement(this, 0.5F);

            if (this.world.isRemote) {
                this.world.spawnParticle(EnumParticleTypes.SNOWBALL, this.posX - this.motionX, this.posY - this.motionY +
                        0.15D, this.posZ - this.motionZ, 0.0D, 0.0D, 0.0D);
            } else if (this.target != null && !this.target.isDead) {

                if (this.steps > 0) {
                    --this.steps;

                    if (this.steps == 0) {
                        this.selectNextMoveDirection(this.direction == null ? null : this.direction.getAxis());
                    }
                }

                if (this.direction != null) {
                    BlockPos blockpos = new BlockPos(this);
                    EnumFacing.Axis enumfacing$axis = this.direction.getAxis();

                    if (this.world.isBlockNormalCube(blockpos.offset(this.direction), false)) {
                        this.selectNextMoveDirection(enumfacing$axis);
                    } else {
                        BlockPos blockpos1 = new BlockPos(this.target);

                        if (enumfacing$axis == EnumFacing.Axis.X && blockpos.getX() == blockpos1.getX() || enumfacing$axis == EnumFacing.Axis.Z && blockpos.getZ() == blockpos1.getZ() || enumfacing$axis == EnumFacing.Axis.Y && blockpos.getY() == blockpos1.getY()) {
                            this.selectNextMoveDirection(enumfacing$axis);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isBurning()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        return distance < 16384.0D;
    }

    @Override
    public float getBrightness()
    {
        return 1.0F;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender()
    {
        return 15728880;
    }

    protected void bulletHit(RayTraceResult result) {

        if (result.entityHit == null) {
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 2, 0.2D, 0.2D, 0.2D, 0.0D);
            this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0F, 1.0F);
        } else {
            if (result.entityHit instanceof EntityLivingBase) {
                EntityLivingBase hit = ((EntityLivingBase)result.entityHit);
                hit.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
                hit.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 2));
            }
        }
        this.setDead();
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean attackEntityFrom(@Nonnull DamageSource source, float amount) {

        if (!this.world.isRemote) {
            this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0F, 1.0F);
            ((WorldServer)this.world).spawnParticle(EnumParticleTypes.CRIT, this.posX, this.posY, this.posZ, 15, 0.2D, 0.2D, 0.2D, 0.0D);
            this.setDead();
        }
        return true;
    }
}
