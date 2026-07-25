package com.q4magic.results.service;

import java.util.List;
import java.util.Map;

public interface ResultsService {
    List<Map<String, Object>> getResults(Integer userId);
    List<Map<String, Object>> getActivities(Integer userId);

}
