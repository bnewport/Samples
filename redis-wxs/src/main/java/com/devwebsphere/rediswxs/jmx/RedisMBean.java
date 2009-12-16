package com.devwebsphere.rediswxs.jmx;

public interface RedisMBean {

	 void resetStatistics();

	 int getSetCount();

	 double getSetTimeMinMS();

	 double getSetTimeMaxMS();

	 double getSetTimeAvgMS();

	 int getGetCount();

	 double getGetTimeMinMS();

	 double getGetTimeMaxMS();

	 double getGetTimeAvgMS();

	 int getRemoveCount();

	 double getRemoveTimeMinMS();

	 double getRemoveTimeMaxMS();

	 double getRemoveTimeAvgMS();

	 int getInvalidateCount();

	 double getInvalidateTimeMinMS();

	 double getInvalidateTimeMaxMS();

	 double getInvalidateTimeAvgMS();

	 int getIncrCount();

	 double getIncrTimeMinMS();

	 double getIncrTimeMaxMS();

	 double getIncrTimeAvgMS();

	 int getLtrimCount();

	 double getLtrimTimeMinMS();

	 double getLtrimTimeMaxMS();

	 double getLtrimTimeAvgMS();

	 int getLpushCount();

	 double getLpushTimeMinMS();

	 double getLpushTimeMaxMS();

	 double getLpushTimeAvgMS();

	 int getLpopCount();

	 double getLpopTimeMinMS();

	 double getLpopTimeMaxMS();

	 double getLpopTimeAvgMS();

	 int getRpushCount();

	 double getRpushTimeMinMS();

	 double getRpushTimeMaxMS();

	 double getRpushTimeAvgMS();

	 int getRpopCount();

	 double getRpopTimeMinMS();

	 double getRpopTimeMaxMS();

	 double getRpopTimeAvgMS();

	 int getLrangeCount();

	 double getLrangeTimeMinMS();

	 double getLrangeTimeMaxMS();

	 double getLrangeTimeAvgMS();

	 int getLlenCount();

	 double getLlenTimeMinMS();

	 double getLlenTimeMaxMS();

	 double getLlenTimeAvgMS();

	 int getSaddCount();

	 double getSaddTimeMinMS();

	 double getSaddTimeMaxMS();

	 double getSaddTimeAvgMS();

	 int getSremCount();

	 double getSremTimeMinMS();

	 double getSremTimeMaxMS();

	 double getSremTimeAvgMS();

	 int getScardCount();

	 double getScardTimeMinMS();

	 double getScardTimeMaxMS();

	 double getScardTimeAvgMS();

	 int getSisMemberCount();

	 double getSisMemberTimeMinMS();

	 double getSisMemberTimeMaxMS();

	 double getSisMemberTimeAvgMS();

	 int getSinterCount();

	 double getSinterTimeMinMS();

	 double getSinterTimeMaxMS();

	 double getSinterTimeAvgMS();

	 int getSmembersCount();

	 double getSmembersTimeMinMS();

	 double getSmembersTimeMaxMS();

	 double getSmembersTimeAvgMS();
}