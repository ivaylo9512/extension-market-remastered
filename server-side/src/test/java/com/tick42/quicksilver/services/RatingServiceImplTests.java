package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.InvalidInputException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Rating;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.RatingRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RatingServiceImplTests {

    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock RatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test
    public void rateExtension_withInvalidInput() {
        //Arrange
        int extensionId = 1;
        int rating = 40;
        int userId = 5;

        Extension extension = new Extension();
        extension.setId(extensionId);

        InvalidInputException thrown = assertThrows(
                InvalidInputException.class,
                () -> ratingService.rate(extension, rating, userId));

        assertEquals(thrown.getMessage(), "Rating must be between 1 and 5.");
    }

    @Test()
    public void rateExtension_WhenUserHasCurrentRattingForExtension() {
        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(2);
        extension.setTimesRated(2);

        int currentUserRatingForExtension = 2;
        Rating newRating = new Rating(3, 1, 1);

        extension = ratingService.newExtensionRating( currentUserRatingForExtension, newRating, extension);

        assertEquals(2.50, extension.getRating(), 0);
    }

    @Test
    public void rateExtension_WhenUserNoRatingForExtension_ShouldReturnChanged() {
        Extension extension = new Extension();
        extension.setRating(2);
        extension.setTimesRated(2);

        int currentUserRatingForExtension = 0;
        Rating newRating = new Rating(5, 1, 1);

        extension = ratingService.newExtensionRating(currentUserRatingForExtension, newRating, extension);

        assertEquals(3, extension.getRating(), 0);
    }

    @Test
    public void userRatingOnExtensionDelete(){
        UserModel userModel = new UserModel();
        userModel.setRating(4);
        userModel.setExtensionsRated(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(4);
        extension.setTimesRated(2);
        extension.setOwner(userModel);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));
        ratingService.updateRatingOnExtensionDelete(extension);

        assertEquals(4, userModel.getRating(),0);

    }

    @Test
    public void userRatingOnExtensionDelete_whenUserHasOnlyOneExtension_ShouldReturnZero(){
        UserModel userModel = new UserModel();
        userModel.setRating(4);
        userModel.setExtensionsRated(1);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(4);
        extension.setTimesRated(2);
        extension.setOwner(userModel);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));
        ratingService.updateRatingOnExtensionDelete(extension);

        assertEquals(0, userModel.getRating(),0);
        assertEquals(0, userModel.getExtensionsRated(),0);
    }

    @Test
    public void UserRatingOnExtensionDelete_WhenExtensionRatingIsZero_ShouldReturnSame(){
        UserModel userModel = new UserModel();
        userModel.setRating(4);
        userModel.setExtensionsRated(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(0);
        extension.setTimesRated(0);
        extension.setOwner(userModel);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));
        ratingService.updateRatingOnExtensionDelete(extension);

        assertEquals(4, userModel.getRating(),0);
        assertEquals(2, userModel.getExtensionsRated(),0);
    }
}
