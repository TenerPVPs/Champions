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

package c4.champions.common.affix.affix;

import c4.champions.common.affix.core.AffixBase;
import c4.champions.common.affix.core.AffixCategory;
import c4.champions.common.capability.IChampionship;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

public class AffixDampening extends AffixBase {

    public AffixDampening() {
        super("dampening", AffixCategory.DEFENSE);
    }

    @Override
    public float onHurt(EntityLiving entity, IChampionship cap, DamageSource source, float amount, float newAmount) {
        return source instanceof EntityDamageSourceIndirect ? newAmount * 0.2f : newAmount;
    }
}
