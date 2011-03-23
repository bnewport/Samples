/*
 * lru_stl.h
 *
 *  Created on: Mar 7, 2011
 *      Author: ibm
 */

#ifndef LRU_STL_H_
#define LRU_STL_H_

#include <map>

// Class providing fixed-size (by number of records)
// LRU-replacement cache of a function with signature
// V f(K)
template <typename K,typename V> class lru_stl
{
public:

  // Key access history, most recent at back
  typedef std::list<K> key_tracker_type;

  // Key to value and key history iterator
  typedef std::map<
    K,
    std::pair<V,typename key_tracker_type::iterator>
  > key_to_value_type;

  // Constuctor specifies the cached function and
  // the maximum number of records to be stored.
  lru_stl(
    size_t c
  )
    :_capacity(c)
  {
    assert(_capacity!=0);
  }

  // Obtain value of the cached function for k
  V operator()(const K& k) {

    // Attempt to find existing record
    const typename key_to_value_type::iterator it
      =_key_to_value.find(k);

    if (it==_key_to_value.end()) {

    	return NULL;

    } else {

      // We do have it.  Before returning value,
      // update access record by moving accessed
      // key to back of list.
      _key_tracker.splice(
        _key_tracker.end(),
        _key_tracker,
        (*it).second.second
      );
      (*it).second.second=_key_tracker.rbegin().base();

      return (*it).second.first;
    }
  }

  // Obtain the cached keys, most recently used element
  // at head, least recently used at tail.
  // This method is provided purely to support testing.
  template <typename IT> void get_keys(IT dst) const {
    typename key_tracker_type::const_reverse_iterator src
        =_key_tracker.rbegin();
    while (src!=_key_tracker.rend()) {
      *dst++ = *src++;
    }
  }

  // Record a fresh key-value pair in the cache
  void insert(const K& k,const V& v) {

    // Method is only called on cache misses
    assert(_key_to_value.find(k)==_key_to_value.end());

    // Make space if necessary
    if (_key_to_value.size()==_capacity)
      evict();

    // Record k as most-recently-used key
    typename key_tracker_type::iterator it
      =_key_tracker.insert(_key_tracker.end(),k);

    // Create the key-value entry,
    // linked to the usage record.
    _key_to_value.insert(
      std::make_pair(
        k,
        std::make_pair(v,it)
      )
    );
    // No need to check return,
    // given previous assert.
  }

private:

  // Purge the least-recently-used element in the cache
  void evict() {

    // Assert method is never called when cache is empty
    assert(!_key_tracker.empty());

    // Identify least recently used key
    const typename key_to_value_type::iterator it
      =_key_to_value.find(_key_tracker.front());
    assert(it!=_key_to_value.end());

    // Erase both elements to completely purge record
    _key_to_value.erase(it);
    _key_tracker.pop_front();
  }

  // Maximum number of key-value pairs to be retained
  const size_t _capacity;

  // Key access history
  key_tracker_type _key_tracker;

  // Key-to-value lookup
  key_to_value_type _key_to_value;
};

#endif /* LRU_STL_H_ */
