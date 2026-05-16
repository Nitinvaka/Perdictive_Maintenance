import { useState, useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { simulatorApi } from '../api/simulator';
import { sensorsApi } from '../api/sensors';
import { assetsApi } from '../api/assets';
import { thresholdsApi } from '../api/thresholds';
import { Card, CardHeader } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Select } from '../components/ui/Select';
import { PageHeader } from '../components/ui/PageHeader';
import { Radio, CheckCircle2, AlertTriangle, Zap, Lock } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { formatDate, formatRms, formatTemp } from '../utils/formatters';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import { clsx } from 'clsx';

/*
  Simulator Page

  Pretends to be an IoT device sending sensor readings.
  Pick a sensor, type RMS + temperature, hit publish.

  Important rules:
  - Inactive sensors are hidden (can't publish to them)
  - Sensors on inactive assets are hidden too
  - Threshold breach is shown BEFORE publishing as a heads-up
*/

const schema = z.object({
  // sensorId carries everything we need (backend looks up the asset from it)
  sensorId: z.coerce.number().positive('Please pick a sensor'),
  rms:      z.coerce.number().min(0, 'RMS cannot be negative').max(100),
  temp:     z.coerce.number().min(-50, 'Temperature too low').max(300, 'Temperature too high'),
  ts:       z.string().min(1, 'Timestamp is required'),
});

// Shows a friendly summary card after publish succeeds
function ResultCard({ reading }) {
  return (
    <div className="rounded-2xl border p-5 animate-slide-up"
      style={{
        background: 'linear-gradient(135deg, rgba(209,250,229,0.7), rgba(167,243,208,0.4))',
        borderColor: 'rgba(16,185,129,0.28)',
        boxShadow: '0 4px 20px rgba(16,185,129,0.12)',
      }}>
      <div className="flex items-center gap-3 mb-4">
        <div className="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
          style={{ background: 'linear-gradient(135deg, #059669, #10b981)', boxShadow: '0 3px 10px rgba(16,185,129,0.35)' }}>
          <CheckCircle2 className="w-5 h-5 text-white" />
        </div>
        <div>
          <p className="text-sm font-extrabold text-emerald-800">Reading published</p>
          <p className="text-xs text-emerald-600 font-medium mt-0.5">Stored & threshold evaluated</p>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-2.5">
        {[
          { label: 'Reading ID', value: `#${reading.id}` },
          { label: 'Asset',      value: reading.assetName },
          { label: 'RMS',        value: `${formatRms(reading.rms)} mm/s` },
          { label: 'Temperature',value: formatTemp(reading.temperature) },
        ].map(({ label, value }) => (
          <div key={label} className="rounded-xl p-3"
            style={{ background: 'rgba(255,255,255,0.65)', border: '1px solid rgba(16,185,129,0.15)' }}>
            <p className="text-[10px] font-bold text-emerald-600 uppercase tracking-widest mb-1">{label}</p>
            <p className="font-bold text-slate-900 text-sm">{value}</p>
          </div>
        ))}
        <div className="col-span-2 rounded-xl p-3"
          style={{ background: 'rgba(255,255,255,0.65)', border: '1px solid rgba(16,185,129,0.15)' }}>
          <p className="text-[10px] font-bold text-emerald-600 uppercase tracking-widest mb-1">Timestamp</p>
          <p className="font-bold text-slate-900 text-sm">{formatDate(reading.timestamp)}</p>
        </div>
      </div>
    </div>
  );
}

export function Simulator() {
  const { canWrite } = useAuth();
  const qc = useQueryClient();
  const [lastReading, setLastReading] = useState(null);

  // Pull sensors, assets, thresholds - we need all three to filter and warn
  const { data: assets  = [] } = useQuery({ queryKey: ['assets'],  queryFn: () => assetsApi.getAll() });
  const { data: sensors = [] } = useQuery({ queryKey: ['sensors'], queryFn: () => sensorsApi.getAll() });
  const { data: allThresholds = [] } = useQuery({
    queryKey: ['thresholds'],
    queryFn: () => thresholdsApi.getAll(),
  });

  // Build a quick lookup so we can show the right limit for the picked asset
  const thresholdByAsset = useMemo(() => {
    const map = new Map();
    allThresholds.forEach(t => map.set(t.assetId, t));
    return map;
  }, [allThresholds]);

  // Set of active asset IDs - so we can filter out sensors on inactive assets
  const activeAssetIds = useMemo(() => {
    return new Set(assets.filter(a => a.active).map(a => a.id));
  }, [assets]);

  // Combined options: only show sensors that are ACTIVE and on an ACTIVE asset
  // Each option label is "AssetName — SensorName" so user picks both at once
  const sensorOptions = useMemo(() => {
    return sensors
      .filter(s => s.active && activeAssetIds.has(s.assetId))
      .map(s => ({
        value: String(s.id),
        label: `${s.assetName} — ${s.name}`,
        assetId: s.assetId,
      }));
  }, [sensors, activeAssetIds]);

  const { register, handleSubmit, watch, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      sensorId: '',
      rms:      '',
      temp:     '',
      ts:       format(new Date(), "yyyy-MM-dd'T'HH:mm:ss"),
    },
  });

  const selectedSensorId = watch('sensorId');

  // Find which asset this sensor belongs to (for showing its threshold limit)
  const selectedAssetId = useMemo(() => {
    if (!selectedSensorId) return null;
    const sensor = sensors.find(s => s.id === Number(selectedSensorId));
    return sensor?.assetId ?? null;
  }, [selectedSensorId, sensors]);

  // Send the reading to the backend
  const mutation = useMutation({
    mutationFn: (d) => simulatorApi.publishBySensor({
      sensorId: Number(d.sensorId),
      rms:      Number(d.rms),
      temp:     Number(d.temp),
      ts:       d.ts,
    }),
    onSuccess: (reading) => {
      setLastReading(reading);
      // Refresh anything that might have changed
      qc.invalidateQueries({ queryKey: ['readings'] });
      qc.invalidateQueries({ queryKey: ['tickets']  });
      qc.invalidateQueries({ queryKey: ['assets']   });
      toast.success('Reading published');
    },
    onError: (err) => {
      toast.error(err.response?.data?.message ?? 'Failed to publish reading');
    },
  });

  // Live-watch the values to show a "will this breach?" hint
  const currentRms  = Number(watch('rms'))  || 0;
  const currentTemp = Number(watch('temp')) || 0;
  const hasValues   = currentRms > 0 || currentTemp > 0;

  // Show the actual threshold for the selected asset
  const selectedThreshold = selectedAssetId ? thresholdByAsset.get(selectedAssetId) : null;
  const rmsLimit  = selectedThreshold?.rmsMax  ?? null;
  const tempLimit = selectedThreshold?.tempMax ?? null;
  const willBreach =
    (rmsLimit  != null && currentRms  > rmsLimit) ||
    (tempLimit != null && currentTemp > tempLimit);

  // Technicians can't publish - show a friendly locked screen
  if (!canWrite) {
    return (
      <div>
        <PageHeader title="Simulator" subtitle="Publish a reading from a sensor" />
        <div className="flex flex-col items-center justify-center py-24">
          <div className="w-16 h-16 rounded-3xl flex items-center justify-center mb-6"
            style={{ background: 'linear-gradient(135deg, rgba(239,68,68,0.1), rgba(220,38,38,0.15))', border: '1px solid rgba(239,68,68,0.2)' }}>
            <Lock className="w-7 h-7 text-red-400" />
          </div>
          <h3 className="text-xl font-black text-slate-900 mb-2">Access Restricted</h3>
          <p className="text-slate-500 text-sm text-center max-w-sm">
            Publishing readings requires Manager or Admin privileges.
          </p>
        </div>
      </div>
    );
  }

  return (
    <>
      <PageHeader
        title="Simulator"
        subtitle="Publish a reading from a sensor"
      />

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        <Card>
          <CardHeader
            title="Publish Reading"
            subtitle="Pick a sensor, enter values, and publish"
            action={
              <div className="w-9 h-9 rounded-xl flex items-center justify-center"
                style={{ background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', boxShadow: '0 3px 10px rgba(99,102,241,0.35)' }}>
                <Radio className="w-4 h-4 text-white" />
              </div>
            }
          />

          <form onSubmit={handleSubmit(d => mutation.mutate(d))} className="space-y-4">
            {/* Single combined field: Asset — Sensor (active only) */}
            <Select
              label="Asset & Sensor" required error={errors.sensorId?.message}
              placeholder={sensorOptions.length === 0 ? 'No active sensors available' : 'Select asset and sensor…'}
              options={sensorOptions}
              disabled={sensorOptions.length === 0}
              {...register('sensorId')}
            />

            {/* Friendly note when nothing's available */}
            {sensorOptions.length === 0 && (
              <div className="flex items-center gap-2 px-3 py-2.5 rounded-xl"
                style={{ background: 'rgba(245,158,11,0.08)', border: '1px solid rgba(245,158,11,0.25)' }}>
                <AlertTriangle className="w-4 h-4 text-amber-500 flex-shrink-0" />
                <p className="text-xs text-amber-700 font-semibold">
                  No active sensors found. Activate an asset and its sensors to publish readings.
                </p>
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <Input label="RMS (mm/s)" required type="number" step="0.01"
                error={errors.rms?.message}
                hint={rmsLimit != null ? `Breach: > ${rmsLimit}` : 'Pick a sensor to see its limit'}
                {...register('rms')} placeholder="7.5" />
              <Input label="Temperature (°C)" required type="number" step="0.1"
                error={errors.temp?.message}
                hint={tempLimit != null ? `Breach: > ${tempLimit}°C` : 'Pick a sensor to see its limit'}
                {...register('temp')} placeholder="98.0" />
            </div>

            <Input label="Timestamp" required type="datetime-local" step="1"
              error={errors.ts?.message} {...register('ts')} />

            {/* Live preview: will this breach the threshold? */}
            {hasValues && selectedThreshold && (
              <div className={clsx(
                'flex items-center gap-3 text-sm rounded-xl p-4 transition-all duration-300',
                willBreach ? 'border border-red-200' : 'border border-emerald-200'
              )} style={{
                background: willBreach
                  ? 'linear-gradient(135deg, rgba(254,226,226,0.7), rgba(252,165,165,0.25))'
                  : 'linear-gradient(135deg, rgba(209,250,229,0.7), rgba(167,243,208,0.25))',
              }}>
                <div className="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
                  style={{
                    background: willBreach ? 'linear-gradient(135deg, #ef4444, #dc2626)' : 'linear-gradient(135deg, #059669, #10b981)',
                    boxShadow:  willBreach ? '0 3px 8px rgba(239,68,68,0.30)' : '0 3px 8px rgba(16,185,129,0.30)',
                  }}>
                  {willBreach ? <AlertTriangle className="w-4 h-4 text-white" /> : <CheckCircle2 className="w-4 h-4 text-white" />}
                </div>
                <div>
                  {willBreach ? (
                    <>
                      <p className="font-extrabold text-red-700 text-sm">Threshold breach expected</p>
                      <p className="text-xs text-red-500 font-medium mt-0.5">A maintenance ticket will be auto-created</p>
                    </>
                  ) : (
                    <>
                      <p className="font-extrabold text-emerald-700 text-sm">Reading looks normal</p>
                      <p className="text-xs text-emerald-500 font-medium mt-0.5">Unlikely to trigger a ticket</p>
                    </>
                  )}
                </div>
              </div>
            )}

            <Button
              type="submit"
              loading={mutation.isPending}
              icon={<Zap className="w-4 h-4" />}
              className="w-full"
              size="lg"
              disabled={sensorOptions.length === 0}
            >
              Publish Reading
            </Button>
          </form>
        </Card>

        <div className="space-y-5">
          {lastReading && <ResultCard reading={lastReading} />}
        </div>
      </div>
    </>
  );
}
