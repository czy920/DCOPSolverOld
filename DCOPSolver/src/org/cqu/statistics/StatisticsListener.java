package org.cqu.statistics;

public interface StatisticsListener {
	void newStatistics(String statisticsSourceName, String statisticsKey, Object statisticsValue);
}
