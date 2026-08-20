package com.plip.user.domain.policy;

/**
 * 약관 version 비교. 저장 형식은 {@code major.minor} 숫자 세그먼트(예: {@code 1.0}, {@code 2.9}).
 */
public final class TermVersionComparator {

	private TermVersionComparator() {
	}

	public static int compare(String leftVersion, String rightVersion) {
		int[] left = parseSegments(leftVersion);
		int[] right = parseSegments(rightVersion);
		int maxLength = Math.max(left.length, right.length);
		for (int index = 0; index < maxLength; index++) {
			int leftSegment = index < left.length ? left[index] : 0;
			int rightSegment = index < right.length ? right[index] : 0;
			if (leftSegment != rightSegment) {
				return Integer.compare(leftSegment, rightSegment);
			}
		}
		return 0;
	}

	private static int[] parseSegments(String version) {
		if (version == null || version.isBlank()) {
			return new int[0];
		}
		String normalized = version.strip();
		if (normalized.startsWith("v") || normalized.startsWith("V")) {
			normalized = normalized.substring(1);
		}
		String[] parts = normalized.split("\\.");
		int[] segments = new int[parts.length];
		for (int index = 0; index < parts.length; index++) {
			segments[index] = parseSegment(parts[index]);
		}
		return segments;
	}

	private static int parseSegment(String segment) {
		if (segment == null || segment.isBlank()) {
			return 0;
		}
		try {
			return Integer.parseInt(segment.trim());
		}
		catch (NumberFormatException exception) {
			return 0;
		}
	}
}
