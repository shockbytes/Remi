package at.shockbytes.remote.dagger;

import javax.inject.Singleton;

import at.shockbytes.remote.core.WearMainActivity;
import at.shockbytes.remote.fragment.WearAppsFragment;
import at.shockbytes.remote.fragment.WearMouseFragment;
import at.shockbytes.remote.fragment.WearSlidesFragment;
import dagger.Component;

/**
 * @author Martin Macheiner
 *         Date: 21.02.2017.
 */
@Singleton
@Component(modules = {WearAppModule.class})
public interface WearAppComponent {

    void inject(WearMainActivity activity);

    void inject(WearMouseFragment fragment);

    void inject(WearAppsFragment fragment);

    void inject(WearSlidesFragment fragment);
}
