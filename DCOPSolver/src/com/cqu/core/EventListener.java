package com.cqu.core;

import java.util.Map;

public interface EventListener {
	void onStarted();
	void onFinished(Map<String, Object> result);
}
