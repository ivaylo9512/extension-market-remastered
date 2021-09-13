package com.tick42.quicksilver.services;

import com.tick42.quicksilver.exceptions.InvalidInputException;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.Rating;
import com.tick42.quicksilver.models.RatingPK;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.ExtensionRepository;
import com.tick42.quicksilver.repositories.base.RatingRepository;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingService {
    @Mock
    private ExtensionRepository extensionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock RatingRepository ratingRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    @Test()
    public void newExtensionRating_WhenUserHasCurrentRattingForExtension() {
        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(2);
        extension.setTimesRated(2);

        Rating newRating = new Rating(4, 1, 1);

        extension = ratingService.newExtensionRating(2, newRating, extension);

        assertEquals(3, extension.getRating());
        assertEquals(2, extension.getTimesRated());
    }

    @Test()
    public void newExtensionRating_WhenUserDoesNotHaveCurrentRattingForExtension() {
        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(2);
        extension.setTimesRated(2);

        Rating newRating = new Rating(5, 1, 1);

        extension = ratingService.newExtensionRating(0, newRating, extension);

        assertEquals(3, extension.getRating());
        assertEquals(3, extension.getTimesRated());

        verify(ratingRepository, times(1)).save(newRating);
        verify(extensionRepository, times(1)).save(extension);
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

        verify(ratingRepository, times(1)).save(newRating);
        verify(extensionRepository, times(1)).save(extension);
    }

    @Test
    public void newUserRating_WhenExtensionIsNotRated(){
        UserModel owner = new UserModel();
        owner.setRating(3.5);
        owner.setExtensionsRated(2);

        Extension extension = new Extension();
        extension.setRating(5);
        extension.setTimesRated(1);
        extension.setOwner(owner);

        ratingService.newUserRating(0, extension);

        assertEquals(owner.getRating(), 4);
        assertEquals(owner.getExtensionsRated(), 3);

        verify(userRepository, times(1)).save(owner);
    }

    @Test
    public void newUserRating_WhenExtensionIsRated(){
        UserModel owner = new UserModel();
        owner.setRating(3.5);
        owner.setExtensionsRated(3);

        Extension extension = new Extension();
        extension.setRating(3.5);
        extension.setTimesRated(2);
        extension.setOwner(owner);

        ratingService.newUserRating(5, extension);

        assertEquals(owner.getRating(), 3);
        assertEquals(owner.getExtensionsRated(), 3);

        verify(userRepository, times(1)).save(owner);
    }

    @Test
    public void rate_withInvalidInput() {
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

    @Test
    public void rate() {
        UserModel owner = new UserModel();
        owner.setId(2);
        owner.setRating(4);
        owner.setExtensionsRated(3);

        Extension extension = new Extension();
        extension.setId(3);
        extension.setRating(3.5);
        extension.setTimesRated(2);
        extension.setOwner(owner);

        long ratingUserId = 1;

        when(ratingRepository.findById(new RatingPK(extension.getId(),
                ratingUserId))).thenReturn(Optional.of(new Rating(0)));

        ratingService.rate(extension, 5, ratingUserId);

        assertEquals(owner.getRating(), 4.166666666666667);
        assertEquals(owner.getExtensionsRated(), 3);

        assertEquals(extension.getRating(), 4);
        assertEquals(extension.getTimesRated(), 3);
    }

    @Test
    public void userRatingForExtension_withNonExistentRating_ShouldReturn0(){
        when(ratingRepository.findById(new RatingPK(1, 2))).thenReturn(Optional.empty());

        int rating = ratingService.userRatingForExtension(1, 2);

        assertEquals(rating, 0);
    }

    @Test
    public void userRatingOnExtensionDelete(){
        UserModel owner = new UserModel();
        owner.setRating(3.5);
        owner.setExtensionsRated(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(2);
        extension.setTimesRated(2);
        extension.setOwner(owner);

        ratingService.updateRatingOnExtensionDelete(extension);

        final ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(captor.capture());

        final UserModel argument = captor.getValue();

        assertEquals(argument.getRating(), 5);
        assertEquals(argument.getExtensionsRated(), 1);
    }

    @Test
    public void userRatingOnExtensionDelete_WhenExtensionRatingIs0(){
        UserModel userModel = new UserModel();
        userModel.setRating(3.5);
        userModel.setExtensionsRated(2);

        Extension extension = new Extension();
        extension.setId(1);
        extension.setRating(0);
        extension.setTimesRated(0);
        extension.setOwner(userModel);

        ratingService.updateRatingOnExtensionDelete(extension);

        verify(userRepository, times(0)).save(userModel);
    }
}
