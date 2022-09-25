# File system simulator

Simulating [file allocation](https://www.geeksforgeeks.org/file-allocation-methods/) in a virtual file system. Was made
as an assignment for the Operating Systems course taught  in Cairo University Faculty of Computers and Artificial 
Intelligence.

## Features

- 3 modes of file allocation
  - Contiguous Allocation (Using Best Fit allocation)
  - Indexed Allocation
  - Linked Allocation
- User authorization system

## Supported commands

- `CreateFile <file name> <size>`
- `CreateFolder <folder name>`
- `DeleteFile <file name>`
- `DeleteFolder <folder name>`
- `DisplayDiskStatus`
- `DisplayDiskStructure`
- `DisplayStorageInfo`
- `TellUser`
- `Cuser <username> <password>`
- `Login <username> <password>`
- `Grant <username> <path> <permissions>`

`<permissions>` is a 2 digit binary number, first bit represent ability to create, second represent ability to delete.