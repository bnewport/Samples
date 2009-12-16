package com.devwebsphere.rediswxs.jmx;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;

public final class RedisMBeanImpl implements RedisMBean 
{
	MinMaxAvgMetric setMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric getMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric removeMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric invalidateMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric incrMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric ltrimMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric lpushMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric lpopMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric rpushMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric rpopMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric lrangeMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric llenMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric saddMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric sremMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric scardMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric sismemberMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric sinterMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric smembersMetrics = new MinMaxAvgMetric();
	
	final static double TIME_SCALE_NS_MS = 1000000.0;
	
	public RedisMBeanImpl()
	{
	}
	
	public void resetStatistics()
	{
		setMetrics.reset();
		getMetrics.reset();
		removeMetrics.reset();
		invalidateMetrics.reset();
		incrMetrics.reset();
		ltrimMetrics.reset();
		lpushMetrics.reset();
		lpopMetrics.reset();
		rpushMetrics.reset();
		rpopMetrics.reset();
		lrangeMetrics.reset();
		llenMetrics.reset();
		saddMetrics.reset();
		sremMetrics.reset();
		scardMetrics.reset();
		sismemberMetrics.reset();
		sinterMetrics.reset();
		smembersMetrics.reset();
	}
	
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSetCount()
	 */
	public int getSetCount()
	{
		return setMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSetTimeMinMS()
	 */
	public double getSetTimeMinMS()
	{
		return setMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSetTimeMaxMS()
	 */
	public double getSetTimeMaxMS()
	{
		return setMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSetTimeAvgMS()
	 */
	public double getSetTimeAvgMS()
	{
		return setMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getGetCount()
	 */
	public int getGetCount()
	{
		return getMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getGetTimeMinMS()
	 */
	public double getGetTimeMinMS()
	{
		return getMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getGetTimeMaxMS()
	 */
	public double getGetTimeMaxMS()
	{
		return getMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getGetTimeAvgMS()
	 */
	public double getGetTimeAvgMS()
	{
		return getMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRemoveCount()
	 */
	public int getRemoveCount()
	{
		return removeMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRemoveTimeMinMS()
	 */
	public double getRemoveTimeMinMS()
	{
		return removeMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRemoveTimeMaxMS()
	 */
	public double getRemoveTimeMaxMS()
	{
		return removeMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRemoveTimeAvgMS()
	 */
	public double getRemoveTimeAvgMS()
	{
		return removeMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getInvalidateCount()
	 */
	public int getInvalidateCount()
	{
		return invalidateMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getInvalidateTimeMinMS()
	 */
	public double getInvalidateTimeMinMS()
	{
		return invalidateMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getInvalidateTimeMaxMS()
	 */
	public double getInvalidateTimeMaxMS()
	{
		return invalidateMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getInvalidateTimeAvgMS()
	 */
	public double getInvalidateTimeAvgMS()
	{
		return invalidateMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getIncrCount()
	 */
	public int getIncrCount()
	{
		return incrMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getIncrTimeMinMS()
	 */
	public double getIncrTimeMinMS()
	{
		return incrMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getIncrTimeMaxMS()
	 */
	public double getIncrTimeMaxMS()
	{
		return incrMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getIncrTimeAvgMS()
	 */
	public double getIncrTimeAvgMS()
	{
		return incrMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLtrimCount()
	 */
	public int getLtrimCount()
	{
		return ltrimMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLtrimTimeMinMS()
	 */
	public double getLtrimTimeMinMS()
	{
		return ltrimMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLtrimTimeMaxMS()
	 */
	public double getLtrimTimeMaxMS()
	{
		return ltrimMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLtrimTimeAvgMS()
	 */
	public double getLtrimTimeAvgMS()
	{
		return ltrimMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpushCount()
	 */
	public int getLpushCount()
	{
		return lpushMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpushTimeMinMS()
	 */
	public double getLpushTimeMinMS()
	{
		return lpushMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpushTimeMaxMS()
	 */
	public double getLpushTimeMaxMS()
	{
		return lpushMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpushTimeAvgMS()
	 */
	public double getLpushTimeAvgMS()
	{
		return lpushMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpopCount()
	 */
	public int getLpopCount()
	{
		return lpopMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpopTimeMinMS()
	 */
	public double getLpopTimeMinMS()
	{
		return lpopMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpopTimeMaxMS()
	 */
	public double getLpopTimeMaxMS()
	{
		return lpopMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLpopTimeAvgMS()
	 */
	public double getLpopTimeAvgMS()
	{
		return lpopMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpushCount()
	 */
	public int getRpushCount()
	{
		return rpushMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpushTimeMinMS()
	 */
	public double getRpushTimeMinMS()
	{
		return rpushMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpushTimeMaxMS()
	 */
	public double getRpushTimeMaxMS()
	{
		return rpushMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpushTimeAvgMS()
	 */
	public double getRpushTimeAvgMS()
	{
		return rpushMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpopCount()
	 */
	public int getRpopCount()
	{
		return rpopMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpopTimeMinMS()
	 */
	public double getRpopTimeMinMS()
	{
		return rpopMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpopTimeMaxMS()
	 */
	public double getRpopTimeMaxMS()
	{
		return rpopMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getRpopTimeAvgMS()
	 */
	public double getRpopTimeAvgMS()
	{
		return rpopMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLrangeCount()
	 */
	public int getLrangeCount()
	{
		return lrangeMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLrangeTimeMinMS()
	 */
	public double getLrangeTimeMinMS()
	{
		return lrangeMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLrangeTimeMaxMS()
	 */
	public double getLrangeTimeMaxMS()
	{
		return lrangeMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLrangeTimeAvgMS()
	 */
	public double getLrangeTimeAvgMS()
	{
		return lrangeMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLlenCount()
	 */
	public int getLlenCount()
	{
		return llenMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLlenTimeMinMS()
	 */
	public double getLlenTimeMinMS()
	{
		return llenMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLlenTimeMaxMS()
	 */
	public double getLlenTimeMaxMS()
	{
		return llenMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getLlenTimeAvgMS()
	 */
	public double getLlenTimeAvgMS()
	{
		return llenMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSaddCount()
	 */
	public int getSaddCount()
	{
		return saddMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSaddTimeMinMS()
	 */
	public double getSaddTimeMinMS()
	{
		return saddMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSaddTimeMaxMS()
	 */
	public double getSaddTimeMaxMS()
	{
		return saddMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSaddTimeAvgMS()
	 */
	public double getSaddTimeAvgMS()
	{
		return saddMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSremCount()
	 */
	public int getSremCount()
	{
		return sremMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSremTimeMinMS()
	 */
	public double getSremTimeMinMS()
	{
		return sremMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSremTimeMaxMS()
	 */
	public double getSremTimeMaxMS()
	{
		return sremMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSremTimeAvgMS()
	 */
	public double getSremTimeAvgMS()
	{
		return sremMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getScardCount()
	 */
	public int getScardCount()
	{
		return scardMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getScardTimeMinMS()
	 */
	public double getScardTimeMinMS()
	{
		return scardMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getScardTimeMaxMS()
	 */
	public double getScardTimeMaxMS()
	{
		return scardMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getScardTimeAvgMS()
	 */
	public double getScardTimeAvgMS()
	{
		return scardMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSisMemberCount()
	 */
	public int getSisMemberCount()
	{
		return sismemberMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSisMemberTimeMinMS()
	 */
	public double getSisMemberTimeMinMS()
	{
		return sismemberMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSisMemberTimeMaxMS()
	 */
	public double getSisMemberTimeMaxMS()
	{
		return sismemberMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSisMemberTimeAvgMS()
	 */
	public double getSisMemberTimeAvgMS()
	{
		return sismemberMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSinterCount()
	 */
	public int getSinterCount()
	{
		return sinterMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSinterTimeMinMS()
	 */
	public double getSinterTimeMinMS()
	{
		return sinterMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSinterTimeMaxMS()
	 */
	public double getSinterTimeMaxMS()
	{
		return sinterMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSinterTimeAvgMS()
	 */
	public double getSinterTimeAvgMS()
	{
		return sinterMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSinterCount()
	 */
	public int getSmembersCount()
	{
		return smembersMetrics.getCount();
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSmembersTimeMinMS()
	 */
	public double getSmembersTimeMinMS()
	{
		return smembersMetrics.getMinTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSmembersTimeMaxMS()
	 */
	public double getSmembersTimeMaxMS()
	{
		return smembersMetrics.getMaxTimeNS() / TIME_SCALE_NS_MS;
	}
	/* (non-Javadoc)
	 * @see redis.jmx.RedisMBean#getSmembersTimeAvgMS()
	 */
	public double getSmembersTimeAvgMS()
	{
		return smembersMetrics.getAvgTimeNS() / TIME_SCALE_NS_MS;
	}

	public final MinMaxAvgMetric getSetMetrics() {
		return setMetrics;
	}

	public final MinMaxAvgMetric getGetMetrics() {
		return getMetrics;
	}

	public final MinMaxAvgMetric getRemoveMetrics() {
		return removeMetrics;
	}

	public final MinMaxAvgMetric getInvalidateMetrics() {
		return invalidateMetrics;
	}

	public final MinMaxAvgMetric getIncrMetrics() {
		return incrMetrics;
	}

	public final MinMaxAvgMetric getLtrimMetrics() {
		return ltrimMetrics;
	}

	public final MinMaxAvgMetric getLpushMetrics() {
		return lpushMetrics;
	}

	public final MinMaxAvgMetric getLpopMetrics() {
		return lpopMetrics;
	}

	public final MinMaxAvgMetric getRpushMetrics() {
		return rpushMetrics;
	}

	public final MinMaxAvgMetric getRpopMetrics() {
		return rpopMetrics;
	}

	public final MinMaxAvgMetric getLrangeMetrics() {
		return lrangeMetrics;
	}

	public final MinMaxAvgMetric getLlenMetrics() {
		return llenMetrics;
	}

	public final MinMaxAvgMetric getSaddMetrics() {
		return saddMetrics;
	}

	public final MinMaxAvgMetric getSremMetrics() {
		return sremMetrics;
	}

	public final MinMaxAvgMetric getScardMetrics() {
		return scardMetrics;
	}

	public final MinMaxAvgMetric getSismemberMetrics() {
		return sismemberMetrics;
	}

	public final MinMaxAvgMetric getSinterMetrics() {
		return sinterMetrics;
	}
	
	public final MinMaxAvgMetric getSmembersMetrics()
	{
		return smembersMetrics;
	}
}
