package expo.modules.bluetooth.objects;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import expo.core.Promise;
import expo.modules.bluetooth.BluetoothConstants;


// Device -> Service -> Characteristic -> Descriptor
public class Service extends EXBluetoothChildObject {

  public Service(BluetoothGattService nativeData, Object parent) {
    super(nativeData, (parent instanceof EXBluetoothObject) ? parent : new Peripheral((BluetoothGatt) parent));
  }


  // TODO: Bacon: Test characteristicProperties query works / is standard
  public Characteristic getCharacteristic(UUID uuid, int characteristicProperties) {
    BluetoothGattCharacteristic characteristic = getService().getCharacteristic(uuid);
    if (characteristic == null) return null;
    if ((characteristic.getProperties() & characteristicProperties) != 0) {
      return new Characteristic(characteristic, this);
    }
    return null;
  }

  public Characteristic getCharacteristic(UUID uuid) {
    BluetoothGattCharacteristic characteristic = getService().getCharacteristic(uuid);
    if (characteristic == null) return null;
    return new Characteristic(characteristic, this);
  }

  public List<Characteristic> getCharacteristics() {
    List<BluetoothGattCharacteristic> input = getService().getCharacteristics();

    ArrayList output = new ArrayList<>(input.size());
    for (BluetoothGattCharacteristic value : input) {
      output.add(new Characteristic(value, this));
    }
    return output;
  }

  public List<Service> getIncludedServices() {
    List<BluetoothGattService> input = getService().getIncludedServices();

    ArrayList output = new ArrayList<>(input.size());
    for (BluetoothGattService value : input) {
      output.add(new Service(value, mParent));
    }
    return output;
  }

  @Override
  public Bundle toJSON() {
    Bundle output = super.toJSON();
    output.putString(BluetoothConstants.JSON.PERIPHERAL_UUID, getPeripheral().getID());
    output.putBoolean(BluetoothConstants.JSON.IS_PRIMARY, getService().getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY);
    output.putParcelableArrayList(BluetoothConstants.JSON.INCLUDED_SERVICES, EXBluetoothObject.listToJSON((List)getIncludedServices()));
    output.putParcelableArrayList(BluetoothConstants.JSON.CHARACTERISTICS, EXBluetoothObject.listToJSON((List)getCharacteristics()));
    return output;
  }

  private BluetoothGattService getService() {
    return (BluetoothGattService) getNativeData();
  }


  // TODO: Bacon: Integrated
  public void discoverIncludedServices(ArrayList<UUID> includedServicesUUIDs, Promise promise) {
    // TODO: Emit full state
    // TODO: Bacon: How do we refresh these?
    Bundle output = new Bundle();
    output.putBundle(BluetoothConstants.JSON.PERIPHERAL, getPeripheral().toJSON());
    output.putBundle(BluetoothConstants.JSON.SERVICE, toJSON());
    promise.resolve(output);
  }

  public void discoverCharacteristics(ArrayList<UUID> characteristicUUIDs, Promise promise) {
    //TODO: Bacon: Are these gotten automatically?
    Bundle output = new Bundle();
    Bundle peripheralData = getPeripheral().toJSON();
    output.putBundle(BluetoothConstants.JSON.PERIPHERAL, peripheralData);
    output.putBundle(BluetoothConstants.JSON.SERVICE, toJSON());
//    output.putString(BluetoothConstants.JSON.TRANSACTION_ID, transactionIdForOperation(BluetoothConstants.OPERATIONS.SCAN));
    promise.resolve(output);
  }
}