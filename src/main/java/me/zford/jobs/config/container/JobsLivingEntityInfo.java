/*
 * Jobs Plugin for Bukkit
 * Copyright (C) 2011  Zak Ford <zak.j.ford@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package me.zford.jobs.config.container;

import java.util.Map;

import me.zford.jobs.resources.jfep.Parser;

public class JobsLivingEntityInfo {
    private Class<?> livingEntityClass;
    private double xpGiven;
    private double moneyGiven;
    private double restrictionLevel;

    /**
     * Constructor
     * 
     * @param livingEntityClass
     *            - LivingEntity that the class represents
     * @param xpGiven
     *            - base xp given for the LivingEntity
     * @param moneyGiven
     *            - base money given for the LivingEntity
     * @param restricted
     *            - if the kill should be restricted
     */
    public JobsLivingEntityInfo(Class<?> livingEntityClass, double xpGiven, double moneyGiven, double restrictionLevel) {
	this.livingEntityClass = livingEntityClass;
	this.xpGiven = xpGiven;
	this.moneyGiven = moneyGiven;
	this.restrictionLevel = restrictionLevel;
    }

    /**
     * Function to return the class the block represents
     * 
     * @return The class the block represents.
     */
    public Class<?> getLivingEntityClass() {
	return livingEntityClass;
    }

    /**
     * Function to get the base xp given for this LivingEntity
     * 
     * @return the xp given for this LivingEntity
     */
    public double getXpGiven() {
	return xpGiven;
    }

    /**
     * Function to get the base money given for this LivingEntity
     * 
     * @return the money given for this LivingEntity
     */
    public double getMoneyGiven() {
	return moneyGiven;
    }

    /**
     * Function to get the money that should be paid out for this block
     * 
     * @param equation
     *            - equation to calculate the payout
     * @param mob
     *            - mob in question
     * @param parameters
     *            - equation parameters
     * @return the money given
     * @return null if it isn't the mob
     */
    public Double getMoneyFromKill(Parser equation, String mob, Map<String, Double> parameters) {
	for (Map.Entry<String, Double> temp : parameters.entrySet())
	    equation.setVariable(temp.getKey(), temp.getValue());
	equation.setVariable("baseincome", moneyGiven);
	return equation.getValue();
    }

    /**
     * Function to get the experience that should be paid out for this block
     * 
     * @param equation
     *            - equation to calculate the experience
     * @param mob
     *            - mob in question
     * @param parameters
     *            - equation parameters
     * @return the experience given
     * @return null if it isn't the mob
     */
    public Double getXPFromKill(Parser equation, String mob, Map<String, Double> parameters) {
	for (Map.Entry<String, Double> temp : parameters.entrySet())
	    equation.setVariable(temp.getKey(), temp.getValue());
	equation.setVariable("baseexperience", xpGiven);
	return equation.getValue();
    }

    /**
     * Function to get the restriction for this LivingEntity
     * 
     * @return the restriction this LivingEntity
     */
    public Double getRestrictionLevel() {
	return restrictionLevel;
    }
}
