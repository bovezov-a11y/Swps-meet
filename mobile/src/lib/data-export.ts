import { Share } from 'react-native';

/** Hands the student's data export to the share sheet, so they can save it to Files or send it to themselves. */
export async function saveDataExport(json: string): Promise<void> {
  await Share.share({ title: 'My Meet SWPS data', message: json });
}
