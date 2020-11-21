package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.InvalidRatingException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Rating;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.RatingRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Optional;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RatingServiceImplTests {

    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock RatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test(expected = InvalidRatingException.class)
    public void rateExtension_withInvalidInput_ShouldThrow() {
        //Arrange
        int extensionId = 1;
        int rating = 40;
        int userId = 5;

        Extension extension = new Extension();
        extension.setId(extensionId);

        //Act
        ratingService.rate(extension, rating, userId);
    }

    @Test()
    public void rateExtension_WhenUserHasCurrentRattingForExtension_ShouldReturnChanged() {
        //Arrange
        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(2);
        extension.setTimesRated(2);
        int currentUserRatingForExtension = 2;
        Rating newRating = new Rating(3, 1, 1);

        //Act
        extension = ratingService.newExtensionRating( currentUserRatingForExtension, newRating, extension);

        //Assert
        Assert.assertEquals(2.50, extension.getRating(), 0);
    }

    @Test
    public void rateExtension_WhenUserNoRatingForExtension_ShouldReturnChanged() {
        //Arrange
        Extension extension = new Extension();
        extension.setRating(2);
        extension.setTimesRated(2);
        int currentUserRatingForExtension = 0;
        Rating newRating = new Rating(5, 1, 1);

        //Act
        extension = ratingService.newExtensionRating(currentUserRatingForExtension, newRating, extension);

        //Assert
        Assert.assertEquals(3, extension.getRating(), 0);
    }

    @Test(expected = NullPointerException.class)
    public void userRatingOnExtensionDelete_whitNonexistentExtension_ShouldThrow(){
        //Arrange
        Extension extension = new Extension();
        extension.setId(2);

        when(extensionRepository.findById(2L)).thenReturn(null);
        //Act
        ratingService.updateRatingOnExtensionDelete(extension);

    }

    @Test
    public void userRatingOnExtensionDelete(){

        //Arrange
        UserModel userModel = new UserModel();
        userModel.setRating(4);
        userModel.setExtensionsRated(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(4);
        extension.setTimesRated(2);
        extension.setOwner(userModel);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));
        //Act
        ratingService.updateRatingOnExtensionDelete(extension);
        //Assert

        Assert.assertEquals(4, userModel.getRating(),0);

    }

    @Test
    public void userRatingOnExtensionDelete_whenUserHasOnlyOneExtension_ShouldReturnZero(){

        //Arrange
        UserModel userModel = new UserModel();
        userModel.setRating(4);
        userModel.setExtensionsRated(1);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(4);
        extension.setTimesRated(2);
        extension.setOwner(userModel);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));
        //Act
        ratingService.updateRatingOnExtensionDelete(extension);
        //Assert

        Assert.assertEquals(0, userModel.getRating(),0);
        Assert.assertEquals(0, userModel.getExtensionsRated(),0);
    }

    @Test
    public void UserRatingOnExtensionDelete_WhenExtensionRatingIsZero_ShouldReturnSame(){

        //Arrange
        UserModel userModel = new UserModel();
        userModel.setRating(4);
        userModel.setExtensionsRated(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(0);
        extension.setTimesRated(0);
        extension.setOwner(userModel);

        when(extensionRepository.findById(1L)).thenReturn(Optional.of(extension));
        //Act
        ratingService.updateRatingOnExtensionDelete(extension);
        //Assert

        Assert.assertEquals(4, userModel.getRating(),0);
        Assert.assertEquals(2, userModel.getExtensionsRated(),0);
    }
}
