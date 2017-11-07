package at.shockbytes.remote.dagger;

import javax.inject.Singleton;

import at.shockbytes.remote.core.LoginActivity;
import at.shockbytes.remote.core.MainActivity;
import at.shockbytes.remote.fragment.AppsFragment;
import at.shockbytes.remote.fragment.FilesFragment;
import at.shockbytes.remote.fragment.KeyboardFragment;
import at.shockbytes.remote.fragment.LoginFragment;
import at.shockbytes.remote.fragment.MouseFragment;
import at.shockbytes.remote.fragment.SettingsFragment;
import at.shockbytes.remote.fragment.SlidesFragment;
import at.shockbytes.remote.fragment.dialog.AcceptDesktopConnectionDialogFragment;
import dagger.Component;

/**
 * @author Martin Macheiner
 *         Date: 26.09.2017.
 */
@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(LoginActivity activity);

    void inject(LoginFragment fragment);

    void inject(MouseFragment fragment);

    void inject(AppsFragment fragment);

    void inject(FilesFragment fragment);

    void inject(KeyboardFragment fragment);

    void inject(SlidesFragment fragment);

    void inject(SettingsFragment fragment);

    void inject(AcceptDesktopConnectionDialogFragment dialogFragment);

}
