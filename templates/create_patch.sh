#!/bin/bash

cd /var
cp ~/patch.patch /var
sudo patch -p1 --dry-run < patch.patch
sudo patch -p1 -b < patch.patch
patch -Rp1 < patch.patch
