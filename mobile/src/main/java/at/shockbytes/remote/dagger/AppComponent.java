package at.shockbytes.remote.dagger;

import javax.inject.Singleton;

import at.shockbytes.remote.core.MainActivity;
import at.shockbytes.remote.fragment.MouseFragment;
import dagger.Component;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */
@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(MouseFragment fragment);

}
