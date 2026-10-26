import { router, useLocalSearchParams } from 'expo-router';
import { useState } from 'react';

import { Button, ChipPicker, Field, Screen, T } from '@/components/ui';
import { ApiError } from '@/lib/api';
import { reportReasonLabels } from '@/lib/labels';
import { useReport } from '@/lib/queries';
import { REPORT_REASONS, type ReportReason } from '@/lib/types';

export default function ReportScreen() {
  const { userId, name } = useLocalSearchParams<{ userId: string; name: string }>();
  const [reason, setReason] = useState<ReportReason | null>(null);
  const [details, setDetails] = useState('');
  const report = useReport();

  return (
    <Screen edges={['bottom']}>
      <T variant="heading">Report {name}</T>
      <T variant="muted">
        Moderators will review it. {name} will not know who reported them, and you will not see each other again.
      </T>
      <ChipPicker
        label="What is wrong?"
        options={REPORT_REASONS}
        labels={reportReasonLabels}
        value={reason}
        onChange={setReason}
      />
      <Field
        label="Anything else moderators should know? (optional)"
        value={details}
        onChangeText={setDetails}
        multiline
        maxLength={1000}
      />
      {report.error instanceof ApiError ? <T variant="error">{report.error.summary}</T> : null}
      <Button
        title="Send report"
        variant="danger"
        disabled={!reason}
        loading={report.isPending}
        onPress={() =>
          report.mutate({ userId, reason: reason!, details }, { onSuccess: () => router.dismissTo('/') })
        }
      />
    </Screen>
  );
}
