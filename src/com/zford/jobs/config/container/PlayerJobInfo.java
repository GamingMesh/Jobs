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

package com.zford.jobs.config.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import com.zford.jobs.Jobs;
import com.zford.jobs.config.JobsConfiguration;
import com.zford.jobs.dao.JobsDAO;
import com.zford.jobs.dao.container.JobsDAOData;
import com.zford.jobs.event.JobsLevelUpEvent;
import com.zford.jobs.event.JobsSkillUpEvent;
import com.zford.jobs.util.DisplayMethod;

public class PlayerJobInfo 
{
	// the player the object belongs to
	private Player player;
	// list of all jobs that the player does
	private List<Job> jobs;
	// progression of the player in each job
	private HashMap<Job, JobProgression> progression;
	// display honorific
	private String honorific = null;
		
	/**
	 * Constructor.
	 * Reads data storage and configures itself.
	 * @param player - the player that has just logged in
	 * @param dao - the data access object
	 */
	public PlayerJobInfo(Player player, JobsDAO dao){
		// set player link
		this.player = player;
		this.jobs = new ArrayList<Job>();
		this.progression = new HashMap<Job, JobProgression>();
		// for all jobs players have
		List<JobsDAOData> list = dao.getAllJobs(player);
		if(list != null){
			for(JobsDAOData job: list){
				if(JobsConfiguration.getInstance().getJob(job.getJobName()) != null){
					// add the job
					jobs.add(JobsConfiguration.getInstance().getJob(job.getJobName()));
					
					// create the progression object
					JobProgression jobProgression = 
						new JobProgression(JobsConfiguration.getInstance().getJob(job.getJobName()), job.getExperience(),job.getLevel(), this);
					// calculate the max level
					
					// add the progression level.
					progression.put(jobProgression.getJob(), jobProgression);
				}
			}
		}
		reloadMaxExperience();
		reloadHonorific();
	}
	
	/**
	 * Broke a block.
	 * 
	 * Give correct experience and income
	 * 
	 * @param block - the block broken
	 */
	public void broke(Block block, Jobs plugin)
	{
		//Check to see if it was placed by a player, thus making it worthless
		if(plugin.placedBlocks.contains(block))
			return;
		
		//
		
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> temp: progression.entrySet()){
			// add the current level to the parameter list
			param.put("joblevel", (double)temp.getValue().getLevel());
			// get the income and give it
			Double income = temp.getKey().getBreakIncome(block, param);
			if(income != null){
				JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
				temp.getValue().addExp(temp.getKey().getBreakExp(block, param));
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = JobsConfiguration.getInstance().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getBreakIncome(block, param);
                if(income != null) {
                    // give income
                    JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
                }
                param.remove("joblevel");
            }
        }
        JobsConfiguration.getInstance().getEconomyLink().updateStats(player);
	}
	
	/**
	 * Placed a block.
	 * 
	 * Give correct experience and income
	 * 
	 * @param block - the block placed
	 */
	public void placed(Block block){
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> temp: progression.entrySet()){
			// add the current level to the parameter list
			param.put("joblevel", (double)temp.getValue().getLevel());
			// get the income and give it
			Double income = temp.getKey().getPlaceIncome(block, param);
			if(income != null){
				// give income
				JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
				temp.getValue().addExp(temp.getKey().getPlaceExp(block, param));
				checkLevels();
			}
			param.remove("joblevel");
		}
		// no job
        if(this.progression.size() == 0) {
            Job jobNone = JobsConfiguration.getInstance().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getPlaceIncome(block, param);
                if(income != null) {
                    // give income
                    JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
                }
                param.remove("joblevel");
            }
        }
		JobsConfiguration.getInstance().getEconomyLink().updateStats(player);
	}
	
	/**
	 * Killed a living entity or owned wolf killed living entity.
	 * 
	 * Give correct experience and income
	 * 
	 * @param mob - the mob killed
	 */
	public void killed(String victim){
		HashMap<String, Double> param = new HashMap<String, Double>();
		// add the number of jobs to the parameter list
		param.put("numjobs", (double)progression.size());
		for(Entry<Job, JobProgression> temp: progression.entrySet()){
			// add the current level to the parameter list
			param.put("joblevel", (double)temp.getValue().getLevel());
			// get the income and give it
			Double income = temp.getKey().getKillIncome(victim, param);
			if(income != null){
				// give income
				JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
				temp.getValue().addExp(temp.getKey().getKillExp(victim, param));
				checkLevels();	
			}
			param.remove("joblevel");
		}
		// no job
		if(this.progression.size() == 0) {
		    Job jobNone = JobsConfiguration.getInstance().getJob("None");
		    if(jobNone != null) {
    		    param.put("joblevel", 1.0);
    		    Double income = jobNone.getKillIncome(victim, param);
    		    if(income != null) {
    		        // give income
    		        JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
    		    }
    		    param.remove("joblevel");
		    }
		}
		JobsConfiguration.getInstance().getEconomyLink().updateStats(player);
	}
	
	/**
	 * Fished an item
	 * 
	 * Give correct experience and income
	 * 
	 * @param item - the item fished
	 */
	public void fished(Item item) {
	    HashMap<String, Double> param = new HashMap<String, Double>();
	    param.put("numjobs", (double)progression.size());
	    for(Entry<Job, JobProgression> temp: progression.entrySet()){
            // add the current level to the parameter list
            param.put("joblevel", (double)temp.getValue().getLevel());
            // get the income and give it
            Double income = temp.getKey().getFishIncome(item, param);
            if(income != null){
                // give income
                JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
                temp.getValue().addExp(temp.getKey().getFishExp(item, param));
                checkLevels();
            }
            param.remove("joblevel");
        }
	    // no job
        if(this.progression.size() == 0) {
            Job jobNone = JobsConfiguration.getInstance().getJob("None");
            if(jobNone != null) {
                param.put("joblevel", 1.0);
                Double income = jobNone.getFishIncome(item, param);
                if(income != null) {
                    // give income
                    JobsConfiguration.getInstance().getEconomyLink().pay(player, income);
                }
                param.remove("joblevel");
            }
        }
        JobsConfiguration.getInstance().getEconomyLink().updateStats(player);
	}
	
	/**
	 * Get the list of jobs
	 * @return the list of jobs
	 */
	public List<Job> getJobs(){
		return jobs;
	}
	
	/**
	 * Get the list of job progressions
	 * @return the list of job progressions
	 */
	public Collection<JobProgression> getJobsProgression(){
		return progression.values();
	}
	
	/**
	 * Get the job progression with the certain job
	 * @return the job progression
	 */
	public JobProgression getJobsProgression(Job job){
		return progression.get(job);
	}
	
	/**
	 * get the player
	 * @return the player
	 */
	public Player getPlayer(){
		return player;
	}
	
	/**
	 * Function called to update the levels and make sure experience < maxExperience
	 */
	public void checkLevels(){
		for(JobProgression temp: progression.values()){
			if(temp.canLevelUp()){
				// user would level up, call the joblevelupevent
				if(Jobs.getJobsServer() != null){
					JobsLevelUpEvent event = new JobsLevelUpEvent(player, temp, this);
					Jobs.getJobsServer().getPluginManager().callEvent(event);
				}
			}
			
			if(JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()) != null && !JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()).equals(temp.getTitle())){
				// user would skill up
				if(Jobs.getJobsServer() != null){
					JobsSkillUpEvent event = new JobsSkillUpEvent(player, temp, JobsConfiguration.getInstance().getTitleForLevel(temp.getLevel()));
					Jobs.getJobsServer().getPluginManager().callEvent(event);
				}
			}
		}
	}
	
	public String getDisplayHonorific(){
		
		String honorific = "";		
		
		if(jobs.size() > 1){
			// has more than 1 job - using shortname mode
			for(JobProgression temp: progression.values()){
				if(temp.getJob().getDisplayMethod().equals(DisplayMethod.FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.TITLE) ||
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_TITLE)){
					// add title to honorific
					if(temp.getTitle() != null){
						honorific += temp.getTitle().getChatColor() + temp.getTitle().getShortName() + ChatColor.WHITE;
					}
				}
				
				if(temp.getJob().getDisplayMethod().equals(DisplayMethod.FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.JOB) ||
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || 
						temp.getJob().getDisplayMethod().equals(DisplayMethod.SHORT_JOB)){
					honorific += temp.getJob().getChatColour() + temp.getJob().getShortName() + ChatColor.WHITE;
				}
				
				if(!temp.getJob().getDisplayMethod().equals(DisplayMethod.NONE)){
					honorific+=" ";
				}
			}
		}
		else{
		    Job job;
		    if(jobs.size() == 0) {
		        job = JobsConfiguration.getInstance().getJob("None");
		    } else {
		        job = jobs.get(0);
		    }
		    
            if(job == null) {
                return null;
            }
            
		    JobProgression jobProgression = progression.get(job);
		    
			// using longname mode
			if(job.getDisplayMethod().equals(DisplayMethod.FULL) || job.getDisplayMethod().equals(DisplayMethod.TITLE)){
				// add title to honorific
				if(jobProgression != null && jobProgression.getTitle() != null){
					honorific += jobProgression.getTitle().getChatColor() + jobProgression.getTitle().getName() + ChatColor.WHITE;
				}
				if(job.getDisplayMethod().equals(DisplayMethod.FULL)){
					honorific += " ";
				}
			}
			if(job.getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || job.getDisplayMethod().equals(DisplayMethod.SHORT_TITLE)){
				// add title to honorific
				if(jobProgression != null && jobProgression.getTitle() != null){
					honorific += jobProgression.getTitle().getChatColor() + jobProgression.getTitle().getShortName() + ChatColor.WHITE;
				}
			}
			
			if(job.getDisplayMethod().equals(DisplayMethod.FULL) || job.getDisplayMethod().equals(DisplayMethod.JOB)){
				honorific += job.getChatColour() + job.getName() + ChatColor.WHITE;
			}
			if(job.getDisplayMethod().equals(DisplayMethod.SHORT_FULL) || job.getDisplayMethod().equals(DisplayMethod.SHORT_JOB)){
				honorific += job.getChatColour() + job.getShortName() + ChatColor.WHITE;
			}
		}
		
		if(honorific.equals("")){
			return null;
		}else{
			return honorific.trim();
		}
	}
	
	/**
	 * Player joins a job
	 * @param job - the job joined
	 */
	public void joinJob(Job job){
		jobs.add(job);
		progression.put(job, new JobProgression(job, 0.0, 1, this));
	}
	
	/**
	 * Player leaves a job
	 * @param job - the job left
	 */
	public void leaveJob(Job job){
		jobs.remove(job);
		progression.remove(job);
	}
	
	/**
	 * Player leaves a job
	 * @param job - the job left
	 */
	public void transferJob(Job oldjob, Job newjob){
		JobProgression prog = progression.get(oldjob);
		jobs.remove(oldjob);
		jobs.add(newjob);
		progression.remove(oldjob);
		prog.setJob(newjob);
		progression.put(newjob, prog);
		
	}
	
	/**
	 * Checks if the player is in this job.
	 * @param job - the job
	 * @return true - they are in the job
	 * @return false - they are not in the job
	 */
	public boolean isInJob(Job job){
		return jobs.contains(job);
	}
	
	/**
	 * Function that reloads your honorific
	 */
	public void reloadHonorific(){
		String newHonorific = getDisplayHonorific();
		if(newHonorific == null && honorific != null){
			// strip the current honorific.
			player.setDisplayName(player.getDisplayName().trim().replaceFirst(honorific + " ", "").trim());
		}
		else if (newHonorific != null && honorific != null){
			// replace the honorific
			player.setDisplayName(player.getDisplayName().trim().replaceFirst(honorific, newHonorific).trim());
		}
		else if(newHonorific != null && honorific == null){
			// new honorific
			player.setDisplayName((newHonorific + " " + player.getDisplayName().trim()).trim());
		}
		// set the new honorific
		honorific = newHonorific;
	}
	
	/**
	 * Function to reload all of the maximum experiences
	 */
	public void reloadMaxExperience(){
		HashMap<String, Double> param = new HashMap<String, Double>();
		param.put("numjobs", (double) progression.size());
		for(JobProgression temp: progression.values()){
			param.put("joblevel", (double) temp.getLevel());
			temp.setMaxExperience((int)temp.getJob().getMaxExp(param));
			param.remove("joblevel");
		}
	}
}
