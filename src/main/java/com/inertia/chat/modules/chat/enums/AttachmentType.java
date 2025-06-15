package com.inertia.chat.modules.chat.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AttachmentType {
    IMAGE(List.of("image/png", "image/jpeg")),  
    GIF  (List.of("image/gif")),
    VIDEO(List.of("video/mp4", "video/quicktime")),
    AUDIO(List.of("audio/mpeg", "audio/ogg")),
    DOCUMENT(List.of(
      "application/pdf", 
      "text/plain",
      "application/msword",
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    ));

    private final List<String> mimes;
    private static final Map<String,AttachmentType> BY_MIME =
      mimesStream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          Map.Entry::getValue
        ));

    AttachmentType(List<String> mimes) {
      this.mimes = mimes;
    }

    /** build a flat stream of (mime → enum) pairs */
    private static Stream<Map.Entry<String,AttachmentType>> mimesStream() {
      return Arrays.stream(values())
        .flatMap(t -> t.mimes.stream().map(m -> Map.entry(m, t)));
    }

    public static AttachmentType fromMimeType(String mime) {
      return BY_MIME.getOrDefault(mime, DOCUMENT);
    }

    public static boolean isSupported(String mime) {
      return BY_MIME.containsKey(mime);
    }
}



