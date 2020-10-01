package com.enseval.gcmuser;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

class RollbackIfFailure implements Continuation<Void, Task<Void>> {
    @Override
    public Task<Void> then(@NonNull Task<Void> task) throws Exception {

        final TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        if (task.isSuccessful()) {
            tcs.setResult(null);
        } else {
            // Rollback everything
        }

        return tcs.getTask();
    }
}