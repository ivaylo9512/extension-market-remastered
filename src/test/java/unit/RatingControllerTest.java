package unit;

import com.tick42.quicksilver.controllers.RatingController;
import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.UserDetails;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.services.base.ExtensionService;
import com.tick42.quicksilver.services.base.RatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingControllerTest {
    @InjectMocks
    private RatingController ratingController;

    @Mock
    private RatingService ratingService;

    @Mock
    private ExtensionService extensionService;

    private final UserModel userModel = new UserModel(1, "username", "email", "password", "ROLE_ADMIN", "info", "Bulgaria");
    private final UserDetails user = new UserDetails(userModel, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    private final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getId());

    @Test
    public void rate() {
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Extension extension = new Extension();
        extension.setId(3);
        extension.setRating(4);

        when(extensionService.findById(extension.getId(), user)).thenReturn(extension);
        when(ratingService.rate(extension, 5, user.getId())).thenReturn(extension);

        double rating = ratingController.rate(extension.getId(), 5);

        verify(extensionService, times(1)).reloadExtension(extension);
        extensionService.reloadExtension(extension);

        assertEquals(rating, extension.getRating());
    }

    @Test
    public void userRatingForExtension() {
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);

        long extensionId = 2;
        when(ratingService.userRatingForExtension(extensionId, user.getId())).thenReturn(5);

        int rating = ratingController.userRatingForExtension(extensionId);

        assertEquals(rating, 5);
    }
}
