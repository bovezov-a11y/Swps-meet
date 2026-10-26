/** Downloads the student's data export as a JSON file. */
export async function saveDataExport(json: string): Promise<void> {
  const url = URL.createObjectURL(new Blob([json], { type: 'application/json' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = 'meet-swps-data.json';
  link.click();
  URL.revokeObjectURL(url);
}
