package uk.co.appsplus.bootstrap.ui.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.getViewModel

inline fun <reified T : ViewModel> Fragment.viewModel(navGraphId: Int): Lazy<T> = lazy {
    findNavController()
        .getViewModelStoreOwner(navGraphId)
        .getViewModel()
}
