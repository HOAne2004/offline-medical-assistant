package com.example.trolyyte.presentation.reminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.trolyyte.MedicalAssistantApplication;
import com.example.trolyyte.databinding.FragmentReminderBinding;
import com.example.trolyyte.di.AppContainer;
import com.example.trolyyte.domain.model.Reminder;

public class ReminderFragment extends Fragment implements ReminderAdapter.ReminderCallback {
    private FragmentReminderBinding binding;
    private ReminderViewModel viewModel;
    private ReminderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReminderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupViewModel();
        observeUiState();
        setupListeners();
    }

    private void setupRecyclerView() {
        adapter = new ReminderAdapter(this);
        binding.rvReminders.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvReminders.setAdapter(adapter);
    }

    private void setupViewModel() {
        AppContainer appContainer = ((MedicalAssistantApplication) requireActivity().getApplication()).appContainer;
        // Sửa lỗi: Truyền trực tiếp reminderRepository thay vì GetRemindersUseCase cũ
        ReminderViewModelFactory factory = new ReminderViewModelFactory(appContainer.reminderRepository);
        
        viewModel = new ViewModelProvider(this, factory).get(ReminderViewModel.class);
    }

    private void setupListeners() {
        binding.fabAddReminder.setOnClickListener(v -> {
            showReminderForm(null);
        });
    }

    private void showReminderForm(Reminder reminder) {
        ReminderFormBottomSheet bottomSheet;
        if (reminder == null) {
            bottomSheet = ReminderFormBottomSheet.newInstance((title, detail, time, type) -> {
                viewModel.addOrUpdateReminder(null, title, detail, time, type);
            });
        } else {
            bottomSheet = ReminderFormBottomSheet.newEditInstance(reminder, (title, detail, time, type) -> {
                viewModel.addOrUpdateReminder(reminder.getId(), title, detail, time, type);
            });
        }
        bottomSheet.show(getChildFragmentManager(), "REMINDER_FORM");
    }

    private void observeUiState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ReminderUiState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.tvEmptyState.setVisibility(View.GONE);
            } else if (state instanceof ReminderUiState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.GONE);
                adapter.submitList(((ReminderUiState.Success) state).getReminders());
            } else if (state instanceof ReminderUiState.Empty) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                adapter.submitList(null);
            } else if (state instanceof ReminderUiState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi: " + ((ReminderUiState.Error) state).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(Reminder reminder) {
        showReminderForm(reminder);
    }

    @Override
    public void onDeleteClick(Reminder reminder) {
        viewModel.deleteReminder(reminder.getId());
        Toast.makeText(getContext(), "Đã xóa: " + reminder.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
