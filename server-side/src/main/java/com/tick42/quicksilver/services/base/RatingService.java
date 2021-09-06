package com.tick42.quicksilver.services.base;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Rating;

public interface RatingService {
    Extension rate(Extension extension, int rating, long userId);

    int userRatingForExtension(long extensionId, long userId);

    void updateRatingOnExtensionDelete(Extension extension);
}
